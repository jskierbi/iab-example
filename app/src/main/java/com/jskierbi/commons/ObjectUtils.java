package com.jskierbi.commons;

import android.text.TextUtils;

import java.util.Collection;
import java.util.List;

/**
 * Utility class exposing convenient generic methods to perform null-safe operations
 *
 * Created by jakub on 20/11/15.
 */
public class ObjectUtils {
  public static <T> T defaultIfNull(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }

  public static <T> T defaultIfNull(T value, T default1, T default2) {
    return defaultIfNull(defaultIfNull(value, default1), default2);
  }

  public static <T> T defaultIfNull(T value, T default1, T default2, T default3) {
    return defaultIfNull(defaultIfNull(value, default1, default2), default3);
  }

  public static <T> T defaultIfNull(T value, T default1, T default2, T default3, T default4) {
    return defaultIfNull(defaultIfNull(value, default1, default2, default3), default4);
  }

  public static <T> T defaultIfNull(T value, T default1, T default2, T default3, T default4, T default5) {
    return defaultIfNull(defaultIfNull(value, default1, default2, default3, default4), default5);
  }

  public static String defaultIfEmpty(String value, String defaultValue) {
    return !TextUtils.isEmpty(value) ? value : defaultValue;
  }

  public static <T> Collection<T> defaultIfEmpty(Collection<T> value, Collection<T> defaultValue) {
    return value != null && !value.isEmpty() ? value : defaultValue;
  }

  public static <T> List<T> defaultIfEmpty(List<T> value, List<T> defaultValue) {
    return value != null && !value.isEmpty() ? value : defaultValue;
  }

  public static boolean isEmpty(String string) {
    return string == null || string.length() == 0;
  }

  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static <T> boolean safeEquals(T lhs, T rhs) {
    return lhs == null && rhs == null || lhs != null && lhs.equals(rhs);
  }

  public static <T> T nullIfNotInstanceOf(Object obj, Class<T> clazz) {
    return clazz.isInstance(obj) ? clazz.cast(obj) : null;
  }
}
