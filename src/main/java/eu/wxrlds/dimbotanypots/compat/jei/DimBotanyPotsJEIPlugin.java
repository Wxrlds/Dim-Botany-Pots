package eu.wxrlds.dimbotanypots.compat.jei;


import eu.wxrlds.dimbotanypots.DimBotanyPots;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class DimBotanyPotsJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(DimBotanyPots.MOD_ID, "jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Add the description info
        registration.addIngredientInfo(
                new ItemStack(DimBotanyPots.DIMENSIONAL_BOTANY_POT.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("dimbotanypots.jei.description")
        );
    }
}
