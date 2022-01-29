/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.lexer;

import static ch.uprisesoft.yali.lexer.TokenType.EOF;
import static ch.uprisesoft.yali.lexer.TokenType.NUMBER;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rmaire
 */
public abstract class Scanner {

    protected static final Logger logger = LoggerFactory.getLogger(Scanner.class);

    protected Lexer context;

    protected String source;
    protected List<Token> tokens = new ArrayList<>();

    protected int start = 0;
    protected int current = 0;
    protected int line = 1;
    protected int linePos = 0;

    protected Token funStart;
    protected Token funEnd;

    public Scanner(Lexer context, String source) {
        this.context = context;
        this.source = source;
    }

    protected Scanner(Lexer context, String source, List<Token> tokens, int start, int current, int line, int linePos, Token funStart, Token funEnd) {
        this.context = context;
        this.source = source;
        this.tokens = tokens;
        this.start = start;
        this.current = current;
        this.line = line;
        this.linePos = linePos;
        this.funStart = funStart;
        this.funEnd = funEnd;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public boolean isAtEnd() {
        start = current;
        boolean end = testEnd();
        if (end == true) {
            tokens.add(new Token(EOF, "", line, linePos, current));
        }
        return end;
    }

    public abstract void scanToken();

    protected boolean testEnd() {
        return current >= source.length();
    }

    protected void string(TokenType type) {
        while (!isNextSpecialChar() && !testEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }
        addToken(type);
    }

    protected abstract boolean isNextSpecialChar();

    protected boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    protected abstract void symbol();

    protected boolean isAlpha(char c) {
        return Character.isAlphabetic(c) || String.valueOf(c).equals("_") || String.valueOf(c).equals("-") || String.valueOf(c).equals("!") || String.valueOf(c).equals("?");
    }

    protected boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    protected void number() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER);
    }

    protected char advance() {
        current++;
        linePos++;
        return source.charAt(current - 1);
    }

    protected boolean match(char expected) {
        if (testEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    protected char peek() {
        if (testEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    protected char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    protected void newLine() {
        addToken(TokenType.NEWLINE);
        line++;
        linePos = 0;
    }

    protected void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line, linePos - text.length(), current - text.length()));
    }

    protected void addTo(TokenType type) {
        String text = source.substring(start, current);
        funStart = new Token(type, text, line, linePos - text.length(), current - text.length());

        tokens.add(funStart);
    }

    protected void addEnd(TokenType type) {
        String text = source.substring(funStart.getAbsolute(), current);
        int absolutePos = current - source.substring(start, current).length();

        funEnd = new Token(type, text, line, linePos - text.length(), absolutePos);

        tokens.add(funEnd);

        funStart = null;
        funEnd = null;
    }
}
