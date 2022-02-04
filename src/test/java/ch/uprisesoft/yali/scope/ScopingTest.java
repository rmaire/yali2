/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.scope;

import ch.qos.logback.classic.Level;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.runtime.functions.builtin.LogicTest;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rma
 */
public class ScopingTest {

    Interpreter it;
    private OutputObserver oo;
    private InputGenerator ig;
    private java.util.List<String> outputs;

    public ScopingTest() {
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

        assertThat(it.env().resolveable("testit"), is(true));
        assertThat(it.env().resolve("testit").type(), is(NodeType.QUOTE));
        assertThat(it.env().resolve("testit").toQuotedWord().getQuote(), is("someval"));
    }

    @Test
    public void testMakeInProcedure() {
        StringBuilder sb = new StringBuilder();
        sb.append("to makeinproc").append("\n");
        sb.append("make \"testvar 10").append("\n");
        sb.append("end").append("\n");
        sb.append("makeinproc").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(it.env().resolveable("testvar"), is(true));
        assertThat(it.env().resolve("testvar").type(), is(NodeType.INTEGER));
        assertThat(it.env().resolve("testvar").toIntegerWord().getInteger(), is(10));
    }

    @Test
    public void testMakeInIf() {
        StringBuilder sb = new StringBuilder();
        sb.append("if true [make \"testvar 10]").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(it.env().resolveable("testvar"), is(true));
        assertThat(it.env().resolve("testvar").type(), is(NodeType.INTEGER));
        assertThat(it.env().resolve("testvar").toIntegerWord().getInteger(), is(10));
    }

    @Test
    public void testMakeInProcedureWithOverride() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar 20").append("\n");
        sb.append("to makeinproc").append("\n");
        sb.append("make \"testvar 10").append("\n");
        sb.append("end").append("\n");
        sb.append("makeinproc").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(it.env().resolveable("testvar"), is(true));
        assertThat(it.env().resolve("testvar").type(), is(NodeType.INTEGER));
        assertThat(it.env().resolve("testvar").toIntegerWord().getInteger(), is(10));
    }

    @Test
    public void testLocalMakeInProcedureWithoutOverride() {
        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar 20").append("\n");
        sb.append("to makeinproc").append("\n");
        sb.append("localmake \"testvar 10").append("\n");
        sb.append("print :testvar").append("\n");
        sb.append("end").append("\n");
        sb.append("makeinproc").append("\n");
        sb.append("print :testvar").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(it.env().resolveable("testvar"), is(true));
        assertThat(it.env().resolve("testvar").type(), is(NodeType.INTEGER));
        assertThat(it.env().resolve("testvar").toIntegerWord().getInteger(), is(20));

        assertThat(outputs.size(), is(2));
        assertThat(outputs.get(0), is("10\n"));
        assertThat(outputs.get(1), is("20\n"));
    }
    
    @Test
    public void testLocalAndMakeInProcedureWithoutOverride() {
        System.out.println("=============");
        System.out.println("testLocalAndMakeInProcedureWithoutOverride");
        System.out.println("=============");
        
        StringBuilder sb = new StringBuilder();
        sb.append("make \"testvar 20").append("\n");
        sb.append("to makeinproc").append("\n");
        sb.append("local \"testvar").append("\n");
        sb.append("make \"testvar 10").append("\n");
        sb.append("print :testvar").append("\n");
        sb.append("end").append("\n");
        sb.append("makeinproc").append("\n");
        sb.append("print :testvar").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(it.env().resolveable("testvar"), is(true));
        assertThat(it.env().resolve("testvar").type(), is(NodeType.INTEGER));
        assertThat(it.env().resolve("testvar").toIntegerWord().getInteger(), is(20));

        assertThat(outputs.size(), is(2));
        assertThat(outputs.get(0), is("10\n"));
        assertThat(outputs.get(1), is("20\n"));
    }
    
    @Test
    public void testLMakeInProcedureinBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("to makeinproc").append("\n");
        sb.append("if true [make \"testvar 10]").append("\n");
        sb.append("print :testvar").append("\n");
        sb.append("end").append("\n");
        sb.append("makeinproc").append("\n");
        sb.append("print :testvar").append("\n");
        Node res = it.eval(sb.toString());

        assertThat(it.env().resolveable("testvar"), is(true));
        assertThat(it.env().resolve("testvar").type(), is(NodeType.INTEGER));
        assertThat(it.env().resolve("testvar").toIntegerWord().getInteger(), is(10));

        assertThat(outputs.size(), is(2));
        assertThat(outputs.get(0), is("10\n"));
        assertThat(outputs.get(1), is("10\n"));
    }

}
