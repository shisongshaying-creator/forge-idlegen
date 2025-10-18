package net.minecraft.client.gui.font;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public interface PlainTextRenderable extends TextRenderable {
    @Override
    default void render(Matrix4f p_426104_, VertexConsumer p_423605_, int p_430551_, boolean p_424881_) {
        float f = 0.0F;
        if (this.shadowColor() != 0) {
            this.renderSprite(p_426104_, p_423605_, p_430551_, this.x() + this.shadowOffset(), this.y() + this.shadowOffset(), 0.0F, this.shadowColor());
            if (!p_424881_) {
                f += 0.03F;
            }
        }

        this.renderSprite(p_426104_, p_423605_, p_430551_, this.x(), this.y(), f, this.color());
    }

    void renderSprite(Matrix4f p_426271_, VertexConsumer p_428953_, int p_422827_, float p_431490_, float p_429259_, float p_426189_, int p_426135_);

    float x();

    float y();

    int color();

    int shadowColor();

    float shadowOffset();
}