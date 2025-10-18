package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.debug.DebugGameEventInfo;
import net.minecraft.util.debug.DebugGameEventListenerInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameEventListenerRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final float BOX_HEIGHT = 1.0F;

    private void forEachListener(DebugValueAccess p_424618_, GameEventListenerRenderer.ListenerVisitor p_426833_) {
        p_424618_.forEachBlock(DebugSubscriptions.GAME_EVENT_LISTENERS, (p_420988_, p_420989_) -> p_426833_.accept(p_420988_.getCenter(), p_420989_.listenerRadius()));
        p_424618_.forEachEntity(DebugSubscriptions.GAME_EVENT_LISTENERS, (p_420998_, p_420999_) -> p_426833_.accept(p_420998_.position(), p_420999_.listenerRadius()));
    }

    @Override
    public void render(
        PoseStack p_173846_, MultiBufferSource p_173847_, double p_173848_, double p_173849_, double p_173850_, DebugValueAccess p_430757_, Frustum p_427556_
    ) {
        VertexConsumer vertexconsumer = p_173847_.getBuffer(RenderType.lines());
        this.forEachListener(
            p_430757_,
            (p_420995_, p_420996_) -> {
                double d0 = p_420996_ * 2.0;
                DebugRenderer.renderVoxelShape(
                    p_173846_,
                    vertexconsumer,
                    Shapes.create(AABB.ofSize(p_420995_, d0, d0, d0)),
                    -p_173848_,
                    -p_173849_,
                    -p_173850_,
                    1.0F,
                    1.0F,
                    0.0F,
                    0.35F,
                    true
                );
            }
        );
        VertexConsumer vertexconsumer1 = p_173847_.getBuffer(RenderType.debugFilledBox());
        this.forEachListener(
            p_430757_,
            (p_269724_, p_426188_) -> ShapeRenderer.addChainedFilledBoxVertices(
                p_173846_,
                vertexconsumer1,
                p_269724_.x() - 0.25 - p_173848_,
                p_269724_.y() - p_173849_,
                p_269724_.z() - 0.25 - p_173850_,
                p_269724_.x() + 0.25 - p_173848_,
                p_269724_.y() - p_173849_ + 1.0,
                p_269724_.z() + 0.25 - p_173850_,
                1.0F,
                1.0F,
                0.0F,
                0.35F
            )
        );
        this.forEachListener(
            p_430757_,
            (p_274713_, p_428150_) -> {
                DebugRenderer.renderFloatingText(
                    p_173846_, p_173847_, "Listener Origin", p_274713_.x(), p_274713_.y() + 1.8F, p_274713_.z(), -1, 0.025F
                );
                DebugRenderer.renderFloatingText(
                    p_173846_,
                    p_173847_,
                    BlockPos.containing(p_274713_).toString(),
                    p_274713_.x(),
                    p_274713_.y() + 1.5,
                    p_274713_.z(),
                    -6959665,
                    0.025F
                );
            }
        );
        p_430757_.forEachEvent(
            DebugSubscriptions.GAME_EVENTS,
            (p_420984_, p_420985_, p_420986_) -> {
                Vec3 vec3 = p_420984_.pos();
                double d0 = 0.4;
                AABB aabb = AABB.ofSize(vec3.add(0.0, 0.5, 0.0), 0.4, 0.9, 0.4);
                renderFilledBox(p_173846_, p_173847_, aabb, 1.0F, 1.0F, 1.0F, 0.2F);
                DebugRenderer.renderFloatingText(
                    p_173846_, p_173847_, p_420984_.event().getRegisteredName(), vec3.x, vec3.y + 0.85F, vec3.z, -7564911, 0.0075F
                );
            }
        );
    }

    private static void renderFilledBox(
        PoseStack p_270351_, MultiBufferSource p_270763_, AABB p_270205_, float p_270707_, float p_270538_, float p_270314_, float p_270966_
    ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            Vec3 vec3 = camera.getPosition().reverse();
            DebugRenderer.renderFilledBox(p_270351_, p_270763_, p_270205_.move(vec3), p_270707_, p_270538_, p_270314_, p_270966_);
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface ListenerVisitor {
        void accept(Vec3 p_426419_, int p_427946_);
    }
}