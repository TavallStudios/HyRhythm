package com.hypixel.hytale.server.core.modules.blockhealth;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.UpdateBlockDamage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class BlockHealthChunk implements Component<ChunkStore> {
   private static final byte SERIALIZATION_VERSION = 2;
   public static final BuilderCodec<BlockHealthChunk> CODEC;
   private final Map<Vector3i, BlockHealth> blockHealthMap = new Object2ObjectOpenHashMap(0);
   private final Map<Vector3i, FragileBlock> blockFragilityMap = new Object2ObjectOpenHashMap(0);
   private Instant lastRepairGameTime;

   public Instant getLastRepairGameTime() {
      return this.lastRepairGameTime;
   }

   public void setLastRepairGameTime(Instant lastRepairGameTime) {
      this.lastRepairGameTime = lastRepairGameTime;
   }

   @Nonnull
   public Map<Vector3i, BlockHealth> getBlockHealthMap() {
      return this.blockHealthMap;
   }

   @Nonnull
   public Map<Vector3i, FragileBlock> getBlockFragilityMap() {
      return this.blockFragilityMap;
   }

   @Nonnull
   public BlockHealth damageBlock(Instant currentUptime, @Nonnull World world, @Nonnull Vector3i block, float health) {
      BlockHealth blockHealth = (BlockHealth)this.blockHealthMap.compute(block, (key, value) -> {
         if (value == null) {
            value = new BlockHealth();
         }

         value.setHealth(value.getHealth() - health);
         value.setLastDamageGameTime(currentUptime);
         return (double)value.getHealth() < 1.0 ? value : null;
      });
      if (blockHealth != null && !blockHealth.isDestroyed()) {
         Predicate<PlayerRef> filter = (player) -> true;
         world.getNotificationHandler().updateBlockDamage(block.getX(), block.getY(), block.getZ(), blockHealth.getHealth(), -health, filter);
      }

      return (BlockHealth)Objects.requireNonNullElse(blockHealth, BlockHealth.NO_DAMAGE_INSTANCE);
   }

   @Nonnull
   public BlockHealth repairBlock(@Nonnull World world, @Nonnull Vector3i block, float progress) {
      BlockHealth blockHealth = (BlockHealth)Objects.requireNonNullElse((BlockHealth)this.blockHealthMap.computeIfPresent(block, (key, value) -> {
         value.setHealth(value.getHealth() + progress);
         return (double)value.getHealth() > 1.0 ? value : null;
      }), BlockHealth.NO_DAMAGE_INSTANCE);
      world.getNotificationHandler().updateBlockDamage(block.getX(), block.getY(), block.getZ(), blockHealth.getHealth(), progress);
      return blockHealth;
   }

   public void removeBlock(@Nonnull World world, @Nonnull Vector3i block) {
      if (this.blockHealthMap.remove(block) != null) {
         world.getNotificationHandler().updateBlockDamage(block.getX(), block.getY(), block.getZ(), BlockHealth.NO_DAMAGE_INSTANCE.getHealth(), 0.0F);
      }

   }

   public void makeBlockFragile(Vector3i blockLocation, float fragileDuration) {
      this.blockFragilityMap.compute(blockLocation, (key, value) -> {
         if (value == null) {
            value = new FragileBlock(fragileDuration);
         }

         value.setDurationSeconds(fragileDuration);
         return (double)value.getDurationSeconds() <= 0.0 ? null : value;
      });
   }

   public boolean isBlockFragile(Vector3i block) {
      return this.blockFragilityMap.get(block) != null;
   }

   public float getBlockHealth(Vector3i block) {
      return ((BlockHealth)this.blockHealthMap.getOrDefault(block, BlockHealth.NO_DAMAGE_INSTANCE)).getHealth();
   }

   public void createBlockDamagePackets(@Nonnull List<ToClientPacket> list) {
      for(Map.Entry<Vector3i, BlockHealth> entry : this.blockHealthMap.entrySet()) {
         Vector3i block = (Vector3i)entry.getKey();
         BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
         list.add(new UpdateBlockDamage(blockPosition, ((BlockHealth)entry.getValue()).getHealth(), 0.0F));
      }

   }

   @Nonnull
   public BlockHealthChunk clone() {
      BlockHealthChunk copy = new BlockHealthChunk();
      copy.lastRepairGameTime = this.lastRepairGameTime;

      for(Map.Entry<Vector3i, BlockHealth> entry : this.blockHealthMap.entrySet()) {
         copy.blockHealthMap.put((Vector3i)entry.getKey(), ((BlockHealth)entry.getValue()).clone());
      }

      for(Map.Entry<Vector3i, FragileBlock> entry : this.blockFragilityMap.entrySet()) {
         copy.blockFragilityMap.put((Vector3i)entry.getKey(), ((FragileBlock)entry.getValue()).clone());
      }

      return copy;
   }

   public void deserialize(@Nonnull byte[] data) {
      this.blockHealthMap.clear();
      ByteBuf buf = Unpooled.wrappedBuffer(data);
      byte version = buf.readByte();
      int healthEntries = buf.readInt();

      for(int i = 0; i < healthEntries; ++i) {
         int x = buf.readInt();
         int y = buf.readInt();
         int z = buf.readInt();
         BlockHealth bh = new BlockHealth();
         bh.deserialize(buf, version);
         this.blockHealthMap.put(new Vector3i(x, y, z), bh);
      }

      if (version > 1) {
         int fragilityEntries = buf.readInt();

         for(int i = 0; i < fragilityEntries; ++i) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            FragileBlock fragileBlock = new FragileBlock();
            fragileBlock.deserialize(buf, version);
            this.blockFragilityMap.put(new Vector3i(x, y, z), fragileBlock);
         }
      }

   }

   public byte[] serialize() {
      ByteBuf buf = Unpooled.buffer();
      buf.writeByte(2);
      buf.writeInt(this.blockHealthMap.size());

      for(Map.Entry<Vector3i, BlockHealth> entry : this.blockHealthMap.entrySet()) {
         Vector3i vec = (Vector3i)entry.getKey();
         buf.writeInt(vec.x);
         buf.writeInt(vec.y);
         buf.writeInt(vec.z);
         BlockHealth bh = (BlockHealth)entry.getValue();
         bh.serialize(buf);
      }

      buf.writeInt(this.blockFragilityMap.size());

      for(Map.Entry<Vector3i, FragileBlock> entry : this.blockFragilityMap.entrySet()) {
         Vector3i vec = (Vector3i)entry.getKey();
         buf.writeInt(vec.x);
         buf.writeInt(vec.y);
         buf.writeInt(vec.z);
         ((FragileBlock)entry.getValue()).serialize(buf);
      }

      return ByteBufUtil.getBytesRelease(buf);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(BlockHealthChunk.class, BlockHealthChunk::new).append(new KeyedCodec("Data", Codec.BYTE_ARRAY), BlockHealthChunk::deserialize, BlockHealthChunk::serialize).documentation("Binary data representing the state of this BlockHealthChunk").add()).append(new KeyedCodec("LastRepairGameTime", Codec.INSTANT), (o, l) -> o.lastRepairGameTime = l, (o) -> o.lastRepairGameTime).documentation("The last tick of the world this BlockHealthChunk processed.").add()).build();
   }
}
