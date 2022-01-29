/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.lexer;

import static ch.uprisesoft.yali.lexer.TokenType.BANG;
import static ch.uprisesoft.yali.lexer.TokenType.BANG_EQUAL;
import static ch.uprisesoft.yali.lexer.TokenType.EQUAL;
import static ch.uprisesoft.yali.lexer.TokenType.EQUAL_EQUAL;
import static ch.uprisesoft.yali.lexer.TokenType.GREATER;
import static ch.uprisesoft.yali.lexer.TokenType.GREATER_EQUAL;
import static ch.uprisesoft.yali.lexer.TokenType.LEFT_BRACE;
import static ch.uprisesoft.yali.lexer.TokenType.LEFT_BRACKET;
import static ch.uprisesoft.yali.lexer.TokenType.LEFT_PAREN;
import static ch.uprisesoft.yali.lexer.TokenType.LESS;
import static ch.uprisesoft.yali.lexer.TokenType.LESS_EQUAL;
import static ch.uprisesoft.yali.lexer.TokenType.MINUS;
import static ch.uprisesoft.yali.lexer.TokenType.PLUS;
import static ch.uprisesoft.yali.lexer.TokenType.QUOTE;
import static ch.uprisesoft.yali.lexer.TokenType.REFERENCE;
import static ch.uprisesoft.yali.lexer.TokenType.RIGHT_BRACE;
import static ch.uprisesoft.yali.lexer.TokenType.RIGHT_BRACKET;
import static ch.uprisesoft.yali.lexer.TokenType.RIGHT_PAREN;
import static ch.uprisesoft.yali.lexer.TokenType.SLASH;
import static ch.uprisesoft.yali.lexer.TokenType.STAR;
import java.util.List;

/**
 *
 * @author rma
 */
public class BaseScanner extends Scanner {

    public BaseScanner(Lexer context, String source) {
        super(context, source);
        logger.debug("(Scanner) Instantiated BaseScanner");
    }

    protected BaseScanner(Lexer context, String source, List<Token> tokens, int start, int current, int line, int linePos, Token funStart, Token funEnd) {
        super(context, source, tokens, start, current, line, linePos, funStart, funEnd);
        logger.debug("(Scanner) Switched to BaseScanner");
    }

    @Override
    public void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case '[':
                addToken(LEFT_BRACKET);
                context.setScanner(new ListScanner(context, source, tokens, start, current, line, linePos, funStart, funEnd));
                break;
            case ']':
                addToken(RIGHT_BRACKET);
                break;
            case '-':
                if (isDigit(peek())) {
                    number();
                } else {
                    addToken(MINUS);
                }
                break;
            case '+':
                addToken(PLUS);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                addToken(SLASH);
                break;
            case ';':
                if (match(';')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !testEnd()) {
                        advance();
                    }
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                newLine();
                break;
            case '"':
                string(QUOTE);
                break;
            case ':':
                string(REFERENCE);
                break;
            case '?':
                question();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    symbol();
                } else {
                    System.out.println("Unexpected character: " + linePos + "/" + line);
                }
                break;
        }
    }

    @Override
    protected boolean isNextSpecialChar() {
        return peek() == ' '
                || peek() == ':'
                || peek() == ' '
                || peek() == '{'
                || peek() == '}'
                || peek() == '['
                || peek() == ']'
                || peek() == '('
                || peek() == ')'
                || peek() == '\t'
                || peek() == '\r'
                || peek() == '\n'
                || testEnd();
    }

    @Override
    protected void symbol() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);

        // Symbol is the default type
        TokenType type = TokenType.SYMBOL;

        if (text.toLowerCase().equals("true")) {
            type = TokenType.TRUE;
        }

        if (text.toLowerCase().equals("false")) {
            type = TokenType.FALSE;
        }

        if (text.toLowerCase().equals("nil")) {
            type = TokenType.NIL;
        }

        if (text.toLowerCase().equals("to")) {
            addTo(TokenType.TO);
            return;
        }

        if (text.toLowerCase().equals("end")) {
            addEnd(TokenType.END);
            return;
        }

        addToken(type);
    }

    protected void question() {
        while (!isNextSpecialChar()) {
            if (isDigit(peek())) {
                advance();
            } else {
                symbol();
                return;
            }
        }

        addToken(TokenType.QUESTION);
    }

}
