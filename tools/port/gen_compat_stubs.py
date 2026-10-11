"""Generates no-op stubs for compat entry points that core code calls.

The real integrations live in src/compat_disabled. To re-enable one, delete its stub
package from src/main/java and move the real package back.
"""
import os

BASE = "src/main/java/com/github/tartaricacid/touhoulittlemaid/compat/"
HDR = ("// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is\n"
       "// re-enabled once the mod has a build for this Minecraft version.\n")
P = "com.github.tartaricacid.touhoulittlemaid"

STUBS = {
    "gun/common/GunCommonUtil": f"""import {P}.entity.passive.EntityMaid;
import {P}.entity.task.TaskManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class GunCommonUtil {{
    public static void initAndAddTask(TaskManager manager) {{
    }}

    public static boolean isInstalled() {{
        return false;
    }}

    public static boolean isGun(ItemStack stack) {{
        return false;
    }}

    public static boolean canStartAttacking(EntityMaid maid) {{
        return false;
    }}

    @Nullable
    public static Identifier getGunId(ItemStack stack) {{
        return null;
    }}

    public static Optional<Boolean> canSee(EntityMaid maid, LivingEntity target) {{
        return Optional.empty();
    }}

    public static void tick(EntityMaid shooter, LivingEntity target, ItemStack gunItem) {{
    }}

    public static int performGunAttack(EntityMaid shooter, LivingEntity target, ItemStack gunItem) {{
        return 0;
    }}
}}
""",
    "gun/swarfare/SWarfareCompat": f"""import {P}.entity.passive.EntityMaid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class SWarfareCompat {{
    public static boolean isInstalled() {{
        return false;
    }}

    public static boolean shouldHideLivingRender(LivingEntity entity) {{
        return false;
    }}

    public static boolean isVehicle(Entity entity) {{
        return false;
    }}

    public static boolean isGrenade(ItemStack stack) {{
        return false;
    }}

    public static Optional<Boolean> canVehicleSee(EntityMaid maid, LivingEntity target) {{
        return Optional.empty();
    }}
}}
""",
    "immersivemelodies/client/ImmersiveMelodiesCompat": f"""import {P}.client.animation.HardcodedAnimationManger;
import {P}.client.animation.gecko.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

public class ImmersiveMelodiesCompat {{
    public static void init() {{
    }}

    public static void addAnimation(HardcodedAnimationManger manger) {{
    }}

    public static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData imData) {{
    }}

    public static void addBinding(CtrlBinding binding) {{
    }}

    public static final class ImmersiveMelodiesData {{
        public float pitch = 0f;
        public float volume = 0f;
        public float current = 0f;
        public long delta = 0L;
        public long time = 0L;
    }}
}}
""",
    "immersivemelodies/server/ImmersiveMelodiesServerCompat": """import net.minecraft.world.item.ItemStack;

public class ImmersiveMelodiesServerCompat {
    public static void init() {
    }

    public static boolean isInstrumentItem(ItemStack stack) {
        return false;
    }
}
""",
    "curios/CuriosCompat": f"""import {P}.entity.passive.EntityMaid;
import net.minecraft.world.MenuProvider;

public class CuriosCompat {{
    public static void init() {{
    }}

    public static boolean isLoaded() {{
        return false;
    }}

    public static boolean isLoadedOrEnable() {{
        return false;
    }}

    public static MenuProvider create(EntityMaid maid) {{
        return maid.getMaidBackpackType().getGuiProvider(maid.getId());
    }}

    public static void clientUpdatePage(int page) {{
    }}

    public static void clientResetPage() {{
    }}
}}
""",
    "cloth/ClothConfigCompat": """public final class ClothConfigCompat {
    public static void openConfigScreen() {
    }
}
""",
    "cloth/MenuIntegration": """import net.neoforged.fml.ModContainer;

public class MenuIntegration {
    public static void registerModsPage(ModContainer modContainer) {
    }
}
""",
    "embeddium/EmbeddiumCompat": """public class EmbeddiumCompat {
    public static void init() {
    }

    public static boolean isEmbeddiumInstalled() {
        return false;
    }
}
""",
    "oculus/OculusCompat": """public final class OculusCompat {
    public static void init() {
    }

    public static boolean isOculusInstalled() {
        return false;
    }
}
""",
    "sodium/SodiumCompat": """public class SodiumCompat {
    public static void init() {
    }

    public static boolean isSodiumInstalled() {
        return false;
    }
}
""",
    "patpat/PatPatCompat": """public class PatPatCompat {
    public static void init() {
    }
}
""",
    "ponder/PonderCompat": """public class PonderCompat {
    public static final String MOD_ID = "ponder";

    public static void register() {
    }
}
""",
    "simplehats/SimpleHatsCompat": """public class SimpleHatsCompat {
    public static void init() {
    }
}
""",
    "kubejs/ModKubeJSCompat": f"""import {P}.client.overlay.MaidTipsOverlay;
import {P}.entity.task.TaskManager;
import {P}.item.bauble.BaubleManager;

public class ModKubeJSCompat {{
    public static boolean ENABLE = false;

    public static void maidTipsOverlayInit(MaidTipsOverlay overlay) {{
    }}

    public static void maidBaubleInit(BaubleManager manager) {{
    }}

    public static void maidTaskInit(TaskManager manager) {{
    }}
}}
""",
    "slashblade/SlashBladeCompat": f"""import {P}.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;

public class SlashBladeCompat {{
    public static boolean isSlashBladeLoaded() {{
        return false;
    }}

    public static boolean isSlashBladeItem(ItemStack stack) {{
        return false;
    }}

    public static void swingSlashBlade(EntityMaid maid, ItemStack itemInHand) {{
    }}
}}
""",
    "farmersdelight/FarmersDelightCompat": f"""import {P}.entity.ai.edible.MaidEdibleBlockManager;

public class FarmersDelightCompat {{
    public static final String ID = "farmersdelight";

    public static void addFarmersDelightEdible(MaidEdibleBlockManager manager) {{
    }}
}}
""",
    "jmc/JmcCompat": f"""import {P}.entity.ai.edible.MaidEdibleBlockManager;

public class JmcCompat {{
    public static final String ID = "jmc";

    public static void addJmcEdible(MaidEdibleBlockManager manager) {{
    }}
}}
""",
    "kaleidoscope/KaleidoscopeCompat": f"""import {P}.entity.ai.edible.MaidEdibleBlockManager;
import {P}.entity.task.crop.SpecialCropManager;

public class KaleidoscopeCompat {{
    public static final String COOKERY_ID = "kaleidoscope_cookery";

    public static void addCropHandlers(SpecialCropManager manager) {{
    }}

    public static void addBlockFoods(MaidEdibleBlockManager manager) {{
    }}
}}
""",
    "aquaculture/AquacultureCompat": f"""import {P}.entity.ai.fishing.FishingTypeManager;
import net.neoforged.bus.api.IEventBus;

public class AquacultureCompat {{
    public static void init(IEventBus eventBus) {{
    }}

    public static void registerFishingType(FishingTypeManager manager) {{
    }}
}}
""",
    "patchouli/PatchouliCompat": """public class PatchouliCompat {
    public static void init() {
    }
}
""",
    "sbackpack/SBackpackCompat": """import net.minecraft.world.item.ItemStack;

public class SBackpackCompat {
    public static void init() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static boolean isBackpack(ItemStack stack) {
        return false;
    }
}
""",
    "tbackpack/TBackpackCompat": """import net.minecraft.world.item.ItemStack;

public class TBackpackCompat {
    public static void init() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static boolean isBackpack(ItemStack stack) {
        return false;
    }
}
""",
    "ironchest/IronChestType": f"""import {P}.inventory.chest.ChestManager;

public final class IronChestType {{
    public static void register(ChestManager manager) {{
    }}
}}
""",
    "invtweaks/InvTweaksCompat": """public class InvTweaksCompat {
    public static final String INV_TWEAKS_ID = "invtweaks";

    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }
}
""",
}


def main():
    for path, body in STUBS.items():
        pkg = P + ".compat." + ".".join(path.split("/")[:-1])
        f = BASE + path + ".java"
        os.makedirs(os.path.dirname(f), exist_ok=True)
        if body.startswith("import"):
            imports, rest = body.split("\n\npublic ", 1)
            rest = "public " + rest
            text = "package %s;\n\n%s\n\n%s%s" % (pkg, imports, HDR, rest)
        else:
            text = "package %s;\n\n%s%s" % (pkg, HDR, body)
        with open(f, "w", encoding="utf-8") as out:
            out.write(text)
    print(len(STUBS), "stubs written")


if __name__ == "__main__":
    main()
