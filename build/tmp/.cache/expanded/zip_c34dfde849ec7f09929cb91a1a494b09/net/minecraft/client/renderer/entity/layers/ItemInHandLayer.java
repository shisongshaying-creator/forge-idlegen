package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemInHandLayer<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {
    public ItemInHandLayer(RenderLayerParent<S, M> p_234846_) {
        super(p_234846_);
    }

    public void submit(PoseStack p_426728_, SubmitNodeCollector p_428286_, int p_429291_, S p_425988_, float p_428592_, float p_430361_) {
        this.submitArmWithItem(p_425988_, p_425988_.rightHandItem, HumanoidArm.RIGHT, p_426728_, p_428286_, p_429291_);
        this.submitArmWithItem(p_425988_, p_425988_.leftHandItem, HumanoidArm.LEFT, p_426728_, p_428286_, p_429291_);
    }

    protected void submitArmWithItem(
        S p_430201_, ItemStackRenderState p_422416_, HumanoidArm p_425716_, PoseStack p_428646_, SubmitNodeCollector p_423707_, int p_424346_
    ) {
        if (!p_422416_.isEmpty()) {
            p_428646_.pushPose();
            this.getParentModel().translateToHand(p_430201_, p_425716_, p_428646_);
            p_428646_.mulPose(Axis.XP.rotationDegrees(-90.0F));
            p_428646_.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean flag = p_425716_ == HumanoidArm.LEFT;
            p_428646_.translate((flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
            p_422416_.submit(p_428646_, p_423707_, p_424346_, OverlayTexture.NO_OVERLAY, p_430201_.outlineColor);
            p_428646_.popPose();
        }
    }
}