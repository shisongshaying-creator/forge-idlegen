package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CopperGolemStatueModel extends Model<Direction> {
    public CopperGolemStatueModel(ModelPart p_422622_) {
        super(p_422622_, RenderType::entityCutoutNoCull);
    }

    public void setupAnim(Direction p_428776_) {
        this.root.y = 0.0F;
        this.root.yRot = p_428776_.getOpposite().toYRot() * (float) (Math.PI / 180.0);
        this.root.zRot = (float) Math.PI;
    }
}