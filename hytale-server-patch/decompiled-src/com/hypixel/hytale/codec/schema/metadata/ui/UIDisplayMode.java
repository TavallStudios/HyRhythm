package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UIDisplayMode implements Metadata {
   public static final UIDisplayMode NORMAL;
   public static final UIDisplayMode COMPACT;
   public static final UIDisplayMode HIDDEN;
   private final DisplayMode mode;

   private UIDisplayMode(DisplayMode mode) {
      this.mode = mode;
   }

   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiDisplayMode(this.mode);
   }

   static {
      NORMAL = new UIDisplayMode(UIDisplayMode.DisplayMode.NORMAL);
      COMPACT = new UIDisplayMode(UIDisplayMode.DisplayMode.COMPACT);
      HIDDEN = new UIDisplayMode(UIDisplayMode.DisplayMode.HIDDEN);
   }

   public static enum DisplayMode {
      NORMAL,
      COMPACT,
      HIDDEN;
   }
}
