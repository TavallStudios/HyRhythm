package com.hypixel.hytale.server.core.entity.entities.player;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.MouseButtonState;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class CameraManager implements Component<EntityStore> {
   private final Map<MouseButtonType, MouseButtonState> mouseStates;
   private final Map<MouseButtonType, Vector3i> mousePressedPosition;
   private final Map<MouseButtonType, Vector3i> mouseReleasedPosition;
   private Vector2d lastScreenPoint;
   private Vector3i lastTargetBlock;

   public static ComponentType<EntityStore, CameraManager> getComponentType() {
      return EntityModule.get().getCameraManagerComponentType();
   }

   public CameraManager() {
      this.mouseStates = new EnumMap(MouseButtonType.class);
      this.mousePressedPosition = new EnumMap(MouseButtonType.class);
      this.mouseReleasedPosition = new EnumMap(MouseButtonType.class);
      this.lastScreenPoint = Vector2d.ZERO;
   }

   public CameraManager(@Nonnull CameraManager other) {
      this();
      this.lastScreenPoint = other.lastScreenPoint;
      this.lastTargetBlock = other.lastTargetBlock;
   }

   public void resetCamera(@Nonnull PlayerRef ref) {
      ref.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, (ServerCameraSettings)null));
      this.mouseStates.clear();
   }

   public void handleMouseButtonState(MouseButtonType mouseButtonType, MouseButtonState state, Vector3i targetBlock) {
      this.mouseStates.put(mouseButtonType, state);
      if (state == MouseButtonState.Pressed) {
         this.mousePressedPosition.put(mouseButtonType, targetBlock);
      }

      if (state == MouseButtonState.Released) {
         this.mouseReleasedPosition.put(mouseButtonType, targetBlock);
      }

   }

   public MouseButtonState getMouseButtonState(MouseButtonType mouseButtonType) {
      return (MouseButtonState)this.mouseStates.getOrDefault(mouseButtonType, MouseButtonState.Released);
   }

   public Vector3i getLastMouseButtonPressedPosition(MouseButtonType mouseButtonType) {
      return (Vector3i)this.mousePressedPosition.get(mouseButtonType);
   }

   public Vector3i getLastMouseButtonReleasedPosition(MouseButtonType mouseButtonType) {
      return (Vector3i)this.mouseReleasedPosition.get(mouseButtonType);
   }

   public void setLastScreenPoint(Vector2d lastScreenPoint) {
      this.lastScreenPoint = lastScreenPoint;
   }

   public Vector2d getLastScreenPoint() {
      return this.lastScreenPoint;
   }

   public void setLastBlockPosition(Vector3i targetBlock) {
      this.lastTargetBlock = targetBlock;
   }

   public Vector3i getLastTargetBlock() {
      return this.lastTargetBlock;
   }

   @Nonnull
   public Component<EntityStore> clone() {
      return new CameraManager(this);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.mouseStates);
      return "CameraManager{mouseStates=" + var10000 + ", mousePressedPosition=" + String.valueOf(this.mousePressedPosition) + ", mouseReleasedPosition=" + String.valueOf(this.mouseReleasedPosition) + ", lastScreenPoint=" + String.valueOf(this.lastScreenPoint) + ", lastTargetBlock=" + String.valueOf(this.lastTargetBlock) + "}";
   }
}
