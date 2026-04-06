package com.hypixel.hytale.server.npc.decisionmaker.stateevaluator;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.npc.decisionmaker.core.Option;
import javax.annotation.Nonnull;

public class StateOption extends Option {
   public static final BuilderCodec<StateOption> CODEC;
   protected String state;
   protected String subState;
   protected int stateIndex;
   protected int subStateIndex;

   protected StateOption() {
   }

   public String getState() {
      return this.state;
   }

   public String getSubState() {
      return this.subState;
   }

   public int getStateIndex() {
      return this.stateIndex;
   }

   public int getSubStateIndex() {
      return this.subStateIndex;
   }

   public void setStateIndex(int stateIndex, int subStateIndex) {
      this.stateIndex = stateIndex;
      this.subStateIndex = subStateIndex;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.state;
      return "StateOption{state=" + var10000 + ", stateIndex=" + this.stateIndex + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(StateOption.class, StateOption::new, Option.ABSTRACT_CODEC).append(new KeyedCodec("State", Codec.STRING), (option, s) -> option.state = s, (option) -> option.state).documentation("The main state name.").addValidator(Validators.nonNull()).addValidator(Validators.nonEmptyString()).add()).append(new KeyedCodec("SubState", Codec.STRING), (option, s) -> option.state = s, (option) -> option.state).documentation("The (optional) substate name.").add()).build();
   }
}
