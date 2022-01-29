/*
 * Copyright 2020 rmaire.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.uprisesoft.yali.lexer;

import ch.qos.logback.classic.Level;
import java.util.List;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rmaire
 */
public class LexerTest {

    public LexerTest() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.toLevel("off"));
    }

    @Test
    public void testTokenize() {
        String testInput = "asymbol [symbol 1 in list]";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(8));
        assertThat(tokens.get(tokens.size() - 2).getLexeme(), is("]"));
        assertThat(tokens.get(tokens.size() - 2).getLine(), is(1));
        assertThat(tokens.get(tokens.size() - 2).getPos(), is(25));
        assertThat(tokens.get(3).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(tokens.size() - 2).type(), is(TokenType.RIGHT_BRACKET));
    }

    @Test
    public void testNumberTokens() {
        String testInput = "1 * 2.0";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(4));
        assertThat(tokens.get(0).type(), is(TokenType.NUMBER));
        assertThat(tokens.get(1).type(), is(TokenType.STAR));
        assertThat(tokens.get(2).type(), is(TokenType.NUMBER));
    }

    @Test
    public void testNewline() {
        String testInput = "one\ntwo";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(4));
        assertThat(tokens.get(1).type(), is(TokenType.NEWLINE));
    }

    @Test
    public void testReference() {
        String testInput = ":ref";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.REFERENCE));
    }

    @Test
    public void testQuote() {
        String testInput = "\"quot";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.QUOTE));
    }

    @Test
    public void testFunDef() {
        String testInput = "to :bla :blubb\nend";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(6));
        assertThat(tokens.get(0).type(), is(TokenType.TO));
        assertThat(tokens.get(1).type(), is(TokenType.REFERENCE));
        assertThat(tokens.get(4).type(), is(TokenType.END));
    }

    @Test
    public void testBoolean() {
        String testInput = "true TRUE True truE";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(5));
        assertThat(tokens.get(0).type(), is(TokenType.TRUE));
        assertThat(tokens.get(1).type(), is(TokenType.TRUE));
        assertThat(tokens.get(2).type(), is(TokenType.TRUE));
        assertThat(tokens.get(3).type(), is(TokenType.TRUE));

        testInput = "false FALSE False falsE";

        tokenizer = new Lexer();
        tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(5));
        assertThat(tokens.get(0).type(), is(TokenType.FALSE));
        assertThat(tokens.get(1).type(), is(TokenType.FALSE));
        assertThat(tokens.get(2).type(), is(TokenType.FALSE));
        assertThat(tokens.get(3).type(), is(TokenType.FALSE));
    }

    @Test
    public void testNil() {
        String testInput = "nil NIL nIl";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(4));
        assertThat(tokens.get(0).type(), is(TokenType.NIL));
        assertThat(tokens.get(1).type(), is(TokenType.NIL));
        assertThat(tokens.get(2).type(), is(TokenType.NIL));
    }

    @Test
    public void testSymbol() {
        String testInput = "asymbol";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.SYMBOL));

        testInput = "asymbol?";

        tokenizer = new Lexer();
        tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(0).getLexeme(), is("asymbol?"));

        testInput = "asymbol!";

        tokenizer = new Lexer();
        tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(0).getLexeme(), is("asymbol!"));

        testInput = "シス卿";

        tokenizer = new Lexer();
        tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(0).getLexeme(), is("シス卿"));
    }

    @Test
    public void testSourcePos() {
        String testInput = "to testit\n"
                + "funcall\n"
                + "end";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        System.out.println(tokens);

        assertThat(tokens.size(), is(7));
        assertThat(tokens.get(1).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(1).getAbsolute(), is(3));
        assertThat(tokens.get(5).type(), is(TokenType.END));
        assertThat(tokens.get(5).getAbsolute(), is(18));
    }

    @Test
    public void testQuoteQuotationmark() {
        String testInput = "\"\"";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.QUOTE));
        assertThat(tokens.get(0).getLexeme(), is("\"\""));
    }

    @Test
    public void testRootTokenizer() {
        String testInput = "5+3";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(4));
        assertThat(tokens.get(0).type(), is(TokenType.NUMBER));
        assertThat(tokens.get(0).getLexeme(), is("5"));
        assertThat(tokens.get(1).type(), is(TokenType.PLUS));
        assertThat(tokens.get(1).getLexeme(), is("+"));
        assertThat(tokens.get(2).type(), is(TokenType.NUMBER));
        assertThat(tokens.get(2).getLexeme(), is("3"));
    }

    @Test
    public void testListTokenizer() {
        String testInput = "[5+3 print]";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        assertThat(tokens.size(), is(5));
        assertThat(tokens.get(0).type(), is(TokenType.LEFT_BRACKET));
        assertThat(tokens.get(1).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(1).getLexeme(), is("5+3"));
        assertThat(tokens.get(2).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(2).getLexeme(), is("print"));
        assertThat(tokens.get(3).type(), is(TokenType.RIGHT_BRACKET));
    }

    @Test
    public void testQuestionNumber() {
        String testInput = "?123";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        System.out.println("++++++++++++++++++");
        System.out.println(tokens.get(0).getLexeme());
        System.out.println("++++++++++++++++++");

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.QUESTION));
        assertThat(tokens.get(0).getLexeme(), is("?123"));
    }

    @Test
    public void testQuestionChars() {
        String testInput = "?abc";

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(testInput);

        System.out.println("++++++++++++++++++");
        System.out.println(tokens.get(0).getLexeme());
        System.out.println("++++++++++++++++++");

        assertThat(tokens.size(), is(2));
        assertThat(tokens.get(0).type(), is(TokenType.SYMBOL));
        assertThat(tokens.get(0).getLexeme(), is("?abc"));
    }

}
