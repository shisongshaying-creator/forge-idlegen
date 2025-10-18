package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NeighborsUpdateRenderer implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void render(
        PoseStack p_113600_, MultiBufferSource p_113601_, double p_113602_, double p_113603_, double p_113604_, DebugValueAccess p_431022_, Frustum p_430756_
    ) {
        int i = DebugSubscriptions.NEIGHBOR_UPDATES.expireAfterTicks();
        double d0 = 1.0 / (i * 2);
        Map<BlockPos, NeighborsUpdateRenderer.LastUpdate> map = new HashMap<>();
        p_431022_.forEachEvent(DebugSubscriptions.NEIGHBOR_UPDATES, (p_421001_, p_421002_, p_421003_) -> {
            long j = p_421003_ - p_421002_;
            NeighborsUpdateRenderer.LastUpdate neighborsupdaterenderer$lastupdate2 = map.getOrDefault(p_421001_, NeighborsUpdateRenderer.LastUpdate.NONE);
            map.put(p_421001_, neighborsupdaterenderer$lastupdate2.tryCount((int)j));
        });
        VertexConsumer vertexconsumer = p_113601_.getBuffer(RenderType.lines());

        for (Entry<BlockPos, NeighborsUpdateRenderer.LastUpdate> entry : map.entrySet()) {
            BlockPos blockpos = entry.getKey();
            NeighborsUpdateRenderer.LastUpdate neighborsupdaterenderer$lastupdate = entry.getValue();
            AABB aabb = new AABB(BlockPos.ZERO)
                .inflate(0.002)
                .deflate(d0 * neighborsupdaterenderer$lastupdate.age)
                .move(blockpos.getX(), blockpos.getY(), blockpos.getZ())
                .move(-p_113602_, -p_113603_, -p_113604_);
            ShapeRenderer.renderLineBox(
                p_113600_.last(),
                vertexconsumer,
                aabb.minX,
                aabb.minY,
                aabb.minZ,
                aabb.maxX,
                aabb.maxY,
                aabb.maxZ,
                1.0F,
                1.0F,
                1.0F,
                1.0F
            );
        }

        for (Entry<BlockPos, NeighborsUpdateRenderer.LastUpdate> entry1 : map.entrySet()) {
            BlockPos blockpos1 = entry1.getKey();
            NeighborsUpdateRenderer.LastUpdate neighborsupdaterenderer$lastupdate1 = entry1.getValue();
            DebugRenderer.renderFloatingText(
                p_113600_,
                p_113601_,
                String.valueOf(neighborsupdaterenderer$lastupdate1.count),
                blockpos1.getX(),
                blockpos1.getY(),
                blockpos1.getZ(),
                -1
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    record LastUpdate(int count, int age) {
        static final NeighborsUpdateRenderer.LastUpdate NONE = new NeighborsUpdateRenderer.LastUpdate(0, Integer.MAX_VALUE);

        public NeighborsUpdateRenderer.LastUpdate tryCount(int p_428172_) {
            if (p_428172_ == this.age) {
                return new NeighborsUpdateRenderer.LastUpdate(this.count + 1, p_428172_);
            } else {
                return p_428172_ < this.age ? new NeighborsUpdateRenderer.LastUpdate(1, p_428172_) : this;
            }
        }
    }
}