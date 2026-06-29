package eu.zunix.ore_and_alloy.core;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class StandaloneMaterialItems {
    private static final List<Entry> ENTRIES = List.of(
            new Entry("crude_rubber", StandaloneItemType.RUBBER),
            new Entry("rubber", StandaloneItemType.RUBBER),
            new Entry("rubber_sheet", StandaloneItemType.RUBBER)
    );

    private StandaloneMaterialItems() {}

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static List<String> itemIds() {
        return ENTRIES.stream().map(Entry::id).toList();
    }

    public static Optional<Entry> byId(String id) {
        String normalized = id == null ? "" : id.toLowerCase(Locale.ROOT);
        return ENTRIES.stream().filter(entry -> entry.id().equals(normalized)).findFirst();
    }

    public record Entry(String id, StandaloneItemType type) {}
}
