package com.github.tartaricacid.touhoulittlemaid.client.resource.models;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.inner.InnerAnimation;
import com.github.tartaricacid.touhoulittlemaid.client.model.PlayerMaidModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jspecify.annotations.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public final class PlayerMaidModels {
    private static final Cache<String, GameProfile> GAME_PROFILE_CACHE = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final GameProfile EMPTY_GAME_PROFILE = new GameProfile(UUID.randomUUID(), "alex");
    private static final PlayerMaidModel PLAYER_MAID_MODEL = new PlayerMaidModel(false);
    private static final PlayerMaidModel PLAYER_MAID_MODEL_SLIM = new PlayerMaidModel(true);
    private static final List<Identifier> PLAYER_MAID_ANIMATION_RES = Lists.newArrayList(
            Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/head/default.js"),
            Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/head/beg.js"),
            Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/leg/default.js"),
            Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/player/arm/default.js"),
            Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/default/arm/swing.js"),
            Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "animation/maid/player/sit/default.js")
    );
    private static final Identifier TEXTURE_ALEX = Identifier.withDefaultNamespace("textures/entity/player/slim/alex.png");
    private static final List<Object> PLAYER_MAID_ANIMATIONS = Lists.newArrayList();
    private static MaidModelInfo playerMaidInfo;
    private static Identifier playerSkin;

    public static void reload() {
        PLAYER_MAID_ANIMATIONS.clear();
        for (Identifier res : PLAYER_MAID_ANIMATION_RES) {
            PLAYER_MAID_ANIMATIONS.add(InnerAnimation.get(res));
        }
        playerMaidInfo = new MaidModelInfo() {
            @Override
            public Identifier getTexture() {
                return playerSkin;
            }
        };
    }

    public static BedrockModel<Mob> getPlayerMaidModel(String name) {
        PlayerSkin skin = getSkin(name);
        return skin != null && skin.model() == PlayerModelType.SLIM ? PLAYER_MAID_MODEL_SLIM : PLAYER_MAID_MODEL;
    }

    public static List<Object> getPlayerMaidAnimations() {
        return PLAYER_MAID_ANIMATIONS;
    }

    public static MaidModelInfo getPlayerMaidInfo(String name) {
        playerSkin = getPlayerSkin(name);
        return playerMaidInfo;
    }

    public static Identifier getPlayerSkin(String name) {
        PlayerSkin skin = getSkin(name);
        return skin != null ? skin.body().texturePath() : TEXTURE_ALEX;
    }

    /**
     * The skin of the named player, or null while the profile is still being looked up (that happens off-thread).
     */
    private static @Nullable PlayerSkin getSkin(String name) {
        GameProfile profile = GAME_PROFILE_CACHE.getIfPresent(name);
        if (profile == null) {
            GAME_PROFILE_CACHE.put(name, EMPTY_GAME_PROFILE);
            CompletableFuture.supplyAsync(() -> Minecraft.getInstance().services().profileResolver().fetchByName(name), Util.backgroundExecutor())
                    .thenAccept(result -> GAME_PROFILE_CACHE.put(name, result.orElse(EMPTY_GAME_PROFILE)));
            return null;
        }
        return Minecraft.getInstance().getSkinManager().createLookup(profile, false).get();
    }
}
