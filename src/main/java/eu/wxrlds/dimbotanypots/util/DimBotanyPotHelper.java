package eu.wxrlds.dimbotanypots.util;


import edivad.dimstorage.api.Frequency;
import edivad.dimstorage.items.DimTablet;
import edivad.dimstorage.items.ItemDimBase;
import edivad.dimstorage.items.components.DimStorageComponents;
import edivad.dimstorage.items.components.FrequencyTabletComponent;
import eu.wxrlds.dimbotanypots.block.DimensionalBotanyPotBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;


public class DimBotanyPotHelper {
    // Checks if the item is a valid source of Frequency data.
    // Includes: Ender Chest, Ender Tank, Ender Pouch, and
    // Ender Botany Pot with all addon mods, if they are of type EnderBotanyPotBlock
    public static boolean isValidFrequencyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // DimTablet
        if (stack.getItem() instanceof DimTablet) {
            FrequencyTabletComponent comp = stack.get(DimStorageComponents.FREQUENCY_TABLET);
            return comp != null && comp.bound();
        }

        // DimChest / DimTank
        if (stack.getItem() instanceof ItemDimBase) {
            return stack.has(DimStorageComponents.FREQUENCY);
        }

        //  DimBotanyPot logic
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DimensionalBotanyPotBlock) {
            return stack.has(DimStorageComponents.FREQUENCY);
        }

        return false;
    }


    // Extracts the Frequency from the stack. Returns null if invalid or not present.
    public static Frequency getFrequency(ItemStack stack) {
        if (!isValidFrequencyItem(stack)) return new Frequency();

        if (stack.getItem() instanceof DimTablet) {
            FrequencyTabletComponent comp = stack.get(DimStorageComponents.FREQUENCY_TABLET);
            return comp != null ? comp.frequency() : new Frequency();
        }

        // For Chests, Tanks, and Pots, the frequency is stored in the standard FREQUENCY component
        Frequency freq = stack.get(DimStorageComponents.FREQUENCY);
        return freq != null ? freq : new Frequency();
    }

    public static void writeFrequencyToStack(Frequency frequency, ItemStack stack) {
        if (frequency != null) {
            stack.set(DimStorageComponents.FREQUENCY, frequency);
        }
    }
}
