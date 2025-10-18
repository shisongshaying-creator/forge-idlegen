package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.debug.DebugGoalInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST = 160;
    private final Minecraft minecraft;

    public GoalSelectorDebugRenderer(Minecraft p_113546_) {
        this.minecraft = p_113546_;
    }

    @Override
    public void render(
        PoseStack p_113552_, MultiBufferSource p_113553_, double p_113554_, double p_113555_, double p_113556_, DebugValueAccess p_422887_, Frustum p_423081_
    ) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0, camera.getPosition().z);
        p_422887_.forEachEntity(DebugSubscriptions.GOAL_SELECTORS, (p_423496_, p_429662_) -> {
            if (blockpos.closerThan(p_423496_.blockPosition(), 160.0)) {
                for (int i = 0; i < p_429662_.goals().size(); i++) {
                    DebugGoalInfo.DebugGoal debuggoalinfo$debuggoal = p_429662_.goals().get(i);
                    double d0 = p_423496_.getBlockX() + 0.5;
                    double d1 = p_423496_.getY() + 2.0 + i * 0.25;
                    double d2 = p_423496_.getBlockZ() + 0.5;
                    int j = debuggoalinfo$debuggoal.isRunning() ? -16711936 : -3355444;
                    DebugRenderer.renderFloatingText(p_113552_, p_113553_, debuggoalinfo$debuggoal.name(), d0, d1, d2, j);
                }
            }
        });
    }
}