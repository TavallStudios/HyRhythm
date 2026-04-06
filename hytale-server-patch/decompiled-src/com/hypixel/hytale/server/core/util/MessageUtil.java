package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.protocol.BoolParamValue;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.DoubleParamValue;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.IntParamValue;
import com.hypixel.hytale.protocol.LongParamValue;
import com.hypixel.hytale.protocol.ParamValue;
import com.hypixel.hytale.protocol.StringParamValue;
import com.hypixel.hytale.protocol.packets.asseteditor.FailureReply;
import com.hypixel.hytale.protocol.packets.asseteditor.SuccessReply;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Colors;

public class MessageUtil {
   private static final String[] ICU_PLURAL_KEYWORDS = new String[]{"zero", "one", "two", "few", "many", "other"};

   public static AttributedString toAnsiString(@Nonnull Message message) {
      AttributedStyle style = AttributedStyle.DEFAULT;
      String color = message.getColor();
      if (color != null) {
         style = hexToStyle(color);
      }

      AttributedStringBuilder sb = new AttributedStringBuilder();
      sb.style(style).append(message.getAnsiMessage());

      for(Message child : message.getChildren()) {
         sb.append(toAnsiString(child));
      }

      return sb.toAttributedString();
   }

   public static AttributedStyle hexToStyle(@Nonnull String str) {
      Color color = ColorParseUtil.parseColor(str);
      if (color == null) {
         return AttributedStyle.DEFAULT;
      } else {
         int colorId = Colors.roundRgbColor(color.red & 255, color.green & 255, color.blue & 255, 256);
         return AttributedStyle.DEFAULT.foreground(colorId);
      }
   }

   /** @deprecated */
   @Deprecated
   public static void sendSuccessReply(@Nonnull PlayerRef playerRef, int token) {
      sendSuccessReply(playerRef, token, (Message)null);
   }

   /** @deprecated */
   @Deprecated
   public static void sendSuccessReply(@Nonnull PlayerRef playerRef, int token, @Nullable Message message) {
      FormattedMessage msg = message != null ? message.getFormattedMessage() : null;
      playerRef.getPacketHandler().writeNoCache(new SuccessReply(token, msg));
   }

   /** @deprecated */
   @Deprecated
   public static void sendFailureReply(@Nonnull PlayerRef playerRef, int token, @Nonnull Message message) {
      FormattedMessage msg = message != null ? message.getFormattedMessage() : null;
      playerRef.getPacketHandler().writeNoCache(new FailureReply(token, msg));
   }

   @Nonnull
   public static String formatText(String text, @Nullable Map<String, ParamValue> params, @Nullable Map<String, FormattedMessage> messageParams) {
      if (text == null) {
         throw new IllegalArgumentException("text cannot be null");
      } else if (params == null && messageParams == null) {
         return text;
      } else {
         int len = text.length();
         StringBuilder sb = new StringBuilder(text.length());
         int lastWritePos = 0;

         for(int i = 0; i < len; ++i) {
            char ch = text.charAt(i);
            if (ch == '{') {
               if (i + 1 < len && text.charAt(i + 1) == '{') {
                  if (i > lastWritePos) {
                     sb.append(text, lastWritePos, i);
                  }

                  sb.append('{');
                  ++i;
                  lastWritePos = i + 1;
               } else {
                  int end = findMatchingBrace(text, i);
                  if (end >= 0) {
                     if (i > lastWritePos) {
                        sb.append(text, lastWritePos, i);
                     }

                     int contentStart = i + 1;
                     int c1 = text.indexOf(44, contentStart, end);
                     int c2 = c1 >= 0 ? text.indexOf(44, c1 + 1, end) : -1;
                     int nameEndExclusive = c1 >= 0 && c1 < end ? c1 : end;
                     int ns = trimStart(text, contentStart, nameEndExclusive - 1);
                     int nl = trimEnd(text, ns, nameEndExclusive - 1);
                     String key = nl > 0 ? text.substring(ns, ns + nl) : "";
                     String format = null;
                     if (c1 >= 0 && c1 < end) {
                        int formatStart = c1 + 1;
                        int formatEndExclusive = c2 >= 0 ? c2 : end;
                        int fs = trimStart(text, formatStart, formatEndExclusive - 1);
                        int fl = trimEnd(text, fs, formatEndExclusive - 1);
                        if (fl > 0) {
                           format = text.substring(fs, fs + fl);
                        }
                     }

                     String options = null;
                     if (c2 >= 0 && c2 < end) {
                        int optionsStart = c2 + 1;
                        int os = trimStart(text, optionsStart, end - 1);
                        int ol = trimEnd(text, os, end - 1);
                        if (ol > 0) {
                           options = text.substring(os, os + ol);
                        }
                     }

                     ParamValue replacement = params != null ? (ParamValue)params.get(key) : null;
                     FormattedMessage replacementMessage = messageParams != null ? (FormattedMessage)messageParams.get(key) : null;
                     if (replacementMessage != null) {
                        if (replacementMessage.rawText != null) {
                           sb.append(replacementMessage.rawText);
                        } else if (replacementMessage.messageId != null) {
                           String message = I18nModule.get().getMessage("en-US", replacementMessage.messageId);
                           if (message != null) {
                              sb.append(formatText(message, replacementMessage.params, replacementMessage.messageParams));
                           } else {
                              sb.append(replacementMessage.messageId);
                           }
                        }
                     } else if (replacement != null) {
                        String formattedReplacement;
                        formattedReplacement = "";
                        byte var23 = 0;
                        label171:
                        //$FF: var23->value
                        //0->upper
                        //1->lower
                        //2->number
                        //3->plural
                        //4->date
                        //5->time
                        //6->select
                        switch (format.typeSwitch<invokedynamic>(format, var23)) {
                           case -1:
                           default:
                              break;
                           case 0:
                              if (replacement instanceof StringParamValue) {
                                 StringParamValue s = (StringParamValue)replacement;
                                 formattedReplacement = s.value.toUpperCase();
                              }
                              break;
                           case 1:
                              if (replacement instanceof StringParamValue) {
                                 StringParamValue s = (StringParamValue)replacement;
                                 formattedReplacement = s.value.toLowerCase();
                              }
                              break;
                           case 2:
                              byte var51 = 0;
                              //$FF: var51->value
                              //0->integer
                              //1->decimal
                              switch (options.typeSwitch<invokedynamic>(options, var51)) {
                                 case -1:
                                 case 1:
                                 default:
                                    Objects.requireNonNull(replacement);
                                    byte var56 = 0;
                                    String var64;
                                    //$FF: var56->value
                                    //0->com/hypixel/hytale/protocol/StringParamValue
                                    //1->com/hypixel/hytale/protocol/BoolParamValue
                                    //2->com/hypixel/hytale/protocol/DoubleParamValue
                                    //3->com/hypixel/hytale/protocol/IntParamValue
                                    //4->com/hypixel/hytale/protocol/LongParamValue
                                    switch (replacement.typeSwitch<invokedynamic>(replacement, var56)) {
                                       case 0:
                                          StringParamValue s = (StringParamValue)replacement;
                                          var64 = s.value;
                                          break;
                                       case 1:
                                          BoolParamValue b = (BoolParamValue)replacement;
                                          var64 = b.value ? "1" : "0";
                                          break;
                                       case 2:
                                          DoubleParamValue d = (DoubleParamValue)replacement;
                                          var64 = Double.toString((double)((int)d.value));
                                          break;
                                       case 3:
                                          IntParamValue iv = (IntParamValue)replacement;
                                          var64 = Integer.toString(iv.value);
                                          break;
                                       case 4:
                                          LongParamValue l = (LongParamValue)replacement;
                                          var64 = Long.toString(l.value);
                                          break;
                                       default:
                                          var64 = "";
                                    }

                                    formattedReplacement = var64;
                                    break label171;
                                 case 0:
                                    Objects.requireNonNull(replacement);
                                    byte var55 = 0;
                                    String var10000;
                                    //$FF: var55->value
                                    //0->com/hypixel/hytale/protocol/StringParamValue
                                    //1->com/hypixel/hytale/protocol/BoolParamValue
                                    //2->com/hypixel/hytale/protocol/DoubleParamValue
                                    //3->com/hypixel/hytale/protocol/IntParamValue
                                    //4->com/hypixel/hytale/protocol/LongParamValue
                                    switch (replacement.typeSwitch<invokedynamic>(replacement, var55)) {
                                       case 0:
                                          StringParamValue s = (StringParamValue)replacement;
                                          var10000 = s.value;
                                          break;
                                       case 1:
                                          BoolParamValue b = (BoolParamValue)replacement;
                                          var10000 = b.value ? "1" : "0";
                                          break;
                                       case 2:
                                          DoubleParamValue d = (DoubleParamValue)replacement;
                                          var10000 = Integer.toString((int)d.value);
                                          break;
                                       case 3:
                                          IntParamValue iv = (IntParamValue)replacement;
                                          var10000 = Integer.toString(iv.value);
                                          break;
                                       case 4:
                                          LongParamValue l = (LongParamValue)replacement;
                                          var10000 = Long.toString(l.value);
                                          break;
                                       default:
                                          var10000 = "";
                                    }

                                    formattedReplacement = var10000;
                                    break label171;
                              }
                           case 3:
                              if (options != null) {
                                 Map<String, String> pluralTexts = parsePluralOptions(options);
                                 int value = Integer.parseInt(replacement.toString());
                                 String category = getPluralCategory(value, "en-US");
                                 String selected;
                                 if (pluralTexts.containsKey(category)) {
                                    selected = (String)pluralTexts.get(category);
                                 } else if (pluralTexts.containsKey("other")) {
                                    selected = (String)pluralTexts.get("other");
                                 } else {
                                    selected = pluralTexts.isEmpty() ? "" : (String)pluralTexts.values().iterator().next();
                                 }

                                 formattedReplacement = formatText(selected, params, messageParams);
                              }
                              break;
                           case 4:
                              Instant instant = parseDateTime(replacement);
                              if (instant != null) {
                                 DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault());
                                 formattedReplacement = formatter.format(instant.atZone(ZoneId.systemDefault()));
                              } else {
                                 formattedReplacement = "";
                              }
                              break;
                           case 5:
                              Instant instant = parseDateTime(replacement);
                              if (instant != null) {
                                 DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault());
                                 formattedReplacement = formatter.format(instant.atZone(ZoneId.systemDefault()));
                              } else {
                                 formattedReplacement = "";
                              }
                              break;
                           case 6:
                              if (options != null) {
                                 Map<String, String> selectOptions = parseSelectOptions(options);
                                 String selectKey = replacement.toString();
                                 String selected;
                                 if (selectOptions.containsKey(selectKey)) {
                                    selected = (String)selectOptions.get(selectKey);
                                 } else if (selectOptions.containsKey("other")) {
                                    selected = (String)selectOptions.get("other");
                                 } else {
                                    selected = selectOptions.isEmpty() ? "" : (String)selectOptions.values().iterator().next();
                                 }

                                 formattedReplacement = formatText(selected, params, messageParams);
                              }
                        }

                        if (format == null) {
                           Objects.requireNonNull(replacement);
                           var23 = 0;
                           String var65;
                           //$FF: var23->value
                           //0->com/hypixel/hytale/protocol/StringParamValue
                           //1->com/hypixel/hytale/protocol/BoolParamValue
                           //2->com/hypixel/hytale/protocol/DoubleParamValue
                           //3->com/hypixel/hytale/protocol/IntParamValue
                           //4->com/hypixel/hytale/protocol/LongParamValue
                           switch (replacement.typeSwitch<invokedynamic>(replacement, var23)) {
                              case 0:
                                 StringParamValue s = (StringParamValue)replacement;
                                 var65 = s.value;
                                 break;
                              case 1:
                                 BoolParamValue b = (BoolParamValue)replacement;
                                 var65 = Boolean.toString(b.value);
                                 break;
                              case 2:
                                 DoubleParamValue d = (DoubleParamValue)replacement;
                                 var65 = Double.toString(d.value);
                                 break;
                              case 3:
                                 IntParamValue iv = (IntParamValue)replacement;
                                 var65 = Integer.toString(iv.value);
                                 break;
                              case 4:
                                 LongParamValue l = (LongParamValue)replacement;
                                 var65 = Long.toString(l.value);
                                 break;
                              default:
                                 var65 = "";
                           }

                           formattedReplacement = var65;
                        }

                        sb.append(formattedReplacement);
                     } else {
                        sb.append(text, i, end);
                     }

                     i = end;
                     lastWritePos = end + 1;
                  }
               }
            } else if (ch == '}' && i + 1 < len && text.charAt(i + 1) == '}') {
               if (i > lastWritePos) {
                  sb.append(text, lastWritePos, i);
               }

               sb.append('}');
               ++i;
               lastWritePos = i + 1;
            }
         }

         if (lastWritePos < len) {
            sb.append(text, lastWritePos, len);
         }

         return sb.toString();
      }
   }

   private static int findMatchingBrace(@Nonnull String text, int start) {
      int depth = 0;
      int len = text.length();

      for(int i = start; i < len; ++i) {
         if (text.charAt(i) == '{') {
            ++depth;
         } else if (text.charAt(i) == '}') {
            --depth;
            if (depth == 0) {
               return i;
            }
         }
      }

      return -1;
   }

   private static int trimStart(@Nonnull String text, int start, int end) {
      int i;
      for(i = start; i <= end && Character.isWhitespace(text.charAt(i)); ++i) {
      }

      return i;
   }

   private static int trimEnd(@Nonnull String text, int start, int end) {
      int i;
      for(i = start; end >= i && Character.isWhitespace(text.charAt(i)); --end) {
      }

      return end >= i ? end - i + 1 : 0;
   }

   @Nonnull
   private static Map<String, String> parsePluralOptions(@Nonnull String options) {
      HashMap<String, String> result = new HashMap();

      for(String keyword : ICU_PLURAL_KEYWORDS) {
         String searchPattern = keyword + " {";
         int idx = options.indexOf(searchPattern);
         if (idx >= 0) {
            int braceStart = idx + keyword.length() + 1;
            int end = findMatchingBrace(options, braceStart);
            if (end > braceStart + 1) {
               result.put(keyword, options.substring(braceStart + 1, end));
            }
         }
      }

      return result;
   }

   @Nonnull
   private static String getPluralCategory(int n, @Nonnull String locale) {
      String var10000;
      switch (locale.contains("-") ? locale.substring(0, locale.indexOf(45)) : locale) {
         case "en":
            var10000 = getEnglishPluralCategory(n);
            break;
         case "fr":
            var10000 = getFrenchPluralCategory(n);
            break;
         case "de":
            var10000 = getGermanPluralCategory(n);
            break;
         case "pt":
            var10000 = !locale.equals("pt-BR") && !locale.equals("pt_BR") ? getPortuguesePluralCategory(n) : getPortugueseBrazilianPluralCategory(n);
            break;
         case "ru":
            var10000 = getRussianPluralCategory(n);
            break;
         case "es":
            var10000 = getSpanishPluralCategory(n);
            break;
         case "pl":
            var10000 = getPolishPluralCategory(n);
            break;
         case "tr":
            var10000 = getTurkishPluralCategory(n);
            break;
         case "uk":
            var10000 = getUkrainianPluralCategory(n);
            break;
         case "it":
            var10000 = getItalianPluralCategory(n);
            break;
         case "nl":
            var10000 = getDutchPluralCategory(n);
            break;
         case "da":
            var10000 = getDanishPluralCategory(n);
            break;
         case "fi":
            var10000 = getFinnishPluralCategory(n);
            break;
         case "no":
         case "nb":
         case "nn":
            var10000 = getNorwegianPluralCategory(n);
            break;
         case "zh":
            var10000 = getChinesePluralCategory(n);
            break;
         case "ja":
            var10000 = getJapanesePluralCategory(n);
            break;
         case "ko":
            var10000 = getKoreanPluralCategory(n);
            break;
         default:
            var10000 = getEnglishPluralCategory(n);
      }

      return var10000;
   }

   @Nonnull
   private static String getEnglishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getFrenchPluralCategory(int n) {
      return n != 0 && n != 1 ? "other" : "one";
   }

   @Nonnull
   private static String getGermanPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getPortuguesePluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getPortugueseBrazilianPluralCategory(int n) {
      return n != 0 && n != 1 ? "other" : "one";
   }

   @Nonnull
   private static String getRussianPluralCategory(int n) {
      int absN = Math.abs(n);
      int mod10 = absN % 10;
      int mod100 = absN % 100;
      if (mod10 == 1 && mod100 != 11) {
         return "one";
      } else if (mod10 < 2 || mod10 > 4 || mod100 >= 12 && mod100 <= 14) {
         return mod10 != 0 && (mod10 < 5 || mod10 > 9) && (mod100 < 11 || mod100 > 14) ? "other" : "many";
      } else {
         return "few";
      }
   }

   @Nonnull
   private static String getSpanishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getPolishPluralCategory(int n) {
      int absN = Math.abs(n);
      int mod10 = absN % 10;
      int mod100 = absN % 100;
      if (n == 1) {
         return "one";
      } else if (mod10 < 2 || mod10 > 4 || mod100 >= 12 && mod100 <= 14) {
         return mod10 != 0 && mod10 != 1 && (mod10 < 5 || mod10 > 9) && (mod100 < 12 || mod100 > 14) ? "other" : "many";
      } else {
         return "few";
      }
   }

   @Nonnull
   private static String getTurkishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getUkrainianPluralCategory(int n) {
      int absN = Math.abs(n);
      int mod10 = absN % 10;
      int mod100 = absN % 100;
      if (mod10 == 1 && mod100 != 11) {
         return "one";
      } else if (mod10 < 2 || mod10 > 4 || mod100 >= 12 && mod100 <= 14) {
         return mod10 != 0 && (mod10 < 5 || mod10 > 9) && (mod100 < 11 || mod100 > 14) ? "other" : "many";
      } else {
         return "few";
      }
   }

   @Nonnull
   private static String getItalianPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getDutchPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getDanishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getFinnishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getNorwegianPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getChinesePluralCategory(int n) {
      return "other";
   }

   @Nonnull
   private static String getJapanesePluralCategory(int n) {
      return "other";
   }

   @Nonnull
   private static String getKoreanPluralCategory(int n) {
      return "other";
   }

   @Nonnull
   private static Map<String, String> parseSelectOptions(@Nonnull String options) {
      HashMap<String, String> result = new HashMap();
      int i = 0;

      int braceEnd;
      for(int len = options.length(); i < len; i = braceEnd + 1) {
         while(i < len && Character.isWhitespace(options.charAt(i))) {
            ++i;
         }

         if (i >= len) {
            break;
         }

         int keyStart;
         for(keyStart = i; i < len && !Character.isWhitespace(options.charAt(i)) && options.charAt(i) != '{'; ++i) {
         }

         if (i == keyStart) {
            break;
         }

         String key;
         for(key = options.substring(keyStart, i); i < len && Character.isWhitespace(options.charAt(i)); ++i) {
         }

         if (i >= len || options.charAt(i) != '{') {
            break;
         }

         braceEnd = findMatchingBrace(options, i);
         if (braceEnd < 0) {
            break;
         }

         if (braceEnd > i + 1) {
            result.put(key, options.substring(i + 1, braceEnd));
         } else {
            result.put(key, "");
         }
      }

      return result;
   }

   @Nullable
   private static Instant parseDateTime(@Nonnull ParamValue value) {
      Objects.requireNonNull(value);
      byte var2 = 0;
      Instant var10000;
      //$FF: var2->value
      //0->com/hypixel/hytale/protocol/LongParamValue
      //1->com/hypixel/hytale/protocol/StringParamValue
      switch (value.typeSwitch<invokedynamic>(value, var2)) {
         case 0:
            LongParamValue l = (LongParamValue)value;
            Instant var9 = Instant.ofEpochMilli(l.value);
            var10000 = var9;
            break;
         case 1:
            StringParamValue s = (StringParamValue)value;

            Instant var8;
            try {
               var8 = Instant.parse(s.value);
            } catch (DateTimeParseException var7) {
               Object var3 = null;
               var10000 = (Instant)var3;
               break;
            }

            var10000 = var8;
            break;
         default:
            Object var10 = null;
            var10000 = (Instant)var10;
      }

      return var10000;
   }
}
