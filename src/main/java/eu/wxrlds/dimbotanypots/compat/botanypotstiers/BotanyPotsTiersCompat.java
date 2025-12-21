package eu.wxrlds.dimbotanypots.compat.botanypotstiers;

import com.ultramega.botanypotstiers.common.impl.PotTier;
import eu.wxrlds.dimbotanypots.DimBotanyPots;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.stream.Stream;

public class BotanyPotsTiersCompat {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, DimBotanyPots.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, DimBotanyPots.MOD_ID);

    public static final DeferredHolder<Block, TieredDimensionalBotanyPotBlock> ELITE_DIM_BOTANY_POT = BLOCKS.register("elite_dimensional_botany_pot", () -> new TieredDimensionalBotanyPotBlock(PotTier.ELITE));
    public static final DeferredHolder<Block, TieredDimensionalBotanyPotBlock> ULTRA_DIM_BOTANY_POT = BLOCKS.register("ultra_dimensional_botany_pot", () -> new TieredDimensionalBotanyPotBlock(PotTier.ULTRA));
    public static final DeferredHolder<Block, TieredDimensionalBotanyPotBlock> MEGA_DIM_BOTANY_POT = BLOCKS.register("mega_dimensional_botany_pot", () -> new TieredDimensionalBotanyPotBlock(PotTier.MEGA));

    public static final DeferredHolder<Item, BlockItem> ELITE_DIM_BOTANY_POT_ITEM = ITEMS.register("elite_dimensional_botany_pot", () -> new BlockItem(ELITE_DIM_BOTANY_POT.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ULTRA_DIM_BOTANY_POT_ITEM = ITEMS.register("ultra_dimensional_botany_pot", () -> new BlockItem(ULTRA_DIM_BOTANY_POT.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> MEGA_DIM_BOTANY_POT_ITEM = ITEMS.register("mega_dimensional_botany_pot", () -> new BlockItem(MEGA_DIM_BOTANY_POT.get(), new Item.Properties()));

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    public static List<Block> getTieredPots() {
        return List.of(ELITE_DIM_BOTANY_POT.get(), ULTRA_DIM_BOTANY_POT.get(), MEGA_DIM_BOTANY_POT.get());
    }

    public static Stream<Item> getTabItems() {
        return Stream.of(ELITE_DIM_BOTANY_POT_ITEM.get(), ULTRA_DIM_BOTANY_POT_ITEM.get(), MEGA_DIM_BOTANY_POT_ITEM.get());
    }


}
