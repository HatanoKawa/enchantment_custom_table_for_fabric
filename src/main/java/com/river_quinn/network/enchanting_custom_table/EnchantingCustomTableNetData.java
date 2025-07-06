package com.river_quinn.network.enchanting_custom_table;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.river_quinn.EnchantmentCustomTable.MOD_ID;

public record EnchantingCustomTableNetData(String operateType) implements CustomPayload {

    public enum OperateType {
        EXPORT_ALL_ENCHANTMENTS,
        NEXT_PAGE,
        PREVIOUS_PAGE
    }

    public static final PacketCodec<PacketByteBuf, EnchantingCustomTableNetData> STREAM_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            EnchantingCustomTableNetData::operateType,
            EnchantingCustomTableNetData::new
    );

    public static final CustomPayload.Type<PacketByteBuf, EnchantingCustomTableNetData> TYPE =
            new CustomPayload.Type<PacketByteBuf, EnchantingCustomTableNetData>(new Id(Identifier.of(MOD_ID, "enchanting_custom")), STREAM_CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE.id();
    }
}
