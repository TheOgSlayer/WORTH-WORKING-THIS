package com.worthlore;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the worth lore only in packets sent to the client, so the real items are never modified.
 */
public class LorePacketListener {

    // NOTE: named "worth" (not "plugin") because PacketAdapter has its own protected "plugin" field
    private final WorthLore worth;

    public LorePacketListener(WorthLore worth) {
        this.worth = worth;
    }

    public void register() {
        var manager = ProtocolLibrary.getProtocolManager();
        final WorthLore wl = this.worth;

        // Server -> client: add lore
        manager.addPacketListener(new PacketAdapter(wl, ListenerPriority.HIGH,
                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket().deepClone();
                event.setPacket(packet);

                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    packet.getItemModifier().modify(0, item -> wl.withWorth(item));
                } else {
                    packet.getItemListModifier().modify(0, items -> {
                        List<ItemStack> out = new ArrayList<>(items.size());
                        for (ItemStack i : items) out.add(wl.withWorth(i));
                        return out;
                    });
                    try {
                        // item held on the cursor (exists on newer versions)
                        packet.getItemModifier().modify(0, item -> wl.withWorth(item));
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        // Client -> server: creative mode sends full items back, strip our lore so it's never saved
        manager.addPacketListener(new PacketAdapter(wl, ListenerPriority.LOW,
                PacketType.Play.Client.SET_CREATIVE_SLOT) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                event.getPacket().getItemModifier().modify(0, item -> wl.stripWorth(item));
            }
        });
    }
}
