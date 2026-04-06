package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UIEditor implements Metadata {
   public static final CodecMapCodec<EditorComponent> CODEC = new CodecMapCodec<EditorComponent>("component");
   public static final Timeline TIMELINE = new Timeline();
   public static final WeightedTimeline WEIGHTED_TIMELINE = new WeightedTimeline();
   private final EditorComponent component;

   public UIEditor(EditorComponent component) {
      this.component = component;
   }

   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiEditorComponent(this.component);
   }

   public static void init() {
      CODEC.register((String)"Timeline", Timeline.class, UIEditor.Timeline.CODEC);
      CODEC.register((String)"WeightedTimeline", WeightedTimeline.class, UIEditor.WeightedTimeline.CODEC);
      CODEC.register((String)"Number", FormattedNumber.class, UIEditor.FormattedNumber.CODEC);
      CODEC.register((String)"Text", TextField.class, UIEditor.TextField.CODEC);
      CODEC.register((String)"MultilineText", MultilineTextField.class, UIEditor.MultilineTextField.CODEC);
      CODEC.register((String)"Dropdown", Dropdown.class, UIEditor.Dropdown.CODEC);
      CODEC.register((String)"Icon", Icon.class, UIEditor.Icon.CODEC);
      CODEC.register((String)"LocalizationKey", LocalizationKeyField.class, UIEditor.LocalizationKeyField.CODEC);
   }

   public static class Timeline implements EditorComponent {
      public static final BuilderCodec<Timeline> CODEC = BuilderCodec.builder(Timeline.class, Timeline::new).build();
   }

   public static class Icon implements EditorComponent {
      public static final BuilderCodec<Icon> CODEC;
      private String defaultPathTemplate;
      private int width;
      private int height;

      public Icon(String defaultPathTemplate, int width, int height) {
         this.defaultPathTemplate = defaultPathTemplate;
         this.width = width;
         this.height = height;
      }

      public Icon() {
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Icon.class, Icon::new).addField(new KeyedCodec("defaultPathTemplate", Codec.STRING, true, true), (o, i) -> o.defaultPathTemplate = i, (o) -> o.defaultPathTemplate)).addField(new KeyedCodec("width", Codec.INTEGER, true, true), (o, i) -> o.width = i, (o) -> o.width)).addField(new KeyedCodec("height", Codec.INTEGER, true, true), (o, i) -> o.height = i, (o) -> o.height)).build();
      }
   }

   public static class WeightedTimeline implements EditorComponent {
      public static final BuilderCodec<WeightedTimeline> CODEC = BuilderCodec.builder(WeightedTimeline.class, WeightedTimeline::new).build();
   }

   public static class FormattedNumber implements EditorComponent {
      public static final BuilderCodec<FormattedNumber> CODEC;
      private Double step;
      private String suffix;
      private Integer maxDecimalPlaces;

      public FormattedNumber(Double step, String suffix, Integer maxDecimalPlaces) {
         this.step = step;
         this.suffix = suffix;
         this.maxDecimalPlaces = maxDecimalPlaces;
      }

      public FormattedNumber() {
      }

      @Nonnull
      public FormattedNumber setStep(Double step) {
         this.step = step;
         return this;
      }

      @Nonnull
      public FormattedNumber setSuffix(String suffix) {
         this.suffix = suffix;
         return this;
      }

      @Nonnull
      public FormattedNumber setMaxDecimalPlaces(Integer maxDecimalPlaces) {
         this.maxDecimalPlaces = maxDecimalPlaces;
         return this;
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(FormattedNumber.class, FormattedNumber::new).addField(new KeyedCodec("step", Codec.DOUBLE, false, true), (o, i) -> o.step = i, (o) -> o.step)).addField(new KeyedCodec("suffix", Codec.STRING, false, true), (o, i) -> o.suffix = i, (o) -> o.suffix)).addField(new KeyedCodec("maxDecimalPlaces", Codec.INTEGER, false, true), (o, i) -> o.maxDecimalPlaces = i, (o) -> o.maxDecimalPlaces)).build();
      }
   }

   public static class Dropdown implements EditorComponent {
      public static final BuilderCodec<Dropdown> CODEC;
      private String dataSet;

      public Dropdown(String dataSet) {
         this.dataSet = dataSet;
      }

      protected Dropdown() {
      }

      static {
         CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(Dropdown.class, Dropdown::new).addField(new KeyedCodec("dataSet", Codec.STRING, false, true), (o, i) -> o.dataSet = i, (o) -> o.dataSet)).build();
      }
   }

   public static class TextField implements EditorComponent {
      public static final BuilderCodec<TextField> CODEC;
      private String dataSet;

      public TextField(String dataSet) {
         this.dataSet = dataSet;
      }

      protected TextField() {
      }

      static {
         CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(TextField.class, TextField::new).addField(new KeyedCodec("dataSet", Codec.STRING, false, true), (o, i) -> o.dataSet = i, (o) -> o.dataSet)).build();
      }
   }

   public static class MultilineTextField implements EditorComponent {
      public static final BuilderCodec<MultilineTextField> CODEC = BuilderCodec.builder(MultilineTextField.class, MultilineTextField::new).build();
   }

   public static class LocalizationKeyField implements EditorComponent {
      public static final BuilderCodec<LocalizationKeyField> CODEC;
      private String keyTemplate;
      private boolean generateDefaultKey;

      public LocalizationKeyField(String keyTemplate) {
         this(keyTemplate, false);
      }

      public LocalizationKeyField(String keyTemplate, boolean generateDefaultKey) {
         this.keyTemplate = keyTemplate;
         this.generateDefaultKey = generateDefaultKey;
      }

      public LocalizationKeyField() {
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(LocalizationKeyField.class, LocalizationKeyField::new).addField(new KeyedCodec("keyTemplate", Codec.STRING, false, true), (o, i) -> o.keyTemplate = i, (o) -> o.keyTemplate)).addField(new KeyedCodec("generateDefaultKey", Codec.BOOLEAN, false, true), (o, i) -> o.generateDefaultKey = i, (o) -> o.generateDefaultKey)).build();
      }
   }

   public interface EditorComponent {
   }
}
