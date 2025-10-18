package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.debug.DebugStructureInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void render(
        PoseStack p_113688_, MultiBufferSource p_113689_, double p_113690_, double p_113691_, double p_113692_, DebugValueAccess p_429502_, Frustum p_430376_
    ) {
        VertexConsumer vertexconsumer = p_113689_.getBuffer(RenderType.lines());
        p_429502_.forEachChunk(DebugSubscriptions.STRUCTURES, (p_421009_, p_421010_) -> {
            for (DebugStructureInfo debugstructureinfo : p_421010_) {
                renderBox(p_113688_, p_113690_, p_113691_, p_113692_, vertexconsumer, debugstructureinfo.boundingBox(), 1.0F, 1.0F, 1.0F, 1.0F);

                for (DebugStructureInfo.Piece debugstructureinfo$piece : debugstructureinfo.pieces()) {
                    if (debugstructureinfo$piece.isStart()) {
                        renderBox(p_113688_, p_113690_, p_113691_, p_113692_, vertexconsumer, debugstructureinfo$piece.boundingBox(), 0.0F, 1.0F, 0.0F, 1.0F);
                    } else {
                        renderBox(p_113688_, p_113690_, p_113691_, p_113692_, vertexconsumer, debugstructureinfo$piece.boundingBox(), 0.0F, 0.0F, 1.0F, 1.0F);
                    }
                }
            }
        });
    }

    private static void renderBox(
        PoseStack p_430873_,
        double p_425390_,
        double p_422287_,
        double p_429568_,
        VertexConsumer p_423037_,
        BoundingBox p_428530_,
        float p_424472_,
        float p_425367_,
        float p_424920_,
        float p_430374_
    ) {
        ShapeRenderer.renderLineBox(
            p_430873_.last(),
            p_423037_,
            p_428530_.minX() - p_425390_,
            p_428530_.minY() - p_422287_,
            p_428530_.minZ() - p_429568_,
            p_428530_.maxX() + 1 - p_425390_,
            p_428530_.maxY() + 1 - p_422287_,
            p_428530_.maxZ() + 1 - p_429568_,
            p_424472_,
            p_425367_,
            p_424920_,
            p_430374_,
            p_424472_,
            p_425367_,
            p_424920_
        );
    }
}