package me.sfiguz7.transcendence.implementation.items.machines;

import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.sfiguz7.transcendence.lists.TEItems;
import me.sfiguz7.transcendence.lists.TERecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class QuirpAnnihilator extends AContainer implements RecipeDisplayItem {

    public QuirpAnnihilator() {
        super(TEItems.transcendence, TEItems.QUIRP_ANNIHILATOR, TERecipeType.NANOBOT_CRAFTER,
            new ItemStack[] {SlimefunItems.ADVANCED_CIRCUIT_BOARD.item(), TEItems.QUIRP_UP.item(),
                SlimefunItems.ADVANCED_CIRCUIT_BOARD.item(),
                TEItems.QUIRP_LEFT.item(), SlimefunItems.HEATED_PRESSURE_CHAMBER_2.item(), TEItems.QUIRP_RIGHT.item(),
                SlimefunItems.REINFORCED_PLATE.item(), TEItems.QUIRP_DOWN.item(), SlimefunItems.REINFORCED_PLATE.item()});
    }

    @Override
    protected void registerDefaultRecipes() {

        registerRecipe(8, new ItemStack[] {TEItems.QUIRP_UP.item(), TEItems.QUIRP_DOWN.item()},
            new ItemStack[] {TEItems.QUIRP_CONDENSATE.item()});
        registerRecipe(8, new ItemStack[] {TEItems.QUIRP_LEFT.item(), TEItems.QUIRP_RIGHT.item()},
            new ItemStack[] {TEItems.QUIRP_CONDENSATE.item()});

    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.SHIELD);
    }

    @Override
    public String getInventoryTitle() {
        return "&cQuirp Annihilator";
    }

    @Override
    public String getMachineIdentifier() {
        return "QUIRP_ANNIHILATOR";
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


