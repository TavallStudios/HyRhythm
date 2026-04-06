package com.hypixel.hytale.builtin.hytalegenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class Registry<T> {
   private Map<T, Integer> objectToId = new HashMap();
   private Map<Integer, T> idToObject = new HashMap();

   public int getIdOrRegister(T object) {
      Integer id = (Integer)this.objectToId.get(object);
      if (id != null) {
         return id;
      } else {
         id = this.objectToId.size();
         this.idToObject.put(id, object);
         this.objectToId.put(object, id);
         return id;
      }
   }

   public T getObject(int id) {
      return (T)this.idToObject.get(id);
   }

   public int size() {
      return this.objectToId.size();
   }

   @Nonnull
   public List<T> getAllValues() {
      return new ArrayList(this.idToObject.values());
   }

   public void forEach(@Nonnull BiConsumer<Integer, T> consumer) {
      Map var10000 = this.idToObject;
      Objects.requireNonNull(consumer);
      var10000.forEach(consumer::accept);
   }
}
