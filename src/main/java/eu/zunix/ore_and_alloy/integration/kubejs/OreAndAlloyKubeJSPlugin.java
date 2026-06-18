package eu.zunix.ore_and_alloy.integration.kubejs;

import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;

public final class OreAndAlloyKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow(OreAndAlloyKubeJS.class);
        filter.allow(OreAndAlloyRegistryEvent.class);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("OreAndAlloy", OreAndAlloyKubeJS.class);
        for (MetalMaterial material : MetalMaterial.values()) {
            String canonical = material.materialName();
            bindings.add(canonical, canonical);

            String preferred = MaterialItemOrder.preferredItemMaterialToken(canonical);
            if (!preferred.equals(canonical)) {
                bindings.add(preferred, preferred);
            }
        }
        bindings.add("chrome", "chrome");
    }
}
