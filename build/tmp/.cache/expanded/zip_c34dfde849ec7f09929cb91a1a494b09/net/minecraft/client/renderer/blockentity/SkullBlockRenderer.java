package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullBlockRenderer implements BlockEntityRenderer<SkullBlockEntity, SkullBlockRenderState> {
    private static Map<SkullBlock.Type, Function<EntityModelSet, SkullModelBase>> customModels;
    private final Function<SkullBlock.Type, SkullModelBase> modelByType;
    public static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), p_340906_ -> {
        p_340906_.put(SkullBlock.Types.SKELETON, ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png"));
        p_340906_.put(SkullBlock.Types.WITHER_SKELETON, ResourceLocation.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png"));
        p_340906_.put(SkullBlock.Types.ZOMBIE, ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png"));
        p_340906_.put(SkullBlock.Types.CREEPER, ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png"));
        p_340906_.put(SkullBlock.Types.DRAGON, ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png"));
        p_340906_.put(SkullBlock.Types.PIGLIN, ResourceLocation.withDefaultNamespace("textures/entity/piglin/piglin.png"));
        p_340906_.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultTexture());
    });
    private final PlayerSkinRenderCache playerSkinRenderCache;

    @Nullable
    public static SkullModelBase createModel(EntityModelSet p_376421_, SkullBlock.Type p_377655_) {
        if (p_377655_ instanceof SkullBlock.Types skullblock$types) {
            return (SkullModelBase)(switch (skullblock$types) {
                case SKELETON -> new SkullModel(p_376421_.bakeLayer(ModelLayers.SKELETON_SKULL));
                case WITHER_SKELETON -> new SkullModel(p_376421_.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL));
                case PLAYER -> new SkullModel(p_376421_.bakeLayer(ModelLayers.PLAYER_HEAD));
                case ZOMBIE -> new SkullModel(p_376421_.bakeLayer(ModelLayers.ZOMBIE_HEAD));
                case CREEPER -> new SkullModel(p_376421_.bakeLayer(ModelLayers.CREEPER_HEAD));
                case DRAGON -> new DragonHeadModel(p_376421_.bakeLayer(ModelLayers.DRAGON_SKULL));
                case PIGLIN -> new PiglinHeadModel(p_376421_.bakeLayer(ModelLayers.PIGLIN_HEAD));
            });
        } else {
            if (customModels == null)
                customModels = net.minecraftforge.client.event.ForgeEventFactoryClient.onCreateSkullModels();
            return customModels.getOrDefault(p_377655_, k -> null).apply(p_376421_);
        }
    }

    public SkullBlockRenderer(BlockEntityRendererProvider.Context p_173660_) {
        EntityModelSet entitymodelset = p_173660_.entityModelSet();
        this.playerSkinRenderCache = p_173660_.playerSkinRenderCache();
        this.modelByType = Util.memoize(p_374644_ -> createModel(entitymodelset, p_374644_));
    }

    public SkullBlockRenderState createRenderState() {
        return new SkullBlockRenderState();
    }

    public void extractRenderState(
        SkullBlockEntity p_428948_, SkullBlockRenderState p_425051_, float p_428826_, Vec3 p_425640_, @Nullable ModelFeatureRenderer.CrumblingOverlay p_428325_
    ) {
        BlockEntityRenderer.super.extractRenderState(p_428948_, p_425051_, p_428826_, p_425640_, p_428325_);
        p_425051_.animationProgress = p_428948_.getAnimation(p_428826_);
        BlockState blockstate = p_428948_.getBlockState();
        boolean flag = blockstate.getBlock() instanceof WallSkullBlock;
        p_425051_.direction = flag ? blockstate.getValue(WallSkullBlock.FACING) : null;
        int i = flag ? RotationSegment.convertToSegment(p_425051_.direction.getOpposite()) : blockstate.getValue(SkullBlock.ROTATION);
        p_425051_.rotationDegrees = RotationSegment.convertToDegrees(i);
        p_425051_.skullType = ((AbstractSkullBlock)blockstate.getBlock()).getType();
        p_425051_.renderType = this.resolveSkullRenderType(p_425051_.skullType, p_428948_);
    }

    public void submit(SkullBlockRenderState p_427348_, PoseStack p_429151_, SubmitNodeCollector p_428428_, CameraRenderState p_428415_) {
        SkullModelBase skullmodelbase = this.modelByType.apply(p_427348_.skullType);
        submitSkull(
            p_427348_.direction,
            p_427348_.rotationDegrees,
            p_427348_.animationProgress,
            p_429151_,
            p_428428_,
            p_427348_.lightCoords,
            skullmodelbase,
            p_427348_.renderType,
            0,
            p_427348_.breakProgress
        );
    }

    public static void submitSkull(
        @Nullable Direction p_429199_,
        float p_431016_,
        float p_424944_,
        PoseStack p_424955_,
        SubmitNodeCollector p_427335_,
        int p_430325_,
        SkullModelBase p_428083_,
        RenderType p_430350_,
        int p_422297_,
        @Nullable ModelFeatureRenderer.CrumblingOverlay p_431224_
    ) {
        p_424955_.pushPose();
        if (p_429199_ == null) {
            p_424955_.translate(0.5F, 0.0F, 0.5F);
        } else {
            float f = 0.25F;
            p_424955_.translate(0.5F - p_429199_.getStepX() * 0.25F, 0.25F, 0.5F - p_429199_.getStepZ() * 0.25F);
        }

        p_424955_.scale(-1.0F, -1.0F, 1.0F);
        SkullModelBase.State skullmodelbase$state = new SkullModelBase.State();
        skullmodelbase$state.animationPos = p_424944_;
        skullmodelbase$state.yRot = p_431016_;
        p_427335_.submitModel(p_428083_, skullmodelbase$state, p_424955_, p_430350_, p_430325_, OverlayTexture.NO_OVERLAY, p_422297_, p_431224_);
        p_424955_.popPose();
    }

    private RenderType resolveSkullRenderType(SkullBlock.Type p_427761_, SkullBlockEntity p_425433_) {
        if (p_427761_ == SkullBlock.Types.PLAYER) {
            ResolvableProfile resolvableprofile = p_425433_.getOwnerProfile();
            if (resolvableprofile != null) {
                return this.playerSkinRenderCache.getOrDefault(resolvableprofile).renderType();
            }
        }

        return getSkullRenderType(p_427761_, null);
    }

    public static RenderType getSkullRenderType(SkullBlock.Type p_408012_, @Nullable ResourceLocation p_408211_) {
        return RenderType.entityCutoutNoCullZOffset(p_408211_ != null ? p_408211_ : SKIN_BY_TYPE.get(p_408012_));
    }

    public static RenderType getPlayerSkinRenderType(ResourceLocation p_408933_) {
        return RenderType.entityTranslucent(p_408933_);
    }
}
