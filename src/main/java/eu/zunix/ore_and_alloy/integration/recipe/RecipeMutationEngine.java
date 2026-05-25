package eu.zunix.ore_and_alloy.integration.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RecipeMutationEngine {
    private static final String CREATE_PROCESSING_OUTPUT = "com.simibubi.create.content.processing.recipe.ProcessingOutput";
    private static final Map<Class<?>, List<Field>> MUTABLE_RECIPE_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Optional<CreateProcessingOutputAccessor>> CREATE_ACCESSORS = new ConcurrentHashMap<>();

    private RecipeMutationEngine() {}

    public static int rewriteRecipeStacks(Recipe<?> recipe, Map<Item, Item> aliasToCanonical) {
        int rewrites = 0;
        for (Field field : mutableRecipeFields(recipe.getClass())) {
            try {
                Object value = field.get(recipe);
                rewrites += rewriteFieldValue(recipe, field, value, aliasToCanonical);
            } catch (IllegalAccessException ignored) {
            }
        }
        return rewrites;
    }

    private static int rewriteFieldValue(Object owner, Field field, Object value, Map<Item, Item> aliasToCanonical) throws IllegalAccessException {
        if (value instanceof ItemStack stack) {
            ItemStack unified = unifyStack(stack, aliasToCanonical);
            if (unified != stack) {
                field.set(owner, unified);
                return 1;
            }
            return 0;
        }

        if (value instanceof Ingredient ingredient) {
            Ingredient unified = unifyIngredient(ingredient, aliasToCanonical);
            if (unified != ingredient) {
                field.set(owner, unified);
                return 1;
            }
            return 0;
        }

        if (value instanceof ItemStack[] stacks) {
            int changed = 0;
            for (int i = 0; i < stacks.length; i++) {
                ItemStack unified = unifyStack(stacks[i], aliasToCanonical);
                if (unified != stacks[i]) {
                    stacks[i] = unified;
                    changed++;
                }
            }
            return changed;
        }

        if (value instanceof Ingredient[] ingredients) {
            int changed = 0;
            for (int i = 0; i < ingredients.length; i++) {
                Ingredient unified = unifyIngredient(ingredients[i], aliasToCanonical);
                if (unified != ingredients[i]) {
                    ingredients[i] = unified;
                    changed++;
                }
            }
            return changed;
        }

        if (value instanceof ShapedRecipePattern pattern) {
            return rewriteIngredientList(pattern.ingredients(), aliasToCanonical);
        }

        if (value instanceof List<?> list) {
            int changed = 0;
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                @SuppressWarnings("unchecked")
                List<Object> mutable = (List<Object>) list;

                if (element instanceof ItemStack stack) {
                    ItemStack unified = unifyStack(stack, aliasToCanonical);
                    if (unified == stack) continue;
                    try {
                        mutable.set(i, unified);
                        changed++;
                    } catch (UnsupportedOperationException ignored) {
                    }
                    continue;
                }

                if (element instanceof Ingredient ingredient) {
                    Ingredient unified = unifyIngredient(ingredient, aliasToCanonical);
                    if (unified == ingredient) continue;
                    try {
                        mutable.set(i, unified);
                        changed++;
                    } catch (UnsupportedOperationException ignored) {
                    }
                    continue;
                }

                Object unifiedElement = unifyKnownOutputObject(element, aliasToCanonical);
                if (unifiedElement == element) continue;
                try {
                    mutable.set(i, unifiedElement);
                    changed++;
                } catch (UnsupportedOperationException ignored) {
                }
            }
            return changed;
        }

        Object unifiedWrapper = unifyKnownOutputObject(value, aliasToCanonical);
        if (unifiedWrapper != value) {
            field.set(owner, unifiedWrapper);
            return 1;
        }

        return 0;
    }

    private static ItemStack unifyStack(ItemStack stack, Map<Item, Item> aliasToCanonical) {
        if (stack == null || stack.isEmpty()) return stack;

        Item canonical = aliasToCanonical.get(stack.getItem());
        if (canonical == null || canonical == stack.getItem()) return stack;

        return stack.transmuteCopy(canonical, stack.getCount());
    }

    private static Ingredient unifyIngredient(Ingredient ingredient, Map<Item, Item> aliasToCanonical) {
        if (ingredient == null || ingredient.isEmpty() || ingredient.isCustom()) return ingredient;

        ItemStack[] original = ingredient.getItems();
        if (original.length == 0) return ingredient;

        ItemStack[] unified = new ItemStack[original.length];
        boolean changed = false;
        for (int i = 0; i < original.length; i++) {
            ItemStack stack = original[i];
            ItemStack mapped = unifyStack(stack, aliasToCanonical);
            unified[i] = mapped;
            if (mapped != stack) changed = true;
        }

        if (!changed) return ingredient;

        Ingredient rebuilt = Ingredient.of(Arrays.stream(unified).map(ItemStack::copy));
        return rebuilt.isEmpty() ? ingredient : rebuilt;
    }

    private static int rewriteIngredientList(List<Ingredient> list, Map<Item, Item> aliasToCanonical) {
        int changed = 0;
        for (int i = 0; i < list.size(); i++) {
            Ingredient ingredient = list.get(i);
            Ingredient unified = unifyIngredient(ingredient, aliasToCanonical);
            if (unified == ingredient) continue;

            try {
                list.set(i, unified);
                changed++;
            } catch (UnsupportedOperationException ignored) {
            }
        }
        return changed;
    }

    private static Object unifyKnownOutputObject(Object value, Map<Item, Item> aliasToCanonical) {
        if (value == null) return null;

        Optional<CreateProcessingOutputAccessor> accessorOpt = CREATE_ACCESSORS.computeIfAbsent(
                value.getClass(), RecipeMutationEngine::resolveCreateAccessor);
        if (accessorOpt.isEmpty()) return value;

        CreateProcessingOutputAccessor accessor = accessorOpt.get();
        try {
            ItemStack original = accessor.getStack(value);
            ItemStack unified = unifyStack(original, aliasToCanonical);
            if (unified == original) return value;
            return accessor.newInstance(unified, accessor.getChance(value));
        } catch (ReflectiveOperationException ignored) {
            return value;
        }
    }

    private static Optional<CreateProcessingOutputAccessor> resolveCreateAccessor(Class<?> type) {
        if (!CREATE_PROCESSING_OUTPUT.equals(type.getName())) return Optional.empty();
        try {
            Method getStack = type.getMethod("getStack");
            Method getChance = type.getMethod("getChance");
            Constructor<?> ctor = type.getConstructor(ItemStack.class, float.class);
            return Optional.of(new CreateProcessingOutputAccessor(getStack, getChance, ctor));
        } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
        }
    }

    private static List<Field> mutableRecipeFields(Class<?> recipeClass) {
        return MUTABLE_RECIPE_FIELDS.computeIfAbsent(recipeClass, clazz -> {
            List<Field> fields = new ArrayList<>();
            Class<?> cursor = clazz;

            while (cursor != null && cursor != Object.class) {
                for (Field field : cursor.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) continue;

                    Class<?> type = field.getType();
                    boolean candidate = type == ItemStack.class
                            || type == Ingredient.class
                            || type == ShapedRecipePattern.class
                            || type == ItemStack[].class
                            || type == Ingredient[].class
                            || List.class.isAssignableFrom(type);
                    if (!candidate) continue;

                    if (field.trySetAccessible()) {
                        fields.add(field);
                    }
                }
                cursor = cursor.getSuperclass();
            }

            return List.copyOf(fields);
        });
    }

    private record CreateProcessingOutputAccessor(Method getStack, Method getChance, Constructor<?> ctor) {
        ItemStack getStack(Object target) throws ReflectiveOperationException {
            return (ItemStack) getStack.invoke(target);
        }

        float getChance(Object target) throws ReflectiveOperationException {
            Object value = getChance.invoke(target);
            if (value instanceof Number number) return number.floatValue();
            return 1.0F;
        }

        Object newInstance(ItemStack stack, float chance) throws ReflectiveOperationException {
            return ctor.newInstance(stack, chance);
        }
    }
}
