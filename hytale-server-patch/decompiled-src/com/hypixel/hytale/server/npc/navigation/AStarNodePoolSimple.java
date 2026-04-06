package com.hypixel.hytale.server.npc.navigation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

public class AStarNodePoolSimple implements AStarNodePool {
   protected final List<AStarNode> nodePool = new ObjectArrayList();
   private final int childCount;

   public AStarNodePoolSimple(int childCount) {
      this.childCount = childCount;
   }

   public AStarNode allocate() {
      return this.nodePool.isEmpty() ? new AStarNode(this.childCount) : (AStarNode)this.nodePool.removeLast();
   }

   public void deallocate(AStarNode node) {
      this.nodePool.add(node);
   }
}
