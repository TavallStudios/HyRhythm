package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.MovementType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.ViewBobbing;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateViewBobbing implements Packet, ToClientPacket {
   public static final int PACKET_ID = 76;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type;
   @Nullable
   public Map<MovementType, ViewBobbing> profiles;

   public int getId() {
      return 76;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateViewBobbing() {
      this.type = UpdateType.Init;
   }

   public UpdateViewBobbing(@Nonnull UpdateType type, @Nullable Map<MovementType, ViewBobbing> profiles) {
      this.type = UpdateType.Init;
      this.type = type;
      this.profiles = profiles;
   }

   public UpdateViewBobbing(@Nonnull UpdateViewBobbing other) {
      this.type = UpdateType.Init;
      this.type = other.type;
      this.profiles = other.profiles;
   }

   @Nonnull
   public static UpdateViewBobbing deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateViewBobbing obj = new UpdateViewBobbing();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int profilesCount = VarInt.peek(buf, pos);
         if (profilesCount < 0) {
            throw ProtocolException.negativeLength("Profiles", profilesCount);
         }

         if (profilesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Profiles", profilesCount, 4096000);
         }

         pos += VarInt.size(profilesCount);
         obj.profiles = new HashMap(profilesCount);

         for(int i = 0; i < profilesCount; ++i) {
            MovementType key = MovementType.fromValue(buf.getByte(pos));
            ++pos;
            ViewBobbing val = ViewBobbing.deserialize(buf, pos);
            pos += ViewBobbing.computeBytesConsumed(buf, pos);
            if (obj.profiles.put(key, val) != null) {
               throw ProtocolException.duplicateKey("profiles", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for(int i = 0; i < dictLen; ++i) {
            ++pos;
            pos += ViewBobbing.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.profiles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.profiles != null) {
         if (this.profiles.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Profiles", this.profiles.size(), 4096000);
         }

         VarInt.write(buf, this.profiles.size());

         for(Map.Entry<MovementType, ViewBobbing> e : this.profiles.entrySet()) {
            buf.writeByte(((MovementType)e.getKey()).getValue());
            ((ViewBobbing)e.getValue()).serialize(buf);
         }
      }

   }

   public int computeSize() {
      int size = 2;
      if (this.profiles != null) {
         int profilesSize = 0;

         for(Map.Entry<MovementType, ViewBobbing> kvp : this.profiles.entrySet()) {
            profilesSize += 1 + ((ViewBobbing)kvp.getValue()).computeSize();
         }

         size += VarInt.size(this.profiles.size()) + profilesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int profilesCount = VarInt.peek(buffer, pos);
            if (profilesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Profiles");
            }

            if (profilesCount > 4096000) {
               return ValidationResult.error("Profiles exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for(int i = 0; i < profilesCount; ++i) {
               ++pos;
               pos += ViewBobbing.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateViewBobbing clone() {
      UpdateViewBobbing copy = new UpdateViewBobbing();
      copy.type = this.type;
      if (this.profiles != null) {
         Map<MovementType, ViewBobbing> m = new HashMap();

         for(Map.Entry<MovementType, ViewBobbing> e : this.profiles.entrySet()) {
            m.put((MovementType)e.getKey(), ((ViewBobbing)e.getValue()).clone());
         }

         copy.profiles = m;
      }

      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof UpdateViewBobbing)) {
         return false;
      } else {
         UpdateViewBobbing other = (UpdateViewBobbing)obj;
         return Objects.equals(this.type, other.type) && Objects.equals(this.profiles, other.profiles);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.profiles});
   }
}
