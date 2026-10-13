package com.github.tartaricacid.touhoulittlemaid.compat.immersivemelodies.client;

import com.github.tartaricacid.touhoulittlemaid.client.animation.HardcodedAnimationManger;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class ImmersiveMelodiesCompat {
    public static void init() {
    }

    public static void addAnimation(HardcodedAnimationManger manger) {
    }

    public static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData imData) {
    }

    public static void addBinding(CtrlBinding binding) {
        // animations reference these even without the mod installed
        binding.livingEntityVar("im_pitch", ctx -> 0f);
        binding.livingEntityVar("im_volume", ctx -> 0f);
        binding.livingEntityVar("im_current", ctx -> 0f);
        binding.livingEntityVar("im_delta", ctx -> 0L);
        binding.livingEntityVar("im_time", ctx -> 0L);
    }

    public static final class ImmersiveMelodiesData {
        public float pitch = 0f;
        public float volume = 0f;
        public float current = 0f;
        public long delta = 0L;
        public long time = 0L;
    }
}
