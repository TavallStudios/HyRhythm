package com.hypixel.hytale.server.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum NameMatching {
   EXACT((s1, s2) -> s1.equals(s2) ? 0 : -2147483648, String::equals),
   EXACT_IGNORE_CASE((s1, s2) -> s1.equalsIgnoreCase(s2) ? 0 : -2147483648, String::equalsIgnoreCase),
   STARTS_WITH((s1, s2) -> s1.startsWith(s2) ? s1.length() - s2.length() : -2147483648, String::equals),
   STARTS_WITH_IGNORE_CASE((s1, s2) -> s1.toLowerCase().startsWith(s2.toLowerCase()) ? s1.length() - s2.length() : -2147483648, String::equalsIgnoreCase);

   @Nonnull
   public static NameMatching DEFAULT = STARTS_WITH_IGNORE_CASE;
   private final Comparator<String> comparator;
   private final BiPredicate<String, String> equality;

   private NameMatching(Comparator<String> comparator, BiPredicate<String, String> equality) {
      this.comparator = comparator;
      this.equality = equality;
   }

   public Comparator<String> getComparator() {
      return this.comparator;
   }

   @Nullable
   public <T> T find(@Nonnull Collection<T> players, String value, @Nonnull Function<T, String> getter) {
      return (T)find(players, value, getter, this.comparator, this.equality);
   }

   @Nullable
   public static <T> T find(@Nonnull Collection<T> players, String value, @Nonnull Function<T, String> getter, @Nonnull Comparator<String> comparator, @Nonnull BiPredicate<String, String> equality) {
      T closest = null;
      int highestScore = -2147483648;

      for(T player : players) {
         String name = (String)getter.apply(player);
         if (equality.test(name, value)) {
            return player;
         }

         int comparison = comparator.compare(name, value);
         if (comparison > highestScore) {
            highestScore = comparison;
            closest = player;
         }
      }

      if (highestScore == -2147483648) {
         return null;
      } else {
         return closest;
      }
   }
}
