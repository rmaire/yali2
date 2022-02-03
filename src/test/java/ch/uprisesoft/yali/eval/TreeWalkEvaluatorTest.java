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
package ch.uprisesoft.yali.eval;

import ch.qos.logback.classic.Level;
import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.parser.Reader;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.scope.Scope;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rma
 */
public class TreeWalkEvaluatorTest {

    TreeWalkEvaluator twe;
    Reader p;
    Lexer l;

    public TreeWalkEvaluatorTest() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.toLevel("off"));
    }

    @BeforeEach
    public void setUp() {
        ObjectMother om = new ObjectMother();
        l = om.getLexer();
        p = om.getParser();
        twe = new TreeWalkEvaluator(om.getInterpreter());
    }

    private Node parse(String input) {
        return p.read(l.scan(input)).getChildren().get(0);
    }

    private Node eval(Node node) {
        node.accept(twe);
        return twe.getResult();
    }

    @Test
    public void testBasicAddition() {
        String input = "5 + 3.0";

        Call node = (Call) parse(input);
        Node result = eval(node);

        assertThat(result.type(), is(NodeType.FLOAT));
        assertThat(result.toFloatWord().getFloat(), is(8.0));

    }

    @Test
    public void testMultipleAddition() {
        String input = "5 + 3 + 2 + 5";

        Call node = (Call) parse(input);
        Node result = eval(node);

        assertThat(result.type(), is(NodeType.INTEGER));
        assertThat(result.toIntegerWord().getInteger(), is(15));

    }

    @Test
    public void testDefinedFunction() {
//        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
//        rootLogger.setLevel(Level.toLevel("debug"));

        String input = "5 + 3 + 2.0 + 5";

        Call node = (Call) parse(input);
        Node result = eval(node);

        assertThat(result.type(), is(NodeType.FLOAT));
        assertThat(result.toFloatWord().getFloat(), is(15.0));
    }
}
