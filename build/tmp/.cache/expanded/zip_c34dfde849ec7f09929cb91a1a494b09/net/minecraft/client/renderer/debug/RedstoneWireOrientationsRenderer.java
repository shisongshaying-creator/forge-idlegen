package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class RedstoneWireOrientationsRenderer implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void render(
        PoseStack p_366468_, MultiBufferSource p_362070_, double p_365839_, double p_366895_, double p_362271_, DebugValueAccess p_425493_, Frustum p_426822_
    ) {
        VertexConsumer vertexconsumer = p_362070_.getBuffer(RenderType.lines());
        p_425493_.forEachBlock(DebugSubscriptions.REDSTONE_WIRE_ORIENTATIONS, (p_426849_, p_428489_) -> {
            Vector3f vector3f = p_426849_.getBottomCenter().subtract(p_365839_, p_366895_ - 0.1, p_362271_).toVector3f();
            ShapeRenderer.renderVector(p_366468_, vertexconsumer, vector3f, p_428489_.getFront().getUnitVec3().scale(0.5), -16776961);
            ShapeRenderer.renderVector(p_366468_, vertexconsumer, vector3f, p_428489_.getUp().getUnitVec3().scale(0.4), -65536);
            ShapeRenderer.renderVector(p_366468_, vertexconsumer, vector3f, p_428489_.getSide().getUnitVec3().scale(0.3), -256);
        });
    }
}