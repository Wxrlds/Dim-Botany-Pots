package eu.wxrlds.dimbotanypots.recipe;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import edivad.dimstorage.api.Frequency;
import eu.wxrlds.dimbotanypots.DimBotanyPots;
import eu.wxrlds.dimbotanypots.util.DimBotanyPotHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import javax.annotation.Nonnull;

// Custom recipe class to copy the NBT (frequency) from an ingredient (Dim Chest/Tank/Tablet) to the result
public class DimBotanyPotRecipe extends ShapelessRecipe {

    public DimBotanyPotRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Nonnull
    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        // Get the standard result
        ItemStack result = super.assemble(inv, registries);

        // Loop through the crafting grid
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            // Find the Frequency Item (DimChest, Tablet, etc)
            if (DimBotanyPotHelper.isValidFrequencyItem(stack)) {

                // Read the frequency
                Frequency freq = DimBotanyPotHelper.getFrequency(stack);

                // Write it to the result
                DimBotanyPotHelper.writeFrequencyToStack(freq, result);

                // Stop after finding the first valid item
                break;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DimBotanyPots.DIMENSIONAL_BOTANY_POT_RECIPE.get();
    }

    // Serializer for the recipe type so that it can be loaded from JSON
    // Main logic from Ender Storage / covers1624
    public static class Serializer implements RecipeSerializer<DimBotanyPotRecipe> {
        private static final MapCodec<DimBotanyPotRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(DimBotanyPotRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(DimBotanyPotRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.getResultItem(RegistryAccess.EMPTY)),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                        .flatXmap(
                                ingredients -> {
                                    Ingredient[] aingredient = ingredients.toArray(Ingredient[]::new);
                                    if (aingredient.length == 0) {
                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                    } else if (aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()) {
                                        return DataResult.error(() -> "Too many ingredients for shapeless recipe");
                                    } else {
                                        return DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                    }
                                },
                                DataResult::success
                        ).forGetter(ShapelessRecipe::getIngredients)
        ).apply(builder, DimBotanyPotRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DimBotanyPotRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, DimBotanyPotRecipe::getGroup,
                CraftingBookCategory.STREAM_CODEC, DimBotanyPotRecipe::category,
                ItemStack.STREAM_CODEC, r -> r.getResultItem(RegistryAccess.EMPTY),
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), DimBotanyPotRecipe::getIngredients,
                (group, category, result, ingredients) -> new DimBotanyPotRecipe(group, category, result, NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)))
        );

        @Override
        public MapCodec<DimBotanyPotRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DimBotanyPotRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
