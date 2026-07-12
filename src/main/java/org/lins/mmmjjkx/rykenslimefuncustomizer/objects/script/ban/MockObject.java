package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ban;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.Super;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

public class MockObject {
    private static final Map<Class<?>, Mocker<?>> mocks = new LinkedHashMap<>();

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

        if (obj instanceof Map<?,?> map) {
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
            if (obj.getClass().isAssignableFrom(entry.getKey())) {
                try {
                    return ((Mocker<T>) entry.getValue()).mock(obj);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to create Mocked" + obj.getClass().getSimpleName(), e);
                }
            }
        }

        return obj;
    }

    static {
        mocks.put(Server.class, new BanMocker<>(Server.class, List.of(
                "banIP",
                "clearRecipes",
                "createWorld",
                "dispatchCommand",
                "getBanList",
                "getIPBans",
                "getWhitelistedPlayers",
                "getWorlds",
                "reload",
                "resetRecipes",
                "selectEntities",
                "setDefaultGameMode",
                "shutdown",
                "unloadWorld"
        )));

        mocks.put(Player.class, new BanMocker<>(Player.class, List.of(
                "ban",
                "banIp",
                "banPlayerFull",
                "banPlayerIp",
                "chat",
                "getHAProxyAddress",
                "kick",
                "kickPlayer",
                "performCommand",
                "setGameMode",
                "setOp",
                "transfer"
        )));

        mocks.put(PluginManager.class, new PluginManagerMocker());

        List<Class<?>> listenClasses = List.of(
                Entity.class,
                CommandSender.class,
                JavaPlugin.class,
                Location.class,
                Block.class,
                BlockMenu.class,
                SlimefunBlockData.class
        );

        for (Class<?> clazz : listenClasses) {
            mocks.put(clazz, new EmptyMocker<>(clazz));
        }
    }

    public static class EmptyMocker<T> extends BanMocker<T> {

        public EmptyMocker(Class<T> clazz) {
            super(clazz, List.of());
        }
    }

    public static class PluginManagerMocker implements Mocker<PluginManager> {
        @Override
        public PluginManager mock(PluginManager delegate) throws InstantiationException, IllegalAccessException{
            return new PluginManagerMock(delegate).mock().newInstance();
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
            if (((Restriction) instance).restriction() && (
                    !method.getName().equals("getPlugin")
                            || args.length != 1
                            || args[0] == null
                            || !args[0].equals(RykenSlimefunCustomizer.INSTANCE.getJavaPlugin().getName()))
            ) {
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
        public T mock(T delegate) throws InstantiationException, IllegalAccessException{
            return new RestrictedMock<T>(delegate, clazz, prechecker, allowedMethodList).mock().newInstance();
        }
    }

    @RequiredArgsConstructor
    public static class BanMocker<T> implements Mocker<T> {
        private final Class<T> clazz;
        private final List<String> banMethodList;
        protected Prechecker prechecker = null;

        @Override
        @Contract(pure = true)
        public T mock(T delegate) throws InstantiationException, IllegalAccessException {
            return new BanMock<T>(delegate, clazz, prechecker, banMethodList).mock().newInstance();
        }
    }

    @FunctionalInterface
    public interface Prechecker {
        boolean precheck(Method method, Object[] args, Object instance);
    }

    public interface Mocker<T> {
        @Contract(pure = true)
        T mock(T delegate) throws InstantiationException, IllegalAccessException;
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
        default Class<T> mock() {
            ByteBuddy builder = new ByteBuddy();
            DynamicType.Builder<?> dynamic;

            if (extend().isInterface()) {
                dynamic = builder.subclass(Object.class).implement(extend());
            } else {
                dynamic = builder.subclass(extend());
            }

            dynamic = dynamic
            .name("Mocked" + extend().getSimpleName())
            .implement(Restriction.class)
            .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
            .intercept(MethodDelegation.to(new Interceptor(this)));

            Class<?> clazz;
            try (DynamicType.Unloaded<?> unloaded = dynamic.make()) {
                clazz = unloaded.load(RykenSlimefunCustomizer.INSTANCE.getJavaPlugin().getClass().getClassLoader()).getLoaded();
            }

            return (Class<T>) clazz;
        }
    }

    public interface Restriction {
        Object2BooleanMap<Restriction> restrictions = new Object2BooleanOpenHashMap<>() {{
            defaultReturnValue(true);
        }};

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
        @Nullable
        public Object intercept(@Origin Method method,
                                @AllArguments Object[] args,
                                @Super(strategy = Super.Instantiation.UNSAFE) Object object) throws Throwable {
            return mock.intercept(method, args, object);
        }
    }
}
