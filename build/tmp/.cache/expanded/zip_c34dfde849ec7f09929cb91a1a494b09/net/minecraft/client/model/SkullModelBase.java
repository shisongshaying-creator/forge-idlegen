package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SkullModelBase extends Model<SkullModelBase.State> {
    public SkullModelBase(ModelPart p_365382_) {
        super(p_365382_, RenderType::entityTranslucent);
    }

    @OnlyIn(Dist.CLIENT)
    public static class State {
        public float animationPos;
        public float yRot;
        public float xRot;
    }
}