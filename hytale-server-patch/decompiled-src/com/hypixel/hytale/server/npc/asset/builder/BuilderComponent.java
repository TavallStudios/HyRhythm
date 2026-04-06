package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.BooleanSchema;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.instructions.Motion;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderComponent<T> extends BuilderBase<T> {
   private final Class<T> classType;
   @Nonnull
   private final BuilderObjectReferenceHelper<T> content;

   public BuilderComponent(Class<T> classType) {
      this.classType = classType;
      this.content = new BuilderObjectReferenceHelper<T>(classType, (BuilderContext)null);
   }

   @Nullable
   public String getShortDescription() {
      return null;
   }

   @Nullable
   public String getLongDescription() {
      return null;
   }

   public T build(@Nonnull BuilderSupport builderSupport) {
      return this.content.build(builderSupport);
   }

   public Class<T> category() {
      return this.classType;
   }

   @Nullable
   public BuilderDescriptorState getBuilderDescriptorState() {
      return null;
   }

   public boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Nonnull
   public Builder<T> readConfig(@Nonnull JsonElement data) {
      this.requireObject(data, "Content", this.content, (BuilderDescriptorState)null, (String)null, (String)null, this.validationHelper);
      return this;
   }

   public boolean validate(String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, @Nonnull ExecutionContext context, Scope globalScope, @Nonnull List<String> errors) {
      return super.validate(configName, validationHelper, context, globalScope, errors) & this.content.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   public boolean canRequireFeature() {
      return this.classType.isAssignableFrom(Action.class) || this.classType.isAssignableFrom(Motion.class);
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context) {
      ObjectSchema s = (ObjectSchema)super.toSchema(context);
      Map<String, Schema> props = s.getProperties();
      props.put("Class", new StringSchema());
      props.put("Interface", new StringSchema());
      props.put("Default", new StringSchema());
      props.put("DefaultState", new StringSchema());
      props.put("ResetOnStateChange", new BooleanSchema());
      return s;
   }
}
