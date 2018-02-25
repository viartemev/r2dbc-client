/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nebhale.r2dbc.postgresql.codec;

import com.nebhale.r2dbc.postgresql.client.Parameter;
import com.nebhale.r2dbc.postgresql.message.Format;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The default {@link Codec} implementation.  Delegates to type-specific codec implementations.
 */
public final class DefaultCodecs implements Codecs {

    private final List<Codec<?>> codecs;

    /**
     * Creates a new instance of {@link DefaultCodecs}.
     *
     * @param byteBufAllocator the {@link ByteBufAllocator} to use for encoding
     */
    public DefaultCodecs(ByteBufAllocator byteBufAllocator) {
        requireNonNull(byteBufAllocator, "byteBufAllocator must not be null");

        this.codecs = Arrays.asList(
            new NullCodec(),
            new BigDecimalCodec(byteBufAllocator),
            new BooleanCodec(byteBufAllocator),
            new ByteCodec(byteBufAllocator),
            new CharacterCodec(byteBufAllocator),
            new DateCodec(byteBufAllocator),
            new DoubleCodec(byteBufAllocator),
            new EnumCodec(byteBufAllocator),
            new FloatCodec(byteBufAllocator),
            new InetAddressCodec(byteBufAllocator),
            new InstantCodec(byteBufAllocator),
            new IntegerCodec(byteBufAllocator),
            new LocalDateCodec(byteBufAllocator),
            new LocalDateTimeCodec(byteBufAllocator),
            new LocalTimeCodec(byteBufAllocator),
            new LongCodec(byteBufAllocator),
            new OffsetDateTimeCodec(byteBufAllocator),
            new ShortCodec(byteBufAllocator),
            new StringCodec(byteBufAllocator),
            new UriCodec(byteBufAllocator),
            new UrlCodec(byteBufAllocator),
            new UuidCodec(byteBufAllocator),
            new ZoneIdCodec(byteBufAllocator),
            new ZonedDateTimeCodec(byteBufAllocator)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(ByteBuf byteBuf, int dataType, Format format, Class<? extends T> type) {
        return this.codecs.stream()
            .filter(codec -> codec.canDecode(byteBuf, dataType, format, type))
            .map(codec -> ((Codec<T>) codec).decode(byteBuf, format, type))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot decode value of type %s", type.getName())));
    }

    @Override
    public Parameter encode(Object value) {
        return this.codecs.stream()
            .filter(codec -> codec.canEncode(value))
            .map(codec -> codec.encode(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot encode parameter of type %s", value.getClass().getName())));
    }

}