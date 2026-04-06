package com.hypixel.hytale.assetstore.map;

import it.unimi.dsi.fastutil.Hash;

public class CaseInsensitiveHashStrategy<K> implements Hash.Strategy<K> {
   private static final CaseInsensitiveHashStrategy INSTANCE = new CaseInsensitiveHashStrategy();

   public static <K> CaseInsensitiveHashStrategy<K> getInstance() {
      return INSTANCE;
   }

   public int hashCode(K key) {
      if (key == null) {
         return 0;
      } else if (!(key instanceof String)) {
         return key.hashCode();
      } else {
         String s = (String)key;
         int hash = 0;

         for(int i = 0; i < s.length(); ++i) {
            hash = 31 * hash + Character.toLowerCase(s.charAt(i));
         }

         return hash;
      }
   }

   public boolean equals(K a, K b) {
      if (a == b) {
         return true;
      } else if (a != null && b != null) {
         if (a instanceof String) {
            String sa = (String)a;
            if (b instanceof String) {
               String sb = (String)b;
               return sa.equalsIgnoreCase(sb);
            }
         }

         return a.equals(b);
      } else {
         return false;
      }
   }
}
