"""One-off port patch: model details screen 3D preview -> MC 26.x GuiGraphicsExtractor#entity."""
import re
import sys

B = "src/main/java/com/github/tartaricacid/touhoulittlemaid/client/gui/entity/detail/"


def edit(path, pairs, imports=()):
    t = open(B + path, encoding="utf-8").read()
    for old, new in pairs:
        if old not in t:
            sys.exit("NOT FOUND in %s:\n%s" % (path, old))
        t = t.replace(old, new)
    for fqn in imports:
        if "import %s;" % fqn not in t:
            t = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % fqn, t, count=1, flags=re.M)
    open(B + path, "w", encoding="utf-8", newline="\n").write(t)


p = B + "AbstractModelDetailsGui.java"
t = open(p, encoding="utf-8").read()
s = t.index("    private void renderEntity(int middleWidth, int middleHeight, GuiGraphicsExtractor graphics) {")
e = t.index("    @Override\n    public boolean isPauseScreen()")
t = t[:s] + '''    private void renderEntity(int middleWidth, int middleHeight, GuiGraphicsExtractor graphics) {
        // Little Maid Continued: MC 26.x draws GUI entities as picture-in-picture render states
        // TODO: the debug floor is not drawn yet
        int x0 = 132;
        int y0 = 15;
        int x1 = this.width;
        int y1 = this.height - 16;
        int offsetX = (int) posX + middleWidth - (x0 + x1) / 2;
        int offsetY = (int) posY + middleHeight - (y0 + y1) / 2;
        Quaternionf rotation = Axis.ZP.rotationDegrees(180.0F).mul(Axis.YP.rotationDegrees(yaw)).mul(Axis.XP.rotationDegrees(-pitch));
        Quaternionf camera = Axis.XP.rotationDegrees(-pitch).conjugate();
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        graphics.enableScissor(x0, y0, x1, y1);
        EntityRenderState state = manager.extractEntity(guiEntity, partialTick);
        state.shadowPieces.clear();
        graphics.entity(state, scale, new Vector3f(0, 0, 0), rotation, camera, x0 + offsetX, y0 + offsetY, x1 + offsetX, y1 + offsetY);
        Entity extra = this.extraEntity();
        if (extra != null) {
            EntityRenderState extraState = manager.extractEntity(extra, partialTick);
            extraState.shadowPieces.clear();
            graphics.entity(extraState, scale, new Vector3f(0, this.extraEntityYOffset(), 0), rotation, camera,
                    x0 + offsetX, y0 + offsetY, x1 + offsetX, y1 + offsetY);
        }
        graphics.disableScissor();
    }

''' + t[e:]
t = t.replace("    abstract protected void renderExtraEntity(EntityRenderDispatcher manager, PoseStack matrix, MultiBufferSource.BufferSource bufferIn);",
              "    /**\n     * Second entity drawn in the preview (vehicle or passenger), or null.\n     */\n    @Nullable\n    protected Entity extraEntity() {\n        return null;\n    }\n\n    protected float extraEntityYOffset() {\n        return 0;\n    }")
t = re.sub(r"\n    private void fillGradient\(GuiGraphicsExtractor graphics, Rectangle vec4d, int color, int zLevel\) \{\n.*?\n    \}\n", "\n", t, flags=re.S)
t = re.sub(r"fillGradient\(graphics, (\w+), (\w+), \w+\)", r"fillGradient(graphics, \1, \2)", t)
for fqn in ["net.minecraft.client.renderer.entity.state.EntityRenderState", "org.joml.Vector3f", "org.joml.Quaternionf",
            "net.minecraft.world.entity.Entity", "org.jspecify.annotations.Nullable", "com.mojang.math.Axis",
            "net.minecraft.client.renderer.entity.EntityRenderDispatcher"]:
    if "import %s;" % fqn not in t:
        t = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % fqn, t, count=1, flags=re.M)
open(p, "w", encoding="utf-8", newline="\n").write(t)

edit("ChairModelDetailsGui.java", [
    ('''    @Override
    protected void renderExtraEntity(EntityRenderDispatcher manager, PoseStack matrix, MultiBufferSource.BufferSource bufferIn) {
        if (showPassenger) {
            manager.render(maid, 0, -0.375 + modelInfo.getMountedYOffset(), 0, 0, 1, matrix, bufferIn, 0xf000f0);
        }
    }''', '''    @Override
    protected Entity extraEntity() {
        return showPassenger ? maid : null;
    }

    @Override
    protected float extraEntityYOffset() {
        return -0.375F + modelInfo.getMountedYOffset();
    }'''),
    ("maid.startRiding(guiEntity, true);", "maid.startRiding(guiEntity, true, false);"),
    ("this.maid = InitEntities.MAID.get().create(Minecraft.getInstance().level);", "this.maid = InitEntities.MAID.get().create(Minecraft.getInstance().level, EntitySpawnReason.LOAD);"),
], ["net.minecraft.world.entity.Entity", "net.minecraft.world.entity.EntitySpawnReason"])

edit("MaidModelDetailsGui.java", [
    ('''    @Override
    protected void renderExtraEntity(EntityRenderDispatcher manager, PoseStack matrix, MultiBufferSource.BufferSource bufferIn) {
        if (guiEntity.isPassenger() && chair != null) {
            manager.render(chair, 0, -0.95, 0, 0, 1, matrix, bufferIn, 0xf000f0);
        }
    }''', '''    @Override
    protected Entity extraEntity() {
        return guiEntity.isPassenger() ? chair : null;
    }

    @Override
    protected float extraEntityYOffset() {
        return -0.95F;
    }'''),
    ("guiEntity.startRiding(chair, true);", "guiEntity.startRiding(chair, true, false);"),
    ("guiEntity.walkAnimation.update(speed, 0.4f);", "guiEntity.walkAnimation.update(speed, 0.4f, 1.0f);"),
    ("this.chair = InitEntities.CHAIR.get().create(Minecraft.getInstance().level);", "this.chair = InitEntities.CHAIR.get().create(Minecraft.getInstance().level, EntitySpawnReason.LOAD);"),
], ["net.minecraft.world.entity.Entity", "net.minecraft.world.entity.EntitySpawnReason"])
print("details gui patched")
