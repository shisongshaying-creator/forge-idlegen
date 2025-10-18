package net.minecraft.server.jsonrpc.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;

public record ParamInfo(String name, Schema schema, boolean required) {
    public static final MapCodec<ParamInfo> CODEC = RecordCodecBuilder.mapCodec(
        p_422307_ -> p_422307_.group(
                Codec.STRING.fieldOf("name").forGetter(ParamInfo::name),
                Schema.CODEC.fieldOf("schema").forGetter(ParamInfo::schema),
                Codec.BOOL.fieldOf("required").forGetter(ParamInfo::required)
            )
            .apply(p_422307_, ParamInfo::new)
    );

    public ParamInfo(String p_428974_, Schema p_426709_) {
        this(p_428974_, p_426709_, true);
    }
}