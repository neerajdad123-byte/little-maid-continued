"""One-off port patch: remaining GUI API changes (MC 1.21.1 -> 26.2)."""
import re
import sys

B = "src/main/java/com/github/tartaricacid/touhoulittlemaid/client/gui/"


def edit(path, pairs, imports=(), regex=False):
    t = open(B + path, encoding="utf-8").read()
    for old, new in pairs:
        if regex:
            nt = re.sub(old, new, t)
            if nt == t:
                sys.exit("NO MATCH in %s: %s" % (path, old))
            t = nt
        else:
            if old not in t:
                sys.exit("NOT FOUND in %s:\n%s" % (path, old))
            t = t.replace(old, new)
    for fqn in imports:
        if "import %s;" % fqn not in t:
            t = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % fqn, t, count=1, flags=re.M)
    open(B + path, "w", encoding="utf-8", newline="\n").write(t)


for f in ["block/MaidBeaconGui.java", "entity/ModelDownloadGui.java"]:
    edit(f, [("InputConstants.getKey(keyCode, scanCode)", "InputConstants.getKey(event)")])
edit("entity/maid/ai/HistoryAIChatScreen.java", [
    ("graphics.pose().translate(0, scroll);", "graphics.pose().translate(0, (float) scroll);"),
    ("return mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();",
     "return mc.getSkinManager().createLookup(player.getGameProfile(), false).get().body().texturePath();"),
])
edit("entity/maid/ai/SettingEditScreen.java", [('''new MultiLineEditBox(font,
                posX, 70, boxWidth, this.height - 100,
                Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.edit"),
                Component.literal("Custom Setting Box"))''', '''MultiLineEditBox.builder().setX(posX).setY(70)
                .setPlaceholder(Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.edit"))
                .build(font, boxWidth, this.height - 100, Component.literal("Custom Setting Box"))''')])
edit("item/WirelessIOConfigSlotGui.java", [("this.extractBackground(graphics, mouseY, partialTicks, mouseX);\n", "")])
edit("item/WirelessIOContainerGui.java", [(r"\(m, x, y\) -> m\.renderTooltip\(font, ", "(m, x, y) -> m.setTooltipForNextFrame(font, ")], regex=True)
for f, y in [("mod/OptifineScreen.java", "70"), ("mod/PatchouliWarningScreen.java", "80")]:
    edit(f, [("this.message.renderCentered(graphics, this.width / 2, %s);" % y,
              "this.message.visitLines(TextAlignment.CENTER, this.width / 2, %s, 9, graphics.textRenderer());" % y)],
         ["net.minecraft.client.gui.TextAlignment"])
edit("widget/button/BeaconEffectButton.java", [
    ("this.sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());", "this.sprite = Hud.getMobEffectSprite(effect.getEffect());"),
    ("graphics.blit(this.getX() + 2, this.getY() + 2, 0, 18, 18, this.sprite);",
     "graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);"),
    (r"TextureAtlasSprite sprite", "Identifier sprite"),
], ["net.minecraft.client.gui.Hud", "net.minecraft.client.renderer.RenderPipelines", "net.minecraft.resources.Identifier"])
edit("widget/button/HistoryChatWidget.java", [
    (r"\n[ \t]*graphics\.setColor\([^;]*\);", ""),
    (r"PlayerFaceExtractor\.extractRenderState\(graphics, this\.playerSkin, ([^;]*), size\);",
     r"PlayerFaceExtractor.extractRenderState(graphics, this.playerSkin, \1, size, true, false, -1);"),
], regex=True)
print("gui misc patched")
