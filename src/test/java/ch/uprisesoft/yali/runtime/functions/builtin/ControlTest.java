/* 
 * Copyright 2020 Uprise Software.
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
package ch.uprisesoft.yali.runtime.functions.builtin;

import ch.qos.logback.classic.Level;
import ch.uprisesoft.yali.ast.node.List;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class ControlTest {

    Interpreter it;
    private OutputObserver oo;
    private InputGenerator ig;
    private java.util.List<String> outputs;

    public ControlTest() {
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

        it = om.getInterpreter();
    }

    @Test
    public void testMake() {
        it.eval("make \"testit \"someval");

//        assertThat(it.scope().defined("testit"), is(true));
        assertThat(it.resolve("testit").type(), is(NodeType.QUOTE));
        assertThat(it.resolve("testit").toQuotedWord().getQuote(), is("someval"));
    }
    
    @Test
    public void testReferenceAsVariableName() {
        String input = "make \"varone \"testvar\n"
                + "make :varone \"one\n";

        it.eval(input);

//        assertThat(it.scope().defined("testvar"), is(true));
        assertThat(it.resolve("testvar").toQuotedWord().getQuote(), is("one"));
    }

    @Test
    public void testSymbolAsVariableName() {
        String input = "make varone \"test\n";

        it.eval(input);

        assertThat(it.scope().defined("varone"), is(true));
        assertThat(it.scope().resolve("varone").toQuotedWord().getQuote(), is("test"));
    }

    @Test
    public void testTurtle() {
        StringBuilder sb = new StringBuilder();
        sb.append("fd 100").append("\n");
        sb.append("rt 90").append("\n");
        sb.append("fd 100").append("\n");
        sb.append("turtlepos").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.toList().getChildren().get(0).toIntegerWord().getInteger(), is(100));
        assertThat(res.toList().getChildren().get(1).toIntegerWord().getInteger(), is(100));
    }

    @Test
    public void testFunctionAndMake() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"angle 90").append("\n");
        sb.append("to move").append("\n");
        sb.append("fd 100").append("\n");
        sb.append("rt :angle").append("\n");
        sb.append("fd 100").append("\n");
        sb.append("end").append("\n");
        sb.append("move").append("\n");
        sb.append("turtlepos").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.toList().getChildren().get(0).toIntegerWord().getInteger(), is(100));
        assertThat(res.toList().getChildren().get(1).toIntegerWord().getInteger(), is(100));
    }

    @Test
    public void testFunctionWithParamAndMake() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"angle 90").append("\n");
        sb.append("to move :dist").append("\n");
        sb.append("fd :dist").append("\n");
        sb.append("rt :angle").append("\n");
        sb.append("fd :dist").append("\n");
        sb.append("end").append("\n");
        sb.append("move 100").append("\n");
        sb.append("turtlepos").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.toList().getChildren().get(0).toIntegerWord().getInteger(), is(100));
        assertThat(res.toList().getChildren().get(1).toIntegerWord().getInteger(), is(100));
    }

    @Test
    public void testRepeat() {
        StringBuilder sb = new StringBuilder();
        sb.append("repeat 2 [fd 100 rt 90]").append("\n");
        sb.append("turtlepos").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.toList().getChildren().get(0).toIntegerWord().getInteger(), is(100));
        assertThat(res.toList().getChildren().get(1).toIntegerWord().getInteger(), is(100));
    }

    @Test
    public void testRepeatAndMake() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"count 2").append("\n");
        sb.append("repeat :count [fd 100 rt 90]").append("\n");
        sb.append("turtlepos").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.toList().getChildren().get(0).toIntegerWord().getInteger(), is(100));
        assertThat(res.toList().getChildren().get(1).toIntegerWord().getInteger(), is(100));
    }
    
    @Test
    public void testRepeatScope() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"count 1").append("\n");
        sb.append("repeat :count [make \"testvar \"yes]").append("\n");
        Node res = it.eval(sb.toString());

//        assertThat(it.scope().defined("testvar"), is(true));
        assertThat(it.resolve("testvar").type(), is(NodeType.QUOTE));
        assertThat(it.resolve("testvar").toQuotedWord().getQuote(), is("yes"));
    }
    
        @Test
    public void testIfScope() {
        StringBuilder sb = new StringBuilder();
        sb.append("if true [make \"testvar \"yes]").append("\n");
        Node res = it.eval(sb.toString());

//        assertThat(it.scope().defined("testvar"), is(true));
        assertThat(it.resolve("testvar").type(), is(NodeType.QUOTE));
        assertThat(it.resolve("testvar").toQuotedWord().getQuote(), is("yes"));
    }

    @Test
    public void testRun() {
        StringBuilder sb = new StringBuilder();
        sb.append("run [3 + 2]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.INTEGER));
        assertThat(res.toIntegerWord().getInteger(), is(5));

        sb = new StringBuilder();
        sb.append("run [fd 100 rt 90 fd 100]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.LIST));
        List turtlepos = (List) res;
        assertThat(turtlepos.getChildren().size(), is(2));
        assertThat(turtlepos.getChildren().get(0).type(), is(NodeType.INTEGER));
        assertThat(turtlepos.getChildren().get(1).type(), is(NodeType.INTEGER));
        assertThat(turtlepos.getChildren().get(0).toIntegerWord().getInteger(), is(100));
        assertThat(turtlepos.getChildren().get(1).toIntegerWord().getInteger(), is(100));

    }
    
    @Test
    public void testRunScope() {
        StringBuilder sb = new StringBuilder();
        sb.append("run [make \"testvar \"yes]").append("\n");
        sb.append("print :testvar").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.NIL));
//        assertThat(it.scope().defined("testvar"), is(true));
        assertThat(it.resolve("testvar").type(), is(NodeType.QUOTE));
        assertThat(it.resolve("testvar").toQuotedWord().getQuote(), is("yes"));
    }

    @Test
    public void testIfTrueGreater() {
        StringBuilder sb = new StringBuilder();

        sb.append("if 3 > 2 [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("Yes\n"));
    }

    @Test
    public void testIfFalseLess() {
        StringBuilder sb = new StringBuilder();
        sb.append("if 3 < 2 [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.NIL));
        assertThat(outputs.size(), is(0));
    }
    
    @Test
    public void testIfFalseLiteral() {
        StringBuilder sb = new StringBuilder();
        sb.append("if false [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.NIL));
        assertThat(outputs.size(), is(0));
    }
    
    @Test
    public void testIfTrueLiteral() {
        StringBuilder sb = new StringBuilder();
        sb.append("if true [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("Yes\n"));
    }
    
    @Test
    public void testIfTrueVarLiteral() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar true").append("\n");
        sb.append("if :testvar [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("Yes\n"));
    }
    
    @Test
    public void testIfFalseVarLiteral() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar false").append("\n");
        sb.append("if :testvar [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.NIL));
        assertThat(outputs.size(), is(0));
    }
    
    @Test
    public void testIfTrueVarEval() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar 3 > 2").append("\n");
        sb.append("if :testvar [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("Yes\n"));
    }
    
    @Test
    public void testIfFalseVarEval() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar 3 < 2").append("\n");
        sb.append("if :testvar [print \"Yes]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.NIL));
        assertThat(outputs.size(), is(0));
    }

    @Test
    public void testIfElse() {
        StringBuilder sb = new StringBuilder();

        sb = new StringBuilder();
        sb.append("ifelse 3 > 2 [output Yes] [output No]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("Yes"));

        sb = new StringBuilder();
        sb.append("ifelse 3 < 2 [output Yes] [output No]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("No"));

        sb = new StringBuilder();
        sb.append("ifelse false [output Yes] [output No]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("No"));

        sb = new StringBuilder();
        sb.append("ifelse true [output Yes] [output No]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("Yes"));

        sb = new StringBuilder();
        sb.append("make \"testvar true").append("\n");
        sb.append("ifelse :testvar [output Yes] [output No]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("Yes"));

        sb = new StringBuilder();
        sb.append("make \"testvar false").append("\n");
        sb.append("ifelse :testvar [output Yes] [output No]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("No"));

        sb = new StringBuilder();
        sb.append("make \"testvar 3 > 2").append("\n");
        sb.append("ifelse :testvar [output Yes] [output No]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("Yes"));

        sb = new StringBuilder();
        sb.append("make \"testvar 3 < 2").append("\n");
        sb.append("ifelse :testvar [output Yes] [output No]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.SYMBOL));
        assertThat(res.toSymbolWord().getSymbol(), is("No"));

        sb = new StringBuilder();
        sb.append("make \"testvar 3 > 2").append("\n");
        sb.append("make \"yes \"Yes").append("\n");
        sb.append("make \"no \"No").append("\n");
        sb.append("ifelse :testvar [output :yes] [output :no]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.QUOTE));
        assertThat(res.toQuotedWord().getQuote(), is("Yes"));

        sb = new StringBuilder();
        sb.append("make \"testvar 3 < 2").append("\n");
        sb.append("make \"yes \"Yes").append("\n");
        sb.append("make \"no \"No").append("\n");
        sb.append("ifelse :testvar [output :yes] [output :no]").append("\n");
        res = it.eval(sb.toString());

        assertThat(res.type(), is(NodeType.QUOTE));
        assertThat(res.toQuotedWord().getQuote(), is("No"));
    }

    // TODO
    
//    @Test
//    public void testLocalMake() {
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("make \"testvar \"Bye!").append("\n");
//        sb.append("\n");
//        sb.append("to testfun").append("\n");
//        sb.append("local \"testvar").append("\n");
//        sb.append("make \"testvar \"Hello!").append("\n");
//        sb.append("print :testvar").append("\n");
//        sb.append("end").append("\n");
//        sb.append("").append("\n");
//        sb.append("testfun").append("\n");
//        sb.append("").append("\n");
//        sb.append("print :testvar").append("\n");
//
//        it.eval(sb.toString());
//
//        assertThat(it.scope().defined("testvar"), is(true));
//        assertThat(it.scope().resolve("testvar").type(), is(NodeType.QUOTE));
//        assertThat(it.scope().resolve("testvar").toQuotedWord().getQuote(), is("Bye!"));
//
//        assertThat(outputs.size(), is(2));
//        assertThat(outputs.get(0), is("Hello!\n"));
//        assertThat(outputs.get(1), is("Bye!\n"));
//    }
    
    @Test
    public void testThing() {

        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar \"Hello!").append("\n");
        sb.append("\n");
        sb.append("print thing \"testvar").append("\n");

        it.eval(sb.toString());

//        assertThat(it.scope().defined("testvar"), is(true));
        assertThat(it.resolve("testvar").type(), is(NodeType.QUOTE));
        assertThat(it.resolve("testvar").toQuotedWord().getQuote(), is("Hello!"));

        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("Hello!\n"));
    }

}
