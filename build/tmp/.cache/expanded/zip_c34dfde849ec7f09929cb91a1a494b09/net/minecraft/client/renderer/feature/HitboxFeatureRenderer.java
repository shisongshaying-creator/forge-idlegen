package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.entity.state.ServerHitboxesRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HitboxFeatureRenderer {
    public void render(SubmitNodeCollection p_423444_, MultiBufferSource.BufferSource p_427016_) {
        for (SubmitNodeStorage.HitboxSubmit submitnodestorage$hitboxsubmit : p_423444_.getHitboxSubmits()) {
            VertexConsumer vertexconsumer = p_427016_.getBuffer(RenderType.lines());
            PoseStack posestack = new PoseStack();
            posestack.mulPose(submitnodestorage$hitboxsubmit.pose());
            renderHitboxesAndViewVector(posestack, submitnodestorage$hitboxsubmit.hitboxesRenderState(), vertexconsumer, submitnodestorage$hitboxsubmit.entityRenderState().eyeHeight);
            ServerHitboxesRenderState serverhitboxesrenderstate = submitnodestorage$hitboxsubmit.entityRenderState().serverHitboxesRenderState;
            if (serverhitboxesrenderstate != null) {
                if (serverhitboxesrenderstate.missing()) {
                    HitboxRenderState hitboxrenderstate = submitnodestorage$hitboxsubmit.hitboxesRenderState().hitboxes().getFirst();
                    DebugRenderer.renderFloatingText(
                        posestack,
                        p_427016_,
                        "Missing",
                        submitnodestorage$hitboxsubmit.entityRenderState().x,
                        hitboxrenderstate.y1() + 1.5,
                        submitnodestorage$hitboxsubmit.entityRenderState().z,
                        -65536
                    );
                } else if (serverhitboxesrenderstate.hitboxes() != null) {
                    posestack.translate(
                        serverhitboxesrenderstate.serverEntityX() - submitnodestorage$hitboxsubmit.entityRenderState().x,
                        serverhitboxesrenderstate.serverEntityY() - submitnodestorage$hitboxsubmit.entityRenderState().y,
                        serverhitboxesrenderstate.serverEntityZ() - submitnodestorage$hitboxsubmit.entityRenderState().z
                    );
                    renderHitboxesAndViewVector(posestack, serverhitboxesrenderstate.hitboxes(), vertexconsumer, serverhitboxesrenderstate.eyeHeight());
                    Vec3 vec3 = new Vec3(serverhitboxesrenderstate.deltaMovementX(), serverhitboxesrenderstate.deltaMovementY(), serverhitboxesrenderstate.deltaMovementZ());
                    ShapeRenderer.renderVector(posestack, vertexconsumer, new Vector3f(), vec3, -256);
                }
            }
        }
    }

    private static void renderHitboxesAndViewVector(PoseStack p_425852_, HitboxesRenderState p_422966_, VertexConsumer p_430336_, float p_424610_) {
        for (HitboxRenderState hitboxrenderstate : p_422966_.hitboxes()) {
            renderHitbox(p_425852_, p_430336_, hitboxrenderstate);
        }

        Vec3 vec3 = new Vec3(p_422966_.viewX(), p_422966_.viewY(), p_422966_.viewZ());
        ShapeRenderer.renderVector(p_425852_, p_430336_, new Vector3f(0.0F, p_424610_, 0.0F), vec3.scale(2.0), -16776961);
    }

    private static void renderHitbox(PoseStack p_426714_, VertexConsumer p_427400_, HitboxRenderState p_431201_) {
        p_426714_.pushPose();
        p_426714_.translate(p_431201_.offsetX(), p_431201_.offsetY(), p_431201_.offsetZ());
        ShapeRenderer.renderLineBox(
            p_426714_.last(),
            p_427400_,
            p_431201_.x0(),
            p_431201_.y0(),
            p_431201_.z0(),
            p_431201_.x1(),
            p_431201_.y1(),
            p_431201_.z1(),
            p_431201_.red(),
            p_431201_.green(),
            p_431201_.blue(),
            1.0F
        );
        p_426714_.popPose();
    }
}