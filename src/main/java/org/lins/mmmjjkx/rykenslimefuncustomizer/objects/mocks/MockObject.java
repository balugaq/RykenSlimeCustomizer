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
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;

public class MockObject {
    @Getter
    private static final Map<Class<?>, Mocker<?>> mocks = new LinkedHashMap<>();
    private static final Map<Class<?>, Instantizer<?>> instantizers = new LinkedHashMap<>();
    private static final ThreadLocal<Boolean> NEED_INTERCEPT = ThreadLocal.withInitial(() -> true);

    private static final Field PLAYER_FIELD = PlayerEvent.class.getDeclaredFields()[0];
    private static final Field BLOCK_FIELD = BlockEvent.class.getDeclaredFields()[0];

    static {
        PLAYER_FIELD.setAccessible(true);
        BLOCK_FIELD.setAccessible(true);
        MockRegistry.register();
    }

    private static Set<Class<?>> getAllSubInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        while (clazz != null) {
            for (Class<?> iface : clazz.getInterfaces()) {
                interfaces.add(iface);
                interfaces.addAll(getAllSubInterfaces(iface));
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    private static List<Method> getAllDeclaredMethods(Class<?> clazz) {
        List<Method> list = new ArrayList<>();
        getAllDeclaredMethods(clazz, new HashSet<>(), list);
        return list;
    }

    private static void getAllDeclaredMethods(@Nullable Class<?> clazz, Set<Class<?>> visited, List<Method> methods) {
        if (clazz == null || visited.contains(clazz)) return;
        visited.add(clazz);
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        for (Class<?> iface : clazz.getInterfaces()) {
            visited.add(iface);
            methods.addAll(Arrays.asList(iface.getDeclaredMethods()));
           getAllDeclaredMethods(iface, visited, methods);
        }
        getAllDeclaredMethods(clazz.getSuperclass(), visited, methods);
    }

    @Contract(pure = true, value = "null -> null")
    public static <T> T mock(@Nullable T obj) {
        if (obj == null) return null;
        if (obj instanceof Restriction || obj.getClass().getSimpleName().startsWith("Mocked")) {
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

    public static class InstantizableMocker<T> implements Mocker<T> {
        private final Class<T> clazz;
        private final Instantizer<T> instantizer;
        protected Prechecker<T> prechecker = null;

        public InstantizableMocker(Class<T> clazz, Instantizer<T> instantizer) {
            this.clazz = clazz;
            this.instantizer = instantizer;
            instantizers.put(clazz, instantizer);
        }

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
        private final Prechecker<T> prechecker;
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
        public Prechecker<T> prechecker() {
            return prechecker;
        }

        @Override
        public Instantizer<T> instantizer() {
            return instantizer;
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
        public Object intercept(PluginManager self, Method method, Object[] args, Object instance) throws Throwable {
            if (restriction(self)
                    && (!method.getName().equals("getPlugin")
                            || args.length != 1
                            || args[0] == null
                            || !args[0].equals(RykenSlimefunCustomizer.INSTANCE
                                    .getJavaPlugin()
                                    .getName()))) {
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(self, method, args, instance);
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
    public interface Prechecker<T> {
        boolean precheck(T self, Method method, Object[] args, Object object);
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
        public Prechecker<T> prechecker() {
            return prechecker;
        }

        @Override
        public Object intercept(T self, Method method, Object[] args, Object instance) throws Throwable {
            if (restriction(self) && banMethodList.contains(method.getName())) {
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(self, method, args, instance);
        }
    }

    @RequiredArgsConstructor
    public static class RestrictedMock<T> implements Mock<T> {
        private final T delegate;
        private final Class<T> extend;
        private final Prechecker<T> prechecker;
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
        public Prechecker<T> prechecker() {
            return prechecker;
        }

        @Override
        public Object intercept(T self, Method method, Object[] args, Object instance) throws Throwable {
            if (restriction(self) && !allowedMethodList.contains(method.getName())) {
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(self, method, args, instance);
        }
    }

    public static Object invokeSuperMethod(Method method, Object delegate, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = getMethod(method, delegate);
        m.setAccessible(true);
        return m.invoke(delegate, args);
    }

    public static <T> T unmock(@Nullable T obj) {
        if (obj == null) return null;
        if (obj.getClass().getSimpleName().startsWith("Mocked")) {
            try {
                return (T) obj.getClass().getMethod("delegate").invoke(obj);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return obj;
    }

    public static boolean equals(Object instance1, Object instance2) {
        instance1 = MockObject.unmock(instance1);
        instance2 = MockObject.unmock(instance2);

        return Objects.equals(instance1, instance2);
    }

    public static int hashCode(Object obj) {
        Object unmocked = MockObject.unmock(obj);
        return Objects.hashCode(unmocked);
    }

    /*
    public static <T> T mockEquals(T obj, Class<T> extend) {
        try {
            return new EqualsMock<>(obj, extend, null, (Instantizer<T>) instantizers.get(extend)).mock();
        } catch (InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException
                 | InvocationTargetException e) {
            throw failedToMockObject(obj, e);
        }
    }

    @RequiredArgsConstructor
    public static class EqualsMock<T> implements Mock<T> {
        private final T delegate;
        private final Class<T> extend;
        private final Prechecker<T> prechecker;
        private final Instantizer<T> instantizer;

        @Override
        public T delegate() {
            return delegate;
        }

        @Override
        public Class<T> extend() {
            return extend;
        }

        @Override
        public Prechecker<T> prechecker() {
            return prechecker;
        }

        @Override
        public Instantizer<T> instantizer() {
            return instantizer;
        }

        @Override
        public ElementMatcher.Junction<MethodDescription> methodMatcher() {
            return ElementMatchers.isDeclaredBy(Object.class).and(ElementMatchers.named("equals").or(ElementMatchers.named("hashCode")));
        }

        @Override
        public Object intercept(T self, Method method, Object[] args, Object object) {
            if (prechecker() != null && !prechecker().precheck(self, method, args, object)) {
                return null;
            }

            if (method.getName().equals("equals")) {
                return MockObject.equals(args[0], self);
            }

            return MockObject.hashCode(self);
        }
    }
     */

    private static Method getMethod(Method method, Object instance) throws NoSuchMethodException {
        return getMethod(method, instance.getClass());
    }

    private static Method getMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod(method.getName(), method.getParameterTypes());
    }

    private static Method getUnmockedMethod(Method method, Object instance) throws NoSuchMethodException {
        try {
            return instance.getClass().getMethod(method.getName() + "Unmocked", method.getParameterTypes());
        } catch (NoSuchMethodException ignored) {
            return getMethod(method, instance);
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

        default ElementMatcher.Junction<MethodDescription> methodMatcher() {
            return ElementMatchers.not(ElementMatchers.isDeclaredBy(Restriction.class)).and(ElementMatchers.not(ElementMatchers.isPrivate())).and(ElementMatchers.not(ElementMatchers.nameEndsWith("Unmocked")));
        }

        default Prechecker<T> prechecker() {
            return (s, m, a, i) -> true;
        }

        default Object intercept(T self, Method method, Object[] args, Object object) throws Throwable {
            if (prechecker() != null && !prechecker().precheck(self, method, args, object)) {
                return null;
            }

            // equals and hashCode should ignore `needIntercept()`
            if (method.getName().equals("equals") && args.length == 1) {
                return MockObject.equals(args[0], self);
            }

            if (method.getName().equals("hashCode") && args.length == 0) {
                return MockObject.hashCode(self);
            }

            if (needIntercept()) {
                return getUnmockedMethod(method, self).invoke(self, args);
            } else {
                return method.invoke(delegate(), args);
            }
        }

        @Contract(pure = true)
        default T mock()
                throws InstantiationException, IllegalAccessException, NoSuchMethodException,
                        InvocationTargetException {
            ByteBuddy builder = new ByteBuddy();
            DynamicType.Builder<?> dynamic;

            Class<?> clazz = delegate().getClass();

            if (instantizer() == null) {
                while (clazz != Object.class && (
                        Modifier.isFinal(clazz.getModifiers())
                                || (!clazz.isInterface()
                                && Arrays.stream(clazz.getDeclaredConstructors())
                                .noneMatch(ctor -> ctor.getParameterCount() == 0)))) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (clazz == Object.class
                    || Modifier.isFinal(clazz.getModifiers())
                    || Arrays.stream(clazz.getDeclaredConstructors())
                        .noneMatch(ctor -> ctor.getParameterCount() == 0)
                    || clazz.getSimpleName().startsWith("Craft")) {
                clazz = extend();
            }

            if (clazz.isInterface()) {
                dynamic = builder.subclass(Object.class);

                dynamic = dynamic.implement(clazz);
                Set<Class<?>> allInterfaces = getAllSubInterfaces(clazz);
                for (Class<?> iface : allInterfaces) {
                    dynamic = dynamic.implement(iface);
                }
            } else {
                dynamic = builder.subclass(clazz);
            }

            Set<String> mocked = new HashSet<>();
            for (Method method : getAllDeclaredMethods(clazz)) {
                String methodKey = method.getName() + Arrays.toString(method.getParameterTypes());
                if (!mocked.add(methodKey) || Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }

                // define unmocked method for calling origin logic
                var paramDef = dynamic
                        .defineMethod(method.getName() + "Unmocked", method.getReturnType(), Visibility.PUBLIC)
                        .withParameters(Arrays.asList(method.getParameterTypes()));

                if (method.getExceptionTypes().length > 0) {
                    paramDef = paramDef.throwing(Arrays.asList(method.getExceptionTypes()));
                }

                dynamic = paramDef.intercept(MethodDelegation.to(new MethodInterceptor<>(method, delegate())));

                if (!Modifier.isFinal(method.getModifiers())) {
                    // define override methods
                    var paramDef2 = dynamic
                            .defineMethod(method.getName(), method.getReturnType(), Visibility.PUBLIC)
                            .withParameters(Arrays.asList(method.getParameterTypes()));

                    if (method.getExceptionTypes().length > 0) {
                        paramDef2 = paramDef2.throwing(Arrays.asList(method.getExceptionTypes()));
                    }

                    dynamic = paramDef2.intercept(MethodDelegation.to(new SuperInterceptor<>(delegate())));
                }
            }

            dynamic = dynamic.name("RSC$Mocked" + clazz.getSimpleName() + "$" + Math.abs(dynamic.hashCode()));


            var builder2 = dynamic.method(methodMatcher());

            dynamic = builder2
                    .intercept(MethodDelegation.to(new Interceptor<>(this)))
                    .implement(Restriction.class);

            Class<T> instanceClazz;
            try (DynamicType.Unloaded<?> unloaded = dynamic.make()) {
                ClassLoader loader = RykenSlimefunCustomizer.INSTANCE.getClass().getClassLoader();
                instanceClazz = (Class<T>) unloaded
                        .load(loader, ClassLoadingStrategy.Default.CHILD_FIRST_PERSISTENT)
                        .getLoaded();
            }

            T instance;
            if (instantizer() != null) {
                instance = instantizer().apply(instanceClazz, delegate());
            } else {
                instance = instanceClazz.getConstructor().newInstance();
            }

            for (Field field : instance.getClass().getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers()) ||
                        Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                field.set(instance, MockObject.mock(field.get(instance)));
            }

            if (instance instanceof PlayerEvent) {
                PLAYER_FIELD.set(instance, MockObject.mock(PLAYER_FIELD.get(instance)));
            }

            if (instance instanceof BlockEvent) {
                BLOCK_FIELD.set(instance, MockObject.mock(BLOCK_FIELD.get(instance)));
            }

            return instance;
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

    public static boolean restriction(Object instance) {
        if (instance.getClass().getSimpleName().startsWith("Mocked")) {
            try {
                return (boolean) instance.getClass().getMethod("restriction").invoke(instance);
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    public record SuperInterceptor<T>(T delegate) {
        @RuntimeType
        @Nullable public Object intercept(
                @This T self,
                @Origin Method method,
                @AllArguments Object[] args,
                @Super(strategy = Super.Instantiation.UNSAFE) Object object)
                throws Throwable {
            return MockObject.mock(getMethod(method, delegate).invoke(delegate, args));
        }
    }

    public record MethodInterceptor<T>(Method method, T delegate) {
        @RuntimeType
        @Nullable public Object intercept(
                @This T self,
                @Origin Method method,
                @AllArguments Object[] args,
                @Super(strategy = Super.Instantiation.UNSAFE) Object object)
                throws Throwable {
            setNeedIntercept(false);
            Object result = MockObject.mock(method.invoke(delegate, args));
            setNeedIntercept(true);
            return result;
        }
    }

    public static boolean needIntercept() {
        return NEED_INTERCEPT.get();
    }

    public static void setNeedIntercept(boolean needIntercept) {
        NEED_INTERCEPT.set(needIntercept);
    }

    public record Interceptor<T>(Mock<T> mock) {
        @RuntimeType
        @Nullable public Object intercept(
                @This T self,
                @Origin Method method,
                @AllArguments Object[] args,
                @Super(strategy = Super.Instantiation.UNSAFE) Object object)
                throws Throwable {
            return MockObject.mock(mock.intercept(self, method, args, object));
        }
    }
}
