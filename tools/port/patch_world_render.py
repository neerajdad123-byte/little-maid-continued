"""One-off port patch: world overlay render events -> NeoForge 26.x SubmitCustomGeometryEvent."""
import re
import sys

B = "src/main/java/com/github/tartaricacid/touhoulittlemaid/"
EVENTS = ["CompassRenderEvent", "MaidAreaRenderEvent", "WirelessIORenderEvent", "ScrollRenderEvent"]


def imp(t, fqn):
    if "import %s;" % fqn in t:
        return t
    return re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % fqn, t, count=1, flags=re.M)


# RenderHelper: draw into the frame's recording buffer instead of the removed immediate buffer source
p = B + "util/RenderHelper.java"
t = open(p, encoding="utf-8").read()
t = t.replace("        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();",
              "        MultiBufferSource buffer = BUFFERS;\n        if (buffer == null) {\n            return;\n        }")
t = re.sub(r"\n[ \t]*buffer\.endBatch\(\);", "", t)
t = re.sub(r"(public (?:final )?class RenderHelper \{\n)", r'''\1    /**
     * Little Maid Continued: recording buffer of the world overlay currently being drawn (see {@link #draw}).
     */
    public static @Nullable SubmitBufferSource BUFFERS;

    /**
     * Runs a MC 1.21.1 style world overlay renderer inside the MC 26.x submit pipeline.
     */
    public static void draw(SubmitCustomGeometryEvent event, Runnable renderer) {
        try (SubmitBufferSource buffers = new SubmitBufferSource(event.getSubmitNodeCollector())) {
            BUFFERS = buffers;
            renderer.run();
        } finally {
            BUFFERS = null;
        }
    }

''', t, count=1)
for fqn in ["com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.SubmitBufferSource",
            "net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent", "org.jspecify.annotations.Nullable"]:
    t = imp(t, fqn)
open(p, "w", encoding="utf-8", newline="\n").write(t)

for name in EVENTS:
    p = B + "client/event/%s.java" % name
    t = open(p, encoding="utf-8").read()
    m = re.search(r"    @SubscribeEvent\n    public static void (\w+)\(RenderLevelStageEvent event\) \{\n", t)
    if not m:
        sys.exit("no handler in " + name)
    method = m.group(1)
    t = t[:m.start()] + ("    @SubscribeEvent\n    public static void %s(SubmitCustomGeometryEvent event) {\n"
                         "        RenderHelper.draw(event, () -> render(event));\n    }\n\n"
                         "    private static void render(SubmitCustomGeometryEvent event) {\n") % method + t[m.end():]
    t = t.replace("RenderLevelStageEvent event", "SubmitCustomGeometryEvent event")
    t = re.sub(r"event\.getStage\(\) == RenderLevelStageEvent\.Stage\.\w+", "true", t)
    t = re.sub(r"RenderLevelStageEvent\.Stage\.\w+\.equals\(event\.getStage\(\)\)", "true", t)
    t = t.replace("event.getCamera().getPosition()", "event.getLevelRenderState().cameraRenderState.pos")
    t = re.sub(r"(?:mc|Minecraft\.getInstance\(\))\.renderBuffers\(\)\.bufferSource\(\)\.getBuffer\(RenderType\.LINES\)",
               "RenderHelper.BUFFERS.getBuffer(RenderTypes.lines())", t)
    t = re.sub(r"DebugRenderer\.renderFilledBox\(poseStack, mc\.renderBuffers\(\)\.bufferSource\(\), (\w+), ([^;]+)\);",
               r"LegacyDraw.lineBox(poseStack, RenderHelper.BUFFERS, \1, \2);  // shortcut: outline instead of the old filled box", t)
    t = re.sub(r"LevelRenderer\.renderLineBox\(([^,]+), buffer, ", r"LegacyDraw.lineBox(\1, RenderHelper.BUFFERS, ", t)
    t = t.replace("import net.neoforged.neoforge.client.event.RenderLevelStageEvent;", "import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;")
    for fqn in ["net.minecraft.client.renderer.rendertype.RenderTypes", "com.github.tartaricacid.touhoulittlemaid.util.RenderHelper",
                "com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.LegacyDraw"]:
        if fqn.split(".")[-1] + "." in t:
            t = imp(t, fqn)
    open(p, "w", encoding="utf-8", newline="\n").write(t)
print("world render patched")
