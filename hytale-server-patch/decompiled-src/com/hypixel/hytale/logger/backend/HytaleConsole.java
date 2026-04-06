package com.hypixel.hytale.logger.backend;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HytaleConsole extends Thread {
   public static final String TYPE_DUMB = "dumb";
   public static final HytaleConsole INSTANCE = new HytaleConsole();
   private final BlockingQueue<LogRecord> logRecords = new LinkedBlockingQueue();
   private final HytaleLogFormatter formatter = new HytaleLogFormatter(this::shouldPrintAnsi);
   @Nullable
   private OutputStreamWriter soutwriter;
   @Nullable
   private OutputStreamWriter serrwriter;
   private String terminalType;

   private HytaleConsole() {
      super("HytaleConsole");
      this.soutwriter = new OutputStreamWriter(HytaleLoggerBackend.REAL_SOUT, StandardCharsets.UTF_8);
      this.serrwriter = new OutputStreamWriter(HytaleLoggerBackend.REAL_SERR, StandardCharsets.UTF_8);
      this.terminalType = "dumb";
      this.setDaemon(true);
      this.start();
   }

   public void publish(@Nonnull LogRecord logRecord) {
      if (!this.isAlive()) {
         this.publish0(logRecord);
      } else {
         this.logRecords.offer(logRecord);
      }
   }

   public void run() {
      try {
         while(!this.isInterrupted()) {
            this.publish0((LogRecord)this.logRecords.take());
         }
      } catch (InterruptedException var2) {
         Thread.currentThread().interrupt();
      }

   }

   public void shutdown() {
      this.interrupt();

      try {
         this.join();
      } catch (InterruptedException var5) {
         Thread.currentThread().interrupt();
      }

      List<LogRecord> list = new ObjectArrayList();
      this.logRecords.drainTo(list);
      list.forEach(this::publish0);
      if (this.soutwriter != null) {
         try {
            this.soutwriter.flush();
         } catch (Exception var4) {
         }

         this.soutwriter = null;
      }

      if (this.serrwriter != null) {
         try {
            this.serrwriter.flush();
         } catch (Exception var3) {
         }

         this.serrwriter = null;
      }

   }

   private void publish0(@Nonnull LogRecord record) {
      String msg;
      try {
         msg = this.formatter.format(record);
      } catch (Exception ex) {
         if (this.serrwriter != null) {
            ex.printStackTrace(new PrintWriter(this.serrwriter));
         } else {
            ex.printStackTrace(System.err);
         }

         return;
      }

      try {
         if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
            if (this.serrwriter != null) {
               this.serrwriter.write(msg);

               try {
                  this.serrwriter.flush();
               } catch (Exception var5) {
               }
            } else {
               HytaleLoggerBackend.REAL_SERR.print(msg);
            }
         } else if (this.soutwriter != null) {
            this.soutwriter.write(msg);

            try {
               this.soutwriter.flush();
            } catch (Exception var4) {
            }
         } else {
            HytaleLoggerBackend.REAL_SOUT.print(msg);
         }
      } catch (Exception var6) {
      }

   }

   public void setTerminal(String type) {
      this.terminalType = type;
   }

   private boolean shouldPrintAnsi() {
      return !"dumb".equals(this.terminalType);
   }

   public HytaleLogFormatter getFormatter() {
      return this.formatter;
   }
}
