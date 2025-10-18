package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class AtlasGlyphProvider {
    private static final float WIDTH = 8.0F;
    private static final float HEIGHT = 8.0F;
    static final GlyphInfo GLYPH_INFO = GlyphInfo.simple(8.0F);
    final TextureAtlas atlas;
    final GlyphRenderTypes renderTypes;
    private final GlyphSource missingWrapper;
    private final Map<ResourceLocation, GlyphSource> wrapperCache = new HashMap<>();
    private final Function<ResourceLocation, GlyphSource> spriteResolver;

    public AtlasGlyphProvider(TextureAtlas p_423776_) {
        this.atlas = p_423776_;
        this.renderTypes = GlyphRenderTypes.createForColorTexture(p_423776_.location());
        TextureAtlasSprite textureatlassprite = p_423776_.missingSprite();
        this.missingWrapper = this.createSprite(textureatlassprite);
        this.spriteResolver = p_429565_ -> {
            TextureAtlasSprite textureatlassprite1 = p_423776_.getSprite(p_429565_);
            return textureatlassprite1 == textureatlassprite ? this.missingWrapper : this.createSprite(textureatlassprite1);
        };
    }

    public GlyphSource sourceForSprite(ResourceLocation p_427856_) {
        return this.wrapperCache.computeIfAbsent(p_427856_, this.spriteResolver);
    }

    private GlyphSource createSprite(final TextureAtlasSprite p_430930_) {
        return new SingleSpriteSource(
            new BakedGlyph() {
                @Override
                public GlyphInfo info() {
                    return AtlasGlyphProvider.GLYPH_INFO;
                }

                @Override
                public TextRenderable createGlyph(
                    float p_422755_, float p_422593_, int p_429125_, int p_428352_, Style p_425631_, float p_426190_, float p_429163_
                ) {
                    return new AtlasGlyphProvider.Instance(
                        AtlasGlyphProvider.this.renderTypes,
                        AtlasGlyphProvider.this.atlas.getTextureView(),
                        p_430930_,
                        p_422755_,
                        p_422593_,
                        p_429125_,
                        p_428352_,
                        p_429163_
                    );
                }
            }
        );
    }

    @OnlyIn(Dist.CLIENT)
    record Instance(
        GlyphRenderTypes renderTypes,
        GpuTextureView textureView,
        TextureAtlasSprite sprite,
        float x,
        float y,
        int color,
        int shadowColor,
        float shadowOffset
    ) implements PlainTextRenderable {
        @Override
        public void renderSprite(Matrix4f p_425478_, VertexConsumer p_429855_, int p_428090_, float p_429012_, float p_431372_, float p_430727_, int p_424820_) {
            float f = p_429012_ + this.left();
            float f1 = p_429012_ + this.right();
            float f2 = p_431372_ + this.top();
            float f3 = p_431372_ + this.bottom();
            p_429855_.addVertex(p_425478_, f, f2, p_430727_)
                .setUv(this.sprite.getU0(), this.sprite.getV0())
                .setColor(p_424820_)
                .setLight(p_428090_);
            p_429855_.addVertex(p_425478_, f, f3, p_430727_)
                .setUv(this.sprite.getU0(), this.sprite.getV1())
                .setColor(p_424820_)
                .setLight(p_428090_);
            p_429855_.addVertex(p_425478_, f1, f3, p_430727_)
                .setUv(this.sprite.getU1(), this.sprite.getV1())
                .setColor(p_424820_)
                .setLight(p_428090_);
            p_429855_.addVertex(p_425478_, f1, f2, p_430727_)
                .setUv(this.sprite.getU1(), this.sprite.getV0())
                .setColor(p_424820_)
                .setLight(p_428090_);
        }

        @Override
        public RenderType renderType(Font.DisplayMode p_429668_) {
            return this.renderTypes.select(p_429668_);
        }

        @Override
        public RenderPipeline guiPipeline() {
            return this.renderTypes.guiPipeline();
        }

        @Override
        public GpuTextureView textureView() {
            return this.textureView;
        }

        @Override
        public float left() {
            return 0.0F;
        }

        @Override
        public float right() {
            return 8.0F;
        }

        @Override
        public float top() {
            return -1.0F;
        }

        @Override
        public float bottom() {
            return 7.0F;
        }

        @Override
        public float x() {
            return this.x;
        }

        @Override
        public float y() {
            return this.y;
        }

        @Override
        public int color() {
            return this.color;
        }

        @Override
        public int shadowColor() {
            return this.shadowColor;
        }

        @Override
        public float shadowOffset() {
            return this.shadowOffset;
        }
    }
}