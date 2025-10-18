package net.minecraft.advancements.critereon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public interface MinMaxBounds<T extends Number & Comparable<T>> {
    SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
    SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));

    MinMaxBounds.Bounds<T> bounds();

    default Optional<T> min() {
        return this.bounds().min;
    }

    default Optional<T> max() {
        return this.bounds().max;
    }

    default boolean isAny() {
        return this.bounds().isAny();
    }

    public record Bounds<T extends Number & Comparable<T>>(Optional<T> min, Optional<T> max) {
        public boolean isAny() {
            return this.min().isEmpty() && this.max().isEmpty();
        }

        public DataResult<MinMaxBounds.Bounds<T>> validateSwappedBoundsInCodec() {
            return this.areSwapped()
                ? DataResult.error(() -> "Swapped bounds in range: " + this.min() + " is higher than " + this.max())
                : DataResult.success(this);
        }

        public boolean areSwapped() {
            return this.min.isPresent() && this.max.isPresent() && this.min.get().compareTo(this.max.get()) > 0;
        }

        public Optional<T> asPoint() {
            Optional<T> optional = this.min();
            Optional<T> optional1 = this.max();
            return optional.equals(optional1) ? optional : Optional.empty();
        }

        public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> any() {
            return new MinMaxBounds.Bounds<T>(Optional.empty(), Optional.empty());
        }

        public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> exactly(T p_424385_) {
            Optional<T> optional = Optional.of(p_424385_);
            return new MinMaxBounds.Bounds<>(optional, optional);
        }

        public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> between(T p_423526_, T p_426317_) {
            return new MinMaxBounds.Bounds<>(Optional.of(p_423526_), Optional.of(p_426317_));
        }

        public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> atLeast(T p_428889_) {
            return new MinMaxBounds.Bounds<>(Optional.of(p_428889_), Optional.empty());
        }

        public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> atMost(T p_423277_) {
            return new MinMaxBounds.Bounds<>(Optional.empty(), Optional.of(p_423277_));
        }

        public <U extends Number & Comparable<U>> MinMaxBounds.Bounds<U> map(Function<T, U> p_423133_) {
            return new MinMaxBounds.Bounds<>(this.min.map(p_423133_), this.max.map(p_423133_));
        }

        static <T extends Number & Comparable<T>> Codec<MinMaxBounds.Bounds<T>> createCodec(Codec<T> p_428287_) {
            Codec<MinMaxBounds.Bounds<T>> codec = RecordCodecBuilder.create(
                p_425161_ -> p_425161_.group(
                        p_428287_.optionalFieldOf("min").forGetter(MinMaxBounds.Bounds::min),
                        p_428287_.optionalFieldOf("max").forGetter(MinMaxBounds.Bounds::max)
                    )
                    .apply(p_425161_, MinMaxBounds.Bounds::new)
            );
            return Codec.either(codec, p_428287_).xmap(p_430695_ -> p_430695_.map(p_429698_ -> p_429698_, p_424963_ -> exactly((T)p_424963_)), p_424085_ -> {
                Optional<T> optional = p_424085_.asPoint();
                return optional.isPresent() ? Either.right(optional.get()) : Either.left((MinMaxBounds.Bounds<T>)p_424085_);
            });
        }

        static <B extends ByteBuf, T extends Number & Comparable<T>> StreamCodec<B, MinMaxBounds.Bounds<T>> createStreamCodec(final StreamCodec<B, T> p_429071_) {
            return new StreamCodec<B, MinMaxBounds.Bounds<T>>() {
                private static final int MIN_FLAG = 1;
                private static final int MAX_FLAG = 2;

                public MinMaxBounds.Bounds<T> decode(B p_431698_) {
                    byte b0 = p_431698_.readByte();
                    Optional<T> optional = (b0 & 1) != 0 ? Optional.of(p_429071_.decode(p_431698_)) : Optional.empty();
                    Optional<T> optional1 = (b0 & 2) != 0 ? Optional.of(p_429071_.decode(p_431698_)) : Optional.empty();
                    return new MinMaxBounds.Bounds<>(optional, optional1);
                }

                public void encode(B p_424922_, MinMaxBounds.Bounds<T> p_427205_) {
                    Optional<T> optional = p_427205_.min();
                    Optional<T> optional1 = p_427205_.max();
                    p_424922_.writeByte((optional.isPresent() ? 1 : 0) | (optional1.isPresent() ? 2 : 0));
                    optional.ifPresent(p_422802_ -> p_429071_.encode(p_424922_, (T)p_422802_));
                    optional1.ifPresent(p_426067_ -> p_429071_.encode(p_424922_, (T)p_426067_));
                }
            };
        }

        public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> fromReader(
            StringReader p_422327_, Function<String, T> p_429737_, Supplier<DynamicCommandExceptionType> p_427777_
        ) throws CommandSyntaxException {
            if (!p_422327_.canRead()) {
                throw MinMaxBounds.ERROR_EMPTY.createWithContext(p_422327_);
            } else {
                int i = p_422327_.getCursor();

                try {
                    Optional<T> optional = readNumber(p_422327_, p_429737_, p_427777_);
                    Optional<T> optional1;
                    if (p_422327_.canRead(2) && p_422327_.peek() == '.' && p_422327_.peek(1) == '.') {
                        p_422327_.skip();
                        p_422327_.skip();
                        optional1 = readNumber(p_422327_, p_429737_, p_427777_);
                    } else {
                        optional1 = optional;
                    }

                    if (optional.isEmpty() && optional1.isEmpty()) {
                        throw MinMaxBounds.ERROR_EMPTY.createWithContext(p_422327_);
                    } else {
                        return new MinMaxBounds.Bounds<>(optional, optional1);
                    }
                } catch (CommandSyntaxException commandsyntaxexception) {
                    p_422327_.setCursor(i);
                    throw new CommandSyntaxException(
                        commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), commandsyntaxexception.getInput(), i
                    );
                }
            }
        }

        private static <T extends Number> Optional<T> readNumber(
            StringReader p_423392_, Function<String, T> p_423913_, Supplier<DynamicCommandExceptionType> p_427182_
        ) throws CommandSyntaxException {
            int i = p_423392_.getCursor();

            while (p_423392_.canRead() && isAllowedInputChar(p_423392_)) {
                p_423392_.skip();
            }

            String s = p_423392_.getString().substring(i, p_423392_.getCursor());
            if (s.isEmpty()) {
                return Optional.empty();
            } else {
                try {
                    return Optional.of(p_423913_.apply(s));
                } catch (NumberFormatException numberformatexception) {
                    throw p_427182_.get().createWithContext(p_423392_, s);
                }
            }
        }

        private static boolean isAllowedInputChar(StringReader p_426722_) {
            char c0 = p_426722_.peek();
            if ((c0 < '0' || c0 > '9') && c0 != '-') {
                return c0 != '.' ? false : !p_426722_.canRead(2) || p_426722_.peek(1) != '.';
            } else {
                return true;
            }
        }
    }

    public record Doubles(MinMaxBounds.Bounds<Double> bounds, MinMaxBounds.Bounds<Double> boundsSqr) implements MinMaxBounds<Double> {
        public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles(MinMaxBounds.Bounds.any());
        public static final Codec<MinMaxBounds.Doubles> CODEC = MinMaxBounds.Bounds.createCodec(Codec.DOUBLE)
            .validate(MinMaxBounds.Bounds::validateSwappedBoundsInCodec)
            .xmap(MinMaxBounds.Doubles::new, MinMaxBounds.Doubles::bounds);
        public static final StreamCodec<ByteBuf, MinMaxBounds.Doubles> STREAM_CODEC = MinMaxBounds.Bounds.createStreamCodec(ByteBufCodecs.DOUBLE)
            .map(MinMaxBounds.Doubles::new, MinMaxBounds.Doubles::bounds);

        private Doubles(MinMaxBounds.Bounds<Double> p_427729_) {
            this(p_427729_, p_427729_.map(Mth::square));
        }

        public static MinMaxBounds.Doubles exactly(double p_154787_) {
            return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.exactly(p_154787_));
        }

        public static MinMaxBounds.Doubles between(double p_154789_, double p_154790_) {
            return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.between(p_154789_, p_154790_));
        }

        public static MinMaxBounds.Doubles atLeast(double p_154805_) {
            return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.atLeast(p_154805_));
        }

        public static MinMaxBounds.Doubles atMost(double p_154809_) {
            return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.atMost(p_154809_));
        }

        public boolean matches(double p_154811_) {
            return this.bounds.min.isPresent() && this.bounds.min.get() > p_154811_
                ? false
                : this.bounds.max.isEmpty() || !(this.bounds.max.get() < p_154811_);
        }

        public boolean matchesSqr(double p_154813_) {
            return this.boundsSqr.min.isPresent() && this.boundsSqr.min.get() > p_154813_
                ? false
                : this.boundsSqr.max.isEmpty() || !(this.boundsSqr.max.get() < p_154813_);
        }

        public static MinMaxBounds.Doubles fromReader(StringReader p_154794_) throws CommandSyntaxException {
            int i = p_154794_.getCursor();
            MinMaxBounds.Bounds<Double> bounds = MinMaxBounds.Bounds.fromReader(
                p_154794_, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble
            );
            if (bounds.areSwapped()) {
                p_154794_.setCursor(i);
                throw ERROR_SWAPPED.createWithContext(p_154794_);
            } else {
                return new MinMaxBounds.Doubles(bounds);
            }
        }

        @Override
        public MinMaxBounds.Bounds<Double> bounds() {
            return this.bounds;
        }
    }

    public record FloatDegrees(MinMaxBounds.Bounds<Float> bounds) implements MinMaxBounds<Float> {
        public static final MinMaxBounds.FloatDegrees ANY = new MinMaxBounds.FloatDegrees(MinMaxBounds.Bounds.any());
        public static final Codec<MinMaxBounds.FloatDegrees> CODEC = MinMaxBounds.Bounds.createCodec(Codec.FLOAT)
            .xmap(MinMaxBounds.FloatDegrees::new, MinMaxBounds.FloatDegrees::bounds);
        public static final StreamCodec<ByteBuf, MinMaxBounds.FloatDegrees> STREAM_CODEC = MinMaxBounds.Bounds.createStreamCodec(ByteBufCodecs.FLOAT)
            .map(MinMaxBounds.FloatDegrees::new, MinMaxBounds.FloatDegrees::bounds);

        public static MinMaxBounds.FloatDegrees fromReader(StringReader p_425543_) throws CommandSyntaxException {
            MinMaxBounds.Bounds<Float> bounds = MinMaxBounds.Bounds.fromReader(
                p_425543_, Float::parseFloat, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidFloat
            );
            return new MinMaxBounds.FloatDegrees(bounds);
        }

        @Override
        public MinMaxBounds.Bounds<Float> bounds() {
            return this.bounds;
        }
    }

    public record Ints(MinMaxBounds.Bounds<Integer> bounds, MinMaxBounds.Bounds<Long> boundsSqr) implements MinMaxBounds<Integer> {
        public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints(MinMaxBounds.Bounds.any());
        public static final Codec<MinMaxBounds.Ints> CODEC = MinMaxBounds.Bounds.createCodec(Codec.INT)
            .validate(MinMaxBounds.Bounds::validateSwappedBoundsInCodec)
            .xmap(MinMaxBounds.Ints::new, MinMaxBounds.Ints::bounds);
        public static final StreamCodec<ByteBuf, MinMaxBounds.Ints> STREAM_CODEC = MinMaxBounds.Bounds.createStreamCodec(ByteBufCodecs.INT)
            .map(MinMaxBounds.Ints::new, MinMaxBounds.Ints::bounds);

        private Ints(MinMaxBounds.Bounds<Integer> p_424466_) {
            this(p_424466_, p_424466_.map(p_420619_ -> Mth.square(p_420619_.longValue())));
        }

        public static MinMaxBounds.Ints exactly(int p_55372_) {
            return new MinMaxBounds.Ints(MinMaxBounds.Bounds.exactly(p_55372_));
        }

        public static MinMaxBounds.Ints between(int p_154815_, int p_154816_) {
            return new MinMaxBounds.Ints(MinMaxBounds.Bounds.between(p_154815_, p_154816_));
        }

        public static MinMaxBounds.Ints atLeast(int p_55387_) {
            return new MinMaxBounds.Ints(MinMaxBounds.Bounds.atLeast(p_55387_));
        }

        public static MinMaxBounds.Ints atMost(int p_154820_) {
            return new MinMaxBounds.Ints(MinMaxBounds.Bounds.atMost(p_154820_));
        }

        public boolean matches(int p_55391_) {
            return this.bounds.min.isPresent() && this.bounds.min.get() > p_55391_
                ? false
                : this.bounds.max.isEmpty() || this.bounds.max.get() >= p_55391_;
        }

        public boolean matchesSqr(long p_154818_) {
            return this.boundsSqr.min.isPresent() && this.boundsSqr.min.get() > p_154818_
                ? false
                : this.boundsSqr.max.isEmpty() || this.boundsSqr.max.get() >= p_154818_;
        }

        public static MinMaxBounds.Ints fromReader(StringReader p_55376_) throws CommandSyntaxException {
            int i = p_55376_.getCursor();
            MinMaxBounds.Bounds<Integer> bounds = MinMaxBounds.Bounds.fromReader(
                p_55376_, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt
            );
            if (bounds.areSwapped()) {
                p_55376_.setCursor(i);
                throw ERROR_SWAPPED.createWithContext(p_55376_);
            } else {
                return new MinMaxBounds.Ints(bounds);
            }
        }

        @Override
        public MinMaxBounds.Bounds<Integer> bounds() {
            return this.bounds;
        }
    }
}