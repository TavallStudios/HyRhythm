package com.hypixel.hytale.common.util;

import com.hypixel.hytale.function.supplier.SupplierUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class HardwareUtil {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int PROCESS_TIMEOUT_SECONDS = 2;
   private static final Pattern UUID_PATTERN = Pattern.compile("([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");
   private static final Supplier<UUID> WINDOWS = SupplierUtil.<UUID>cache(() -> {
      String output = runCommand("reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid");
      if (output != null) {
         for(String line : output.split("\r?\n")) {
            if (line.contains("MachineGuid")) {
               Matcher matcher = UUID_PATTERN.matcher(line);
               if (matcher.find()) {
                  return UUID.fromString(matcher.group(1));
               }
            }
         }
      }

      output = runCommand("powershell", "-NoProfile", "-Command", "(Get-CimInstance -Class Win32_ComputerSystemProduct).UUID");
      if (output != null) {
         UUID uuid = parseUuidFromOutput(output);
         if (uuid != null) {
            return uuid;
         }
      }

      output = runCommand("wmic", "csproduct", "get", "UUID");
      if (output != null) {
         UUID uuid = parseUuidFromOutput(output);
         if (uuid != null) {
            return uuid;
         }
      }

      throw new RuntimeException("Failed to get hardware UUID for Windows - registry, PowerShell, and wmic all failed");
   });
   private static final Supplier<UUID> MAC = SupplierUtil.<UUID>cache(() -> {
      String output = runCommand("/usr/sbin/ioreg", "-rd1", "-c", "IOPlatformExpertDevice");
      if (output != null) {
         for(String line : output.split("\r?\n")) {
            if (line.contains("IOPlatformUUID")) {
               Matcher matcher = UUID_PATTERN.matcher(line);
               if (matcher.find()) {
                  return UUID.fromString(matcher.group(1));
               }
            }
         }
      }

      output = runCommand("/usr/sbin/system_profiler", "SPHardwareDataType");
      if (output != null) {
         for(String line : output.split("\r?\n")) {
            if (line.contains("Hardware UUID")) {
               Matcher matcher = UUID_PATTERN.matcher(line);
               if (matcher.find()) {
                  return UUID.fromString(matcher.group(1));
               }
            }
         }
      }

      throw new RuntimeException("Failed to get hardware UUID for macOS - ioreg and system_profiler both failed");
   });
   private static final Supplier<UUID> LINUX = SupplierUtil.<UUID>cache(() -> {
      UUID machineId = readMachineIdFile(Path.of("/etc/machine-id"));
      if (machineId != null) {
         return machineId;
      } else {
         machineId = readMachineIdFile(Path.of("/var/lib/dbus/machine-id"));
         if (machineId != null) {
            return machineId;
         } else {
            try {
               Path path = Path.of("/sys/class/dmi/id/product_uuid");
               if (Files.isReadable(path)) {
                  String content = Files.readString(path, StandardCharsets.UTF_8).trim();
                  if (!content.isEmpty()) {
                     return UUID.fromString(content);
                  }
               }
            } catch (Exception var3) {
            }

            String output = runCommand("dmidecode", "-s", "system-uuid");
            if (output != null) {
               UUID uuid = parseUuidFromOutput(output);
               if (uuid != null) {
                  return uuid;
               }
            }

            throw new RuntimeException("Failed to get hardware UUID for Linux - all methods failed");
         }
      }
   });

   @Nullable
   private static String runCommand(String... command) {
      try {
         Process process = (new ProcessBuilder(command)).start();
         if (process.waitFor(2L, TimeUnit.SECONDS)) {
            return (new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).trim();
         }

         process.destroyForcibly();
      } catch (Exception var2) {
      }

      return null;
   }

   @Nullable
   private static UUID parseUuidFromOutput(String output) {
      Matcher matcher = UUID_PATTERN.matcher(output);
      return matcher.find() ? UUID.fromString(matcher.group(1)) : null;
   }

   @Nullable
   private static UUID readMachineIdFile(Path path) {
      try {
         if (!Files.isReadable(path)) {
            return null;
         } else {
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            if (!content.isEmpty() && content.length() == 32) {
               String var10000 = content.substring(0, 8);
               return UUID.fromString(var10000 + "-" + content.substring(8, 12) + "-" + content.substring(12, 16) + "-" + content.substring(16, 20) + "-" + content.substring(20, 32));
            } else {
               return null;
            }
         }
      } catch (Exception var2) {
         return null;
      }
   }

   @Nullable
   public static UUID getUUID() {
      try {
         UUID var10000;
         switch (SystemUtil.TYPE) {
            case WINDOWS -> var10000 = (UUID)WINDOWS.get();
            case LINUX -> var10000 = (UUID)LINUX.get();
            case MACOS -> var10000 = (UUID)MAC.get();
            case OTHER -> throw new RuntimeException("Unknown OS!");
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      } catch (Exception e) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(e)).log("Failed to get Hardware UUID");
         return null;
      }
   }
}
