package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Set;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ChestSpecialRenderer implements NoDataSpecialModelRenderer {
    public static final ResourceLocation GIFT_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("christmas");
    public static final ResourceLocation NORMAL_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("normal");
    public static final ResourceLocation TRAPPED_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("trapped");
    public static final ResourceLocation ENDER_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("ender");
    public static final ResourceLocation COPPER_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("copper");
    public static final ResourceLocation EXPOSED_COPPER_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("copper_exposed");
    public static final ResourceLocation WEATHERED_COPPER_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("copper_weathered");
    public static final ResourceLocation OXIDIZED_COPPER_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("copper_oxidized");
    private final MaterialSet materials;
    private final ChestModel model;
    private final Material material;
    private final float openness;

    public ChestSpecialRenderer(MaterialSet p_423388_, ChestModel p_375837_, Material p_377410_, float p_378366_) {
        this.materials = p_423388_;
        this.model = p_375837_;
        this.material = p_377410_;
        this.openness = p_378366_;
    }

    @Override
    public void submit(
        ItemDisplayContext p_430007_, PoseStack p_423292_, SubmitNodeCollector p_422433_, int p_428808_, int p_429897_, boolean p_422441_, int p_431900_
    ) {
        p_422433_.submitModel(
            this.model,
            this.openness,
            p_423292_,
            this.material.renderType(RenderType::entitySolid),
            p_428808_,
            p_429897_,
            -1,
            this.materials.get(this.material),
            p_431900_,
            null
        );
    }

    @Override
    public void getExtents(Set<Vector3f> p_409153_) {
        PoseStack posestack = new PoseStack();
        this.model.setupAnim(this.openness);
        this.model.root().getExtentsForGui(posestack, p_409153_);
    }

    @OnlyIn(Dist.CLIENT)
    public record Unbaked(ResourceLocation texture, float openness) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ChestSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_376535_ -> p_376535_.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(ChestSpecialRenderer.Unbaked::texture),
                    Codec.FLOAT.optionalFieldOf("openness", 0.0F).forGetter(ChestSpecialRenderer.Unbaked::openness)
                )
                .apply(p_376535_, ChestSpecialRenderer.Unbaked::new)
        );

        public Unbaked(ResourceLocation p_376432_) {
            this(p_376432_, 0.0F);
        }

        @Override
        public MapCodec<ChestSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext p_423340_) {
            ChestModel chestmodel = new ChestModel(p_423340_.entityModelSet().bakeLayer(ModelLayers.CHEST));
            Material material = Sheets.CHEST_MAPPER.apply(this.texture);
            return new ChestSpecialRenderer(p_423340_.materials(), chestmodel, material, this.openness);
        }
    }
}