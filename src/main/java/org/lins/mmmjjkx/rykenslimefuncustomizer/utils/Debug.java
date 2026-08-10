package org.lins.mmmjjkx.rykenslimefuncustomizer.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.concurrent.Callable;

public class Debug {
    public static void error(String msg) {
        ExceptionHandler.handleError(msg);
    }

    public static void error(String msg, Throwable x) {
        ExceptionHandler.handleError(msg, x);
    }

    public static void error(File file, ConfigurationSection section, String msg) {
        ExceptionHandler.handleError("文件: " + file.getAbsolutePath());
        ExceptionHandler.handleError("在 " + section.getCurrentPath() + " 发现错误: " + msg);
    }

    public static void error(File file, ConfigurationSection section, String msg, Throwable e) {
        ExceptionHandler.handleError("文件: " + file.getAbsolutePath());
        ExceptionHandler.handleError("在 " + section.getCurrentPath() + " 发现错误: " + msg);
        e.printStackTrace();
    }

    public static void error(File file, ConfigurationSection section, String msg, Number start, Number end) {
        ExceptionHandler.handleError("文件: " + file.getAbsolutePath());
        ExceptionHandler.handleError("在 " + section.getCurrentPath() + " 发现错误: " + msg + ", 数值范围， [" + start + ", " + end + "]");
    }

    public static void warning(File file, ConfigurationSection section, String msg) {
        ExceptionHandler.handleWarning("文件: " + file.getAbsolutePath());
        ExceptionHandler.handleWarning("在 " + section.getCurrentPath() + " 发现问题: " + msg);
    }

    public static void warning(File file, ConfigurationSection section, String msg, Throwable e) {
        ExceptionHandler.handleWarning("文件: " + file.getAbsolutePath());
        ExceptionHandler.handleWarning("在 " + section.getCurrentPath() + " 发现问题: " + msg);
        e.printStackTrace();
    }

    public static void warning(File file, ConfigurationSection section, String msg, Number start, Number end) {
        ExceptionHandler.handleWarning("文件: " + file.getAbsolutePath());
        ExceptionHandler.handleWarning("在 " + section.getCurrentPath() + " 发现问题: " + msg + ", 数值范围，[" + start + ", " + end + "]");
    }

    public static void debug(File file, Callable<String> msg) {
        ExceptionHandler.debugLog(() -> "文件: " + file.getAbsolutePath());
        ExceptionHandler.debugLog(msg);
    }
}
