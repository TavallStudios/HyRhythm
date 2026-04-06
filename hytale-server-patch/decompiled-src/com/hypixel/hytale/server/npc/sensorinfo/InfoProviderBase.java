package com.hypixel.hytale.server.npc.sensorinfo;

import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.ParameterProvider;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class InfoProviderBase implements InfoProvider {
   protected final ParameterProvider parameterProvider;
   @Nullable
   protected final Map<Class<? extends ExtraInfoProvider>, ExtraInfoProvider> extraProviders;
   protected ExtraInfoProvider passedExtraInfo;

   public InfoProviderBase() {
      this((ParameterProvider)null);
   }

   public InfoProviderBase(ParameterProvider parameterProvider) {
      this.parameterProvider = parameterProvider;
      this.extraProviders = null;
   }

   public InfoProviderBase(ParameterProvider parameterProvider, @Nonnull ExtraInfoProvider... providers) {
      this.parameterProvider = parameterProvider;
      this.extraProviders = new HashMap();

      for(ExtraInfoProvider provider : providers) {
         if (this.extraProviders.put(provider.getType(), provider) != null) {
            throw new IllegalArgumentException("More than one type of " + provider.getType().getSimpleName() + " provider registered!");
         }
      }

   }

   @Nullable
   public ParameterProvider getParameterProvider(int parameter) {
      return this.parameterProvider == null ? null : this.parameterProvider.getParameterProvider(parameter);
   }

   @Nullable
   public <E extends ExtraInfoProvider> E getExtraInfo(Class<E> clazz) {
      return (E)(this.extraProviders == null ? null : (ExtraInfoProvider)this.extraProviders.get(clazz));
   }

   public <E extends ExtraInfoProvider> void passExtraInfo(E provider) {
      this.passedExtraInfo = provider;
   }

   public <E extends ExtraInfoProvider> E getPassedExtraInfo(Class<E> clazz) {
      return (E)this.passedExtraInfo;
   }
}
