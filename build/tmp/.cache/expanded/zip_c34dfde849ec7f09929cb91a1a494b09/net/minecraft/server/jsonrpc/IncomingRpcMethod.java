package net.minecraft.server.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.EncodeJsonRpcException;
import net.minecraft.server.jsonrpc.methods.IllegalMethodDefinitionException;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;

public interface IncomingRpcMethod {
    MethodInfo info();

    IncomingRpcMethod.Attributes attributes();

    JsonElement apply(MinecraftApi p_425181_, @Nullable JsonElement p_424519_, ClientInfo p_431211_);

    static <Result> IncomingRpcMethod.IncomingRpcMethodBuilder<IncomingRpcMethod.ParameterlessMethod<Result>> method(
        IncomingRpcMethod.ParameterlessRpcMethodFunction<Result> p_423575_, Codec<Result> p_428061_
    ) {
        return new IncomingRpcMethod.IncomingRpcMethodBuilder<>((p_427707_, p_431730_) -> {
            if (p_427707_.params().isPresent()) {
                throw new IllegalMethodDefinitionException("Method defined as not having parameters but is describing them");
            } else if (p_427707_.result().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method lacks result");
            } else {
                return new IncomingRpcMethod.ParameterlessMethod<>(p_427707_, p_431730_, p_428061_, p_423575_);
            }
        });
    }

    static <Params, Result> IncomingRpcMethod.IncomingRpcMethodBuilder<IncomingRpcMethod.Method<Params, Result>> method(
        IncomingRpcMethod.RpcMethodFunction<Params, Result> p_427772_, Codec<Params> p_424957_, Codec<Result> p_427706_
    ) {
        return new IncomingRpcMethod.IncomingRpcMethodBuilder<>((p_425788_, p_427151_) -> {
            if (p_425788_.params().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method defined as having parameters without describing them");
            } else if (p_425788_.result().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method lacks result");
            } else {
                return new IncomingRpcMethod.Method<>(p_425788_, p_427151_, p_424957_, p_427706_, p_427772_);
            }
        });
    }

    static <Result> IncomingRpcMethod.IncomingRpcMethodBuilder<IncomingRpcMethod.ParameterlessMethod<Result>> method(
        Function<MinecraftApi, Result> p_426897_, Codec<Result> p_424251_
    ) {
        return new IncomingRpcMethod.IncomingRpcMethodBuilder<>((p_431660_, p_423007_) -> {
            if (p_431660_.params().isPresent()) {
                throw new IllegalMethodDefinitionException("Method defined as not having parameters but is describing them");
            } else if (p_431660_.result().isEmpty()) {
                throw new IllegalMethodDefinitionException("Method lacks result");
            } else {
                return new IncomingRpcMethod.ParameterlessMethod<>(p_431660_, p_423007_, p_424251_, (p_430734_, p_428615_) -> p_426897_.apply(p_430734_));
            }
        });
    }

    public record Attributes(boolean runOnMainThread, boolean discoverable) {
    }

    @FunctionalInterface
    public interface Factory<T extends IncomingRpcMethod> {
        T create(MethodInfo p_430088_, IncomingRpcMethod.Attributes p_430430_);
    }

    public static class IncomingRpcMethodBuilder<T extends IncomingRpcMethod> {
        private final IncomingRpcMethod.Factory<T> method;
        private String description = "";
        @Nullable
        private ParamInfo paramInfo;
        @Nullable
        private ResultInfo resultInfo;
        private boolean discoverable = true;
        private boolean runOnMainThread = true;

        public IncomingRpcMethodBuilder(IncomingRpcMethod.Factory<T> p_423701_) {
            this.method = p_423701_;
        }

        public IncomingRpcMethod.IncomingRpcMethodBuilder<T> description(String p_429728_) {
            this.description = p_429728_;
            return this;
        }

        public IncomingRpcMethod.IncomingRpcMethodBuilder<T> response(ResultInfo p_431571_) {
            this.resultInfo = p_431571_;
            return this;
        }

        public IncomingRpcMethod.IncomingRpcMethodBuilder<T> param(ParamInfo p_427075_) {
            this.paramInfo = p_427075_;
            return this;
        }

        public IncomingRpcMethod.IncomingRpcMethodBuilder<T> undiscoverable() {
            this.discoverable = false;
            return this;
        }

        public IncomingRpcMethod.IncomingRpcMethodBuilder<T> notOnMainThread() {
            this.runOnMainThread = false;
            return this;
        }

        public T build() {
            MethodInfo methodinfo = new MethodInfo(this.description, this.paramInfo, this.resultInfo);
            return this.method.create(methodinfo, new IncomingRpcMethod.Attributes(this.runOnMainThread, this.discoverable));
        }

        public T register(Registry<IncomingRpcMethod> p_426192_, String p_428528_) {
            return this.register(p_426192_, ResourceLocation.withDefaultNamespace(p_428528_));
        }

        private T register(Registry<IncomingRpcMethod> p_424076_, ResourceLocation p_427050_) {
            return Registry.register(p_424076_, p_427050_, this.build());
        }
    }

    public record Method<Params, Result>(
        MethodInfo info,
        IncomingRpcMethod.Attributes attributes,
        Codec<Params> paramsCodec,
        Codec<Result> resultCodec,
        IncomingRpcMethod.RpcMethodFunction<Params, Result> function
    ) implements IncomingRpcMethod {
        @Override
        public JsonElement apply(MinecraftApi p_429665_, @Nullable JsonElement p_424548_, ClientInfo p_427391_) {
            if (p_424548_ != null && (p_424548_.isJsonArray() || p_424548_.isJsonObject())) {
                if (this.info.params().isEmpty()) {
                    throw new IllegalArgumentException("Method defined as having parameters without describing them");
                } else {
                    JsonElement jsonelement;
                    if (p_424548_.isJsonObject()) {
                        String s = this.info.params().get().name();
                        JsonElement jsonelement1 = p_424548_.getAsJsonObject().get(s);
                        if (jsonelement1 == null) {
                            throw new InvalidParameterJsonRpcException(
                                String.format(Locale.ROOT, "Params passed by-name, but expected param [%s] does not exist", s)
                            );
                        }

                        jsonelement = jsonelement1;
                    } else {
                        JsonArray jsonarray = p_424548_.getAsJsonArray();
                        if (jsonarray.isEmpty() || jsonarray.size() > 1) {
                            throw new InvalidParameterJsonRpcException("Expected exactly one element in the params array");
                        }

                        jsonelement = jsonarray.get(0);
                    }

                    Params params = this.paramsCodec.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(InvalidParameterJsonRpcException::new);
                    Result result = this.function.apply(p_429665_, params, p_427391_);
                    return this.resultCodec.encodeStart(JsonOps.INSTANCE, result).getOrThrow(EncodeJsonRpcException::new);
                }
            } else {
                throw new InvalidParameterJsonRpcException("Expected params as array or named");
            }
        }

        @Override
        public MethodInfo info() {
            return this.info;
        }

        @Override
        public IncomingRpcMethod.Attributes attributes() {
            return this.attributes;
        }
    }

    public record ParameterlessMethod<Result>(
        MethodInfo info,
        IncomingRpcMethod.Attributes attributes,
        Codec<Result> resultCodec,
        IncomingRpcMethod.ParameterlessRpcMethodFunction<Result> supplier
    ) implements IncomingRpcMethod {
        @Override
        public JsonElement apply(MinecraftApi p_430683_, @Nullable JsonElement p_427524_, ClientInfo p_423407_) {
            if (p_427524_ == null || p_427524_.isJsonArray() && p_427524_.getAsJsonArray().isEmpty()) {
                if (this.info.params().isPresent()) {
                    throw new IllegalArgumentException("Method defined as not having parameters but is describing them");
                } else {
                    Result result = this.supplier.apply(p_430683_, p_423407_);
                    return this.resultCodec.encodeStart(JsonOps.INSTANCE, result).getOrThrow(InvalidParameterJsonRpcException::new);
                }
            } else {
                throw new InvalidParameterJsonRpcException("Expected no params, or an empty array");
            }
        }

        @Override
        public MethodInfo info() {
            return this.info;
        }

        @Override
        public IncomingRpcMethod.Attributes attributes() {
            return this.attributes;
        }
    }

    @FunctionalInterface
    public interface ParameterlessRpcMethodFunction<Result> {
        Result apply(MinecraftApi p_423785_, ClientInfo p_429093_);
    }

    @FunctionalInterface
    public interface RpcMethodFunction<Params, Result> {
        Result apply(MinecraftApi p_426226_, Params p_422805_, ClientInfo p_427691_);
    }
}