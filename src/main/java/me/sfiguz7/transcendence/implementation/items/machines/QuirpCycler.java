package me.sfiguz7.transcendence.implementation.items.machines;

import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.sfiguz7.transcendence.lists.TEItems;
import me.sfiguz7.transcendence.lists.TERecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class QuirpCycler extends AContainer implements RecipeDisplayItem {

    public QuirpCycler() {
        super(TEItems.transcendence, TEItems.QUIRP_CYCLER, TERecipeType.NANOBOT_CRAFTER,
            new ItemStack[] {TEItems.QUIRP_CONDENSATE.item(), TEItems.QUIRP_UP.item(), TEItems.QUIRP_CONDENSATE.item(),
                TEItems.QUIRP_LEFT.item(), TEItems.QUIRP_OSCILLATOR.item(), TEItems.QUIRP_RIGHT.item(),
                TEItems.QUIRP_CONDENSATE.item(), TEItems.QUIRP_DOWN.item(), TEItems.QUIRP_CONDENSATE.item()});
    }

    @Override
    protected void registerDefaultRecipes() {

        registerRecipe(8, new ItemStack[] {TEItems.QUIRP_UP.item()},
            new ItemStack[] {TEItems.QUIRP_RIGHT.item()});
        registerRecipe(8, new ItemStack[] {TEItems.QUIRP_RIGHT.item()},
            new ItemStack[] {TEItems.QUIRP_DOWN.item()});
        registerRecipe(8, new ItemStack[] {TEItems.QUIRP_DOWN.item()},
            new ItemStack[] {TEItems.QUIRP_LEFT.item()});
        registerRecipe(8, new ItemStack[] {TEItems.QUIRP_LEFT.item()},
            new ItemStack[] {TEItems.QUIRP_UP.item()});

    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.SHIELD);
    }

    @Override
    public String getInventoryTitle() {
        return "&cQuirp Cycler";
    }

    @Override
    public String getMachineIdentifier() {
        return "QUIRP_CYCLER";
    }

    @Override
    public int getCapacity() {
        return 1024;
    }

    @Override
    public int getEnergyConsumption() {
        return 256;
    }

    @Override
    public int getSpeed() {
        return 1;
    }

}
