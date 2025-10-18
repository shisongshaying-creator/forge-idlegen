package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerEarsModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Deadmau5EarsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final HumanoidModel<AvatarRenderState> model;

    public Deadmau5EarsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> p_116860_, EntityModelSet p_367465_) {
        super(p_116860_);
        this.model = new PlayerEarsModel(p_367465_.bakeLayer(ModelLayers.PLAYER_EARS));
    }

    public void submit(PoseStack p_424764_, SubmitNodeCollector p_429783_, int p_431323_, AvatarRenderState p_431148_, float p_429090_, float p_428625_) {
        if (p_431148_.showExtraEars && !p_431148_.isInvisible) {
            int i = LivingEntityRenderer.getOverlayCoords(p_431148_, 0.0F);
            p_429783_.submitModel(
                this.model,
                p_431148_,
                p_424764_,
                RenderType.entitySolid(p_431148_.skin.body().texturePath()),
                p_431323_,
                i,
                p_431148_.outlineColor,
                null
            );
        }
    }
}