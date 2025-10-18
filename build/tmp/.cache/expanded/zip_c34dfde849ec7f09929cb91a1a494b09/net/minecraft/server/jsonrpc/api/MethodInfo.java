package net.minecraft.server.jsonrpc.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public record MethodInfo(String description, Optional<ParamInfo> params, Optional<ResultInfo> result) {
    public static final Codec<Optional<ParamInfo>> PARAMS_CODEC = ParamInfo.CODEC
        .codec()
        .listOf()
        .xmap(p_427085_ -> p_427085_.stream().findAny(), p_425957_ -> p_425957_.map(List::of).orElse(List.of()));
    public static final MapCodec<MethodInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_431228_ -> p_431228_.group(
                Codec.STRING.fieldOf("description").forGetter(MethodInfo::description),
                PARAMS_CODEC.fieldOf("params").forGetter(MethodInfo::params),
                ResultInfo.CODEC.codec().optionalFieldOf("result").forGetter(MethodInfo::result)
            )
            .apply(p_431228_, MethodInfo::new)
    );

    public MethodInfo(String p_428854_, @Nullable ParamInfo p_425825_, @Nullable ResultInfo p_431170_) {
        this(p_428854_, Optional.ofNullable(p_425825_), Optional.ofNullable(p_431170_));
    }

    public MethodInfo.Named named(ResourceLocation p_424192_) {
        return new MethodInfo.Named(p_424192_, this);
    }

    public record Named(ResourceLocation name, MethodInfo contents) {
        public static final Codec<MethodInfo.Named> CODEC = RecordCodecBuilder.create(
            p_424168_ -> p_424168_.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(MethodInfo.Named::name),
                    MethodInfo.MAP_CODEC.forGetter(MethodInfo.Named::contents)
                )
                .apply(p_424168_, MethodInfo.Named::new)
        );
    }
}