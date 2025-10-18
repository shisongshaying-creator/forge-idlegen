package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SkullSpecialRenderer implements NoDataSpecialModelRenderer {
    private final SkullModelBase model;
    private final float animation;
    private final RenderType renderType;

    public SkullSpecialRenderer(SkullModelBase p_375443_, float p_377202_, RenderType p_408044_) {
        this.model = p_375443_;
        this.animation = p_377202_;
        this.renderType = p_408044_;
    }

    @Override
    public void submit(
        ItemDisplayContext p_426011_, PoseStack p_426457_, SubmitNodeCollector p_425711_, int p_424696_, int p_426768_, boolean p_431503_, int p_431880_
    ) {
        SkullBlockRenderer.submitSkull(null, 180.0F, this.animation, p_426457_, p_425711_, p_424696_, this.model, this.renderType, p_431880_, null);
    }

    @Override
    public void getExtents(Set<Vector3f> p_407952_) {
        PoseStack posestack = new PoseStack();
        posestack.translate(0.5F, 0.0F, 0.5F);
        posestack.scale(-1.0F, -1.0F, 1.0F);
        SkullModelBase.State skullmodelbase$state = new SkullModelBase.State();
        skullmodelbase$state.animationPos = this.animation;
        skullmodelbase$state.yRot = 180.0F;
        this.model.setupAnim(skullmodelbase$state);
        this.model.root().getExtentsForGui(posestack, p_407952_);
    }

    @OnlyIn(Dist.CLIENT)
    public record Unbaked(SkullBlock.Type kind, Optional<ResourceLocation> textureOverride, float animation) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<SkullSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_375918_ -> p_375918_.group(
                    SkullBlock.Type.CODEC.fieldOf("kind").forGetter(SkullSpecialRenderer.Unbaked::kind),
                    ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(SkullSpecialRenderer.Unbaked::textureOverride),
                    Codec.FLOAT.optionalFieldOf("animation", 0.0F).forGetter(SkullSpecialRenderer.Unbaked::animation)
                )
                .apply(p_375918_, SkullSpecialRenderer.Unbaked::new)
        );

        public Unbaked(SkullBlock.Type p_376549_) {
            this(p_376549_, Optional.empty(), 0.0F);
        }

        @Override
        public MapCodec<SkullSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Nullable
        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext p_423590_) {
            SkullModelBase skullmodelbase = SkullBlockRenderer.createModel(p_423590_.entityModelSet(), this.kind);
            ResourceLocation resourcelocation = this.textureOverride
                .<ResourceLocation>map(p_377495_ -> p_377495_.withPath(p_377715_ -> "textures/entity/" + p_377715_ + ".png"))
                .orElse(null);
            if (skullmodelbase == null) {
                return null;
            } else {
                RenderType rendertype = SkullBlockRenderer.getSkullRenderType(this.kind, resourcelocation);
                return new SkullSpecialRenderer(skullmodelbase, this.animation, rendertype);
            }
        }
    }
}