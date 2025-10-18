package net.minecraft.client.renderer.blockentity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PistonHeadRenderState extends BlockEntityRenderState {
    @Nullable
    public MovingBlockRenderState block;
    @Nullable
    public MovingBlockRenderState base;
    public float xOffset;
    public float yOffset;
    public float zOffset;
}