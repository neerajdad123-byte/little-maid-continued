package com.github.tartaricacid.touhoulittlemaid.compat.kubejs;

import com.github.tartaricacid.touhoulittlemaid.client.overlay.MaidTipsOverlay;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class ModKubeJSCompat {
    public static boolean ENABLE = false;

    public static void maidTipsOverlayInit(MaidTipsOverlay overlay) {
    }

    public static void maidBaubleInit(BaubleManager manager) {
    }

    public static void maidTaskInit(TaskManager manager) {
    }
}
