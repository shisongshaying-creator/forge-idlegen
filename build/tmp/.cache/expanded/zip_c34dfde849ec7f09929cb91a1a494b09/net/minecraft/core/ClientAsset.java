package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public interface ClientAsset {
    ResourceLocation id();

    public record DownloadedTexture(ResourceLocation texturePath, String url) implements ClientAsset.Texture {
        @Override
        public ResourceLocation id() {
            return this.texturePath;
        }

        @Override
        public ResourceLocation texturePath() {
            return this.texturePath;
        }
    }

    public record ResourceTexture(ResourceLocation id, ResourceLocation texturePath) implements ClientAsset.Texture {
        public static final Codec<ClientAsset.ResourceTexture> CODEC = ResourceLocation.CODEC
            .xmap(ClientAsset.ResourceTexture::new, ClientAsset.ResourceTexture::id);
        public static final MapCodec<ClientAsset.ResourceTexture> DEFAULT_FIELD_CODEC = CODEC.fieldOf("asset_id");
        public static final StreamCodec<ByteBuf, ClientAsset.ResourceTexture> STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .map(ClientAsset.ResourceTexture::new, ClientAsset.ResourceTexture::id);

        public ResourceTexture(ResourceLocation p_428377_) {
            this(p_428377_, p_428377_.withPath(p_425999_ -> "textures/" + p_425999_ + ".png"));
        }

        @Override
        public ResourceLocation id() {
            return this.id;
        }

        @Override
        public ResourceLocation texturePath() {
            return this.texturePath;
        }
    }

    public interface Texture extends ClientAsset {
        ResourceLocation texturePath();
    }
}