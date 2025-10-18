package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityWithBoundingBoxRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BlockEntityWithBoundingBoxRenderer<T extends BlockEntity & BoundingBoxRenderable>
    implements BlockEntityRenderer<T, BlockEntityWithBoundingBoxRenderState> {
    public BlockEntityWithBoundingBoxRenderState createRenderState() {
        return new BlockEntityWithBoundingBoxRenderState();
    }

    public void extractRenderState(
        T p_425469_,
        BlockEntityWithBoundingBoxRenderState p_428653_,
        float p_431045_,
        Vec3 p_429829_,
        @Nullable ModelFeatureRenderer.CrumblingOverlay p_422357_
    ) {
        BlockEntityRenderer.super.extractRenderState(p_425469_, p_428653_, p_431045_, p_429829_, p_422357_);
        extract(p_425469_, p_428653_);
    }

    public static <T extends BlockEntity & BoundingBoxRenderable> void extract(T p_422522_, BlockEntityWithBoundingBoxRenderState p_428222_) {
        LocalPlayer localplayer = Minecraft.getInstance().player;
        p_428222_.isVisible = localplayer.canUseGameMasterBlocks() || localplayer.isSpectator();
        p_428222_.box = p_422522_.getRenderableBox();
        p_428222_.mode = p_422522_.renderMode();
        BlockPos blockpos = p_428222_.box.localPos();
        Vec3i vec3i = p_428222_.box.size();
        BlockPos blockpos1 = p_428222_.blockPos;
        BlockPos blockpos2 = blockpos1.offset(blockpos);
        if (p_428222_.isVisible && p_422522_.getLevel() != null && p_428222_.mode == BoundingBoxRenderable.Mode.BOX_AND_INVISIBLE_BLOCKS) {
            p_428222_.invisibleBlocks = new BlockEntityWithBoundingBoxRenderState.InvisibleBlockType[vec3i.getX() * vec3i.getY() * vec3i.getZ()];

            for (int i = 0; i < vec3i.getX(); i++) {
                for (int j = 0; j < vec3i.getY(); j++) {
                    for (int k = 0; k < vec3i.getZ(); k++) {
                        int l = k * vec3i.getX() * vec3i.getY() + j * vec3i.getX() + i;
                        BlockState blockstate = p_422522_.getLevel().getBlockState(blockpos2.offset(i, j, k));
                        if (blockstate.isAir()) {
                            p_428222_.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.AIR;
                        } else if (blockstate.is(Blocks.STRUCTURE_VOID)) {
                            p_428222_.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.STRUCUTRE_VOID;
                        } else if (blockstate.is(Blocks.BARRIER)) {
                            p_428222_.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.BARRIER;
                        } else if (blockstate.is(Blocks.LIGHT)) {
                            p_428222_.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.LIGHT;
                        }
                    }
                }
            }
        } else {
            p_428222_.invisibleBlocks = null;
        }

        if (p_428222_.isVisible) {
        }

        p_428222_.structureVoids = null;
    }

    public void submit(BlockEntityWithBoundingBoxRenderState p_426313_, PoseStack p_426054_, SubmitNodeCollector p_428487_, CameraRenderState p_431343_) {
        if (p_426313_.isVisible) {
            BoundingBoxRenderable.Mode boundingboxrenderable$mode = p_426313_.mode;
            if (boundingboxrenderable$mode != BoundingBoxRenderable.Mode.NONE) {
                BoundingBoxRenderable.RenderableBox boundingboxrenderable$renderablebox = p_426313_.box;
                BlockPos blockpos = boundingboxrenderable$renderablebox.localPos();
                Vec3i vec3i = boundingboxrenderable$renderablebox.size();
                if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1) {
                    float f = 1.0F;
                    float f1 = 0.9F;
                    float f2 = 0.5F;
                    BlockPos blockpos1 = blockpos.offset(vec3i);
                    p_428487_.submitCustomGeometry(
                        p_426054_,
                        RenderType.lines(),
                        (p_420909_, p_420910_) -> ShapeRenderer.renderLineBox(
                            p_420909_,
                            p_420910_,
                            blockpos.getX(),
                            blockpos.getY(),
                            blockpos.getZ(),
                            blockpos1.getX(),
                            blockpos1.getY(),
                            blockpos1.getZ(),
                            0.9F,
                            0.9F,
                            0.9F,
                            1.0F,
                            0.5F,
                            0.5F,
                            0.5F
                        )
                    );
                    this.submitInvisibleBlocks(p_426313_, blockpos, vec3i, p_428487_, p_426054_);
                }
            }
        }
    }

    private void submitInvisibleBlocks(
        BlockEntityWithBoundingBoxRenderState p_422570_, BlockPos p_424027_, Vec3i p_427586_, SubmitNodeCollector p_425575_, PoseStack p_423098_
    ) {
        if (p_422570_.invisibleBlocks != null) {
            BlockPos blockpos = p_422570_.blockPos;
            BlockPos blockpos1 = blockpos.offset(p_424027_);
            p_425575_.submitCustomGeometry(
                p_423098_,
                RenderType.lines(),
                (p_420915_, p_420916_) -> {
                    for (int i = 0; i < p_427586_.getX(); i++) {
                        for (int j = 0; j < p_427586_.getY(); j++) {
                            for (int k = 0; k < p_427586_.getZ(); k++) {
                                int l = k * p_427586_.getX() * p_427586_.getY() + j * p_427586_.getX() + i;
                                BlockEntityWithBoundingBoxRenderState.InvisibleBlockType blockentitywithboundingboxrenderstate$invisibleblocktype = p_422570_.invisibleBlocks[l];
                                if (blockentitywithboundingboxrenderstate$invisibleblocktype != null) {
                                    float f = blockentitywithboundingboxrenderstate$invisibleblocktype
                                            == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.AIR
                                        ? 0.05F
                                        : 0.0F;
                                    double d0 = blockpos1.getX() + i - blockpos.getX() + 0.45F - f;
                                    double d1 = blockpos1.getY() + j - blockpos.getY() + 0.45F - f;
                                    double d2 = blockpos1.getZ() + k - blockpos.getZ() + 0.45F - f;
                                    double d3 = blockpos1.getX() + i - blockpos.getX() + 0.55F + f;
                                    double d4 = blockpos1.getY() + j - blockpos.getY() + 0.55F + f;
                                    double d5 = blockpos1.getZ() + k - blockpos.getZ() + 0.55F + f;
                                    if (blockentitywithboundingboxrenderstate$invisibleblocktype
                                        == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.AIR) {
                                        ShapeRenderer.renderLineBox(p_420915_, p_420916_, d0, d1, d2, d3, d4, d5, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
                                    } else if (blockentitywithboundingboxrenderstate$invisibleblocktype
                                        == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.STRUCUTRE_VOID) {
                                        ShapeRenderer.renderLineBox(p_420915_, p_420916_, d0, d1, d2, d3, d4, d5, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
                                    } else if (blockentitywithboundingboxrenderstate$invisibleblocktype
                                        == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.BARRIER) {
                                        ShapeRenderer.renderLineBox(p_420915_, p_420916_, d0, d1, d2, d3, d4, d5, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                                    } else if (blockentitywithboundingboxrenderstate$invisibleblocktype
                                        == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.LIGHT) {
                                        ShapeRenderer.renderLineBox(p_420915_, p_420916_, d0, d1, d2, d3, d4, d5, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
                                    }
                                }
                            }
                        }
                    }
                }
            );
        }
    }

    private void renderStructureVoids(BlockEntityWithBoundingBoxRenderState p_425318_, BlockPos p_397752_, Vec3i p_395592_, VertexConsumer p_395461_, Matrix4f p_429649_) {
        if (p_425318_.structureVoids != null) {
            BlockPos blockpos = p_425318_.blockPos;
            DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(p_395592_.getX(), p_395592_.getY(), p_395592_.getZ());

            for (int i = 0; i < p_395592_.getX(); i++) {
                for (int j = 0; j < p_395592_.getY(); j++) {
                    for (int k = 0; k < p_395592_.getZ(); k++) {
                        int l = k * p_395592_.getX() * p_395592_.getY() + j * p_395592_.getX() + i;
                        if (p_425318_.structureVoids[l]) {
                            discretevoxelshape.fill(i, j, k);
                        }
                    }
                }
            }

            discretevoxelshape.forAllFaces((p_392316_, p_394067_, p_391919_, p_397211_) -> {
                float f = 0.48F;
                float f1 = p_394067_ + p_397752_.getX() - blockpos.getX() + 0.5F - 0.48F;
                float f2 = p_391919_ + p_397752_.getY() - blockpos.getY() + 0.5F - 0.48F;
                float f3 = p_397211_ + p_397752_.getZ() - blockpos.getZ() + 0.5F - 0.48F;
                float f4 = p_394067_ + p_397752_.getX() - blockpos.getX() + 0.5F + 0.48F;
                float f5 = p_391919_ + p_397752_.getY() - blockpos.getY() + 0.5F + 0.48F;
                float f6 = p_397211_ + p_397752_.getZ() - blockpos.getZ() + 0.5F + 0.48F;
                ShapeRenderer.renderFace(p_429649_, p_395461_, p_392316_, f1, f2, f3, f4, f5, f6, 0.75F, 0.75F, 1.0F, 0.2F);
            });
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}