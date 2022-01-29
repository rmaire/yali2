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

import ch.uprisesoft.yali.ast.node.ProcedureCall;
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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rma
 */
public class Interpreter implements OutputObserver {

    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

//    private ProcedureDispatcher functions;
    private Scope variables;
    private TreeWalkEvaluator eval;
    
    private Map<String, Procedure> functions = new HashMap<>();
    private Map<String, Integer> arities = new HashMap<>();

    Interpreter(Scope variables) {
        this.variables = variables;
        eval = new TreeWalkEvaluator(variables, this);
        
    }

    public Node eval(String source) {
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        eval.evaluate(node.getChildren());
        return eval.getResult();
    }

    public Node eval(String source, Scope scope) {
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        eval.evaluate(node.getChildren());
        return eval.getResult();
    }

    public Node eval(Node node) {
        node.accept(eval);
        return eval.getResult();
    }

    public Node eval(Node node, Scope scope) {
        node.accept(eval);
        return eval.getResult();
    }

    public Node read(String source) {
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        return node;
    }

    public Scope scope() {
        return variables;
    }

    public TreeWalkEvaluator evaluator() {
        return eval;
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
                pp.evaluate(expression.toProcedureCall());
            } else {
                throw new NodeTypeException(expression, expression.type(), NodeType.PROCCALL);
            }
        }
        return pp.build();
    }
    
    public Node apply(String name, Scope scope, java.util.List<Node> args) {

        // TODO here is a good place to set parent scope too
        logger.debug("(FunctionDispatcher) dispatch function " + name + " with scope " + scope.getScopeName());

        if (!functions.containsKey(name)) {
            throw new FunctionNotFoundException(name);
        }

        Procedure function = functions.get(name);

        // TODO check last function call for recursion       
        Scope callScope = scope;

        Node result = Node.nil();

        // TODO differentiate from macros
        if (function.isNative() || function.isMacro()) {

            logger.debug("(FunctionDispatcher) native function");
            result = function.getNativeCall().apply(callScope, args);
        } else {
            logger.debug("(FunctionDispatcher) non-native function");

            TreeWalkEvaluator evaluator = new TreeWalkEvaluator(callScope, this);

            for (Node line : function.getChildren()) {

                // every direct child should be a function call
                if (!line.type().equals(NodeType.PROCCALL)) {
                    throw new NodeTypeException(line, line.type(), NodeType.PROCCALL);
                }

                line.accept(evaluator);
                result = evaluator.getResult();

                // Check if function call is output or stop. If yes, no further
                // lines will be evaluated
                if (line.toProcedureCall().getName().equals("output") || line.toProcedureCall().getName().equals("stop")) {
                    logger.debug("(FunctionDispatcher) function " + function.getName() + " is cancelled.");
                    break;
                }
            }
        }

        return result;
    }

    private boolean checkIfRecursiveCall(Scope scope) {
        String currentName = scope.getScopeName();
        Scope currentScope = scope;
        while (currentScope.getEnclosingScope().isPresent()) {
            currentScope = currentScope.getEnclosingScope().get();
            if (currentName.equals(currentScope.getScopeName())) {
                return true;
            }
        }
        return false;
    }

    private int calcDistanceToRecurse(Scope scope) {
        int dist = 1;
        String currentName = scope.getScopeName();
        Scope currentScope = scope;
        while (currentScope.getEnclosingScope().isPresent()) {
            dist++;
            currentScope = currentScope.getEnclosingScope().get();
            if (currentName.equals(currentScope.getScopeName())) {
                return dist;
            }
        }
        return -1;
    }

    private Scope removaRecursion(Scope scope, int depth) {
        Scope newParent = scope.getEnclosingScope().get();
        Scope currentScope = scope;

        for (int i = 0; i < depth; i++) {
            currentScope = currentScope.getEnclosingScope().get();
        }

        return currentScope;
    }

    public void define(Procedure function) {
        functions.put(function.getName(), function);
        arities.put(function.getName(), function.getArity());
    }

    public Boolean defined(String name) {
        return functions.containsKey(name);
    }

    public Map<String, Integer> getArities() {
        return arities;
    }

    public Map<String, Procedure> getFunctions() {
        return functions;
    }

    public void alias(String original, String alias) {
        if (!(functions.containsKey(original))) {
            throw new FunctionNotFoundException(original);
        }

        functions.put(alias, functions.get(original));
        arities.put(alias, functions.get(original).getArity());
    }

    public Interpreter loadStdLib(Interpreter it, OutputObserver oo) {

        logger.debug("Loading StdLib");

        Logic logic = new Logic();
        logic.registerProcedures(it);

        Control control = new Control();
        control.registerProcedures(it);

        Arithmetic arithmetic = new Arithmetic();
        arithmetic.registerProcedures(it);

        Template template = new Template();
        template.registerProcedures(it);

        Data data = new Data();
        data.registerProcedures(it);

        return it;
    }

    public Interpreter loadStdLib(Interpreter it, OutputObserver oo, InputGenerator ig) {
        logger.debug("Loading StdLib with IO");
        IO com = new IO();
        com.register(oo);
        com.register(ig);
        com.registerProcedures(it);

        return loadStdLib(it, oo);
    }
}
