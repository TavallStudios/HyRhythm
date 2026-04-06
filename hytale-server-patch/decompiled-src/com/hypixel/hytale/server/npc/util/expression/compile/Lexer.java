package com.hypixel.hytale.server.npc.util.expression.compile;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.text.ParseException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Lexer<Token extends Supplier<String>> {
   public static final String UNTERMINATED_STRING = "Unterminated string";
   public static final String INVALID_NUMBER_FORMAT = "Invalid number format";
   public static final String INVALID_CHARACTER_IN_EXPRESSION = "Invalid character in expression :";
   private final Token tokenEnd;
   private final Token tokenIdent;
   private final Token tokenString;
   private final Token tokenNumber;
   private final CharacterSequenceMatcher<Token> characterSequenceMatcher;

   public Lexer(Token tokenEnd, Token tokenIdent, Token tokenString, Token tokenNumber, @Nonnull Stream<Token> operators) {
      this.tokenEnd = tokenEnd;
      this.tokenIdent = tokenIdent;
      this.tokenString = tokenString;
      this.tokenNumber = tokenNumber;
      this.characterSequenceMatcher = new CharacterSequenceMatcher<Token>();
      operators.forEach((token) -> this.characterSequenceMatcher.addToken(token, (String)token.get()));
   }

   public Token nextToken(@Nonnull LexerContext<Token> context) throws ParseException {
      context.resetToken();
      if (!context.eatWhiteSpace()) {
         return context.setToken(this.tokenEnd);
      } else {
         char ch = context.currentChar();
         if (!Character.isLetter(ch) && ch != '_') {
            if (context.isNumber(ch)) {
               context.parseNumber(ch);
               return context.setToken(this.tokenNumber);
            } else if (ch != '"' && ch != '\'') {
               CharacterSequenceMatcher<Token> lastTerminal = null;
               CharacterSequenceMatcher<Token> matcher = this.characterSequenceMatcher.matchLetter(ch);

               int lastValidPosition;
               for(lastValidPosition = context.getPosition(); matcher != null; matcher = matcher.matchLetter(ch)) {
                  if (matcher.token != null) {
                     lastValidPosition = context.getPosition();
                     lastTerminal = matcher;
                  }

                  ch = context.addTokenCharacter(ch);
                  if (!context.haveChar()) {
                     break;
                  }
               }

               if (lastTerminal != null) {
                  context.adjustPosition(lastValidPosition + 1);
                  return context.setToken(lastTerminal.token);
               } else {
                  throw new ParseException("Invalid character in expression :" + ch, context.getTokenPosition());
               }
            } else {
               context.parseString(ch);
               return context.setToken(this.tokenString);
            }
         } else {
            context.parseIdent(ch);
            return context.setToken(this.tokenIdent);
         }
      }
   }

   protected static class CharacterSequenceMatcher<Token> {
      @Nullable
      public Token token = null;
      public char letter;
      @Nullable
      public List<CharacterSequenceMatcher<Token>> children;

      public CharacterSequenceMatcher() {
         this.letter = 0;
         this.children = null;
      }

      public CharacterSequenceMatcher(char letter) {
         this.letter = letter;
         this.children = null;
      }

      protected void addToken(Token token, int depth, @Nonnull String text, int maxDepth) {
         char ch = text.charAt(depth);
         if (this.children == null) {
            this.children = new ObjectArrayList();
            this.append(token, depth, text, maxDepth, ch);
         } else {
            int index = 0;

            int size;
            for(size = this.children.size(); index < size && ((CharacterSequenceMatcher)this.children.get(index)).letter < ch; ++index) {
            }

            if (index == size) {
               this.append(token, depth, text, maxDepth, ch);
            } else {
               CharacterSequenceMatcher<Token> child = (CharacterSequenceMatcher)this.children.get(index);
               if (child.letter == ch) {
                  if (depth == maxDepth) {
                     if (child.token != null) {
                        throw new RuntimeException("Duplicate operator " + text);
                     }

                     child.token = token;
                  } else {
                     child.addToken(token, depth + 1, text, maxDepth);
                  }
               } else {
                  CharacterSequenceMatcher<Token> lookup = new CharacterSequenceMatcher<Token>(ch);
                  this.children.add(index, lookup);
                  this.addTail(token, depth, text, maxDepth, lookup);
               }
            }

         }
      }

      protected void addToken(Token token, @Nonnull String text) {
         this.addToken(token, 0, text, text.length() - 1);
      }

      private void append(Token token, int depth, @Nonnull String text, int maxDepth, char ch) {
         CharacterSequenceMatcher<Token> lookup = new CharacterSequenceMatcher<Token>(ch);
         this.children.add(lookup);
         this.addTail(token, depth, text, maxDepth, lookup);
      }

      private void addTail(Token token, int depth, @Nonnull String text, int maxDepth, @Nonnull CharacterSequenceMatcher<Token> lookup) {
         if (depth == maxDepth) {
            lookup.token = token;
         } else {
            lookup.addToken(token, depth + 1, text, maxDepth);
         }

      }

      @Nullable
      protected CharacterSequenceMatcher<Token> matchLetter(char ch) {
         if (this.children != null) {
            int index = 0;

            for(int size = this.children.size(); index < size; ++index) {
               CharacterSequenceMatcher<Token> characterSequenceMatcher = (CharacterSequenceMatcher)this.children.get(index);
               char letter = characterSequenceMatcher.letter;
               if (letter == ch) {
                  return characterSequenceMatcher;
               }

               if (letter > ch) {
                  return null;
               }
            }
         }

         return null;
      }
   }
}
