package com.github.tartaricacid.touhoulittlemaid.entity.ai.brain;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;

/**
 * Little Maid Continued: the maid work / rest timetable.
 * <p>
 * MC 1.21.1 kept these as registered vanilla {@code Schedule}s on the brain. MC 26.x replaced schedules with
 * environment attributes, so the timetable lives here and the brain is switched by
 * {@link com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidUpdateActivityFromSchedule}.
 */
public final class MaidActivitySchedule {
    private MaidActivitySchedule() {
    }

    /**
     * @param dayTime time of day in ticks, 0 ~ 23999 (0 = 06:00)
     */
    public static Activity getActivityAt(MaidSchedule schedule, int dayTime) {
        return switch (schedule) {
            // 全天工作
            case ALL -> Activity.WORK;
            // 18:00 ~ 06:00 工作, 06:00 ~ 14:00 睡觉, 14:00 ~ 18:00 娱乐
            case NIGHT -> dayTime < 8000 ? Activity.REST : dayTime < 12000 ? Activity.IDLE : Activity.WORK;
            // 06:00 ~ 18:00 工作, 18:00 ~ 22:00 娱乐, 22:00 ~ 06:00 睡觉
            default -> dayTime < 12000 ? Activity.WORK : dayTime < 16000 ? Activity.IDLE : Activity.REST;
        };
    }

    /**
     * Same as MC 1.21.1 {@code Brain#updateActivityFromSchedule(dayTime, gameTime)} with the maid's own timetable.
     */
    public static void updateActivity(Brain<EntityMaid> brain, EntityMaid maid, long dayTime, long gameTime) {
        if (gameTime - brain.lastScheduleUpdate > 20L) {
            brain.lastScheduleUpdate = gameTime;
            Activity activity = getActivityAt(maid.getSchedule(), (int) (dayTime % 24000L));
            if (!brain.getActiveActivities().contains(activity)) {
                brain.setActiveActivityIfPossible(activity);
            }
        }
    }
}
