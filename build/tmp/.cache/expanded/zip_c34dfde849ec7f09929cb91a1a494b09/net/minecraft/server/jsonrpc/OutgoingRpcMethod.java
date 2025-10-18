package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.methods.IllegalMethodDefinitionException;

public interface OutgoingRpcMethod<Params, Result> {
    String NOTIFICATION_PREFIX = "notification/";

    MethodInfo info();

    OutgoingRpcMethod.Attributes attributes();

    @Nullable
    default JsonElement encodeParams(Params p_431028_) {
        return null;
    }

    @Nullable
    default Result decodeResult(JsonElement p_426415_) {
        return null;
    }

    static OutgoingRpcMethod.OutgoingRpcMethodBuilder<OutgoingRpcMethod.ParmeterlessNotification> notification() {
        return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>((p_427366_, p_427051_) -> {
            if (p_427366_.params().isPresent()) {
                throw new IllegalMethodDefinitionException("Method defined as not having parameters but is describing them");
            } else if (p_427366_.result().isPresent()) {
                throw new IllegalMethodDefinitionException("Method defined as not having result but is describing it");
            } else {
                return new OutgoingRpcMethod.ParmeterlessNotification(p_427366_, p_427051_);
            }
        });
    }

    static <Params> OutgoingRpcMethod.OutgoingRpcMethodBuilder<OutgoingRpcMethod.Notification<Params>> notification(Codec<Params> p_429313_) {
        return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>((p_427774_, p_425953_) -> {
            if (p_427774_.params().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method defined as having parameters without describing them");
            } else if (p_427774_.result().isPresent()) {
                throw new IllegalMethodDefinitionException("Method defined as not having result but is describing it");
            } else {
                return new OutgoingRpcMethod.Notification<>(p_427774_, p_425953_, p_429313_);
            }
        });
    }

    static <Result> OutgoingRpcMethod.OutgoingRpcMethodBuilder<OutgoingRpcMethod.ParameterlessMethod<Result>> request(Codec<Result> p_427305_) {
        return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>((p_424520_, p_424204_) -> {
            if (p_424520_.params().isPresent()) {
                throw new IllegalMethodDefinitionException("Method defined as not having parameters but is describing them");
            } else if (p_424520_.result().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method lacks result");
            } else {
                return new OutgoingRpcMethod.ParameterlessMethod<>(p_424520_, p_424204_, p_427305_);
            }
        });
    }

    static <Params, Result> OutgoingRpcMethod.OutgoingRpcMethodBuilder<OutgoingRpcMethod.Method<Params, Result>> request(
        Codec<Params> p_429322_, Codec<Result> p_428638_
    ) {
        return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>((p_425018_, p_428709_) -> {
            if (p_425018_.params().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method defined as having parameters without describing them");
            } else if (p_425018_.result().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method lacks result");
            } else {
                return new OutgoingRpcMethod.Method<>(p_425018_, p_428709_, p_429322_, p_428638_);
            }
        });
    }

    public record Attributes(boolean discoverable) {
    }

    @FunctionalInterface
    public interface Factory<T extends OutgoingRpcMethod<?, ?>> {
        T create(MethodInfo p_430664_, OutgoingRpcMethod.Attributes p_425247_);
    }

    public record Method<Params, Result>(MethodInfo info, OutgoingRpcMethod.Attributes attributes, Codec<Params> paramsCodec, Codec<Result> resultCodec)
        implements OutgoingRpcMethod<Params, Result> {
        @Nullable
        @Override
        public JsonElement encodeParams(Params p_431477_) {
            return this.paramsCodec.encodeStart(JsonOps.INSTANCE, p_431477_).getOrThrow();
        }

        @Override
        public Result decodeResult(JsonElement p_426216_) {
            return this.resultCodec.parse(JsonOps.INSTANCE, p_426216_).getOrThrow();
        }

        @Override
        public MethodInfo info() {
            return this.info;
        }

        @Override
        public OutgoingRpcMethod.Attributes attributes() {
            return this.attributes;
        }
    }

    public record Notification<Params>(MethodInfo info, OutgoingRpcMethod.Attributes attributes, Codec<Params> paramsCodec)
        implements OutgoingRpcMethod<Params, Void> {
        @Nullable
        @Override
        public JsonElement encodeParams(Params p_424475_) {
            return this.paramsCodec.encodeStart(JsonOps.INSTANCE, p_424475_).getOrThrow();
        }

        @Override
        public MethodInfo info() {
            return this.info;
        }

        @Override
        public OutgoingRpcMethod.Attributes attributes() {
            return this.attributes;
        }
    }

    public static class OutgoingRpcMethodBuilder<T extends OutgoingRpcMethod<?, ?>> {
        public static final OutgoingRpcMethod.Attributes DEFAULT_ATTRIBUTES = new OutgoingRpcMethod.Attributes(true);
        private final OutgoingRpcMethod.Factory<T> method;
        private String description = "";
        @Nullable
        private ParamInfo paramInfo;
        @Nullable
        private ResultInfo resultInfo;

        public OutgoingRpcMethodBuilder(OutgoingRpcMethod.Factory<T> p_424582_) {
            this.method = p_424582_;
        }

        public OutgoingRpcMethod.OutgoingRpcMethodBuilder<T> description(String p_426554_) {
            this.description = p_426554_;
            return this;
        }

        public OutgoingRpcMethod.OutgoingRpcMethodBuilder<T> response(ResultInfo p_422779_) {
            this.resultInfo = p_422779_;
            return this;
        }

        public OutgoingRpcMethod.OutgoingRpcMethodBuilder<T> param(ParamInfo p_426469_) {
            this.paramInfo = p_426469_;
            return this;
        }

        private T build() {
            MethodInfo methodinfo = new MethodInfo(this.description, this.paramInfo, this.resultInfo);
            return this.method.create(methodinfo, DEFAULT_ATTRIBUTES);
        }

        public Holder.Reference<T> register(String p_423728_) {
            return this.register(ResourceLocation.withDefaultNamespace("notification/" + p_423728_));
        }

        private Holder.Reference<T> register(ResourceLocation p_431430_) {
            return Registry.registerForHolder(BuiltInRegistries.OUTGOING_RPC_METHOD, p_431430_, this.build());
        }
    }

    public record ParameterlessMethod<Result>(MethodInfo info, OutgoingRpcMethod.Attributes attributes, Codec<Result> resultCodec)
        implements OutgoingRpcMethod<Void, Result> {
        @Override
        public Result decodeResult(JsonElement p_426562_) {
            return this.resultCodec.parse(JsonOps.INSTANCE, p_426562_).getOrThrow();
        }

        @Override
        public MethodInfo info() {
            return this.info;
        }

        @Override
        public OutgoingRpcMethod.Attributes attributes() {
            return this.attributes;
        }
    }

    public record ParmeterlessNotification(MethodInfo info, OutgoingRpcMethod.Attributes attributes) implements OutgoingRpcMethod<Void, Void> {
        @Override
        public MethodInfo info() {
            return this.info;
        }

        @Override
        public OutgoingRpcMethod.Attributes attributes() {
            return this.attributes;
        }
    }
}