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
package ch.uprisesoft.yali.runtime.functions.builtin;

import ch.qos.logback.classic.Level;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.parser.Reader;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rma
 */
public class TemplateTest {
    
    private final Logger logger = LoggerFactory.getLogger(LogicTest.class);
    
    private Lexer l;
    private Reader p;
    private Interpreter it;
    private OutputObserver oo;
    private InputGenerator ig;
    private java.util.List<String> outputs;

    public TemplateTest() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.toLevel("off"));
    }
    
    @BeforeEach
    public void setUp() {
        logger.debug("(setUp) @BeforeEach");
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
        
        l = om.getLexer();
        p = om.getParser();
        it = om.getInterpreter();
        
        outputs = new ArrayList<>();
    }
    
    @Test
    public void testMap() {
        Node result = it.eval("map [? * ?] [1 2 3]");
        
        assertThat(result.getChildren().size(), is(3));
        assertThat(result.getChildren().get(0).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).toSymbolWord().getSymbol(), is("1"));
        assertThat(result.getChildren().get(1).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(1).toSymbolWord().getSymbol(), is("4"));
        assertThat(result.getChildren().get(2).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(2).toSymbolWord().getSymbol(), is("9"));
    }
    
    @Test
    public void testMap2() {
        Node result = it.eval("map [equal? mod ? 2 1] [1 2 3 4]");
        
        assertThat(result.getChildren().size(), is(4));
        assertThat(result.getChildren().get(0).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).toSymbolWord().getSymbol(), is("true"));
        assertThat(result.getChildren().get(1).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(1).toSymbolWord().getSymbol(), is("false"));
        assertThat(result.getChildren().get(2).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(2).toSymbolWord().getSymbol(), is("true"));
        assertThat(result.getChildren().get(3).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(3).toSymbolWord().getSymbol(), is("false"));
    }
    
     @Test
    public void testMapQuote() {
        Node result = it.eval("map [uppercase ?] \"abcd");
        
        assertThat(result.type(), is(NodeType.QUOTE));
        assertThat(result.toQuotedWord().getQuote(), is("ABCD"));
    }
    
    @Test
    public void testFilter() {
        Node result = it.eval("filter [equal? mod ? 2 1] [1 2 3 4]");
        
        assertThat(result.getChildren().size(), is(2));
        assertThat(result.getChildren().get(0).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(0).toSymbolWord().getSymbol(), is("1"));
        assertThat(result.getChildren().get(1).type(), is(NodeType.SYMBOL));
        assertThat(result.getChildren().get(1).toSymbolWord().getSymbol(), is("3")); 
    }
    
    @Test
    public void testFilterQuote() {
        Node result = it.eval("filter [notequal? ? \"b] \"abcd");
        
        assertThat(result.type(), is(NodeType.QUOTE));
        assertThat(result.toQuotedWord().getQuote(), is("acd"));
    }
    
    @Test
    public void testFind() {
        Node result = it.eval("find [equal? ? 2] [1 2 3 4]");
        
        assertThat(result.toSymbolWord().getSymbol(), is("2"));
    }
    
    @Test
    public void testFindWord() {
        Node result = it.eval("find [equal? ? \"b] \"abcd");
        
        assertThat(result.toQuotedWord().getQuote(), is("b"));
    }
}
