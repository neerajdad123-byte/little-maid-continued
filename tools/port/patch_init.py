"""One-off port patch: registration code (loot types, reload listeners, components, spawn eggs, capabilities)."""
import re
import sys

B = "src/main/java/com/github/tartaricacid/touhoulittlemaid/"


def edit(path, pairs, imports=(), drop_imports=(), regex=False):
    t = open(B + path, encoding="utf-8").read()
    for old, new in pairs:
        if regex:
            nt = re.sub(old, new, t, flags=re.S)
            if nt == t:
                sys.exit("NO MATCH in %s: %s" % (path, old))
            t = nt
        else:
            if old not in t:
                sys.exit("NOT FOUND in %s:\n%s" % (path, old))
            t = t.replace(old, new)
    for fqn in drop_imports:
        t = t.replace("import %s;\n" % fqn, "")
    for fqn in imports:
        if "import %s;" % fqn not in t:
            t = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % fqn, t, count=1, flags=re.M)
    open(B + path, "w", encoding="utf-8", newline="\n").write(t)


# loot condition / function registries hold MapCodecs since 1.21.2
edit("init/InitLootModifier.java", [
    ("DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES", "DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES"),
    ("DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES", "DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTION_TYPES"),
    ("Supplier<LootItemConditionType> LOOT_TABLE_TYPE = LOOT_CONDITION_TYPES.register(\"loot_table_type\",\n            () -> new LootItemConditionType(LootTableTypeCondition.CODEC));",
     "Supplier<MapCodec<LootTableTypeCondition>> LOOT_TABLE_TYPE = LOOT_CONDITION_TYPES.register(\"loot_table_type\",\n            () -> LootTableTypeCondition.CODEC);"),
], ["com.mojang.serialization.MapCodec", "net.minecraft.world.level.storage.loot.predicates.LootItemCondition",
    "net.minecraft.world.level.storage.loot.functions.LootItemFunction"],
   ["net.minecraft.world.level.storage.loot.functions.LootItemFunctionType", "net.minecraft.world.level.storage.loot.predicates.LootItemConditionType"])
t = open(B + "init/InitLootModifier.java", encoding="utf-8").read()
t = re.sub(r"Supplier<LootItemFunctionType<\? extends LootItemConditionalFunction>> (\w+) =\s*LOOT_FUNCTION_TYPES\.register\(\"(\w+)\", \(\) -> new LootItemFunctionType<>\((\w+)\.CODEC\)\);",
           r"Supplier<MapCodec<\3>> \1 =\n            LOOT_FUNCTION_TYPES.register(\"\2\", () -> \3.CODEC);", t)
open(B + "init/InitLootModifier.java", "w", encoding="utf-8", newline="\n").write(t)

edit("init/registry/DatapackRegistry.java", [
    ("public static void onAddReloadListenerEvent(AddReloadListenerEvent event) {", "public static void onAddReloadListenerEvent(AddServerReloadListenersEvent event) {"),
    ("event.addListener(new KaomojiDataReloadListener());", "event.addListener(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, \"kaomoji\"), new KaomojiDataReloadListener());"),
    ("event.addListener(new BoardStateDataReloadListener());", "event.addListener(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, \"board_state\"), new BoardStateDataReloadListener());"),
    ("event.addListener(new SkillsDataReloadListener());", "event.addListener(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, \"skills\"), new SkillsDataReloadListener());"),
], ["net.neoforged.neoforge.event.AddServerReloadListenersEvent", "net.minecraft.resources.Identifier"], ["net.neoforged.neoforge.event.AddReloadListenerEvent"])

edit("init/InitDataComponent.java", [("DeferredRegister.createDataComponents(TouhouLittleMaid.MOD_ID)", "DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TouhouLittleMaid.MOD_ID)")],
     ["net.minecraft.core.registries.Registries"])

edit("init/InitItems.java", [("new DeferredSpawnEggItem(() -> EntityMaid.TYPE, 0xffffff, 0xffffff, new Item.Properties())",
                              "new SpawnEggItem(new Item.Properties().spawnEgg(EntityMaid.TYPE))")],
     ["net.minecraft.world.item.SpawnEggItem"], ["net.neoforged.neoforge.common.DeferredSpawnEggItem"])
p = B + "item/ItemFairySpawnEgg.java"
open(p, "w", encoding="utf-8", newline="\n").write('''package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Little Maid Continued: MC 26.x spawn eggs take their entity from {@code Item.Properties#spawnEgg}.
 * TODO: the old "egg on a fairy spawns a baby fairy" hook is static in 26.x and was dropped.
 */
public class ItemFairySpawnEgg extends SpawnEggItem {
    public ItemFairySpawnEgg() {
        super(new Item.Properties().spawnEgg(EntityFairy.TYPE));
    }
}
''')

edit("init/InitCapabilities.java", [('''
        // 适配 forge 的实体 cap
        event.registerEntity(Capabilities.ItemHandler.ENTITY, InitEntities.MAID.get(), (maid, direction) -> maid.getAllInv());''',
                                    '''
        // TODO(Little Maid Continued): NeoForge 26.x moved entity item access to the ResourceHandler transfer API''')])

edit("init/InitCreativeTabs.java", [("EnchantedBookItem.createForEnchantment(instance)", "EnchantmentHelper.createBook(instance)")],
     ["net.minecraft.world.item.enchantment.EnchantmentHelper"], ["net.minecraft.world.item.EnchantedBookItem"])

edit("client/init/ClientSetupEvent.java", [], ["net.minecraft.resources.Identifier", "com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid"])
print("init patched")
