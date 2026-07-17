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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

@NullMarked
public class MockObject {
    @Getter
    private static final Map<Class<?>, Mocker<?>> mocks = new LinkedHashMap<>();
    private static final Map<Class<?>, Instantizer<?>> instantizers = new LinkedHashMap<>();
//    private static final ThreadLocal<Boolean> NEED_INTERCEPT = ThreadLocal.withInitial(() -> true);

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


    /**
     * 返回 clazz 及其所有父类（含 Object）、所有实现的接口（含父接口）中声明的全部方法。
     * 如果 clazz == null，返回空集合。
     */
    public static Set<Method> getAllDeclaredMethods(@Nullable Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptySet();
        }

        // 1. 收集所有相关的类型（类、父类、接口、父接口）
        Set<Class<?>> types = new LinkedHashSet<>();
        collectTypes(clazz, types);

        // 2. 确保 Object.class 被包含（接口的父类链中没有 Object）
        types.add(Object.class);

        // 3. 收集每个类型自身声明的所有方法
        Set<Method> methods = new LinkedHashSet<>();
        for (Class<?> type : types) {
            try {
                methods.addAll(Arrays.asList(type.getDeclaredMethods()));
            } catch (SecurityException ignored) {
                // 忽略无权访问的类（如某些系统类）
            }
        }
        return methods;
    }

    /**
     * 递归收集类型本身、所有父类（仅对类）、所有直接/间接实现的接口。
     */
    private static void collectTypes(@Nullable Class<?> type, Set<Class<?>> types) {
        if (type == null || types.contains(type)) {
            return;
        }
        types.add(type);

        // 如果是类（非接口），向上递归父类（最终会到达 Object）
        if (!type.isInterface()) {
            collectTypes(type.getSuperclass(), types);
        }

        // 递归处理所有直接实现的接口（它们的父接口会自动递归）
        for (Class<?> iface : type.getInterfaces()) {
            collectTypes(iface, types);
        }
    }

    @Contract(pure = true, value = "null -> null")
    @Nullable
    public static <T> T mock(@Nullable T obj) {
        debug(() -> "Mocking " + obj);
        if (obj == null) return null;
        if (obj instanceof Restriction || obj.getClass().getSimpleName().startsWith("Mocked")) {
            // mocked object
            return obj;
        }

        if (obj instanceof List<?> list) {
            List<Object> clone = new ArrayList<>();
            for (Object o : list) clone.add(mockObject(o));
            return (T) clone;
        }

        if (obj instanceof Set<?> set) {
            Set<Object> clone = new HashSet<>();
            for (Object o : set) clone.add(mockObject(o));
            return (T) clone;
        }

        if (obj instanceof Map<?, ?> map) {
            Map<Object, Object> clone = new HashMap<>();
            for (var e : map.entrySet()) clone.put(mockObject(e.getKey()), mockObject(e.getValue()));
            return (T) clone;
        }

        if (obj instanceof Collection<?> col) {
            List<Object> clone = new ArrayList<>();
            for (Object o : col) clone.add(mockObject(o));
            return (T) clone;
        }

        if (obj.getClass().isArray()) {
            Object[] clone = (Object[]) Array.newInstance(obj.getClass().getComponentType(), 0);
            for (int i = 0; i < Array.getLength(obj); i++) {
                clone[i] = mockObject(Array.get(obj, i));
            }
            return (T) clone;
        }

        if (obj.getClass().isPrimitive()) {
            return obj;
        }

        return mockObject(obj);
    }

    private static <T> T mockObject(@Nullable T obj) {
        if (obj == null) return null;
        for (var entry : mocks.entrySet()) {
            if (entry.getKey().isAssignableFrom(obj.getClass())) {
                debug(() -> "Mocking(object): " + obj + " as " + entry.getKey());
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
        protected @Nullable Prechecker<T> prechecker = null;

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
        public String name(Class<?> clazz) {
            return "RSC$PluginManagerMocked" + clazz.getSimpleName() + "$" + Math.abs(Objects.hashCode(delegate()));
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
                ExceptionHandler.handleError("已禁止 " + method.getName() + " 方法的调用");
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(self, method, args, instance);
        }
    }

    @RequiredArgsConstructor
    public static class RestrictedMocker<T> implements Mocker<T> {
        private final Class<T> clazz;
        private final List<String> allowedMethodList;
        protected @Nullable Prechecker<T> prechecker = null;

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
        protected @Nullable Prechecker<T> prechecker = null;

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
        private final @Nullable Prechecker<T> prechecker;
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
        public String name(Class<?> clazz) {
            return "RSC$BanMocked" + clazz.getSimpleName() + "$" + Math.abs(Objects.hashCode(delegate()));
        }

        @Override
        public @Nullable Object intercept(T self, Method method, Object[] args, Object instance) throws Throwable {
            if (restriction(self) && banMethodList.contains(method.getName())) {
                ExceptionHandler.handleError("已禁止 " + method.getName() + " 方法的调用");
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(self, method, args, instance);
        }
    }

    @RequiredArgsConstructor
    public static class RestrictedMock<T> implements Mock<T> {
        private final T delegate;
        private final Class<T> extend;
        private final @Nullable Prechecker<T> prechecker;
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
        public String name(Class<?> clazz) {
            return "RSC$RestrictedMocked" + clazz.getSimpleName() + "$" + Math.abs(Objects.hashCode(delegate()));
        }

        @Override
        public @Nullable Object intercept(T self, Method method, Object[] args, Object instance) throws Throwable {
            if (restriction(self) && !allowedMethodList.contains(method.getName())) {
                ExceptionHandler.handleError("已禁止 " + method.getName() + " 方法的调用");
                throw new UnsupportedOperationException("Method " + method.getName() + " is banned and inaccessible.");
            }

            return Mock.super.intercept(self, method, args, instance);
        }
    }

    @Contract("null -> null")
    @Nullable
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

//    public static <T> T mockEquals(T obj, Class<T> extend) {
//        debug(() -> "Mocking equals and hashCode for " + obj + " with extend: " + extend);
//        try {
//            return new EqualsMock<>(obj, extend, null, (Instantizer<T>) instantizers.get(extend)).mock();
//        } catch (InstantiationException
//                 | IllegalAccessException
//                 | NoSuchMethodException
//                 | InvocationTargetException e) {
//            throw failedToMockObject(obj, e);
//        }
//    }
//
//    @RequiredArgsConstructor
//    public static class EqualsMock<T> implements Mock<T> {
//        private final T delegate;
//        private final Class<T> extend;
//        private final Prechecker<T> prechecker;
//        private final Instantizer<T> instantizer;
//
//        @Override
//        public T delegate() {
//            return delegate;
//        }
//
//        @Override
//        public Class<T> extend() {
//            return extend;
//        }
//
//        @Override
//        public Prechecker<T> prechecker() {
//            return prechecker;
//        }
//
//        @Override
//        public Instantizer<T> instantizer() {
//            return instantizer;
//        }
//
//        @Override
//        public ElementMatcher.Junction<MethodDescription> methodMatcher() {
//            return ElementMatchers.isDeclaredBy(Object.class)
//                    .and(ElementMatchers.named("equals").or(ElementMatchers.named("hashCode")));
//        }
//
//        @Override
//        public String name(Class<?> clazz) {
//            return "RSC$EqualsMocked" + clazz.getSimpleName() + "$" + Math.abs(Objects.hashCode(delegate()));
//        }
//
//        @Override
//        public Object intercept(T self, Method method, Object[] args, Object object) {
//            if (prechecker() != null && !prechecker().precheck(self, method, args, object)) {
//                return null;
//            }
//
//            if (method.getName().equals("equals")) {
//                return MockObject.equals(args[0], self);
//            }
//
//            return MockObject.hashCode(self);
//        }
//    }

    private static Method getMethod(Method method, Object instance) throws NoSuchMethodException {
        return getMethod(method, instance.getClass());
    }

    private static Method getMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod(method.getName(), method.getParameterTypes());
    }

//    private static Method getUnmockedMethod(Method method, Object instance) throws NoSuchMethodException {
//        return instance.getClass().getMethod(method.getName() + "Unmocked", method.getParameterTypes());
//    }

    @NullMarked
    public interface Mock<T> {
        @Contract(pure = true)
        T delegate();

        @Contract(pure = true)
        Class<T> extend();

        @Contract(pure = true)
        default @Nullable Instantizer<T> instantizer() {
            return null;
        }

        @Contract(pure = true)
        default ElementMatcher.Junction<MethodDescription> methodMatcher() {
            return ElementMatchers.not(ElementMatchers.isDeclaredBy(Restriction.class)).and(ElementMatchers.not(ElementMatchers.isPrivate()))/*.and(ElementMatchers.not(ElementMatchers.nameEndsWith("Unmocked")))*/;
        }

        @Contract(pure = true)
        default String name(Class<?> clazz) {
            return "RSC$Mocked" + clazz.getSimpleName() + "$" + Math.abs(Objects.hashCode(delegate()));
        }

        @Contract(pure = true)
        default @Nullable Prechecker<T> prechecker() {
            return (s, m, a, i) -> true;
        }

        default @Nullable Object intercept(T self, Method method, Object[] args, Object object) throws Throwable {
            if (prechecker() != null && !prechecker().precheck(self, method, args, object)) {
                return null;
            }

            debug(() -> "Intercepted method: " + humanize(method));

            // equals and hashCode should ignore `needIntercept()`
            if (method.getName().equals("equals") && args.length == 1) {
                return MockObject.equals(args[0], self);
            }

            if (method.getName().equals("hashCode") && args.length == 0) {
                return MockObject.hashCode(self);
            }

//            debug(() -> "needIntercept: " + needIntercept());
//            if (needIntercept()) {
//                Method unmocked = getUnmockedMethod(method, self);
//                debug(() -> "Calling unmocked method: " + humanize(unmocked));
//                debug(() -> "instance=" + self);
//                debug(() -> "args=" + Arrays.toString(args));
//                return unmocked.invoke(self, args);
//            } else {
            debug(() -> "Calling origin method: " + humanize(getMethod(method, extend())));
            debug(() -> "instance=" + self);
            debug(() -> "args=" + Arrays.toString(args));
            return invokeExact(method, extend(), self, args);
//            }
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

            MethodDelegation delegation = MethodDelegation.to(new Interceptor<>(this));

            var allMethods = getAllDeclaredMethods(clazz);
            Set<String> mocked = new HashSet<>();
            for (Method method : allMethods) {
                String methodKey = method.getName() + Arrays.toString(method.getParameterTypes());
                if (!mocked.add(methodKey) || Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }

            /*
            // define unmocked method for calling origin logic
            var paramDef = dynamic
                    .defineMethod(method.getName() + "Unmocked", method.getReturnType(), Visibility.PUBLIC)
                    .withParameters(Arrays.asList(method.getParameterTypes()));

            if (method.getExceptionTypes().length > 0) {
                paramDef = paramDef.throwing(Arrays.asList(method.getExceptionTypes()));
            }

            dynamic = paramDef.intercept(MethodDelegation.to(new MethodInterceptor<>(method, delegate())));

             */

                if (!Modifier.isFinal(method.getModifiers())) {
                    // define override methods
                    var paramDef2 = dynamic
                        .defineMethod(method.getName(), method.getReturnType(), Visibility.PUBLIC)
                        .withParameters(Arrays.asList(method.getParameterTypes()));

                    if (method.getExceptionTypes().length > 0) {
                        paramDef2 = paramDef2.throwing(Arrays.asList(method.getExceptionTypes()));
                    }

                    dynamic = paramDef2.intercept(delegation);
                }
            }

            String name = name(clazz);
            dynamic = dynamic.name(name);
            debug(() -> "Generated mocked class: " + name + " for " + delegate());

            var builder2 = dynamic.method(methodMatcher());

            dynamic = builder2
                    .intercept(delegation)
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
            debug(() -> "Disabled restriction for " + this);
            restrictions.put(this, false);
        }

        default void enableRestriction() {
            debug(() -> "Enabled restriction for " + this);
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

    public record SuperInterceptor<T>(T delegate, Class<?> extend) {
        @RuntimeType
        @Nullable public Object intercept(
                @This T self,
                @Origin Method method,
                @AllArguments Object[] args,
                @Super(strategy = Super.Instantiation.UNSAFE) Object object)
                throws Throwable {
            debug(() -> "SuperInterceptor: intercepted " + method + " at " + self);
            // here, self is a MockedEqualsObject
            // we want to call its super method
            return MockObject.mock(invokeExact(method, extend(), self, args));
        }
    }

    public static MethodHandle findNearestSuperMethod(Object instance, String methodName, MethodType type) throws Throwable {
        Class<?> currentClass = instance.getClass();
        // 从当前类的直接父类开始向上查找，因为当前类自己的方法是我们想绕过的
        Class<?> superClass = currentClass.getSuperclass();

        while (superClass != null && superClass != Object.class) {
            try {
                // 获取父类中声明的方法（包括非 public）
                Method method = superClass.getDeclaredMethod(methodName, type.parameterArray());
                method.setAccessible(true);

                // 检查该方法是否为具体实现（非抽象）
                if (!Modifier.isAbstract(method.getModifiers())) {
                    // 找到最近的具体父类！
                    // 创建 Lookup，使用 currentClass 作为调用者上下文（允许访问父类 protected/package 方法）
                    MethodHandles.Lookup lookup = MethodHandles.lookup();

                    // 关键点：findSpecial 的第四个参数必须传 currentClass（实际子类），
                    // 这样 JVM 才允许调用 superClass 的方法
                    return lookup.findSpecial(superClass, methodName, type, currentClass);
                }
            } catch (NoSuchMethodException ignored) {
                // 当前父类没有此方法，继续向上
            } catch (IllegalAccessException e) {
                // 如果父类方法是 private，无法通过反射访问，抛出异常
                throw new IllegalAccessException("Cannot access method in superclass: " + superClass.getName());
            }
            // 继续向上遍历
            superClass = superClass.getSuperclass();
        }

        throw new NoSuchMethodException("No concrete super method found for " + methodName + " in hierarchy of " + instance.getClass());
    }

    private static <T> Object invokeExact(Method method, Class<?> extend, T self, Object[] args) throws Throwable {
        MethodType type = MethodType.methodType(method.getReturnType(), method.getParameterTypes());

        return findNearestSuperMethod(self, method.getName(), type).invokeExact(args);
    }

//    public record MethodInterceptor<T>(Method method, T delegate) {
//        @RuntimeType
//        @Nullable public Object intercept(
//                @This T self,
//                @Origin Method mockedMethod,
//                @AllArguments Object[] args,
//                @Super(strategy = Super.Instantiation.UNSAFE) Object object)
//                throws Throwable {
//            try {
//                setNeedIntercept(false);
//                return MockObject.mock(method.invoke(self, args));
//            } finally {
//                setNeedIntercept(true);
//            }
//        }
//    }

//    public static boolean needIntercept() {
//        return NEED_INTERCEPT.get();
//    }
//
//    public static void setNeedIntercept(boolean needIntercept) {
//        NEED_INTERCEPT.set(needIntercept);
//    }

    public record Interceptor<T>(Mock<T> mock) {
        @RuntimeType
        @Nullable public Object intercept(
                @This T self,
                @Origin Method method,
                @AllArguments Object[] args,
                @Super(strategy = Super.Instantiation.UNSAFE) Object object)
                throws Throwable {
            debug(() -> "Interceptor: intercepted " + method + " at " + self);
            return MockObject.mock(mock.intercept(self, method, args, object));
        }
    }

    public static String humanize(Method method) {
        StringBuilder name = new StringBuilder(method.getDeclaringClass().getSimpleName() + "#" + method.getName() + "(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            name.append(parameterType.getSimpleName()).append(", ");
        }
        return name.substring(0, name.length() - 1) + ")";
    }

    public static void debug(Callable<String> message) {
        ExceptionHandler.debugLog(message);
    }
}
