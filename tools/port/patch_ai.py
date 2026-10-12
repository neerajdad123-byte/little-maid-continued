"""One-off port patch for maid AI classes (MC 1.21.1 -> 26.2)."""
import os
import re
import sys

B = "src/main/java/com/github/tartaricacid/touhoulittlemaid/"


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


# Brain#addActivity(activity, list) became the 4-argument form (no conditions, nothing erased on stop)
edit("entity/ai/brain/MaidBrain.java",
     [(r"brain\.addActivity\(([^,]+), (ImmutableList\.copyOf\(\w+\))\);", r"brain.addActivity(\1, \2, ImmutableSet.of(), ImmutableSet.of());")],
     ["com.google.common.collect.ImmutableSet"], regex=True)

edit("entity/ai/brain/sensor/MaidNearestLivingEntitySensor.java",
     [("new NearestVisibleLivingEntities(maid, list)", "new NearestVisibleLivingEntities(world, maid, list)")])

edit("entity/ai/brain/task/MaidBreathAirTask.java", [('''        FoodProperties foodProperties = stack.get(DataComponents.FOOD);
        if (foodProperties == null) {
            return false;
        }
        List<FoodProperties.PossibleEffect> effects = foodProperties.effects();
        if (effects.isEmpty()) {
            return false;
        }
        for (FoodProperties.PossibleEffect effect : effects) {
            if (effect.effect().getEffect() == MobEffects.WATER_BREATHING) {
                return true;
            }
        }
        return false;''', '''        if (stack.get(DataComponents.FOOD) == null) {
            return false;
        }
        for (MobEffectInstance effect : PortUtil.foodEffects(stack)) {
            if (effect.getEffect() == MobEffects.WATER_BREATHING) {
                return true;
            }
        }
        return false;''')], ["com.github.tartaricacid.touhoulittlemaid.util.PortUtil", "net.minecraft.world.effect.MobEffectInstance"])

edit("entity/ai/brain/task/MaidClimbTask.java", [("maid.moveTo(", "maid.snapTo(")])
edit("entity/ai/brain/task/MaidFindSitTask.java", [("maid.startRiding(this.sitEntity, true);", "maid.startRiding(this.sitEntity, true, true);")])
edit("entity/ai/brain/task/MaidMeleeAttack.java", [
    ("maid.doHurtTarget(target);", "maid.doHurtTarget(level, target);"),
    ("maid.canFireProjectileWeapon(projectile)", "maid.canUseNonMeleeWeapon(itemStack)"),
])
edit("entity/ai/brain/task/SetWalkTargetAwayFrom.java", [(r"\.getRestrictRadius\(\)", ".getHomeRadius()")], regex=True)
edit("entity/ai/brain/task/MaidShearTask.java", [("shearableEntity.spawnAtLocation(stack, 1.0F)", "shearableEntity.spawnAtLocation((ServerLevel) shearableEntity.level(), stack, 1.0F)")],
     ["net.minecraft.server.level.ServerLevel"])
edit("entity/ai/brain/task/MaidSnowballTargetTask.java", [
    ("new Snowball(shooter.level(), shooter.getX(), shooter.getY(), shooter.getZ());",
     "new Snowball(shooter.level(), shooter.getX(), shooter.getY(), shooter.getZ(), new ItemStack(Items.SNOWBALL));")],
     ["net.minecraft.world.item.ItemStack", "net.minecraft.world.item.Items"])
edit("entity/ai/control/MaidMoveControl.java", [("maid.getSwimManager().getSwimTarget().getCenter()", "Vec3.atCenterOf(maid.getSwimManager().getSwimTarget())")],
     ["net.minecraft.world.phys.Vec3"])
edit("entity/ai/goal/FairyNearestAttackableTargetGoal.java", [("this.target = level.getNearestEntity(", "this.target = getServerLevel(this.mob).getNearestEntity(")])
edit("entity/ai/goal/MaidTemptGoal.java", [
    ("TEMP_TARGETING.copy().selector(this::shouldFollow)", "TEMP_TARGETING.copy().selector((entity, level) -> this.shouldFollow(entity))"),
    ("this.maid = this.mob.level().getNearestEntity(", "this.maid = getServerLevel(this.mob).getNearestEntity("),
])
edit("entity/ai/navigation/MaidNodeEvaluator.java", [
    ("                case DAMAGE_FIRE -> PathType.FIRE;", "                case FIRE -> PathType.FIRE;"),
    ("                case DAMAGE_OTHER -> PathType.DAMAGING;", "                case DAMAGING -> PathType.DAMAGING;"),
])
# debug-only path overlay hooked a private PathFinder method that changed; drop that override
p = B + "entity/ai/path/MaidWrappedPathFinder.java"
t = open(p, encoding="utf-8").read()
s = t.index("    @Nullable\n    @Override\n    public Path findPath(ProfilerFiller profiler")
e = t.rindex("}")
t = t[:s].rstrip() + "\n}\n"
open(p, "w", encoding="utf-8", newline="\n").write(t)
print("ai patched")
