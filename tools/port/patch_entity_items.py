"""One-off port patch for small entity API changes in entity/item and the culling box move."""
import re
import sys

B = "src/main/java/com/github/tartaricacid/touhoulittlemaid/"


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


edit("entity/item/AbstractEntityFromItem.java", [
    ("    public void knockback(double strength, double ratioX, double ratioZ) {",
     "    public void knockback(double strength, double ratioX, double ratioZ, DamageSource source, float damage) {"),
    ("    public ItemStack getPickedResult(HitResult target) {", "    public ItemStack getPickResult() {"),
    ('''    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

''', ""),
], ["net.minecraft.world.damagesource.DamageSource"])

edit("entity/item/EntityBroom.java", [
    ('''    @Override
    @Nullable
    public UUID getOwnerUUID() {''', '''    @Override
    @Nullable
    public EntityReference<LivingEntity> getOwnerReference() {
        UUID uuid = this.getOwnerUUID();
        return uuid == null ? null : EntityReference.of(uuid);
    }

    @Nullable
    public UUID getOwnerUUID() {'''),
], ["net.minecraft.world.entity.EntityReference", "net.minecraft.world.entity.LivingEntity"])

edit("entity/item/EntityBox.java", [
    ("    public void kill(ServerLevel serverLevel) {\n        if (this.level instanceof ServerLevel serverLevel) {",
     "    public void kill(ServerLevel serverLevel) {\n        {"),
])

edit("entity/item/EntityExtinguishingAgent.java", [("level.addParticle(ParticleTypes.CLOUD, false,", "level.addParticle(ParticleTypes.CLOUD,")])

edit("entity/item/EntityTombstone.java", [
    ("ItemStack stack = Arrays.stream(ntrItem.getItems()).findFirst().orElse(ItemStack.EMPTY);",
     "ItemStack stack = ntrItem.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);"),
])

edit("entity/item/EntityPowerPoint.java", [("getChunkSource().broadcast(this, ", "getChunkSource().sendToTrackingPlayers(this, ")])

edit("entity/item/EntityChair.java", [
    ('''    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        BedrockModel<EntityChair> model = CustomPackLoader.CHAIR_MODELS.getModel(getModelId()).orElse(null);
        if (model == null) {
            return super.getBoundingBoxForCulling();
        }''', '''    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        BedrockModel<EntityChair> model = CustomPackLoader.CHAIR_MODELS.getModel(getModelId()).orElse(null);
        if (model == null) {
            return this.getBoundingBox();
        }'''),
])
for path, cls in [("entity/item/EntityChair.java", "EntityChair"), ("entity/passive/EntityMaid.java", "EntityMaid")]:
    t = open(B + path, encoding="utf-8").read()
    t = re.sub(r"(public class %s extends [^{]*?implements )" % cls, r"\1HasCullingBox, ", t, count=1)
    if "HasCullingBox, " not in t:
        sys.exit("no implements clause in " + cls)
    t = t.replace("    @OnlyIn(Dist.CLIENT)\n    public AABB getBoundingBoxForCulling() {", "    @Override\n    @OnlyIn(Dist.CLIENT)\n    public AABB getBoundingBoxForCulling() {")
    t = t.replace("    @Override\n    @Override\n", "    @Override\n")
    t = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.HasCullingBox;", t, count=1, flags=re.M)
    open(B + path, "w", encoding="utf-8", newline="\n").write(t)

edit("client/renderer/legacy/LegacyEntityRenderer.java", [
    ('''    public Identifier getTextureLocation(T entity) {''', '''    @Override
    protected AABB getBoundingBoxForCulling(T entity) {
        return entity instanceof HasCullingBox box ? box.getBoundingBoxForCulling() : super.getBoundingBoxForCulling(entity);
    }

    public Identifier getTextureLocation(T entity) {'''),
], ["net.minecraft.world.phys.AABB"])
print("entity items patched")
