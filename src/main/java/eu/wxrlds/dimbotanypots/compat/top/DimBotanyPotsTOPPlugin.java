package eu.wxrlds.dimbotanypots.compat.top;


import edivad.dimstorage.api.Frequency;
import eu.wxrlds.dimbotanypots.DimBotanyPots;
import eu.wxrlds.dimbotanypots.block.DimensionalBotanyPotBlock;
import eu.wxrlds.dimbotanypots.block.DimensionalBotanyPotBlockEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;


public class DimBotanyPotsTOPPlugin implements Function<ITheOneProbe, Void>, IProbeInfoProvider {

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath(DimBotanyPots.MOD_ID, "top_plugin");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hit) {
        if (state.getBlock() instanceof DimensionalBotanyPotBlock) {
            BlockEntity tile = level.getBlockEntity(hit.getPos());
            if (tile instanceof DimensionalBotanyPotBlockEntity pot) {
                Frequency freq = pot.getFrequency();

                // Owner
                if (freq.hasOwner()) {
                    Component ownerLabel = Component.translatable("dimbotanypots.tooltip.owner");
                    info.text(ownerLabel.copy().append(": " + freq.getOwner())
                            .withStyle(ChatFormatting.DARK_RED));
                }

                // Frequency
                Component freqLabel = Component.translatable("dimbotanypots.tooltip.frequency");
                info.text(freqLabel.copy().append(": " + freq.channel()));
            }
        }
    }
}