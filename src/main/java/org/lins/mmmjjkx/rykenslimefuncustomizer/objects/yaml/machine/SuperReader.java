/*
 * RykenSlimefunCustomizer
 * Copyright (C) 2026 lijinhong11(mmmjjjkx) and balugaq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.RSCItemGroupLegacy;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ReflectionUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class SuperReader extends YamlReader<SlimefunItem> {
    @Override
    public String getFileName() {
        return Constants.SUPERS_FILE;
    }

    public SuperReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public SlimefunItem readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        var base = getBase(section, s);
        if (base == null) return null;

        String className = section.getString("class", "");
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Debug.error(file, section, "不存在基类", e);
            return null;
        }

        if (!SlimefunItem.class.isAssignableFrom(clazz)) {
            Debug.error(file, section, "基类不是粘液物品");
            return null;
        }

        // a dangerous option to ignore accessibility
        // Author: balugaq
        boolean ignoreAccessible = section.getBoolean("ignore_accessible", false);

        if (ignoreAccessible) {
            Debug.warning(file, section, "ignore_accessible 选项被启用，这可能会导致潜在的安全漏洞!");
        }
        // 0-based index
        int constructorIndex = section.getInt("ctor", 0);
        if (clazz.getConstructors().length < constructorIndex + 1) {
            if (ignoreAccessible) {
                // try to find a private constructor
                if (clazz.getDeclaredConstructors().length < constructorIndex + 1) {
                    Debug.error(file, section, "无法找到第 " + constructorIndex + " 个构造函数 (0-based)");
                    return null;
                }
            } else {
                Debug.error(file, section, "无法找到第 " + constructorIndex + " 个可访问构造函数 (0-based)");
                return null;
            }
        }
        Constructor<? extends SlimefunItem> constructor;
        if (!ignoreAccessible) {
            constructor = (Constructor<? extends SlimefunItem>) clazz.getConstructors()[constructorIndex];
        } else {
            constructor = (Constructor<? extends SlimefunItem>) clazz.getDeclaredConstructors()[constructorIndex];
        }

        Object[] args = section.getList("args", new ArrayList<>()).toArray();
        List<Object> argTemplate =
                (List<Object>) section.getList("arg_template", List.of("group", "item", "recipe_type", "recipe"));
        Object[] originArgs = argTemplate.stream()
                .map(x -> {
                    if (x.equals("group")) return base.itemGroup();
                    if (x.equals("item")) return base.sfis();
                    if (x.equals("recipe_type")) return base.recipeType();
                    if (x.equals("recipe")) return base.recipe();
                    return x;
                })
                .toArray();

        SlimefunItem instance;
        try {
            List<Object> newArgs = new ArrayList<>(List.of(originArgs));
            newArgs.addAll(List.of(args));

            Class<?> dynamicClass = new ByteBuddy()
                .subclass(constructor.getDeclaringClass())
                .name(constructor.getDeclaringClass().getSimpleName() + "$$ByteBuddy")
                .method(ElementMatchers.named("load").and(ElementMatchers.takesArguments(0)))
                .intercept(MethodDelegation.to(LoadMethodInterceptor.class))
                .make()
                .load(RykenSlimefunCustomizer.INSTANCE.getJavaPlugin().getClass().getClassLoader())
                .getLoaded();

            Constructor<?> dynamicConstructor = dynamicClass.getDeclaredConstructor(
                Arrays.stream(constructor.getParameterTypes()).toArray(Class[]::new)
            );
            dynamicConstructor.setAccessible(true);

            instance = (SlimefunItem) dynamicConstructor.newInstance(newArgs.toArray());

        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            Debug.error(file, section, "无法创建类", e);
            return null;
        }

        if (section.contains("method")) {
            ConfigurationSection methodArray = section.getConfigurationSection("method");
            for (String methodName : methodArray.getKeys(false)) {
                Object[] args1;

                if (methodArray.isList(methodName)) {
                    args1 = methodArray.getList(methodName, new ArrayList<>()).toArray();
                } else {
                    args1 = new Object[] {methodArray.get(methodName)};
                }

                Method method = getMethod(
                        clazz,
                        methodName,
                        Arrays.stream(args1).map(Object::getClass).toArray(Class<?>[]::new));
                if (method == null) {
                    Debug.warning(file, methodArray, "没有找到方法 " + methodName);
                    continue;
                }

                try {
                    if (ignoreAccessible) {
                        method.setAccessible(true);
                    }
                    method.invoke(instance, args1);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    Debug.warning(file, methodArray, "无法调用方法 " + methodName, e);
                    continue;
                }
            }
        }

        if (section.contains("field")) {
            ConfigurationSection fieldArray = section.getConfigurationSection("field");
            for (String fieldName : fieldArray.getKeys(false)) {
                try {
                    Field field = getField(clazz, fieldName);

                    if (field == null) {
                        Debug.warning(file, fieldArray, "无法找到字段 " + fieldName);
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        Debug.warning(file, fieldArray, "字段 " + fieldName + " 为 static");
                        continue;
                    }

                    if (Modifier.isFinal(field.getModifiers())) {
                        Debug.warning(file, fieldArray, "字段 " + fieldName + " 为 final");
                        continue;
                    }

                    if (ignoreAccessible) {
                        field.setAccessible(true);
                    }
                    Object object = fieldArray.getObject(fieldName, field.getType());
                    field.set(instance, object);
                } catch (Throwable e) {
                    Debug.warning(file, fieldArray, "无法修改字段 " + fieldName, e);
                    continue;
                }
            }
        }
        try {
            instance.register(RykenSlimefunCustomizer.INSTANCE);
        } catch (Throwable e) {
            Debug.error(file, section, "无法注册类", e);
            return null;
        }

        return instance;
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return anyPreloadItems(s);
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        for (Method method : ReflectionUtil.getAllMethods(clazz)) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }

        return null;
    }

    private Field getField(Object obj, String name) {
        for (Field field : ReflectionUtil.getAllFields(obj.getClass())) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
    }

    public static class LoadMethodInterceptor {
        @RuntimeType
        public static Object intercept(@This Object instance) {
            String className = instance.getClass().getSimpleName();
            SlimefunItem sf = (SlimefunItem) instance;

            if (!sf.isHidden()) {
                RSCItemGroupLegacy.addItemToGroup(sf.getItemGroup(), sf);
            }

            sf.getRecipeType().register(sf.getRecipe(), sf.getRecipeOutput());
            return null;
        }
    }
}
