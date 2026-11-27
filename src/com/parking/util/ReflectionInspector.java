package com.parking.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Demonstrates Unit 1: Java Reflection & JVM runtime class metadata inspection.
 */
public class ReflectionInspector {

    public static String inspectClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            StringBuilder sb = new StringBuilder();
            sb.append("=== Reflection Report: ").append(clazz.getName()).append(" ===\n");
            sb.append("Modifiers   : ").append(Modifier.toString(clazz.getModifiers())).append("\n");
            sb.append("Package     : ").append(clazz.getPackage() != null ? clazz.getPackage().getName() : "(default)").append("\n");
            sb.append("Superclass  : ").append(clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "None").append("\n");

            Class<?>[] interfaces = clazz.getInterfaces();
            sb.append("Interfaces  : ");
            if (interfaces.length == 0) {
                sb.append("None\n");
            } else {
                for (int i = 0; i < interfaces.length; i++) {
                    sb.append(interfaces[i].getSimpleName());
                    if (i < interfaces.length - 1) sb.append(", ");
                }
                sb.append("\n");
            }

            sb.append("\n--- Declared Fields ---\n");
            for (Field f : clazz.getDeclaredFields()) {
                sb.append("  ")
                  .append(Modifier.toString(f.getModifiers())).append(" ")
                  .append(f.getType().getSimpleName()).append(" ")
                  .append(f.getName()).append("\n");
            }

            sb.append("\n--- Declared Constructors ---\n");
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                sb.append("  ").append(Modifier.toString(c.getModifiers())).append(" ")
                  .append(clazz.getSimpleName()).append("(");
                Class<?>[] params = c.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    sb.append(params[i].getSimpleName());
                    if (i < params.length - 1) sb.append(", ");
                }
                sb.append(")\n");
            }

            sb.append("\n--- Declared Methods ---\n");
            for (Method m : clazz.getDeclaredMethods()) {
                sb.append("  ")
                  .append(Modifier.toString(m.getModifiers())).append(" ")
                  .append(m.getReturnType().getSimpleName()).append(" ")
                  .append(m.getName()).append("(");
                Class<?>[] params = m.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    sb.append(params[i].getSimpleName());
                    if (i < params.length - 1) sb.append(", ");
                }
                sb.append(")\n");
            }

            Runtime rt = Runtime.getRuntime();
            sb.append("\n--- JVM Runtime Info ---\n");
            sb.append("Java Version : ").append(System.getProperty("java.version")).append("\n");
            sb.append("JVM Name     : ").append(System.getProperty("java.vm.name")).append("\n");
            sb.append("Processors   : ").append(rt.availableProcessors()).append("\n");
            sb.append("Free/Total MB: ").append(rt.freeMemory() / (1024 * 1024))
              .append(" / ").append(rt.totalMemory() / (1024 * 1024)).append(" MB\n");

            return sb.toString();
        } catch (ClassNotFoundException e) {
            return "Class not found: " + className;
        }
    }
}
