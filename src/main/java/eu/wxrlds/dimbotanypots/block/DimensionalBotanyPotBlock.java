package eu.wxrlds.dimbotanypots.block;


import edivad.dimstorage.api.Frequency;
import edivad.dimstorage.items.components.DimStorageComponents;
import eu.wxrlds.dimbotanypots.DimBotanyPots;
import eu.wxrlds.dimbotanypots.util.DimBotanyPotHelper;
import net.darkhax.botanypots.common.impl.block.BotanyPotBlock;
import net.darkhax.botanypots.common.impl.block.PotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DimensionalBotanyPotBlock extends BotanyPotBlock {

    public DimensionalBotanyPotBlock() {
        // In 1.21.1 the pot has to be of type hopper, so that we can also require a harvest item
        super(MapColor.COLOR_BLACK, PotType.HOPPER);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DimensionalBotanyPotBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, DimBotanyPots.DIMENSIONAL_BOTANY_POT_TILE.get(), DimensionalBotanyPotBlockEntity::tick);
    }

    // Places the block and uses the frequency of the items NBT
    // Overwrites the items NBT if the player is holding the item in the off-hand
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof DimensionalBotanyPotBlockEntity pot && placer != null) {

            // Read original from the item being placed
            Frequency originalFreq = DimBotanyPotHelper.getFrequency(stack);
            Frequency freqToSet = originalFreq;
            boolean copiedFromHand = false;

            ItemStack mainHandStack = placer.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandStack = placer.getItemInHand(InteractionHand.OFF_HAND);

            // Check if holding pot in main hand and valid frequency item in offhand
            if (mainHandStack.getItem() == this.asItem() && DimBotanyPotHelper.isValidFrequencyItem(offHandStack)) {
                freqToSet = DimBotanyPotHelper.getFrequency(offHandStack);
                copiedFromHand = true;
            }

            pot.setFrequency(freqToSet);

            boolean isDifferent = !originalFreq.toString().equals(freqToSet.toString());

            // Only send message if copied from hand and different from the block item's default
            if (copiedFromHand && isDifferent && !level.isClientSide && placer instanceof Player player) {
                sendFrequencyMessage(player, freqToSet);
            }
        }
    }

    // We need to override getDrops, instead of using Loot Tables, due to changes
    // in Vanilla behaviour which no longer makes Loot Tables a feasible method
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        ItemStack stack = new ItemStack(this);

        if (tile instanceof DimensionalBotanyPotBlockEntity pot) {
            DimBotanyPotHelper.writeFrequencyToStack(pot.getFrequency(), stack);
        }

        drops.add(stack);
        return drops;
    }

    // Right click interaction with other Dim Storage items
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Only continues if the player is holding a Dim Tablet/Chest/Tank/Pot
        if (DimBotanyPotHelper.isValidFrequencyItem(stack)) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof DimensionalBotanyPotBlockEntity pot) {
                Frequency currentFreq = pot.getFrequency();

                // If the pot has an owner, only THAT owner can change it
                if (currentFreq.hasOwner() && !currentFreq.canAccess(player)) {
                    if (!level.isClientSide) {
                        player.sendSystemMessage(Component.translatable("dimbotanypots.chat.not_owner").withStyle(ChatFormatting.RED));
                    }
                    return ItemInteractionResult.FAIL;
                }

                Frequency newFreq = DimBotanyPotHelper.getFrequency(stack);

                if (!currentFreq.toString().equals(newFreq.toString())) {
                    if (!level.isClientSide) {
                        pot.setFrequency(newFreq);
                        sendFrequencyMessage(player, newFreq);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    // Helper to send a formatted chat message when the frequency changes
    private void sendFrequencyMessage(Player player, Frequency freq) {
        // "Frequency Changed: channel (Owner)"
        var msg = Component.translatable("dimbotanypots.chat.frequency_changed");
        msg.append(Component.literal(": "));
        msg.append(Component.literal(String.valueOf(freq.channel())));

        if (freq.hasOwner()) {
            msg.append(Component.literal(" ("));
            msg.append(Component.literal(freq.getOwner()));
            msg.append(Component.literal(")"));
        }

        player.sendSystemMessage(msg);
    }

    // Adds frequency and owner information as well as a general small text
    private static final Component TOOLTIP_NORMAL = Component.translatable("dimbotanypots.tooltip.dimpot").withStyle(ChatFormatting.GRAY);

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(TOOLTIP_NORMAL);

        // Show stored frequency on the item
        if (stack.has(DimStorageComponents.FREQUENCY)) {
            Frequency freq = stack.get(DimStorageComponents.FREQUENCY);
            if (freq != null) {
                if (freq.hasOwner()) {
                    tooltip.add(Component.translatable("dimbotanypots.tooltip.owner").append(": " + freq.getOwner()).withStyle(ChatFormatting.DARK_RED));
                }
                tooltip.add(Component.translatable("dimbotanypots.tooltip.frequency").append(": " + freq.channel()));
            }
        }
    }
}
