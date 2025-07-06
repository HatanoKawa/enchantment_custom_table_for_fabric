package com.river_quinn.network.enchanted_book_converting_table;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.river_quinn.EnchantmentCustomTable.MOD_ID;

public record EnchantmentConversionTableNetData(String operateType) implements CustomPayload {

    public enum OperateType {
        NEXT_PAGE,
        PREVIOUS_PAGE
    }

    public static final PacketCodec<PacketByteBuf, EnchantmentConversionTableNetData> STREAM_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            EnchantmentConversionTableNetData::operateType,
            EnchantmentConversionTableNetData::new
    );

    public static final CustomPayload.Type<PacketByteBuf, EnchantmentConversionTableNetData> TYPE =
            new CustomPayload.Type<PacketByteBuf, EnchantmentConversionTableNetData>(new Id(Identifier.of(MOD_ID, "enchantment_conversion")), STREAM_CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE.id();
    }
}
