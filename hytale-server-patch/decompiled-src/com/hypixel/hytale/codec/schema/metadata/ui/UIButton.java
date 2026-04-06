package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class UIButton {
   public static final BuilderCodec<UIButton> CODEC;
   private String buttonId;
   private String textId;

   public UIButton(String textId, String buttonId) {
      this.textId = textId;
      this.buttonId = buttonId;
   }

   protected UIButton() {
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(UIButton.class, UIButton::new).append(new KeyedCodec("textId", Codec.STRING, false, true), (o, i) -> o.textId = i, (o) -> o.textId).add()).append(new KeyedCodec("buttonId", Codec.STRING, false, true), (o, i) -> o.buttonId = i, (o) -> o.buttonId).add()).build();
   }
}
