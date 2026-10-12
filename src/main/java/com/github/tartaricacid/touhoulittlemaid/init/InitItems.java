package com.github.tartaricacid.touhoulittlemaid.init;

import net.minecraft.world.item.SpawnEggItem;
import com.github.tartaricacid.touhoulittlemaid.util.PendingIds;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.item.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class InitItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TouhouLittleMaid.MOD_ID);

    public static DeferredItem<Item> MAID_BACKPACK_SMALL = ITEMS.register("maid_backpack_small", id -> PendingIds.item(id, ItemMaidBackpack::new));
    public static DeferredItem<Item> MAID_BACKPACK_MIDDLE = ITEMS.register("maid_backpack_middle", id -> PendingIds.item(id, ItemMaidBackpack::new));
    public static DeferredItem<Item> MAID_BACKPACK_BIG = ITEMS.register("maid_backpack_big", id -> PendingIds.item(id, ItemMaidBackpack::new));
    public static DeferredItem<Item> CRAFTING_TABLE_BACKPACK = ITEMS.register("crafting_table_backpack", id -> PendingIds.item(id, ItemMaidBackpack::new));
    public static DeferredItem<Item> ENDER_CHEST_BACKPACK = ITEMS.register("ender_chest_backpack", id -> PendingIds.item(id, ItemMaidBackpack::new));
    public static DeferredItem<Item> FURNACE_BACKPACK = ITEMS.register("furnace_backpack", id -> PendingIds.item(id, ItemMaidBackpack::new));
    public static DeferredItem<Item> TANK_BACKPACK = ITEMS.register("tank_backpack", id -> PendingIds.item(id, ItemTankBackpack::new));
    public static DeferredItem<Item> CHAIR = ITEMS.register("chair", id -> PendingIds.item(id, ItemChair::new));
    public static DeferredItem<Item> HAKUREI_GOHEI = ITEMS.register("hakurei_gohei", id -> PendingIds.item(id, ItemHakureiGohei::new));
    public static DeferredItem<Item> SANAE_GOHEI = ITEMS.register("sanae_gohei", id -> PendingIds.item(id, ItemHakureiGohei::new));
    public static DeferredItem<Item> MAID_BED = ITEMS.register("maid_bed", id -> PendingIds.item(id, ItemMaidBed::new));
    public static DeferredItem<Item> EXTINGUISHER = ITEMS.register("extinguisher", id -> PendingIds.item(id, ItemExtinguisher::new));
    public static DeferredItem<Item> ULTRAMARINE_ORB_ELIXIR = ITEMS.register("ultramarine_orb_elixir", id -> PendingIds.item(id, () -> new ItemDamageableBauble(6)));
    public static DeferredItem<Item> EXPLOSION_PROTECT_BAUBLE = ITEMS.register("explosion_protect_bauble", id -> PendingIds.item(id, () -> new ItemDamageableBauble(32)));
    public static DeferredItem<Item> FIRE_PROTECT_BAUBLE = ITEMS.register("fire_protect_bauble", id -> PendingIds.item(id, () -> new ItemDamageableBauble(128)));
    public static DeferredItem<Item> PROJECTILE_PROTECT_BAUBLE = ITEMS.register("projectile_protect_bauble", id -> PendingIds.item(id, () -> new ItemDamageableBauble(64)));
    public static DeferredItem<Item> MAGIC_PROTECT_BAUBLE = ITEMS.register("magic_protect_bauble", id -> PendingIds.item(id, () -> new ItemDamageableBauble(128)));
    public static DeferredItem<Item> FALL_PROTECT_BAUBLE = ITEMS.register("fall_protect_bauble", id -> PendingIds.item(id, () -> new ItemDamageableBauble(32)));
    public static DeferredItem<Item> DROWN_PROTECT_BAUBLE = ITEMS.register("drown_protect_bauble", id -> PendingIds.item(id, () -> new ItemDamageableBauble(64)));
    public static DeferredItem<Item> NIMBLE_FABRIC = ITEMS.register("nimble_fabric", id -> PendingIds.item(id, () -> new ItemDamageableBauble(64)));
    public static DeferredItem<Item> ITEM_MAGNET_BAUBLE = ITEMS.register("item_magnet_bauble", id -> PendingIds.item(id, ItemNormalBauble::new));
    public static DeferredItem<Item> MUTE_BAUBLE = ITEMS.register("mute_bauble", id -> PendingIds.item(id, ItemNormalBauble::new));
    public static DeferredItem<Item> ENTITY_PLACEHOLDER = ITEMS.register("entity_placeholder", id -> PendingIds.item(id, ItemEntityPlaceholder::new));
    public static DeferredItem<Item> SUBSTITUTE_JIZO = ITEMS.register("substitute_jizo", id -> PendingIds.item(id, ItemSubstituteJizo::new));
    public static DeferredItem<Item> POWER_POINT = ITEMS.register("power_point", id -> PendingIds.item(id, ItemPowerPoint::new));
    public static DeferredItem<Item> CAMERA = ITEMS.register("camera", id -> PendingIds.item(id, ItemCamera::new));
    public static DeferredItem<Item> PHOTO = ITEMS.register("photo", id -> PendingIds.item(id, ItemPhoto::new));
    public static DeferredItem<Item> FILM = ITEMS.register("film", id -> PendingIds.item(id, ItemFilm::new));
    public static DeferredItem<Item> CHISEL = ITEMS.register("chisel", id -> PendingIds.item(id, ItemChisel::new));
    public static DeferredItem<Item> GARAGE_KIT = ITEMS.register("garage_kit", id -> PendingIds.item(id, ItemGarageKit::new));
    public static DeferredItem<Item> SMART_SLAB_INIT = ITEMS.register("smart_slab_init", id -> PendingIds.item(id, () -> new ItemSmartSlab(ItemSmartSlab.Type.INIT)));
    public static DeferredItem<Item> SMART_SLAB_EMPTY = ITEMS.register("smart_slab_empty", id -> PendingIds.item(id, () -> new ItemSmartSlab(ItemSmartSlab.Type.EMPTY)));
    public static DeferredItem<Item> SMART_SLAB_HAS_MAID = ITEMS.register("smart_slab_has_maid", id -> PendingIds.item(id, () -> new ItemSmartSlab(ItemSmartSlab.Type.HAS_MAID)));
    public static DeferredItem<Item> TRUMPET = ITEMS.register("trumpet", id -> PendingIds.item(id, ItemTrumpet::new));
    public static DeferredItem<Item> WIRELESS_IO = ITEMS.register("wireless_io", id -> PendingIds.item(id, ItemWirelessIO::new));
    public static DeferredItem<Item> MAID_BEACON = ITEMS.register("maid_beacon", id -> PendingIds.item(id, ItemMaidBeacon::new));
    public static DeferredItem<Item> MODEL_SWITCHER = ITEMS.register("model_switcher", id -> PendingIds.item(id, ItemModelSwitcher::new));
    public static DeferredItem<Item> CHAIR_SHOW = ITEMS.register("chair_show", id -> PendingIds.item(id, ItemChairShow::new));
    public static DeferredItem<Item> GOMOKU = ITEMS.register("gomoku", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.GOMOKU.get(), new Item.Properties())));
    public static DeferredItem<Item> CCHESS = ITEMS.register("cchess", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.CCHESS.get(), new Item.Properties())));
    public static DeferredItem<Item> WCHESS = ITEMS.register("wchess", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.WCHESS.get(), new Item.Properties())));
    public static DeferredItem<Item> RED_FOX_SCROLL = ITEMS.register("red_fox_scroll", id -> PendingIds.item(id, ItemFoxScroll::new));
    public static DeferredItem<Item> WHITE_FOX_SCROLL = ITEMS.register("white_fox_scroll", id -> PendingIds.item(id, ItemFoxScroll::new));
    public static DeferredItem<Item> KEYBOARD = ITEMS.register("keyboard", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.KEYBOARD.get(), new Item.Properties())));
    public static DeferredItem<Item> BOOKSHELF = ITEMS.register("bookshelf", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.BOOKSHELF.get(), new Item.Properties())));
    public static DeferredItem<Item> COMPUTER = ITEMS.register("computer", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.COMPUTER.get(), new Item.Properties())));
    public static DeferredItem<Item> FAVORABILITY_TOOL_ADD = ITEMS.register("favorability_tool_add", id -> PendingIds.item(id, () -> new ItemFavorabilityTool("add")));
    public static DeferredItem<Item> FAVORABILITY_TOOL_REDUCE = ITEMS.register("favorability_tool_reduce", id -> PendingIds.item(id, () -> new ItemFavorabilityTool("reduce")));
    public static DeferredItem<Item> FAVORABILITY_TOOL_FULL = ITEMS.register("favorability_tool_full", id -> PendingIds.item(id, () -> new ItemFavorabilityTool("full")));
    public static DeferredItem<Item> SHRINE = ITEMS.register("shrine", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.SHRINE.get(), new Item.Properties().rarity(Rarity.RARE))));
    public static DeferredItem<Item> KAPPA_COMPASS = ITEMS.register("kappa_compass", id -> PendingIds.item(id, ItemKappaCompass::new));
    public static DeferredItem<Item> BROOM = ITEMS.register("broom", id -> PendingIds.item(id, ItemBroom::new));
    public static DeferredItem<Item> PICNIC_BASKET = ITEMS.register("picnic_basket", id -> PendingIds.item(id, () -> new ItemPicnicBasket(InitBlocks.PICNIC_MAT.get())));
    public static DeferredItem<Item> SCARECROW = ITEMS.register("scarecrow", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.SCARECROW.get(), new Item.Properties())));
    public static DeferredItem<Item> SERVANT_BELL = ITEMS.register("servant_bell", id -> PendingIds.item(id, ItemServantBell::new));
    public static DeferredItem<Item> ENTITY_ID_COPY = ITEMS.register("entity_id_copy", id -> PendingIds.item(id, ItemEntityIdCopy::new));
    public static DeferredItem<Item> OWNER_CONVERSION_TOOL = ITEMS.register("owner_conversion_tool", id -> PendingIds.item(id, () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))));
    public static DeferredItem<Item> GOMOKU_BOARD_STATE = ITEMS.register("gomoku_board_state", id -> PendingIds.item(id, ItemBoardState::new));
    public static DeferredItem<Item> CCHESS_BOARD_STATE = ITEMS.register("cchess_board_state", id -> PendingIds.item(id, ItemBoardState::new));
    public static DeferredItem<Item> WCHESS_BOARD_STATE = ITEMS.register("wchess_board_state", id -> PendingIds.item(id, ItemBoardState::new));
    public static DeferredItem<Item> SNACK_CABINET = ITEMS.register("snack_cabinet", id -> PendingIds.item(id, () -> new BlockItem(InitBlocks.SNACK_CABINET.get(), new Item.Properties())));
    @SuppressWarnings("deprecation")
    public static DeferredItem<Item> MONSTER_LIST = ITEMS.register("monster_list", id -> PendingIds.item(id, ItemMonsterList::new));

    public static DeferredItem<Item> MAID_SPAWN_EGG = ITEMS.register("maid_spawn_egg", id -> PendingIds.item(id, () -> new SpawnEggItem(new Item.Properties().spawnEgg(EntityMaid.TYPE))));
    public static DeferredItem<Item> FAIRY_SPAWN_EGG = ITEMS.register("fairy_spawn_egg", id -> PendingIds.item(id, ItemFairySpawnEgg::new));

    public static final Identifier MEMORIZABLE_GENSOKYO_LOCATION = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "memorizable_gensokyo");

    // 成就图标
    public static DeferredItem<Item> CHANGE_CHAIR_MODEL = ITEMS.register("change_chair_model", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> CHANGE_MAID_MODEL = ITEMS.register("change_maid_model", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> MAID_100_HEALTHY = ITEMS.register("maid_100_healthy", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> KILL_100 = ITEMS.register("kill_100", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> KILL_SLIME_300 = ITEMS.register("kill_slime_300", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> ALL_NETHERITE_EQUIPMENT = ITEMS.register("all_netherite_equipment", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> KILL_WITHER = ITEMS.register("kill_wither", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> KILL_DRAGON = ITEMS.register("kill_dragon", id -> PendingIds.item(id, ItemAdvancementIcon::new));
    public static DeferredItem<Item> TACZ_GUN_ICON = ITEMS.register("tacz_gun_icon", id -> PendingIds.item(id, ItemAdvancementIcon::new));
}