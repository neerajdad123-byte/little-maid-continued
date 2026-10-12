"""Little Maid Continued porting helper: Entity API 1.21.1 -> 26.x (non-maid entities).

Usage: python tools/port/fix_entities.py <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import  # noqa: E402

PU = "com.github.tartaricacid.touhoulittlemaid.util.PortUtil"


def is_entity_class(t):
    return re.search(r"class \w+(?:<[^>]*>)? extends (?:Entity|AbstractEntityFromItem|Projectile|ThrowableItemProjectile|"
                     r"ThrowableProjectile|AbstractHurtingProjectile|FishingHook|\w*Fairy\w*|Monster|PathfinderMob|Mob|LivingEntity|"
                     r"TamableAnimal|Animal|AbstractArrow)\b", t)


def process(t):
    o = t
    # EntityType.Builder#build(String) -> build(ResourceKey)
    if "EntityType.Builder" in t:
        t = re.sub(r"\.build\(\"(\w+)\"\)",
                   r'.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "\1")))', t)
        if t != o:
            for fqn in ["net.minecraft.resources.ResourceKey", "net.minecraft.core.registries.Registries",
                        "net.minecraft.resources.Identifier", "com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid"]:
                t = ensure_import(t, fqn)
    if not is_entity_class(t):
        return t
    living = re.search(r"extends (?:AbstractEntityFromItem|\w*Fairy\w*|Monster|PathfinderMob|Mob|LivingEntity|TamableAnimal|Animal)\b", t)

    # hurt -> hurtServer
    m = re.search(r"public boolean hurt\((?:@\w+ )?DamageSource (\w+), float (\w+)\)", t)
    if m:
        src, amt = m.groups()
        t = t[:m.start()] + "public boolean hurtServer(ServerLevel serverLevel, DamageSource %s, float %s)" % (src, amt) + t[m.end():]
        t = re.sub(r"\bsuper\.hurt\((\w+), (\w+)\)", r"super.hurtServer(serverLevel, \1, \2)", t)
        if living:
            t = re.sub(r"\bthis\.isInvulnerableTo\((\w+)\)", r"this.isInvulnerableTo(serverLevel, \1)", t)
        else:
            t = re.sub(r"\bthis\.isInvulnerableTo\((\w+)\)", r"this.isInvulnerableToBase(\1)", t)
            t = re.sub(r"(?<![.\w])isInvulnerableTo\((\w+)\)", r"isInvulnerableToBase(\1)", t)
    elif re.search(r"class \w+ extends Entity\b", t) and "hurtServer(" not in t:
        # Entity#hurtServer became abstract; these entities never took damage before
        i = t.rindex("}")
        t = t[:i].rstrip() + ("\n\n    @Override\n    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {\n"
                              "        return false;\n    }\n}\n")
        t = ensure_import(t, "net.minecraft.world.damagesource.DamageSource")
    if "ServerLevel serverLevel" in t:
        t = ensure_import(t, "net.minecraft.server.level.ServerLevel")

    # interact(Player, InteractionHand) -> interact(Player, InteractionHand, Vec3)
    m = re.search(r"public InteractionResult interact\(Player (\w+), InteractionHand (\w+)\)", t)
    if m:
        t = t[:m.start()] + "public InteractionResult interact(Player %s, InteractionHand %s, Vec3 location)" % m.groups() + t[m.end():]
        t = re.sub(r"\bsuper\.interact\((\w+), (\w+)\)", r"super.interact(\1, \2, location)", t)
        t = ensure_import(t, "net.minecraft.world.phys.Vec3")

    # kill() -> kill(ServerLevel)
    if re.search(r"public void kill\(\)", t):
        t = re.sub(r"public void kill\(\)\s*\{", "public void kill(ServerLevel serverLevel) {", t)
        t = t.replace("super.kill();", "super.kill(serverLevel);")
        t = ensure_import(t, "net.minecraft.server.level.ServerLevel")
    t = re.sub(r"(?<![.\w])(?:this\.)?kill\(\);", "if (this.level() instanceof ServerLevel killLevel) {\n            this.kill(killLevel);\n        }", t)

    # spawnAtLocation needs the server level
    t = re.sub(r"(?<![.\w])(this\.)?spawnAtLocation\((?!\(ServerLevel\)|serverLevel|level\b)",
               lambda mm: "%sspawnAtLocation((ServerLevel) this.level(), " % (mm.group(1) or ""), t)
    t = re.sub(r"this::spawnAtLocation", "stack -> this.spawnAtLocation((ServerLevel) this.level(), stack)", t)
    if "(ServerLevel) this.level()" in t:
        t = ensure_import(t, "net.minecraft.server.level.ServerLevel")

    t = re.sub(r"public boolean canBeCollidedWith\(\)", "public boolean canBeCollidedWith(@Nullable Entity other)", t)
    t = re.sub(r"public boolean causeFallDamage\(float (\w+), float (\w+), DamageSource (\w+)\)",
               r"public boolean causeFallDamage(double \1, float \2, DamageSource \3)", t)
    t = re.sub(r"protected AABB makeBoundingBox\(\)\s*\{", "protected AABB makeBoundingBox(Vec3 position) {", t)
    t = re.sub(r"\bNbtUtils\.loadUUID\(", "PortUtil.loadUUID(", t)
    t = re.sub(r"(\w+)\.getDefaultLootTable\(\)", r"\1.getDefaultLootTable().orElseThrow()", t)
    t = re.sub(r"(?<![.\w])(this\.)?level\(\)\.getGameRules\(\)\.getBoolean\((GameRules\.\w+)\)", r"PortUtil.gameRule(this.level(), \2)", t)
    if "PortUtil." in t:
        t = ensure_import(t, PU)
    if "@Nullable Entity other" in t and "Nullable;" not in t:
        t = ensure_import(t, "org.jspecify.annotations.Nullable")
    return t


def main():
    n = 0
    for d, _, fs in os.walk(sys.argv[1]):
        for f in fs:
            if f.endswith(".java") and f != "EntityMaid.java":
                p = os.path.join(d, f)
                t = open(p, encoding="utf-8").read()
                nt = process(t)
                if nt != t:
                    open(p, "w", encoding="utf-8", newline="\n").write(nt)
                    n += 1
    print("entities pass changed", n)


if __name__ == "__main__":
    main()
