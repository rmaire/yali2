/*
 * Copyright 2021 rma.
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
package ch.uprisesoft.yali.parser;

import ch.qos.logback.classic.Level;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.lexer.Token;
import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.runtime.functions.builtin.LogicTest;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rma
 */
//@Disabled
public class ParserTest {

    private java.util.List<String> outputs;
    private OutputObserver oo;
    private InputGenerator ig;
    private Reader p;

    public ParserTest() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.toLevel("off"));
    }

    @BeforeEach
    public void setUp() {
        outputs = new ArrayList<>();
        oo = new OutputObserver() {
            private final Logger logger = LoggerFactory.getLogger(LogicTest.class);

            @Override
            public void inform(String output) {
                outputs.add(output);
            }
        };

        ig = new InputGenerator() {

            @Override
            public String request() {
                return "requestedinput";
            }

            @Override
            public String requestLine() {
                return "requestedinputline";
            }
        };

        ObjectMother om = new ObjectMother(oo, ig);

        p = om.getParser();
    }

    private Node parse(String input) {

        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(input);
        return p.read(tokens);
    }

    private Reader parseAndReturnParser(String input) {
        Lexer tokenizer = new Lexer();
        List<Token> tokens = tokenizer.scan(input);
        p.read(tokens);
        return p;
    }

    @Test
    public void testSymbol() {
        Node result = parse("asymbol");

        assertThat(result.getChildren().get(0).type(), is(NodeType.SYMBOL));
    }

    @Test
    public void testFloat() {
        Node result = parse("1.2");

        assertThat(result.getChildren().get(0).type(), is(NodeType.FLOAT));
        assertThat(result.getChildren().get(0).toFloatWord().getFloat(), is(1.2));
    }

    @Test
    public void testNegativeFloat() {
        Node result = parse("-1.2");
        assertThat(result.getChildren().get(0).type(), is(NodeType.FLOAT));
        assertThat(result.getChildren().get(0).toFloatWord().getFloat(), is(-1.2));
    }

    @Test
    public void testBooleanTrueUpper() {
        Node result = parse("TRUE");

        assertThat(result.getChildren().get(0).type(), is(NodeType.BOOLEAN));
        assertThat(result.getChildren().get(0).toBooleanWord().getBoolean(), is(true));
    }

    @Test
    public void testBooleanTrueLower() {
        Node result = parse("true");

        assertThat(result.getChildren().get(0).type(), is(NodeType.BOOLEAN));
        assertThat(result.getChildren().get(0).toBooleanWord().getBoolean(), is(true));

    }

    @Test
    public void testBooleanTrueMixed() {
        Node result = parse("tRue");

        assertThat(result.getChildren().get(0).type(), is(NodeType.BOOLEAN));
        assertThat(result.getChildren().get(0).toBooleanWord().getBoolean(), is(true));
    }

    @Test
    public void testBooleanFalseUpper() {
        Node result = parse("FALSE");

        assertThat(result.getChildren().get(0).type(), is(NodeType.BOOLEAN));
        assertThat(result.getChildren().get(0).toBooleanWord().getBoolean(), is(false));
    }

    @Test
    public void testBooleanFalseLower() {
        Node result = parse("false");

        assertThat(result.getChildren().get(0).type(), is(NodeType.BOOLEAN));
        assertThat(result.getChildren().get(0).toBooleanWord().getBoolean(), is(false));
    }

    @Test
    public void testBooleanFalseMixed() {
        Node result = parse("fALse");

        assertThat(result.getChildren().get(0).type(), is(NodeType.BOOLEAN));
        assertThat(result.getChildren().get(0).toBooleanWord().getBoolean(), is(false));
    }

    @Test
    public void testQuote() {
        Node result = parse("\"quote");

        assertThat(result.getChildren().get(0).type(), is(NodeType.QUOTE));
        assertThat(result.getChildren().get(0).toQuotedWord().getQuote(), is("quote"));
    }

    @Test
    public void testNilLower() {
        Node result = parse("nil");

        assertThat(result.getChildren().get(0).type(), is(NodeType.NIL));
    }
    
    @Test
    public void testNilUpper() {
        Node result = parse("NIL");

        assertThat(result.getChildren().get(0).type(), is(NodeType.NIL));
    }
    
    @Test
    public void testNilMixed() {
        Node result = parse("nIL");

        assertThat(result.getChildren().get(0).type(), is(NodeType.NIL));
    }

//    @Test
//    public void testNone() {
//        Node result = read("");
//
//        assertThat(result.type(), is(NodeType.NONE));
//    }
    
    @Test
    public void testMathExpressions() {
        Node result = parse("7 - 5 * 3 / (2 + -5)");

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("sub"));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toIntegerWord().getInteger(), is(7));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).type(), is(NodeType.PROCCALL));

    }

    @Test
    public void testLogicExpressions() {
        Node result = parse("5 >= 7");

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("greaterequal?"));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toIntegerWord().getInteger(), is(5));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).toIntegerWord().getInteger(), is(7));
    }

    @Test
    public void testEquality() {
        Node result = parse("5 = 7.0");

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("equal?"));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toIntegerWord().getInteger(), is(5));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).type(), is(NodeType.FLOAT));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).toFloatWord().getFloat(), is(7.0));
    }
    
        @Test
    public void testEqualityEquality() {
        Node result = parse("5.0 == 7");

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("equal?"));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.FLOAT));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toFloatWord().getFloat(), is(5.0));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).toIntegerWord().getInteger(), is(7));
    }
    
        @Test
    public void testInequality() {
        Node result = parse("5 != 7");

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("notequal?"));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toIntegerWord().getInteger(), is(5));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(1).toIntegerWord().getInteger(), is(7));
    }

    @Test
    public void testFunCall() {
        Node result = parse("rt 100 * 2");

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("rt"));
        assertThat(result.getChildren().get(0).getChildren().size(), is(1));

        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().size(), is(2));

        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toCall().name(), is("mul"));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).toIntegerWord().getInteger(), is(100));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).type(), is(NodeType.INTEGER));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).toIntegerWord().getInteger(), is(2));
    }
    
    @Test
    public void testFunCallList() {
        Node result = parse("print [One Two]");

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("print"));
        assertThat(result.getChildren().get(0).getChildren().size(), is(1));

        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.LIST));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().size(), is(2));

        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).toSymbolWord().getSymbol(), is("One"));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).toSymbolWord().getSymbol(), is("Two"));
    }

    @Test
    public void testList() {
        Node result = parse("[fd 100]");

        assertThat(result.getChildren().get(0).type(), is(NodeType.LIST));
        assertThat(result.getChildren().get(0).getChildren().size(), is(2));

        assertThat(result.getChildren().get(0).getChildren().get(0).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).getChildren().get(0).toSymbolWord().getSymbol(), is("fd"));

        assertThat(result.getChildren().get(0).getChildren().get(1).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).getChildren().get(1).toSymbolWord().getSymbol(), is("100"));
    }
    
    @Test
    public void testListIsNotDesugared() {
        Node result = parse("[10 * 5]");

        assertThat(result.getChildren().get(0).type(), is(NodeType.LIST));
        assertThat(result.getChildren().get(0).getChildren().size(), is(3));

        assertThat(result.getChildren().get(0).getChildren().get(0).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).getChildren().get(0).toSymbolWord().getSymbol(), is("10"));

        assertThat(result.getChildren().get(0).getChildren().get(1).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).getChildren().get(1).toSymbolWord().getSymbol(), is("*"));
        
        assertThat(result.getChildren().get(0).getChildren().get(2).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).getChildren().get(2).toSymbolWord().getSymbol(), is("5"));
    }

    @Test
    public void testRef() {
        Node result = parse(":testit");

        assertThat(result.getChildren().get(0).type(), is(NodeType.REFERENCE));
        assertThat(result.getChildren().get(0).toReferenceWord().getReference(), is("testit"));
    }

//    @Test
//    public void testFunDefLiteral() {
//        
//        Reader p = parseAndReturnParser("TO bla :blubb :bli\nprint [Hello World]\nfd 100\nEND");
//        Procedure result = p.getFunctions().getProcedures().get("bla").toProcedureDef();
//
//        assertThat(result.type(), is(NodeType.PROCEDURE));
//        assertThat(result.getName(), is("bla"));
//        assertThat(result.getArity(), is(2));
//        assertThat(result.getArgs().size(), is(2));
//        assertThat(result.getArgs().get(0), is("blubb"));
//        assertThat(result.getArgs().get(1), is("bli"));
//
//        assertThat(result.getChildren().size(), is(2));
//        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
//
//        assertThat(result.getChildren().get(0).toCall().name(), is("print"));
//        assertThat(result.getChildren().get(0).toCall().getChildren().size(), is(1));
//
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.LIST));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toList().getChildren().size(), is(2));
//
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).type(), is(NodeType.SYMBOL));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).toSymbolWord().getSymbol(), is("Hello"));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).type(), is(NodeType.SYMBOL));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).toSymbolWord().getSymbol(), is("World"));
//
//        assertThat(result.getChildren().get(1).type(), is(NodeType.PROCCALL));
//    }
    
//    @Test
//    public void testFunDefStringBuilder() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("TO bla :blubb :bli").append("\n");
//        sb.append("print [Hello World]").append("\n");
//        sb.append("fd 100").append("\n");
//        sb.append("END").append("\n");
//
////        Node result = read(sb.toString());
//        Reader p = parseAndReturnParser(sb.toString());
//        Procedure result = p.getFunctions().getProcedures().get("bla").toProcedureDef();
//
//        assertThat(result.type(), is(NodeType.PROCEDURE));
//        assertThat(result.getName(), is("bla"));
//        assertThat(result.getArity(), is(2));
//        assertThat(result.getArgs().size(), is(2));
//        assertThat(result.getArgs().get(0), is("blubb"));
//        assertThat(result.getArgs().get(1), is("bli"));
//
//        assertThat(result.getChildren().size(), is(2));
//        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
//
//        assertThat(result.getChildren().get(0).toCall().name(), is("print"));
//        assertThat(result.getChildren().get(0).toCall().getChildren().size(), is(1));
//
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).type(), is(NodeType.LIST));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).toList().getChildren().size(), is(2));
//
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).type(), is(NodeType.SYMBOL));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(0).toSymbolWord().getSymbol(), is("Hello"));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).type(), is(NodeType.SYMBOL));
//        assertThat(result.getChildren().get(0).toCall().getChildren().get(0).getChildren().get(1).toSymbolWord().getSymbol(), is("World"));
//
//        assertThat(result.getChildren().get(1).type(), is(NodeType.PROCCALL));
//    }



    @Test
    public void testParseAll() {
        Node result = parse("fd 100.0 rt 90.0 print true");

        assertThat(result.getChildren().size(), is(3));
        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(1).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(2).type(), is(NodeType.PROCCALL));
    }

    @Test
    public void testListRegression() {

        String list = "[fd 100 rt 90 print True fd :ref]";
        Node result = parse(list);

        assertThat(result.getChildren().get(0).type(), is(NodeType.LIST));
        assertThat(result.getChildren().get(0).toList().getChildren().size(), is(8));
    }
    
    // TODO
    @Test
    public void testNestedParens() {

        String list = "5 + (3 + (4 + 6))";
        Node result = parse(list);

        assertThat(result.getChildren().get(0).type(), is(NodeType.PROCCALL));
        assertThat(result.getChildren().get(0).toCall().name(), is("add"));
//        assertThat(result.getChildren().get(0).toList().getChildren().size(), is(8));
    }
    
    @Test
    public void testQuotedQuote() {

        String list = "\"\"";
        Node result = parse(list);
        
        assertThat(result.getChildren().get(0).type(), is(NodeType.QUOTE));
        assertThat(result.getChildren().get(0).toQuotedWord().getQuote(), is("\""));
//        assertThat(result.getChildren().get(0).toList().getChildren().size(), is(8));
    }
}
