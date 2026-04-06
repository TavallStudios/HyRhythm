package com.hypixel.hytale.server.core.modules.interaction.interaction.config.data;

import com.hypixel.hytale.function.function.TriFunction;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class TreeCollector<T> implements Collector {
   private final TriFunction<CollectorTag, InteractionContext, Interaction, T> function;
   private Node<T> root;
   private Node<T> current;

   public TreeCollector(TriFunction<CollectorTag, InteractionContext, Interaction, T> function) {
      this.function = function;
   }

   public Node<T> getRoot() {
      return this.root;
   }

   public void start() {
      this.root = new Node<T>((Node)null);
      this.current = this.root;
   }

   public void into(@Nonnull InteractionContext context, Interaction interaction) {
      if (this.current.children != null) {
         this.current.children = (Node[])Arrays.copyOf(this.current.children, this.current.children.length + 1);
      } else {
         this.current.children = new Node[1];
      }

      this.current = this.current.children[this.current.children.length - 1] = new Node(this.current);
   }

   public boolean collect(@Nonnull CollectorTag tag, @Nonnull InteractionContext context, @Nonnull Interaction interaction) {
      this.current.data = this.function.apply(tag, context, interaction);
      return false;
   }

   public void outof() {
      this.current = this.current.parent;
   }

   public void finished() {
   }

   public static class Node<T> {
      public static final Node[] EMPTY_ARRAY = new Node[0];
      private final Node<T> parent;
      private Node<T>[] children;
      private T data;

      Node(Node<T> parent) {
         this.children = EMPTY_ARRAY;
         this.parent = parent;
      }

      public Node<T> getParent() {
         return this.parent;
      }

      public Node<T>[] getChildren() {
         return this.children;
      }

      public T getData() {
         return this.data;
      }
   }
}
