package eu.zunix.ore_and_alloy.core;

public enum StandaloneItemType {
    RUBBER("rubber_materials");

    private final String tagBucket;

    StandaloneItemType(String tagBucket) {
        this.tagBucket = tagBucket;
    }

    public String tagBucket() {
        return tagBucket;
    }
}
