package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum RelativeDirection {
   FORWARD,
   BACKWARD,
   LEFT,
   RIGHT,
   UP,
   DOWN;

   public static final SingleArgumentType<RelativeDirection> ARGUMENT_TYPE = new SingleArgumentType<RelativeDirection>("Relative Direction", "A direction relative to the player (forward, backward, left, right, up, down)", new String[]{"forward", "backward", "left", "right", "up", "down"}) {
      @Nullable
      public RelativeDirection parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         RelativeDirection var10000;
         switch (input.toLowerCase()) {
            case "forward":
            case "f":
               var10000 = RelativeDirection.FORWARD;
               break;
            case "backward":
            case "back":
            case "b":
               var10000 = RelativeDirection.BACKWARD;
               break;
            case "left":
            case "l":
               var10000 = RelativeDirection.LEFT;
               break;
            case "right":
            case "r":
               var10000 = RelativeDirection.RIGHT;
               break;
            case "up":
            case "u":
               var10000 = RelativeDirection.UP;
               break;
            case "down":
            case "d":
               var10000 = RelativeDirection.DOWN;
               break;
            default:
               parseResult.fail(Message.raw("Invalid direction: " + input + ". Use: forward, backward, left, right, up, down"));
               var10000 = null;
         }

         return var10000;
      }
   };

   @Nonnull
   public static Vector3i toDirectionVector(@Nullable RelativeDirection direction, @Nonnull HeadRotation headRotation) {
      if (direction == null) {
         return headRotation.getAxisDirection();
      } else {
         Vector3i var10000;
         switch (direction.ordinal()) {
            case 0 -> var10000 = headRotation.getHorizontalAxisDirection();
            case 1 -> var10000 = headRotation.getHorizontalAxisDirection().clone().scale(-1);
            case 2 -> var10000 = rotateLeft(headRotation.getHorizontalAxisDirection());
            case 3 -> var10000 = rotateRight(headRotation.getHorizontalAxisDirection());
            case 4 -> var10000 = new Vector3i(0, 1, 0);
            case 5 -> var10000 = new Vector3i(0, -1, 0);
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }
   }

   @Nonnull
   public static Axis toAxis(@Nonnull RelativeDirection direction, @Nonnull HeadRotation headRotation) {
      Axis var10000;
      switch (direction.ordinal()) {
         case 0:
         case 1:
            var10000 = getHorizontalAxis(headRotation);
            break;
         case 2:
         case 3:
            var10000 = getPerpendicularHorizontalAxis(headRotation);
            break;
         case 4:
         case 5:
            var10000 = Axis.Y;
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   private static Axis getHorizontalAxis(@Nonnull HeadRotation headRotation) {
      Vector3i horizontalDir = headRotation.getHorizontalAxisDirection();
      return horizontalDir.getX() != 0 ? Axis.X : Axis.Z;
   }

   @Nonnull
   private static Axis getPerpendicularHorizontalAxis(@Nonnull HeadRotation headRotation) {
      Vector3i horizontalDir = headRotation.getHorizontalAxisDirection();
      return horizontalDir.getX() != 0 ? Axis.Z : Axis.X;
   }

   @Nonnull
   private static Vector3i rotateLeft(@Nonnull Vector3i dir) {
      return new Vector3i(dir.z, 0, -dir.x);
   }

   @Nonnull
   private static Vector3i rotateRight(@Nonnull Vector3i dir) {
      return new Vector3i(-dir.z, 0, dir.x);
   }
}
