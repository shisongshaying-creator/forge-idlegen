package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityWithBoundingBoxRenderState;
import net.minecraft.client.renderer.blockentity.state.TestInstanceRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TestInstanceRenderer implements BlockEntityRenderer<TestInstanceBlockEntity, TestInstanceRenderState> {
    private static final float ERROR_PADDING = 0.02F;
    private final BeaconRenderer<TestInstanceBlockEntity> beacon = new BeaconRenderer<>();
    private final BlockEntityWithBoundingBoxRenderer<TestInstanceBlockEntity> box = new BlockEntityWithBoundingBoxRenderer<>();
    private final Font font;
    private final EntityRenderDispatcher entityRenderer;

    public TestInstanceRenderer(BlockEntityRendererProvider.Context p_392307_) {
        this.font = p_392307_.font();
        this.entityRenderer = p_392307_.entityRenderer();
    }

    public TestInstanceRenderState createRenderState() {
        return new TestInstanceRenderState();
    }

    public void extractRenderState(
        TestInstanceBlockEntity p_427666_,
        TestInstanceRenderState p_422610_,
        float p_423856_,
        Vec3 p_428165_,
        @Nullable ModelFeatureRenderer.CrumblingOverlay p_423288_
    ) {
        BlockEntityRenderer.super.extractRenderState(p_427666_, p_422610_, p_423856_, p_428165_, p_423288_);
        p_422610_.beaconRenderState = new BeaconRenderState();
        BlockEntityRenderState.extractBase(p_427666_, p_422610_.beaconRenderState, p_423288_);
        BeaconRenderer.extract(p_427666_, p_422610_.beaconRenderState, p_423856_, p_428165_);
        p_422610_.blockEntityWithBoundingBoxRenderState = new BlockEntityWithBoundingBoxRenderState();
        BlockEntityRenderState.extractBase(p_427666_, p_422610_.blockEntityWithBoundingBoxRenderState, p_423288_);
        BlockEntityWithBoundingBoxRenderer.extract(p_427666_, p_422610_.blockEntityWithBoundingBoxRenderState);
        p_422610_.errorMarkers.clear();

        for (TestInstanceBlockEntity.ErrorMarker testinstanceblockentity$errormarker : p_427666_.getErrorMarkers()) {
            p_422610_.errorMarkers
                .add(
                    new TestInstanceBlockEntity.ErrorMarker(
                        testinstanceblockentity$errormarker.pos().subtract(p_427666_.getBlockPos()), testinstanceblockentity$errormarker.text()
                    )
                );
        }
    }

    public void submit(TestInstanceRenderState p_424985_, PoseStack p_427676_, SubmitNodeCollector p_423525_, CameraRenderState p_430847_) {
        this.beacon.submit(p_424985_.beaconRenderState, p_427676_, p_423525_, p_430847_);
        this.box.submit(p_424985_.blockEntityWithBoundingBoxRenderState, p_427676_, p_423525_, p_430847_);

        for (TestInstanceBlockEntity.ErrorMarker testinstanceblockentity$errormarker : p_424985_.errorMarkers) {
            this.submitErrorMarker(p_427676_, p_423525_, testinstanceblockentity$errormarker, p_430847_);
        }
    }

    private void submitErrorMarker(PoseStack p_430301_, SubmitNodeCollector p_422807_, TestInstanceBlockEntity.ErrorMarker p_427897_, CameraRenderState p_424894_) {
        BlockPos blockpos = p_427897_.pos();
        p_422807_.order(1).submitCustomGeometry(p_430301_, RenderType.debugFilledBox(), (p_431231_, p_426013_) -> {
            float f1 = blockpos.getX() - 0.02F;
            float f2 = blockpos.getY() - 0.02F;
            float f3 = blockpos.getZ() - 0.02F;
            float f4 = blockpos.getX() + 1.0F + 0.02F;
            float f5 = blockpos.getY() + 1.0F + 0.02F;
            float f6 = blockpos.getZ() + 1.0F + 0.02F;
            PoseStack posestack = new PoseStack();
            posestack.last().set(p_431231_);
            ShapeRenderer.addChainedFilledBoxVertices(posestack, p_426013_, f1, f2, f3, f4, f5, f6, 1.0F, 0.0F, 0.0F, 0.375F);
        });
        FormattedCharSequence formattedcharsequence = p_427897_.text().getVisualOrderText();
        int i = this.font.width(formattedcharsequence);
        float f = 0.01F;
        p_430301_.pushPose();
        p_430301_.translate(blockpos.getX() + 0.5F, blockpos.getY() + 1.2F, blockpos.getZ() + 0.5F);
        p_430301_.mulPose(p_424894_.orientation);
        p_430301_.scale(0.01F, -0.01F, 0.01F);
        p_422807_.order(2).submitText(p_430301_, -i / 2.0F, 0.0F, formattedcharsequence, false, Font.DisplayMode.SEE_THROUGH, 15728880, -1, 0, 0);
        p_430301_.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return this.beacon.shouldRenderOffScreen() || this.box.shouldRenderOffScreen();
    }

    @Override
    public int getViewDistance() {
        return Math.max(this.beacon.getViewDistance(), this.box.getViewDistance());
    }

    public boolean shouldRender(TestInstanceBlockEntity p_393815_, Vec3 p_394435_) {
        return this.beacon.shouldRender(p_393815_, p_394435_) || this.box.shouldRender(p_393815_, p_394435_);
    }
}