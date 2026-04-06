package com.hypixel.hytale.server.npc.asset.builder.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringListHelpers {
   @Nonnull
   private static Pattern listSplitter = Pattern.compile("[,; \t]");
   @Nonnull
   private static Pattern listListSplitter = Pattern.compile("\\|");

   private StringListHelpers() {
   }

   @Nonnull
   public static String stringListToString(@Nullable Collection<String> list) {
      return list == null ? "" : (String)list.stream().map(String::trim).collect(Collectors.joining(", "));
   }

   @Nonnull
   public static List<String> splitToStringList(String string, @Nullable Function<String, String> mapper) {
      if (mapper == null) {
         mapper = Function.identity();
      }

      return (List)listSplitter.splitAsStream(string).filter((s) -> !s.isEmpty()).map(mapper).collect(Collectors.toList());
   }

   public static void splitToStringList(String string, @Nullable Function<String, String> mapper, @Nonnull Collection<String> result) {
      if (mapper == null) {
         mapper = Function.identity();
      }

      Stream var10000 = listSplitter.splitAsStream(string).filter((s) -> !s.isEmpty()).map(mapper);
      Objects.requireNonNull(result);
      var10000.forEachOrdered(result::add);
   }

   @Nonnull
   public static String stringListListToString(@Nonnull Collection<Collection<String>> list) {
      return (String)list.stream().map(StringListHelpers::stringListToString).collect(Collectors.joining("| "));
   }

   @Nonnull
   public static List<List<String>> splitToStringListList(@Nullable String string, Function<String, String> mapper) {
      return string != null && !string.isEmpty() ? (List)listListSplitter.splitAsStream(string).filter((s) -> !s.isEmpty()).map((s) -> splitToStringList(s, mapper)).filter((l) -> l != null && !l.isEmpty()).collect(Collectors.toList()) : Collections.emptyList();
   }

   public static void splitToStringListList(String string, Function<String, String> mapper, @Nonnull Collection<Collection<String>> result, @Nonnull Supplier<Collection<String>> supplier) {
      Stream var10000 = listListSplitter.splitAsStream(string).filter((s) -> !s.isEmpty()).map((s) -> {
         Collection<String> r = (Collection)supplier.get();
         splitToStringList(s, mapper, r);
         return r;
      }).filter((l) -> l != null && !l.isEmpty());
      Objects.requireNonNull(result);
      var10000.forEachOrdered(result::add);
   }

   @Nonnull
   public static Set<String> stringListToStringSet(@Nonnull List<String> list) {
      return new HashSet(list);
   }

   @Nonnull
   public static Set<String> splitToStringSet(@Nullable String input) {
      if (input != null && !input.isEmpty()) {
         List<String> list = splitToStringList(input, (Function)null);
         return new HashSet(list);
      } else {
         return Collections.emptySet();
      }
   }

   @Nonnull
   public static <T> Set<T> splitToStringSet(@Nullable String input, Function<String, T> transform) {
      return input != null && !input.isEmpty() ? (Set)splitToStringList(input, (Function)null).stream().map(transform).collect(Collectors.toSet()) : Collections.emptySet();
   }

   @Nonnull
   public static List<Set<String>> stringListListToStringSetList(@Nonnull List<List<String>> group) {
      return (List)group.stream().map(HashSet::new).collect(Collectors.toList());
   }
}
