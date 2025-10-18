package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(p_259429_ -> p_259429_.point((p_421726_, p_421727_, p_421728_) -> {
            p_421727_.getBrain().updateActivityFromSchedule(p_421726_.getDayTime(), p_421726_.getGameTime());
            return true;
        }));
    }
}