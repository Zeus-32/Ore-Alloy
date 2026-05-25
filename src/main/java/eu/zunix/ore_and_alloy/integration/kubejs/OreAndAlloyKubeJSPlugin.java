package eu.zunix.ore_and_alloy.integration.kubejs;

import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import eu.zunix.ore_and_alloy.core.GemMaterial;
import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MetalMaterial;


public final class OreAndAlloyKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow(OreAndAlloyKubeJS.class);
        filter.allow(MetalMaterial.class);
        filter.allow(MaterialForm.class);
        filter.allow(GemMaterial.class);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("OreAndAlloy", OreAndAlloyKubeJS.class);
        bindings.add("OAMetals", MetalMaterial.class);
        bindings.add("OAForms", MaterialForm.class);
        bindings.add("OAGems", GemMaterial.class);
    }
}
