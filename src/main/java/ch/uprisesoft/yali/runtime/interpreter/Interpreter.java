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
package ch.uprisesoft.yali.runtime.interpreter;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.lexer.Token;
import ch.uprisesoft.yali.parser.Reader;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import ch.uprisesoft.yali.runtime.procedures.FunctionNotFoundException;
import ch.uprisesoft.yali.runtime.procedures.builtin.Arithmetic;
import ch.uprisesoft.yali.runtime.procedures.builtin.Control;
import ch.uprisesoft.yali.runtime.procedures.builtin.Data;
import ch.uprisesoft.yali.runtime.procedures.builtin.IO;
import ch.uprisesoft.yali.runtime.procedures.builtin.Logic;
import ch.uprisesoft.yali.runtime.procedures.builtin.Template;
import ch.uprisesoft.yali.scope.Environment;
import ch.uprisesoft.yali.scope.Scope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rma
 */
public class Interpreter implements OutputObserver {

    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);
    
    private Environment env = new Environment();

    /**
     * Interpreting functionality
     */
    public Node eval(String source) {
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        Node result = Node.none();
        for (Node pc : node.getChildren()) {
            result = eval(pc.toCall());
        }
        return result;
    }

    public Node eval(Node node) {

//        TreeWalkEvaluator eval = new TreeWalkEvaluator(env);
        Node res = Node.none();
        try {
            res = node.evaluate(env);
        } catch (StackOverflowError soe) {
            res = node.evaluate(env);
        }

        return res;
    }

    public Node read(String source) {
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        return node;
    }

    public Environment env() {
        return env;
    }

    public java.util.List<String> stringify(Node arg) {
        java.util.List<String> stringifiedArgs = new ArrayList<>();
        if (arg.type().equals(NodeType.LIST)) {
            stringifiedArgs.addAll(stringify(arg.getChildren()));
        } else {
            logger.debug("stringify " + arg.toString());
            stringifiedArgs.add(arg.toString());
        }
        return stringifiedArgs;
    }

    public java.util.List<String> stringify(java.util.List<Node> args) {
        java.util.List<String> stringifiedArgs = new ArrayList<>();
        for (Node arg : args) {
            if (arg.type().equals(NodeType.LIST)) {
                stringifiedArgs.addAll(stringify(arg.getChildren()));
            } else {
                logger.debug("stringify " + arg.toString());
                stringifiedArgs.add(arg.toString());
            }
        }
        return stringifiedArgs;
    }

    public Interpreter loadStdLib() {

        logger.debug("Loading StdLib");

        Logic logic = new Logic();
        logic.registerProcedures(this);

        Control control = new Control();
        control.registerProcedures(this);

        Arithmetic arithmetic = new Arithmetic();
        arithmetic.registerProcedures(this);

        Template template = new Template();
        template.registerProcedures(this);

        Data data = new Data();
        data.registerProcedures(this);

        return this;
    }

    public Interpreter loadStdLib(OutputObserver oo, InputGenerator ig) {
        logger.debug("Loading StdLib with IO");
        IO com = new IO();
        com.register(oo);
        com.register(ig);
        com.registerProcedures(this);

        return loadStdLib();
    }

    /**
     * Observer and helper methods
     */
    @Override
    public void inform(String output) {
        logger.debug("(Interpreter) " + output);
    }

//    public String pretty(String source) {
//        PrettyPrinter pp = new PrettyPrinter();
//        java.util.List<Token> tokens = new Lexer().scan(source);
//        Node node = new Reader(this).read(tokens);
//        for (Node expression : node.getChildren()) {
//            if (expression.type().equals(NodeType.PROCCALL)) {
//                pp.evaluate(expression.toCall());
//            } else {
//                throw new NodeTypeException(expression, expression.type(), NodeType.PROCCALL);
//            }
//        }
//        return pp.build();
//    }
}
