package me.sfiguz7.transcendence.implementation.listeners;

import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.sfiguz7.transcendence.TranscEndence;
import me.sfiguz7.transcendence.implementation.items.items.Daxi;
import me.sfiguz7.transcendence.lists.TEItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.event.EventPriority.HIGHEST;

public class DaxiDeathListener implements Listener {

    public static final NamespacedKey TINKER_PROTECTION = new NamespacedKey(TranscEndence.getInstance(), "tinker");
    private final JavaPlugin plugin;
    private final Map<UUID, ItemStack> pendingGraveRefunds = new HashMap<>();

    public DaxiDeathListener(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTask(plugin, this::registerGravesXCreateEventListeners);
    }

    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        /*
         * Tinker adds a PDC long to players if they have a full set of Daxi gear. This long is 5 seconds
         * worth of Daxi-loss-on-death protection.
         */
        if (PersistentDataAPI.getLong(p, TINKER_PROTECTION) >= System.currentTimeMillis()) {
            return;
        }

        Set<Daxi.Type> activeTypes = Daxi.getActiveTypes(p);
        if (!activeTypes.isEmpty()) {
            ItemStack returnedBlocks = createRefund(activeTypes.size());
            UUID uuid = p.getUniqueId();
            pendingGraveRefunds.put(uuid, returnedBlocks);

            // Vanilla-style death handlers still consume the regular death drops.
            e.getDrops().add(returnedBlocks);
            plugin.getLogger().info("Queued Daxi death refund of " + returnedBlocks.getAmount()
                + " stable blocks for " + p.getName() + ".");

            Bukkit.getScheduler().runTask(plugin, () -> {
                pendingGraveRefunds.remove(uuid);
                Daxi.clearActiveTypes(p);
            });
        }
    }

    private void registerGravesXCreateEventListeners() {
        Plugin gravesX = Bukkit.getPluginManager().getPlugin("GravesX");
        if (gravesX == null) {
            gravesX = Bukkit.getPluginManager().getPlugin("Graves");
        }

        if (gravesX == null) {
            return;
        }

        registerGravesXCreateEventListener(gravesX, "dev.cwhead.GravesX.event.GraveCreateEvent");
        registerGravesXCreateEventListener(gravesX, "com.ranull.graves.event.GraveCreateEvent");
    }

    @SuppressWarnings("unchecked")
    private void registerGravesXCreateEventListener(Plugin gravesX, String eventClassName) {
        try {
            Class<?> eventClass = gravesX.getClass().getClassLoader().loadClass(eventClassName);
            if (!Event.class.isAssignableFrom(eventClass)) {
                return;
            }

            EventExecutor executor = (listener, event) -> addRefundToGravesXEvent(event);
            Bukkit.getPluginManager().registerEvent((Class<? extends Event>) eventClass, this, EventPriority.HIGHEST,
                executor, plugin, true);
        } catch (ClassNotFoundException ignored) {
            // GravesX has renamed event packages before; missing compatibility events are safe to skip.
        }
    }

    private void addRefundToGravesXEvent(Event event) throws EventException {
        try {
            Player player = getGravesXEventPlayer(event);
            if (player == null) {
                return;
            }

            UUID uuid = player.getUniqueId();
            ItemStack refund = pendingGraveRefunds.get(uuid);
            if (refund == null) {
                Set<Daxi.Type> activeTypes = Daxi.getActiveTypes(player);
                if (activeTypes.isEmpty()) {
                    return;
                }

                refund = createRefund(activeTypes.size());
                pendingGraveRefunds.put(uuid, refund);
            }

            Collection<ItemStack> graveItems = getGravesXEventItems(event);
            if (graveItems != null && !containsSimilarStack(graveItems, refund)) {
                Collection<ItemStack> mutableGraveItems = new ArrayList<>(graveItems);
                mutableGraveItems.add(refund.clone());
                setGravesXEventItems(event, mutableGraveItems);
                plugin.getLogger().info("Added Daxi death refund of " + refund.getAmount()
                    + " stable blocks directly to " + player.getName() + "'s GravesX grave.");
            }
        } catch (ReflectiveOperationException e) {
            throw new EventException(e);
        }
    }

    private Player getGravesXEventPlayer(Event event) throws ReflectiveOperationException {
        Method getPlayer = event.getClass().getMethod("getPlayer");
        Object player = getPlayer.invoke(event);

        if (player instanceof Player) {
            return (Player) player;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Collection<ItemStack> getGravesXEventItems(Event event) throws ReflectiveOperationException {
        Method getGraveItemStackList = event.getClass().getMethod("getGraveItemStackList");

        try {
            Object items = getGraveItemStackList.invoke(event);
            if (items instanceof Collection<?>) {
                return (Collection<ItemStack>) items;
            }
        } catch (InvocationTargetException e) {
            throw new ReflectiveOperationException(e.getCause());
        }

        return null;
    }

    private void setGravesXEventItems(Event event, Collection<ItemStack> items) throws ReflectiveOperationException {
        Method setGraveItemStackList = event.getClass().getMethod("setGraveItemStackList", Collection.class);

        try {
            setGraveItemStackList.invoke(event, items);
        } catch (InvocationTargetException e) {
            throw new ReflectiveOperationException(e.getCause());
        }
    }

    private boolean containsSimilarStack(Collection<ItemStack> items, ItemStack refund) {
        for (ItemStack item : items) {
            if (item != null && item.isSimilar(refund) && item.getAmount() >= refund.getAmount()) {
                return true;
            }
        }

        return false;
    }

    private ItemStack createRefund(int daxiCount) {
        return CustomItemStack.create(TEItems.STABLE_BLOCK.item(), 8 * daxiCount);
    }

}
