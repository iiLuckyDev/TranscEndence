package me.sfiguz7.transcendence;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;
import me.sfiguz7.transcendence.implementation.core.attributes.TERegistry;
import me.sfiguz7.transcendence.implementation.core.commands.TranscEndenceCommand;
import me.sfiguz7.transcendence.implementation.items.generators.QuirpScatterer;
import me.sfiguz7.transcendence.implementation.items.items.Daxi;
import me.sfiguz7.transcendence.implementation.items.items.Polarizer;
import me.sfiguz7.transcendence.implementation.items.items.Quirps;
import me.sfiguz7.transcendence.implementation.items.items.StabilizedItems;
import me.sfiguz7.transcendence.implementation.items.items.UnstableIngots;
import me.sfiguz7.transcendence.implementation.items.items.Zots;
import me.sfiguz7.transcendence.implementation.items.items.Zots_2;
import me.sfiguz7.transcendence.implementation.items.machines.QuirpAnnihilator;
import me.sfiguz7.transcendence.implementation.items.machines.QuirpCycler;
import me.sfiguz7.transcendence.implementation.items.machines.QuirpOscillator;
import me.sfiguz7.transcendence.implementation.items.machines.Stabilizer;
import me.sfiguz7.transcendence.implementation.items.machines.ZotOverloader;
import me.sfiguz7.transcendence.implementation.items.multiblocks.NanobotCrafter;
import me.sfiguz7.transcendence.implementation.listeners.DaxiAnimationArmorStandHeadListener;
import me.sfiguz7.transcendence.implementation.listeners.DaxiDeathListener;
import me.sfiguz7.transcendence.implementation.listeners.DaxiEffectModificationListener;
import me.sfiguz7.transcendence.implementation.listeners.TranscEndenceGuideListener;
import me.sfiguz7.transcendence.implementation.listeners.UnstableIngotDropListener;
import me.sfiguz7.transcendence.implementation.listeners.UnstableListener;
import me.sfiguz7.transcendence.implementation.tasks.RecurrentRefreshTask;
import me.sfiguz7.transcendence.implementation.tasks.StableTask;
import me.sfiguz7.transcendence.implementation.utils.SaveUtils;
import me.sfiguz7.transcendence.lists.TEItems;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

public class TranscEndence extends JavaPlugin implements SlimefunAddon {

    private static TranscEndence instance;
    private final TERegistry registry = new TERegistry();
    private int researchId = 7100;

    private int highchance;
    private int zotRequiredCharge;

    @Override
    public void onEnable() {

        instance = this;

        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        Config cfg = new Config(this);

        if (getConfig().getBoolean("options.auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "Sfiguz7/TranscEndence/master").start();
        }

        int bStatsId = 7329;
        new Metrics(this, bStatsId);


        // Commands
        getCommand("transcendence").setExecutor(new TranscEndenceCommand());
        // Listeners
        new UnstableListener(this);
        new DaxiDeathListener(this);
        new DaxiEffectModificationListener(this);
        new DaxiAnimationArmorStandHeadListener(this);
        new UnstableIngotDropListener(this);
        new TranscEndenceGuideListener(cfg.getBoolean("options.give-guide-on-first-join"));

        // Instability Update Task
        if (cfg.getBoolean("options.enable-instability-effects")) {
            getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                new StableTask(),
                0L,
                cfg.getInt("options.instability-update-interval") * 20L
            );
        }
        // Recurrent refresh task (only really needed for absorption)
        getServer().getScheduler().runTaskTimerAsynchronously(
            this,
            new RecurrentRefreshTask(),
            0L,
            15 * 20L
        );

        // Config fetching
        zotRequiredCharge = getConfig().getInt("options.zot-required-charge");
        highchance = getConfig().getInt("options.polarizer-affinity-chance");
        if (highchance < 26 || highchance > 50) {
            getLogger().log(Level.SEVERE, "Invalid config option: options.polarizer-affinity-chance");
            getLogger().log(Level.SEVERE, "Chance must be > 25 and < 51");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }




        /* Items */
        for (Quirps.Type type : Quirps.Type.values()) {
            new Quirps(type).register(this);
        }

        for (UnstableIngots.Type type : UnstableIngots.Type.values()) {
            new UnstableIngots(type).register(this);
        }

        for (Zots.Type type : Zots.Type.values()) {
            new Zots(type).register(this);
        }

        for (StabilizedItems.Type type : StabilizedItems.Type.values()) {
            new StabilizedItems(type).register(this);
        }

        /* More items moved below for aesthetic purposes */


        /* Machines pt. 1 */
        new QuirpScatterer().register(this);

        new NanobotCrafter().register(this);

        new QuirpOscillator().register(this);


        /* Items pt. 2 */
        for (Zots_2.Type type : Zots_2.Type.values()) {
            new Zots_2(type).register(this);
        }
        for (Daxi.Type type : Daxi.Type.values()) {
            new Daxi(type).register(this);
        }

        for (Polarizer.Type type : Polarizer.Type.values()) {
            new Polarizer(type).register(this);
        }

        /* Machines pt. 2 */
        new QuirpAnnihilator().register(this);

        new QuirpCycler().register(this);

        new Stabilizer().register(this);

        new ZotOverloader().register(this);

        new SlimefunItem(TEItems.transcendence, TEItems.TE_INFO, RecipeType.NULL, new ItemStack[0]
        ).register(this);

        registerResearches();

        // Initialise data if it exists
        SaveUtils.readData();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        SaveUtils.writeData();
        instance = null;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/Sfiguz7/TranscEndence/issues";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public int getHighchance() {
        return instance.highchance;
    }

    public int getZotRequiredCharge() {
        return instance.zotRequiredCharge;
    }

    public static TERegistry getRegistry() {
        return instance.registry;
    }

    public static TranscEndence getInstance() {
        return instance;
    }

    public static String getVersion() {
        return instance.getDescription().getVersion();
    }

    /*
     * Slimefun researches do not support prerequisite chains.
     * We therefore keep the progression through steadily rising XP costs
     * and granular per-item unlocks instead of broad grouped researches.
     */
    private void registerResearches() {
        registerResearch("nanobot_crafter", "Nanobot Crafting", 10,
            TEItems.NANOBOT_CRAFTER);
        registerResearch("quirp_oscillator", "Quirp Oscillator", 14,
            TEItems.QUIRP_OSCILLATOR);
        registerResearch("quirp_up", "Quirp Up", 16,
            TEItems.QUIRP_UP);
        registerResearch("quirp_down", "Quirp Down", 16,
            TEItems.QUIRP_DOWN);
        registerResearch("quirp_left", "Quirp Left", 16,
            TEItems.QUIRP_LEFT);
        registerResearch("quirp_right", "Quirp Right", 16,
            TEItems.QUIRP_RIGHT);
        registerResearch("quirp_condensate", "Quirp Condensate", 18,
            TEItems.QUIRP_CONDENSATE);
        registerResearch("polarizers", "Vertical Polarizer", 20,
            TEItems.VERTICAL_POLARIZER);
        registerResearch("horizontal_polarizer", "Horizontal Polarizer", 20,
            TEItems.HORIZONTAL_POLARIZER);
        registerResearch("unstable", "Unstable Ingot", 22,
            TEItems.UNSTABLE_INGOT);
        registerResearch("unstable_2", "Unstable Ingot 75%", 24,
            TEItems.UNSTABLE_INGOT_2);
        registerResearch("unstable_3", "Unstable Ingot 50%", 26,
            TEItems.UNSTABLE_INGOT_3);
        registerResearch("unstable_4", "Unstable Ingot 25%", 28,
            TEItems.UNSTABLE_INGOT_4);
        registerResearch("quirp_annihilator", "Quirp Annihilator", 30,
            TEItems.QUIRP_ANNIHILATOR);
        registerResearch("quirp_cycler", "Quirp Cycler", 32,
            TEItems.QUIRP_CYCLER);
        registerResearch("stabilizer", "Stabilizer", 34,
            TEItems.STABILIZER);
        registerResearch("stable", "Stable Ingot", 36,
            TEItems.STABLE_INGOT);
        registerResearch("stable_block", "Stable Block", 38,
            TEItems.STABLE_BLOCK);
        registerResearch("quirp_scatterer", "Quirp Scatterer", 40,
            TEItems.QUIRP_SCATTERER);
        registerResearch("zots", "Zot Up", 42,
            TEItems.ZOT_UP);
        registerResearch("zot_down", "Zot Down", 42,
            TEItems.ZOT_DOWN);
        registerResearch("zot_left", "Zot Left", 42,
            TEItems.ZOT_LEFT);
        registerResearch("zot_right", "Zot Right", 42,
            TEItems.ZOT_RIGHT);
        registerResearch("zot_overloader", "Zot Overloader", 46,
            TEItems.ZOT_OVERLOADER);
        registerResearch("zot_up_2", "Charged Zot Up", 48,
            TEItems.ZOT_UP_2);
        registerResearch("zot_down_2", "Charged Zot Down", 48,
            TEItems.ZOT_DOWN_2);
        registerResearch("zot_left_2", "Charged Zot Left", 48,
            TEItems.ZOT_LEFT_2);
        registerResearch("zot_right_2", "Charged Zot Right", 48,
            TEItems.ZOT_RIGHT_2);
        registerResearch("daxis", "Daxi Strength", 52,
            TEItems.DAXI_STRENGTH);
        registerResearch("daxi_absorption", "Daxi Absorption", 54,
            TEItems.DAXI_ABSORPTION);
        registerResearch("daxi_fortitude", "Daxi Fortitude", 56,
            TEItems.DAXI_FORTITUDE);
        registerResearch("daxi_saturation", "Daxi Saturation", 58,
            TEItems.DAXI_SATURATION);
        registerResearch("daxi_regeneration", "Daxi Regeneration", 60,
            TEItems.DAXI_REGENERATION);
    }

    private void registerResearch(String key, String name, int cost, SlimefunItemStack... items) {
        ItemStack[] stacks = Arrays.stream(items)
            .map(SlimefunItemStack::item)
            .toArray(ItemStack[]::new);

        new Research(new NamespacedKey(this, key), ++researchId, name, cost)
            .addItems(stacks)
            .register();
    }

}
