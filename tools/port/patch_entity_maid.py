"""One-off port patch for EntityMaid (MC 1.21.1 -> 26.2). Exact replacements; fails loudly if a target is missing."""
import re
import sys

P = "src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java"
t = open(P, encoding="utf-8").read()


def rep(old, new, count=1):
    global t
    if old not in t:
        sys.exit("NOT FOUND:\n" + old)
    t = t.replace(old, new, count)


def imp(fqn):
    global t
    if "import %s;" % fqn not in t:
        t = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % fqn, t, count=1, flags=re.M)


# entity type key
rep('.sized(0.6f, 1.5f).clientTrackingRange(10).build("maid");',
    '.sized(0.6f, 1.5f).clientTrackingRange(10)\n'
    '            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "maid")));')

# brain: provider + makeBrain(Packed); behaviours are registered by MaidBrain after construction
rep('''    @Override
    protected Brain.Provider<EntityMaid> brainProvider() {
        return Brain.provider(MaidBrain.getMemoryTypes(), MaidBrain.getSensorTypes());
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamicIn) {
        Brain<EntityMaid> brain = this.brainProvider().makeBrain(dynamicIn);
        MaidBrain.registerBrainGoals(brain, this);
        return brain;
    }

    public void refreshBrain(ServerLevel serverWorldIn) {
        Brain<EntityMaid> brain = this.getBrain();
        brain.stopAll(serverWorldIn, this);
        this.brain = brain.copyWithoutBehaviors();
        MaidBrain.registerBrainGoals(this.getBrain(), this);
    }''', '''    @SuppressWarnings("deprecation")
    private static final Brain.Provider<EntityMaid> BRAIN_PROVIDER = Brain.provider(MaidBrain.getMemoryTypes(), MaidBrain.getSensorTypes(), maid -> List.of());

    @Override
    protected Brain<EntityMaid> makeBrain(Brain.Packed packedBrain) {
        Brain<EntityMaid> brain = BRAIN_PROVIDER.makeBrain(this, packedBrain);
        MaidBrain.registerBrainGoals(brain, this);
        return brain;
    }

    public void refreshBrain(ServerLevel serverWorldIn) {
        Brain<EntityMaid> brain = this.getBrain();
        brain.stopAll(serverWorldIn, this);
        // Little Maid Continued: copyWithoutBehaviors() is gone; rebuild the brain keeping its memories
        this.brain = BRAIN_PROVIDER.makeBrain(this, brain.pack());
        MaidBrain.registerBrainGoals(this.getBrain(), this);
    }''')

rep('''    protected void customServerAiStep() {
        long timeRecord = Util.getNanos();''', '''    protected void customServerAiStep(ServerLevel serverLevel) {
        long timeRecord = Util.getNanos();''')
rep('''        super.customServerAiStep();
    }''', '''        super.customServerAiStep(serverLevel);
    }''')

# shields are a data component now
t = t.replace("canPerformAction(ItemAbilities.SHIELD_BLOCK)", "has(DataComponents.BLOCKS_ATTACKS)")

# experience orb value is private
rep("int i = Math.min((int) (entityXPOrb.value * itemstack.getXpRepairRatio()), itemstack.getDamageValue());",
    "int i = Math.min((int) (entityXPOrb.getValue() * itemstack.getXpRepairRatio()), itemstack.getDamageValue());")
rep("entityXPOrb.value -= (i / 2);", "entityXPOrb.setValue(entityXPOrb.getValue() - i / 2);")
rep("if (entityXPOrb.value > 0) {", "if (entityXPOrb.getValue() > 0) {")
rep("this.setExperience(getExperience() + entityXPOrb.value);", "this.setExperience(getExperience() + entityXPOrb.getValue());")

# melee attack
rep("    public boolean doHurtTarget(Entity target) {", "    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {")
rep("boolean result = super.doHurtTarget(target);", "boolean result = super.doHurtTarget(serverLevel, target);")
rep('''            if (target instanceof LivingEntity livingEntity && item.hurtEnemy(mainHandItem, livingEntity, this)) {
                item.postHurtEnemy(mainHandItem, livingEntity, this);
            }''', '''            if (target instanceof LivingEntity livingEntity) {
                item.hurtEnemy(mainHandItem, livingEntity, this);
                item.postHurtEnemy(mainHandItem, livingEntity, this);
            }''')
rep("entity.knockback(0.4, posX, posY);", "entity.knockback(0.4, posX, posY, this.damageSources().mobAttack(this), sweepDamage);")

# damage: hurt -> hurtServer
rep("    public boolean hurt(DamageSource source, float amount) {", "    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {")
t = t.replace("            return super.hurt(source, amount);", "            return super.hurtServer(serverLevel, source, amount);")
t = t.replace("        return super.hurt(source, amount);\n    }", "        return super.hurtServer(serverLevel, source, amount);\n    }")
rep("    protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {\n        if (!this.isInvulnerableTo(damageSrc) && this.damageContainers != null) {",
    "    protected void actuallyHurt(ServerLevel serverLevel, DamageSource damageSrc, float damageAmount) {\n        if (!this.isInvulnerableTo(serverLevel, damageSrc) && this.damageContainers != null) {")

# dimension change -> teleport(TeleportTransition)
rep("    public Entity changeDimension(DimensionTransition pTransition) {", "    public Entity teleport(TeleportTransition pTransition) {")

# owner uuid
rep("    public boolean teleportToOwner(LivingEntity owner) {", '''    /**
     * Little Maid Continued: MC 26.x stores the owner as an {@link EntityReference}.
     */
    @Nullable
    public UUID getOwnerUUID() {
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        return owner == null ? null : owner.getUUID();
    }

    public boolean teleportToOwner(LivingEntity owner) {''')

t = re.sub(r"(this\.)?level\.getGameRules\(\)\.getBoolean\((GameRules\.\w+)\)", r"PortUtil.gameRule(this.level, \2)", t)

rep("    public boolean canAttackType(EntityType<?> typeIn) {", "    public boolean canAttackType(EntityType<?> typeIn) {\n        // Little Maid Continued: no longer a vanilla hook; kept for the mod's own checks")
t = t.replace("    @Override\n    public boolean canAttackType(EntityType<?> typeIn) {", "    public boolean canAttackType(EntityType<?> typeIn) {")

rep("    protected void dropEquipment() {", "    protected void dropEquipment(ServerLevel serverLevel) {")

# eating: vanilla LivingEntity#eat is gone (Consumable component)
rep('''    @Override
    public ItemStack eat(Level level, ItemStack food, FoodProperties pFoodProperties) {
        ItemStack copy = food.copy();
        ItemStack foodAfterEat = super.eat(level, food, pFoodProperties);
        Optional<ItemStack> converts = pFoodProperties.usingConvertsTo();
        NeoForge.EVENT_BUS.post(new MaidAfterEatEvent(this, foodAfterEat.isEmpty() && converts.isPresent() ? copy : foodAfterEat));
        return foodAfterEat;
    }''', '''    public ItemStack eat(Level level, ItemStack food, @Nullable FoodProperties pFoodProperties) {
        ItemStack copy = food.copy();
        ItemStack foodAfterEat = food.finishUsingItem(level, this);
        boolean converts = copy.has(DataComponents.USE_REMAINDER);
        NeoForge.EVENT_BUS.post(new MaidAfterEatEvent(this, foodAfterEat.isEmpty() && converts ? copy : foodAfterEat));
        return foodAfterEat;
    }''')
rep("    public int getBaseExperienceReward() {", "    public int getBaseExperienceReward(ServerLevel serverLevel) {")

rep("public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {",
    "public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn) {")

rep('''        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getMaterial() == ArmorMaterials.NETHERITE;
        }
        return false;''', '''        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.assetId().map(EquipmentAssets.NETHERITE::equals).orElse(false);''')

# AgeableMob#isBaby is final; the maid's age never goes below 0, so it is never a baby
rep('''    @Override
    public boolean isBaby() {
        return false;
    }

''', "")

rep("    public boolean canFireProjectileWeapon(ProjectileWeaponItem shootableItem) {", "    public boolean canUseNonMeleeWeapon(ItemStack shootableItem) {")

# culling box is no longer an entity method; the renderer reads it through getMaidCullingBox()
rep('''    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        BedrockModel<Mob> model = CustomPackLoader.MAID_MODELS.getModel(getModelId()).orElse(null);
        if (model == null) {
            return super.getBoundingBoxForCulling();
        }''', '''    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        BedrockModel<Mob> model = CustomPackLoader.MAID_MODELS.getModel(getModelId()).orElse(null);
        if (model == null) {
            return this.getBoundingBox();
        }''')

# restriction -> home: keep mod names, bridge the new vanilla hooks
for name in ["isWithinRestriction() {", "isWithinRestriction(BlockPos pos) {", "restrictTo(BlockPos pos, int distance) {",
             "getRestrictCenter() {", "getRestrictRadius() {", "clearRestriction() {", "hasRestriction() {"]:
    t = re.sub(r"    @Override\n(    public \w+ %s)" % re.escape(name), r"\1", t)
rep('''    public boolean hasRestriction() {
        return this.isHomeModeEnable();
    }''', '''    public boolean hasRestriction() {
        return this.isHomeModeEnable();
    }

    // Little Maid Continued: MC 26.x renamed mob "restriction" to "home"; route vanilla AI to the maid's own data
    @Override
    public boolean hasHome() {
        return this.hasRestriction();
    }

    @Override
    public boolean isWithinHome() {
        return this.isWithinRestriction();
    }

    @Override
    public boolean isWithinHome(BlockPos pos) {
        return this.isWithinRestriction(pos);
    }

    @Override
    public boolean isWithinHome(Vec3 pos) {
        return this.isWithinRestriction(BlockPos.containing(pos));
    }

    @Override
    public void setHomeTo(BlockPos pos, int radius) {
        this.restrictTo(pos, radius);
    }

    @Override
    public BlockPos getHomePosition() {
        return this.getRestrictCenter();
    }

    @Override
    public int getHomeRadius() {
        return Math.round(this.getRestrictRadius());
    }

    @Override
    public void clearHome() {
        this.clearRestriction();
    }''')

# ingredients
rep("return Ingredient.fromValues(parts.stream().flatMap(i -> Arrays.stream(i.getValues())));",
    "return Ingredient.of(parts.stream().flatMap(i -> i.items().map(Holder::value)));")
t = t.replace("Ingredient.of(TagItem.MAID_TAMED_ITEM)", "Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TagItem.MAID_TAMED_ITEM))")
rep("            return Ingredient.of(tagKey);", "            return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tagKey));")
rep("return Ingredient.of(BuiltInRegistries.ITEM.get(key));", "return Ingredient.of(BuiltInRegistries.ITEM.getValue(key));")

t = t.replace("this.moveTo(", "this.snapTo(")
t = t.replace("this.isControlledByLocalInstance()", "this.isLocalInstanceAuthoritative()")

rep('''    @Override
    public Vec3 handleRelativeFrictionAndCalculateMovement(''', '''    // Little Maid Continued: vanilla made this private in MC 26.x, so this no longer overrides it (TODO: mixin)
    public Vec3 handleRelativeFrictionAndCalculateMovement(''')

# shield: vanilla 26.x handles disabling / damaging through the BlocksAttacks component
t = re.sub(r"    @Override\n    protected void blockUsingShield\(LivingEntity attacker\) \{.*?\n    \}\n\n", "", t, flags=re.S)
t = re.sub(r"    @Override\n    protected void hurtCurrentlyUsedShield\(float damage\) \{.*?\n    \}\n(?=\n|\s*/\*\*|\s*@|\s*public|\s*private|\s*protected)", "", t, count=1, flags=re.S)
t = t.replace("!this.getCooldowns().isOnCooldown(offhandItem.getItem())", "!this.getCooldowns().isOnCooldown(offhandItem)")

rep("ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, stack);",
    "ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack));")

for fqn in ["net.minecraft.resources.ResourceKey", "net.minecraft.core.registries.Registries", "net.minecraft.core.Holder",
            "net.minecraft.core.component.DataComponents", "net.minecraft.world.entity.EntityReference",
            "net.minecraft.world.level.portal.TeleportTransition", "net.minecraft.world.item.equipment.Equippable",
            "net.minecraft.world.item.equipment.EquipmentAssets", "com.github.tartaricacid.touhoulittlemaid.util.PortUtil",
            "net.minecraft.world.entity.EntitySpawnReason", "java.util.UUID", "java.util.List"]:
    imp(fqn)
for gone in ["net.minecraft.world.level.portal.DimensionTransition", "net.minecraft.world.item.ArmorItem",
             "net.minecraft.world.item.ArmorMaterials", "net.minecraft.world.item.ProjectileWeaponItem"]:
    t = t.replace("import %s;\n" % gone, "")

open(P, "w", encoding="utf-8", newline="\n").write(t)
print("EntityMaid patched")
