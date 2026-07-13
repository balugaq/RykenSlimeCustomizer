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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.mocks;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.Super;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;

public class MockObject {
    @Getter
    private static final Map<Class<?>, Mocker<?>> mocks = new LinkedHashMap<>();

    static {
        MockRegistry.register();
    }

    @Contract(pure = true, value = "null -> null")
    public static <T> T mock(@Nullable T obj) {
        if (obj == null) return null;
        if (obj instanceof Restriction) {
            // mocked object
            return obj;
        }

        if (obj instanceof List<?> list) {
            List<Object> clone = new ArrayList<>();
            for (Object o : list) clone.add(mockOne(o));
            return (T) clone;
        }

        if (obj instanceof Set<?> set) {
            Set<Object> clone = new HashSet<>();
            for (Object o : set) clone.add(mockOne(o));
            return (T) clone;
        }

        if (obj instanceof Map<?, ?> map) {
            Map<Object, Object> clone = new HashMap<>();
            for (var e : map.entrySet()) clone.put(mockOne(e.getKey()), mockOne(e.getValue()));
            return (T) clone;
        }

        if (obj instanceof Collection<?> col) {
            List<Object> clone = new ArrayList<>();
            for (Object o : col) clone.add(mockOne(o));
            return (T) clone;
        }

        if (obj.getClass().isArray()) {
            Object[] clone = (Object[]) Array.newInstance(obj.getClass().getComponentType(), 0);
            for (int i = 0; i < Array.getLength(obj); i++) {
                clone[i] = mockOne(Array.get(obj, i));
            }
            return (T) clone;
        }

        return mockOne(obj);
    }

    public static <T> T mockOne(@Nullable T obj) {
        for (var entry : mocks.entrySet()) {
            if (entry.getKey().isAssignableFrom(obj.getClass())) {
                try {
                    return ((Mocker<T>) entry.getValue()).mock(obj);
                } catch (InstantiationException
                        | IllegalAccessException
                        | NoSuchMethodException
                        | InvocationTargetException e) {
                    throw failedToMockObject(obj, e);
                }
            }
        }

        return obj;
    }

    protected static RuntimeException failedToMockObject(Object obj, Exception e) {
        return new RuntimeException(
                "Failed to create Mocked" + obj.getClass().getSimpleName(), e);
    }

    public static class EmptyMocker<T> extends BanMocker<T> {
        public EmptyMocker(Class<T> clazz) {
            super(clazz, List.of());
        }
    }

    @RequiredArgsConstructor
    public static class InstantizableMocker<T> implements Mocker<T> {
        private final Class<T> clazz;
        private final Instantizer<T> instantizer;
        protected Prechecker prechecker = null;

        @Override
        @Contract(pure = true)
        public T mock(T delegate)
                throws InstantiationException, IllegalAccessException, NoSuchMethodException,
                InvocationTargetException {
            return new InstantizableMock<T>(delegate, clazz, prechecker, instantizer).mock();
        }
    }

    @FunctionalInterface
    public interface Instantizer<T> extends BiFunction<Class<T>, T, T> {}

    @RequiredArgsConstructor
    public static class InstantizableMock<T> implements Mock<T> {
        private final T delegate;
        private final Class<T> clazz;
        private final Prechecker prechecker;
        private final Instantizer<T> instantizer;

        @Override
        public T delegate() {
            return delegate;
        }

        @Override
        public Class<T> extend() {
            return clazz;
        }

        @Override
        public Prechecker prechecker() {
            return prechecker;
        }

        @Override
        public Instantizer<T> instantizer() {
            return instantizer;
        }

        @Override
        public Object intercept(Method method, Object[] args, Object instance) throws Throwable {
            return Mock.super.intercept(method, args, instance);
        }
    }

    public static class PluginManagerMocker implements Mocker<PluginManager> {
        @Override
        public PluginManager mock(PluginManager delegate)
                throws InstantiationException, IllegalAccessException, NoSuchMethodException,
                        InvocationTargetException {
            return new PluginManagerMock(delegate).mock();
        }
    }

    @RequiredArgsConstructor
    public static class PluginManagerMock implements Mock<PluginManager> {
        private final PluginManager delegate;

        @Override
        public PluginManager delegate() {
            return delegate;
        }

        @Override
        public Class<PluginManager> extend() {
            return PluginManager.class;
        }

        @Override
        public Object intercept(Method method, Object[] args, Object instance) throws Throwable {
            if (((Restriction) instance).restriction()
                    && (!method.getName().equals("getPlugin")
                            || args.length != 1
                            || args[0] == null
                            || !args[0].equals(RykenSlimefunCustomizer.INSTANCE
                                    .getJavaPlugin()
                                    .getName()))) {
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(method, args, instance);
        }
    }

    @RequiredArgsConstructor
    public static class RestrictedMocker<T> implements Mocker<T> {
        private final Class<T> clazz;
        private final List<String> allowedMethodList;
        protected Prechecker prechecker = null;

        @Override
        @Contract(pure = true)
        public T mock(T delegate)
                throws InstantiationException, IllegalAccessException, NoSuchMethodException,
                        InvocationTargetException {
            return new RestrictedMock<T>(delegate, clazz, prechecker, allowedMethodList).mock();
        }
    }

    @RequiredArgsConstructor
    public static class BanMocker<T> implements Mocker<T> {
        private final Class<T> clazz;
        private final List<String> banMethodList;
        protected Prechecker prechecker = null;

        @Override
        @Contract(pure = true)
        public T mock(T delegate)
                throws InstantiationException, IllegalAccessException, NoSuchMethodException,
                        InvocationTargetException {
            return new BanMock<T>(delegate, clazz, prechecker, banMethodList).mock();
        }
    }

    @FunctionalInterface
    public interface Prechecker {
        boolean precheck(Method method, Object[] args, Object instance);
    }

    public interface Mocker<T> {
        @Contract(pure = true)
        T mock(T delegate)
                throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;
    }

    @RequiredArgsConstructor
    public static class BanMock<T> implements Mock<T> {
        private final T delegate;
        private final Class<T> extend;
        private final Prechecker prechecker;
        private final List<String> banMethodList;

        @Override
        public T delegate() {
            return delegate;
        }

        @Override
        public Class<T> extend() {
            return extend;
        }

        @Override
        public Prechecker prechecker() {
            return prechecker;
        }

        @Override
        public Object intercept(Method method, Object[] args, Object instance) throws Throwable {
            if (((Restriction) instance).restriction() && banMethodList.contains(method.getName())) {
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(method, args, instance);
        }
    }

    @RequiredArgsConstructor
    public static class RestrictedMock<T> implements Mock<T> {
        private final T delegate;
        private final Class<T> extend;
        private final Prechecker prechecker;
        private final List<String> allowedMethodList;

        @Override
        public T delegate() {
            return delegate;
        }

        @Override
        public Class<T> extend() {
            return extend;
        }

        @Override
        public Prechecker prechecker() {
            return prechecker;
        }

        @Override
        public Object intercept(Method method, Object[] args, Object instance) throws Throwable {
            if (((Restriction) instance).restriction() && !allowedMethodList.contains(method.getName())) {
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(method, args, instance);
        }
    }

    public interface Mock<T> {
        @Contract(pure = true)
        T delegate();

        @Contract(pure = true)
        Class<T> extend();

        @Nullable
        default Instantizer<T> instantizer() {
            return null;
        }

        default Prechecker prechecker() {
            return (m, a, i) -> true;
        }

        default Object intercept(Method method, Object[] args, Object instance) throws Throwable {
            if (!prechecker().precheck(method, args, instance)) {
                return null;
            }
            return MockObject.mock(method.invoke(delegate(), args));
        }

        @Contract(pure = true)
        default T mock()
                throws InstantiationException, IllegalAccessException, NoSuchMethodException,
                        InvocationTargetException {
            ByteBuddy builder = new ByteBuddy();
            DynamicType.Builder<?> dynamic;

            Class<?> clazz = delegate().getClass();
            while (Modifier.isFinal(clazz.getModifiers())
                    && clazz != Object.class
                    && Arrays.stream(clazz.getDeclaredConstructors())
                            .noneMatch(ctor -> ctor.getParameterCount() == 0)) {
                clazz = clazz.getSuperclass();
            }

            if (Modifier.isFinal(clazz.getModifiers())
                    && Arrays.stream(clazz.getDeclaredConstructors())
                            .noneMatch(ctor -> ctor.getParameterCount() == 0)) {
                clazz = extend();
            }

            if (clazz.isInterface()) {
                dynamic = builder.subclass(Object.class).implement(clazz);
            } else {
                dynamic = builder.subclass(clazz);
            }

            dynamic = dynamic.name("Mocked" + clazz.getSimpleName())
                    .implement(Restriction.class)
                    .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.to(new Interceptor(this)));

            Class<T> instanceClazz;
            try (DynamicType.Unloaded<?> unloaded = dynamic.make()) {
                ClassLoader loader = RykenSlimefunCustomizer.INSTANCE.getJavaPlugin().getClass().getClassLoader();
                instanceClazz = (Class<T>) unloaded
                        .load(loader)
                        .getLoaded();
            }

            if (instantizer() != null) {
                return instantizer().apply(instanceClazz, delegate());
            }

            return instanceClazz.getConstructor().newInstance();
        }
    }

    public interface Restriction {
        Object2BooleanMap<Restriction> restrictions = new Object2BooleanOpenHashMap<>() {
            {
                defaultReturnValue(true);
            }
        };

        default boolean restriction() {
            return restrictions.getBoolean(this);
        }

        default void disableRestriction() {
            restrictions.put(this, false);
        }

        default void enableRestriction() {
            restrictions.put(this, true);
        }
    }

    public record Interceptor(Mock<?> mock) {
        @RuntimeType
        @Nullable public Object intercept(
                @Origin Method method,
                @AllArguments Object[] args,
                @Super(strategy = Super.Instantiation.UNSAFE) Object object)
                throws Throwable {
            return mock.intercept(method, args, object);
        }
    }
}
