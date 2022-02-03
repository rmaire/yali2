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
import ch.uprisesoft.yali.eval.PrettyPrinter;
import ch.uprisesoft.yali.eval.TreeWalkEvaluator;
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

    // Function definitions
    private Map<String, Procedure> functions = new HashMap<>();

    // Call stack
    private List<Scope> scopeStack = new ArrayList<>();
    private List<Procedure> callStack = new ArrayList<>();

    {
        scopeStack.add(new Scope("global"));
    }

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

        TreeWalkEvaluator eval = new TreeWalkEvaluator(this);
        try {
            node.accept(eval);
        } catch (StackOverflowError soe) {
            node.accept(eval);
        }

        return eval.getResult();
    }

    public Node read(String source) {
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        return node;
    }

    /**
     * Variable management
     */
    public void defineVar(String name, Node value) {

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).defined(name)) {
                logger.debug("(Scope) defining variable " + name + " in scope " + scopeStack.get(i).getScopeName());
                scopeStack.get(i).define(name.toLowerCase(), value);
                return;
            }
        }

        logger.debug("(Scope) defining variable " + name + " in scope " + scopeStack.get(0).getScopeName());
        scopeStack.get(0).define(name.toLowerCase(), value);
    }

    public void localVar(String name) {
        logger.debug("(Scope) Reserve local variable " + name + " in scope " + scope().getScopeName());
        scope().local(name.toLowerCase());
    }

    public Scope scope() {
        return scopeStack.get(scopeStack.size() - 1);
    }

    public void scope(String name) {
        Scope newScope = new Scope(name);
        scopeStack.add(newScope);
    }

    public void unscope() {
        scopeStack.remove(scopeStack.size() - 1);
    }

    public Node resolve(String name) {

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).defined(name)) {
                return scopeStack.get(i).resolve(name);
            }
        }

        return Node.none();
    }

    public Boolean resolveable(String name) {

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).defined(name)) {
                return true;
            }
        }

        return false;
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

    public Node apply(Call call) {

        logger.debug("(FunctionDispatcher) dispatch function " + call.name() + " with scope " + scope().getScopeName());

        if (!functions.containsKey(call.name())) {
            throw new FunctionNotFoundException(call.name());
        }

        Procedure procedure = functions.get(call.name());

        callStack.add(procedure);

        // TODO check last function call for recursion
        if (checkIfRecursiveCall(call.name())) {
            removeRecursion(call.name());
        }

        Node result = Node.nil();

        if (!procedure.isMacro()) {
            scope(procedure.getName());
        }

        // TODO differentiate from macros
        if (procedure.isNative() || procedure.isMacro()) {

            logger.debug("(FunctionDispatcher) native function");

            result = procedure.getNativeCall().apply(scope(), call.args());

        } else {
            logger.debug("(FunctionDispatcher) non-native function");

            TreeWalkEvaluator evaluator = new TreeWalkEvaluator(this);

            for (Node line : procedure.getChildren()) {

                // every direct child should be a function call
                if (!line.type().equals(NodeType.PROCCALL)) {
                    throw new NodeTypeException(line, line.type(), NodeType.PROCCALL);
                }

                // Check if function call is output or stop. If yes, no further
                // lines will be evaluated
                if (line.toCall().name().equals("output") || line.toCall().name().equals("stop")) {
                    logger.debug("(FunctionDispatcher) function " + procedure.getName() + " is cancelled.");
                    break;
                }

                line.accept(evaluator);
                result = evaluator.getResult();
            }
        }

        if (!procedure.isMacro()) {
            unscope();
        }

        callStack.remove(callStack.size() - 1);

        return result;
    }

    private boolean checkIfRecursiveCall(String name) {

        if (scopeStack.size() < 2) {
            return false;
        }
        if (callStack.size() < 2) {
            return false;
        }

        for (int i = scopeStack.size() - 2; i >= 0; i--) {
            if (scopeStack.get(i).getScopeName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void removeRecursion(String name) {
        Scope actualScope = scopeStack.get(scopeStack.size() - 1);
        Procedure actualCall = callStack.get(callStack.size() - 1);

        // Remove scopes
        // First one needs to be removed anyway
        scopeStack.remove(scopeStack.size() - 1);
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).getScopeName().equals(name)) {
                scopeStack.add(actualScope);
                break;
            } else {
                scopeStack.remove(i);
            }
        }

        // Remove calls
        // First one needs to be removed anyway
        callStack.remove(callStack.size() - 1);
        for (int i = callStack.size() - 1; i >= 0; i--) {
            if (callStack.get(i).getName().equals(name)) {
                callStack.add(actualCall);
                break;
            } else {
                callStack.remove(i);
            }
        }
    }

    /**
     * Procedure management functionality
     */
    public void defineProc(Procedure function) {
        functions.put(function.getName(), function);
//        arities.put(function.getName(), function.getArity());
    }

    public Boolean defined(String name) {
        return functions.containsKey(name);
    }

    public Map<String, Procedure> getProcedures() {
        return functions;
    }

    public void alias(String original, String alias) {
        if (!(functions.containsKey(original))) {
            throw new FunctionNotFoundException(original);
        }

        functions.put(alias, functions.get(original));
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

    public String pretty(String source) {
        PrettyPrinter pp = new PrettyPrinter();
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        for (Node expression : node.getChildren()) {
            if (expression.type().equals(NodeType.PROCCALL)) {
                pp.evaluate(expression.toCall());
            } else {
                throw new NodeTypeException(expression, expression.type(), NodeType.PROCCALL);
            }
        }
        return pp.build();
    }
}
