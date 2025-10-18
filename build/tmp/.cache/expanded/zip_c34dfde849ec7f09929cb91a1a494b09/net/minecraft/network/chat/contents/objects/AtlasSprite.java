package net.minecraft.network.chat.contents.objects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.ResourceLocation;

public record AtlasSprite(ResourceLocation atlas, ResourceLocation sprite) implements ObjectInfo {
    public static final ResourceLocation DEFAULT_ATLAS = AtlasIds.BLOCKS;
    public static final MapCodec<AtlasSprite> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_431363_ -> p_431363_.group(
                ResourceLocation.CODEC.optionalFieldOf("atlas", DEFAULT_ATLAS).forGetter(AtlasSprite::atlas),
                ResourceLocation.CODEC.fieldOf("sprite").forGetter(AtlasSprite::sprite)
            )
            .apply(p_431363_, AtlasSprite::new)
    );

    @Override
    public MapCodec<AtlasSprite> codec() {
        return MAP_CODEC;
    }

    @Override
    public FontDescription fontDescription() {
        return new FontDescription.AtlasSprite(this.atlas, this.sprite);
    }

    private static String toShortName(ResourceLocation p_424264_) {
        return p_424264_.getNamespace().equals("minecraft") ? p_424264_.getPath() : p_424264_.toString();
    }

    @Override
    public String description() {
        String s = toShortName(this.sprite);
        return this.atlas.equals(DEFAULT_ATLAS) ? "[" + s + "]" : "[" + s + "@" + toShortName(this.atlas) + "]";
    }
}