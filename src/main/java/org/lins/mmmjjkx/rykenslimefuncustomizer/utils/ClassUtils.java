package org.lins.mmmjjkx.rykenslimefuncustomizer.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ClassUtils {
    private static final Map<String, Class<?>> cache = new HashMap<>();

    public static final List<String> SERVER_BANNED_METHODS = List.of(
            "shutdown", "reload", "dispatchCommand", "getBanList",
            "getIPBans", "getPluginManager", "getOperators", "setDefaultGameMode"
    );

    public static final List<String> PLAYER_BANNED_METHODS = List.of(
            "kickPlayer", "banPlayer", "setOp", "performCommand"
    );

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> generateClass(
            Class<T> extendClass,
            String centerName,
            String nameReplacement,
            Class<?>[] interfaces,
            @Nullable Function<DynamicType.Builder<?>, DynamicType.Builder<?>> delegation) {

        String finalClassName = extendClass.getSimpleName().replace(nameReplacement, "") + centerName + nameReplacement;
        if (cache.containsKey(finalClassName)) {
            return (Class<? extends T>) cache.get(finalClassName);
        }

        DynamicType.Builder<?> builder = new ByteBuddy().subclass(extendClass);

        if (delegation != null) {
            builder = delegation.apply(builder);
        }

        builder = builder.implement(interfaces).name(finalClassName);

        Class<?> clazz;
        try (DynamicType.Unloaded<?> unloaded = builder.make()) {
            clazz = unloaded.load(extendClass.getClassLoader()).getLoaded();
        }

        cache.put(finalClassName, clazz);
        return (Class<? extends T>) clazz;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> generateAgentClass(Class<T> extendClass, String newName, List<String> bannedMethods) {
        if (cache.containsKey(newName)) {
            return (Class<? extends T>) cache.get(newName);
        }

        Set<String> bannedSet = bannedMethods != null ? new HashSet<>(bannedMethods) : Set.of();

        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(extendClass)
                .name(newName)
                .defineField("delegate", extendClass, Visibility.PRIVATE)
                .defineConstructor(1)
                .withParameters(extendClass)
                .intercept(SuperMethodCall.INSTANCE
                        .andThen(FieldAccessor.ofField("delegate").setsArgumentAt(0)))
                .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                .intercept(MethodDelegation.to(new AgentInterceptor(bannedSet)));

        Class<?> clazz;
        try (DynamicType.Unloaded<?> unloaded = builder.make()) {
            clazz = unloaded.load(extendClass.getClassLoader()).getLoaded();
        }

        cache.put(newName, clazz);
        return (Class<? extends T>) clazz;
    }

    public static Server wrapServer(Server server) {
        try {
            Class<? extends Server> agentClass = generateAgentClass(
                    server.getClass(), "org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.agent.ServerAgent",
                    SERVER_BANNED_METHODS);
            return (Server) agentClass.getDeclaredConstructors()[0].newInstance(server);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Server agent", e);
        }
    }

    public static Player wrapPlayer(Player player) {
        try {
            Class<? extends Player> agentClass = generateAgentClass(
                    player.getClass(), "org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.agent.PlayerAgent",
                    PLAYER_BANNED_METHODS);
            return (Player) agentClass.getDeclaredConstructors()[0].newInstance(player);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Player agent", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T unwrap(Object wrapped) {
        try {
            Field delegateField = wrapped.getClass().getDeclaredField("delegate");
            delegateField.setAccessible(true);
            return (T) delegateField.get(wrapped);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return (T) wrapped;
        }
    }

    private record AgentInterceptor(Set<String> bannedMethods) {
        @RuntimeType
            public Object intercept(@Origin Method method, @FieldValue("delegate") Object delegate, @AllArguments Object[] args)
                    throws Throwable {
                if (bannedMethods.contains(method.getName())) {
                    throw new UnsupportedOperationException("Method " + method.getName() + " is banned");
                }
                return method.invoke(delegate, args);
            }
        }
}
