package me.sfiguz7.transcendence.implementation.listeners;

import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.sfiguz7.transcendence.TranscEndence;
import me.sfiguz7.transcendence.implementation.items.items.Daxi;
import me.sfiguz7.transcendence.lists.TEItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.event.EventPriority.LOWEST;

public class DaxiDeathListener implements Listener {

    public static final NamespacedKey TINKER_PROTECTION = new NamespacedKey(TranscEndence.getInstance(), "tinker");

    public DaxiDeathListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        /*
         * Tinker adds a PDC long to players if they have a full set of Daxi gear. This long is 5 seconds
         * worth of Daxi-loss-on-death protection.
         */
        if (PersistentDataAPI.getLong(p, TINKER_PROTECTION) >= System.currentTimeMillis()) {
            return;
        }

        UUID uuid = p.getUniqueId();
        Map<UUID, Set<Daxi.Type>> activePlayers = TranscEndence.getRegistry().getDaxiEffectPlayers();
        if (activePlayers.get(uuid) != null) {
            int howMany = activePlayers.get(uuid).size();
            ItemStack returnedBlocks = CustomItemStack.create(TEItems.STABLE_BLOCK.item(), 8 * howMany);

            if (usesGravesPlugin()) {
                // Graves-style plugins build the grave contents from death drops, not the temporary death-screen inventory.
                e.getDrops().add(returnedBlocks);
            } else {
                // Preserve the old fallback for servers that use keep-inventory without a grave plugin.
                HashMap<Integer, ItemStack> notFitting = p.getInventory().addItem(returnedBlocks);
                if (!notFitting.isEmpty()) {
                    p.getWorld().dropItem(p.getLocation(), notFitting.get(0));
                }
            }

            activePlayers.remove(uuid);
        }
    }

    private boolean usesGravesPlugin() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String name = plugin.getName();

            if (name.equalsIgnoreCase("GravesX") || name.equalsIgnoreCase("Graves")) {
                return true;
            }
        }

        return false;
    }

}
