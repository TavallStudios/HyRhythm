package com.hypixel.hytale.server.worldgen.chunk;

public interface BlockPriorityModifier {
   BlockPriorityModifier NONE = new BlockPriorityModifier() {
      public byte modifyCurrent(byte current, byte target) {
         return current;
      }

      public byte modifyTarget(byte original, byte target) {
         return target;
      }
   };

   byte modifyCurrent(byte var1, byte var2);

   byte modifyTarget(byte var1, byte var2);
}
