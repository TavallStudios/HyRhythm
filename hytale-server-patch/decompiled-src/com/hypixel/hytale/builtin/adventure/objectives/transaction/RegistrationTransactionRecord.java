package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import javax.annotation.Nonnull;

public class RegistrationTransactionRecord extends TransactionRecord {
   protected BooleanConsumer registration;

   public RegistrationTransactionRecord(BooleanConsumer registration) {
      this.registration = registration;
   }

   public void revert() {
      this.registration.accept(false);
   }

   public void complete() {
      this.registration.accept(false);
   }

   public void unload() {
      this.registration.accept(false);
   }

   public boolean shouldBeSerialized() {
      return false;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.registration);
      return "RegistrationTransactionRecord{registration=" + var10000 + "} " + super.toString();
   }

   @Nonnull
   public static TransactionRecord[] wrap(@Nonnull EventRegistry registry) {
      BooleanConsumer[] registrations = (BooleanConsumer[])registry.getRegistrations().toArray((x$0) -> new BooleanConsumer[x$0]);
      TransactionRecord[] records = new TransactionRecord[registrations.length];
      int i = 0;

      for(BooleanConsumer registration : registrations) {
         records[i++] = new RegistrationTransactionRecord(registration);
      }

      return records;
   }

   @Nonnull
   public static TransactionRecord[] append(@Nonnull TransactionRecord[] arr, @Nonnull EventRegistry registry) {
      BooleanConsumer[] registrations = (BooleanConsumer[])registry.getRegistrations().toArray((x$0) -> new BooleanConsumer[x$0]);
      TransactionRecord[] records = new TransactionRecord[arr.length + registrations.length];
      System.arraycopy(arr, 0, records, 0, arr.length);
      int i = registrations.length;

      for(BooleanConsumer registration : registrations) {
         records[i++] = new RegistrationTransactionRecord(registration);
      }

      return records;
   }
}
