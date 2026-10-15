package com.worthlore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class WorthLore extends JavaPlugin {

    private final Map<Material, Double> prices = new TreeMap<>(Comparator.comparing(Material::name));
    private final Map<UUID, SortMode> sortModes = new HashMap<>();

    /**
     * Tag stored on items that already went through us. Value = index in the lore list where our
     * line was added (or -1 if no line was added, e.g. GUI buttons).
     */
    private NamespacedKey tagKey;

    @Override
    public void onEnable() {
        tagKey = new NamespacedKey(this, "worth_lore");

        saveDefaultConfig();
        if (!new File(getDataFolder(), "prices.yml").exists()) {
            saveResource("prices.yml", false);
        }
        loadPrices();

        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        new LorePacketListener(this).register();
        getLogger().info("WorthLore enabled with " + prices.size() + " prices.");
    }

    public void loadPrices() {
        prices.clear();
        File file = new File(getDataFolder(), "prices.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        for (String key : yml.getKeys(false)) {
            Material mat = Material.matchMaterial(key.trim());
            if (mat == null || !mat.isItem()) {
                getLogger().warning("prices.yml: '" + key + "' is not a valid item, skipping.");
                continue;
            }
            if (!yml.isInt(key) && !yml.isDouble(key) && !yml.isLong(key)) {
                getLogger().warning("prices.yml: value for '" + key + "' is not a number, skipping.");
                continue;
            }
            prices.put(mat, yml.getDouble(key));
        }
    }

    public Map<Material, Double> getPrices() {
        return Collections.unmodifiableMap(prices);
    }

    public SortMode getSort(UUID id) {
        return sortModes.getOrDefault(id, SortMode.ALPHABETICAL);
    }

    public void setSort(UUID id, SortMode mode) {
        sortModes.put(id, mode);
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String msg(String path) {
        return color(getConfig().getString("messages." + path, path));
    }

    public static String fmt(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) return String.format("%,d", (long) v);
        return String.format("%,.2f", v);
    }

    /** Tags an item (GUI buttons etc.) so no worth lore is ever added to it. */
    public void markNoWorth(ItemMeta meta) {
        meta.getPersistentDataContainer().set(tagKey, PersistentDataType.INTEGER, -1);
    }

    /** Returns a copy of the item with the worth line added (or the same item if it has no price). */
    public ItemStack withWorth(ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        Double worth = prices.get(item.getType());
        if (worth == null) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(tagKey, PersistentDataType.INTEGER)) return item; // already handled

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        int amount = Math.max(1, item.getAmount());
        String format = amount > 1
                ? getConfig().getString("stack-lore-format", "&7Worth: &a$%worth% &8(&7x%amount% = &a$%total%&8)")
                : getConfig().getString("lore-format", "&7Worth: &a$%worth%");

        String line = format
                .replace("%worth%", fmt(worth))
                .replace("%total%", fmt(worth * amount))
                .replace("%amount%", String.valueOf(amount));

        int index = lore.size();
        lore.add(color(line));
        meta.setLore(lore);
        pdc.set(tagKey, PersistentDataType.INTEGER, index);

        ItemStack copy = item.clone();
        copy.setItemMeta(meta);
        return copy;
    }

    /** Removes our lore line + tag (used when creative clients send items back to the server). */
    public ItemStack stripWorth(ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer index = pdc.get(tagKey, PersistentDataType.INTEGER);
        if (index == null) return item;

        if (index >= 0 && meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            while (lore.size() > index) lore.remove(lore.size() - 1);
            meta.setLore(lore.isEmpty() ? null : lore);
        }
        pdc.remove(tagKey);

        ItemStack copy = item.clone();
        copy.setItemMeta(meta);
        return copy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("worth.reload")) {
                sender.sendMessage(msg("no-permission"));
                return true;
            }
            reloadConfig();
            loadPrices();
            sender.sendMessage(msg("reloaded").replace("%count%", String.valueOf(prices.size())));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg("players-only"));
            return true;
        }
        if (!player.hasPermission("worth.use")) {
            player.sendMessage(msg("no-permission"));
            return true;
        }
        if (prices.isEmpty()) {
            player.sendMessage(msg("no-prices"));
            return true;
        }
        new WorthGUI(this, 0, getSort(player.getUniqueId())).open(player);
        return true;
    }
}
