package com.worthlore;

import org.bukkit.Material;

import java.util.Comparator;
import java.util.Map;

public enum SortMode {
    ALPHABETICAL,
    LOW_TO_HIGH,
    HIGH_TO_LOW;

    public Comparator<Map.Entry<Material, Double>> comparator() {
        Comparator<Map.Entry<Material, Double>> byName =
                Comparator.comparing((Map.Entry<Material, Double> e) -> e.getKey().name());
        switch (this) {
            case LOW_TO_HIGH:
                return Comparator.comparingDouble((Map.Entry<Material, Double> e) -> e.getValue())
                        .thenComparing(byName);
            case HIGH_TO_LOW:
                return Comparator.comparingDouble((Map.Entry<Material, Double> e) -> e.getValue())
                        .reversed()
                        .thenComparing(byName);
            default:
                return byName;
        }
    }
}
