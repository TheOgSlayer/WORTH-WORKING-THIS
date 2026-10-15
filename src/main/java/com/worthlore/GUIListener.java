package com.worthlore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof WorthGUI gui)) return;

        // Nothing can be taken, moved or shift-clicked while the GUI is open
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;

        WorthLore plugin = gui.getPlugin();
        int slot = e.getSlot();

        if (slot == WorthGUI.PREV_SLOT && gui.getPage() > 0) {
            new WorthGUI(plugin, gui.getPage() - 1, gui.getSort()).open(player);
        } else if (slot == WorthGUI.NEXT_SLOT && gui.getPage() < gui.getPages() - 1) {
            new WorthGUI(plugin, gui.getPage() + 1, gui.getSort()).open(player);
        } else if (slot == WorthGUI.SORT_ALPHA_SLOT) {
            changeSort(plugin, player, SortMode.ALPHABETICAL);
        } else if (slot == WorthGUI.SORT_LOW_SLOT) {
            changeSort(plugin, player, SortMode.LOW_TO_HIGH);
        } else if (slot == WorthGUI.SORT_HIGH_SLOT) {
            changeSort(plugin, player, SortMode.HIGH_TO_LOW);
        }
    }

    private void changeSort(WorthLore plugin, Player player, SortMode mode) {
        plugin.setSort(player.getUniqueId(), mode);
        new WorthGUI(plugin, 0, mode).open(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof WorthGUI) {
            e.setCancelled(true);
        }
    }
}
