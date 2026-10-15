package com.worthlore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorthGUI implements InventoryHolder {

    public static final int PER_PAGE = 45;
    public static final int PREV_SLOT = 45;
    public static final int SORT_ALPHA_SLOT = 46;
    public static final int SORT_LOW_SLOT = 47;
    public static final int SORT_HIGH_SLOT = 48;
    public static final int INFO_SLOT = 49;
    public static final int NEXT_SLOT = 53;

    private final WorthLore plugin;
    private final int page;
    private final int pages;
    private final SortMode sort;
    private final Inventory inventory;

    public WorthGUI(WorthLore plugin, int page, SortMode sort) {
        this.plugin = plugin;
        this.sort = sort;

        List<Map.Entry<Material, Double>> entries = new ArrayList<>(plugin.getPrices().entrySet());
        entries.sort(sort.comparator());

        this.pages = Math.max(1, (int) Math.ceil(entries.size() / (double) PER_PAGE));
        this.page = Math.max(0, Math.min(page, pages - 1));

        String title = plugin.color(plugin.getConfig().getString("gui.title", "&8Item Worth"))
                .replace("%page%", String.valueOf(this.page + 1))
                .replace("%pages%", String.valueOf(pages));
        this.inventory = Bukkit.createInventory(this, 54, title);

        int start = this.page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, entries.size());
        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, plugin.withWorth(new ItemStack(entries.get(i).getKey())));
        }

        // Bottom row
        ItemStack filler = button(Material.GRAY_STAINED_GLASS_PANE, " ", null, false);
        for (int slot = 45; slot < 54; slot++) inventory.setItem(slot, filler);

        if (this.page > 0) {
            inventory.setItem(PREV_SLOT, button(Material.ARROW,
                    plugin.getConfig().getString("gui.previous-page", "&a<< Previous Page"), null, false));
        }
        if (this.page < pages - 1) {
            inventory.setItem(NEXT_SLOT, button(Material.ARROW,
                    plugin.getConfig().getString("gui.next-page", "&aNext Page >>"), null, false));
        }

        String pageName = plugin.getConfig().getString("gui.page-item", "&ePage %page%/%pages%")
                .replace("%page%", String.valueOf(this.page + 1))
                .replace("%pages%", String.valueOf(pages));
        inventory.setItem(INFO_SLOT, button(Material.PAPER, pageName, null, false));

        inventory.setItem(SORT_ALPHA_SLOT, sortButton(Material.NAME_TAG, SortMode.ALPHABETICAL,
                plugin.getConfig().getString("gui.sort-alphabetical", "&eSort: Alphabetical (A-Z)")));
        inventory.setItem(SORT_LOW_SLOT, sortButton(Material.COAL, SortMode.LOW_TO_HIGH,
                plugin.getConfig().getString("gui.sort-low-high", "&eSort: Lowest to Highest")));
        inventory.setItem(SORT_HIGH_SLOT, sortButton(Material.EMERALD, SortMode.HIGH_TO_LOW,
                plugin.getConfig().getString("gui.sort-high-low", "&eSort: Highest to Lowest")));
    }

    private ItemStack sortButton(Material mat, SortMode mode, String name) {
        boolean selected = this.sort == mode;
        List<String> lore = new ArrayList<>();
        lore.add(plugin.color(selected
                ? plugin.getConfig().getString("gui.sort-selected", "&aCurrently selected")
                : plugin.getConfig().getString("gui.sort-click", "&7Click to sort this way")));
        return button(mat, name, lore, selected);
    }

    private ItemStack button(Material mat, String name, List<String> lore, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.color(name));
        if (lore != null) meta.setLore(lore);
        if (glow) meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        plugin.markNoWorth(meta); // never add worth lore to GUI buttons
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public SortMode getSort() {
        return sort;
    }

    public WorthLore getPlugin() {
        return plugin;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
