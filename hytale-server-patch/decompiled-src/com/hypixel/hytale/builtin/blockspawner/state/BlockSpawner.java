package com.hypixel.hytale.builtin.blockspawner.state;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSpawner implements Component<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<BlockSpawner> CODEC;
   private String blockSpawnerId;

   public static ComponentType<ChunkStore, BlockSpawner> getComponentType() {
      return BlockSpawnerPlugin.get().getBlockSpawnerComponentType();
   }

   public BlockSpawner() {
   }

   public BlockSpawner(String blockSpawnerId) {
      this.blockSpawnerId = blockSpawnerId;
   }

   public String getBlockSpawnerId() {
      return this.blockSpawnerId;
   }

   public void setBlockSpawnerId(String blockSpawnerId) {
      this.blockSpawnerId = blockSpawnerId;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.blockSpawnerId;
      return "BlockSpawnerState{blockSpawnerId='" + var10000 + "'} " + super.toString();
   }

   @Nullable
   public Component<ChunkStore> clone() {
      return new BlockSpawner(this.blockSpawnerId);
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(BlockSpawner.class, BlockSpawner::new).addField(new KeyedCodec("BlockSpawnerId", Codec.STRING), (state, s) -> state.blockSpawnerId = s, (state) -> state.blockSpawnerId)).build();
   }
}
