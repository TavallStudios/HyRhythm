package com.hypixel.hytale.server.core.auth;

import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultAuthCredentialStore implements IAuthCredentialStore {
   private IAuthCredentialStore.OAuthTokens tokens = new IAuthCredentialStore.OAuthTokens((String)null, (String)null, (Instant)null);
   @Nullable
   private UUID profile;

   public void setTokens(@Nonnull IAuthCredentialStore.OAuthTokens tokens) {
      this.tokens = tokens;
   }

   @Nonnull
   public IAuthCredentialStore.OAuthTokens getTokens() {
      return this.tokens;
   }

   public void setProfile(@Nullable UUID uuid) {
      this.profile = uuid;
   }

   @Nullable
   public UUID getProfile() {
      return this.profile;
   }

   public void clear() {
      this.tokens = new IAuthCredentialStore.OAuthTokens((String)null, (String)null, (Instant)null);
      this.profile = null;
   }
}
