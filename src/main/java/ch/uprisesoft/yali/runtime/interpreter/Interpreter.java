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
import ch.uprisesoft.yali.scope.VariableNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
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
    private Map<String, Integer> arities = new HashMap<>();

    // Call stack
    private List<Scope> scopeStack = new ArrayList<>();
    private List<Procedure> callStack = new ArrayList<>();
    
    Interpreter(Scope variables) {
        scopeStack.add(variables);
    }

    /**
     * Interpreting functionality 
     */
    
    public Node eval(String source) {
        java.util.List<Token> tokens = new Lexer().scan(source);
        Node node = new Reader(this).read(tokens);
        TreeWalkEvaluator eval = new TreeWalkEvaluator(this);
        for(Node pc: node.getChildren()) {
            eval.evaluate(pc.toProcedureCall());
        }
        return eval.getResult();
    }

    public Node eval(Node node) {
        TreeWalkEvaluator eval = new TreeWalkEvaluator(this);
        node.accept(eval);
        return eval.getResult();
    }

    public Node eval(Node node, Scope scope) {
        TreeWalkEvaluator eval = new TreeWalkEvaluator(this);
        node.accept(eval);
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
        
        for(int i = scopeStack.size() - 1; i >= 0; i--) {
            if(scopeStack.get(i).defined(name)) {
                logger.debug("(Scope) defining variable " + name + " in scope " + scopeStack.get(i).getScopeName());
                scopeStack.get(i).define(name.toLowerCase(), value);
                return;
            }
        }
        
//        Iterator<Scope> scopes = scopeStack.iterator();
//        while(scopes.hasNext()) {
//            Scope scope = scopes.next();
//            if(scope.defined(name)) {
//                logger.debug("(Scope) defining variable " + name + " in scope " + scope.getScopeName());
//                scope.define(name.toLowerCase(), value);
//                return;
//            }
//        }

        logger.debug("(Scope) defining variable " + name + " in scope " + scopeStack.get(0).getScopeName());
        scopeStack.get(0).define(name.toLowerCase(), value);
    }
    
    public void localVar(String name) {
        logger.debug("(Scope) Reserve local variable " + name + " in scope " + scope().getScopeName());
        scope().local(name.toLowerCase());
    }

    public Scope scope() {
        return scopeStack.get(scopeStack.size()-1);
    }
    
    public void scope(String name) {
        Scope newScope = new Scope(name);
        scopeStack.add(newScope);
    }
    
    public void unscope(){
        scopeStack.remove(scopeStack.size()-1);
    }
    
    public Node resolve(String name) {
        
        for(int i = scopeStack.size() - 1; i >= 0; i--) {
            if(scopeStack.get(i).defined(name)) {
                return scopeStack.get(i).resolve(name);
            }
        }
        
//        Iterator<Scope> scopes = scopeStack.iterator();
//        
//        while(scopes.hasNext()) {
//            Scope scope = scopes.next();
//            if(scope.defined(name)) {
//                return scope.resolve(name);
//            }
//        }
        
        return Node.none();
    }
    
    public Boolean resolveable(String name) {
        
        for(int i = scopeStack.size() - 1; i >= 0; i--) {
            if(scopeStack.get(i).defined(name)) {
                return true;
            }
        }
        
//        Iterator<Scope> scopes = scopeStack.iterator();
//        
//        while(scopes.hasNext()) {
//            Scope scope = scopes.next();
//            if(scope.defined(name)) {
//                return true;
//            }
//        }
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

    public Node apply(String name, Scope scope, java.util.List<Node> args) {

        // TODO here is a good place to set parent scope too
        logger.debug("(FunctionDispatcher) dispatch function " + name + " with scope " + scope.getScopeName());

        if (!functions.containsKey(name)) {
            throw new FunctionNotFoundException(name);
        }

        Procedure procedure = functions.get(name);
        
        callStack.add(procedure);

        // TODO check last function call for recursion       

        Node result = Node.nil();
        
        if (!procedure.isMacro()) {
            scope(procedure.getName());
        }

        // TODO differentiate from macros
        if (procedure.isNative() || procedure.isMacro()) {

            logger.debug("(FunctionDispatcher) native function");
            result = procedure.getNativeCall().apply(scope(), args);
        } else {
            logger.debug("(FunctionDispatcher) non-native function");

            TreeWalkEvaluator evaluator = new TreeWalkEvaluator(this);

            for (Node line : procedure.getChildren()) {

                // every direct child should be a function call
                if (!line.type().equals(NodeType.PROCCALL)) {
                    throw new NodeTypeException(line, line.type(), NodeType.PROCCALL);
                }

                line.accept(evaluator);
                result = evaluator.getResult();

                // Check if function call is output or stop. If yes, no further
                // lines will be evaluated
                if (line.toProcedureCall().getName().equals("output") || line.toProcedureCall().getName().equals("stop")) {
                    logger.debug("(FunctionDispatcher) function " + procedure.getName() + " is cancelled.");
                    break;
                }
            }
        }
        
        if (!procedure.isMacro()) {
            unscope();
        }
        
        callStack.remove(callStack.size()-1);

        return result;
    }

    private boolean checkIfRecursiveCall(String name) {
        
        return false;
    }

//    private int calcDistanceToRecurse(Scope scope) {
//        int dist = 1;
//        String currentName = scope.getScopeName();
//        Scope currentScope = scope;
//        while (currentScope.getEnclosingScope().isPresent()) {
//            dist++;
//            currentScope = currentScope.getEnclosingScope().get();
//            if (currentName.equals(currentScope.getScopeName())) {
//                return dist;
//            }
//        }
//        return -1;
//    }

//    private Scope removeRecursion(Scope scope, int depth) {
//        Scope newParent = scope.getEnclosingScope().get();
//        Scope currentScope = scope;
//
//        for (int i = 0; i < depth; i++) {
//            currentScope = currentScope.getEnclosingScope().get();
//        }
//
//        return currentScope;
//    }
    
    /**
     * Procedure management functionality 
     */

    public void defineProc(Procedure function) {
        functions.put(function.getName(), function);
        arities.put(function.getName(), function.getArity());
    }

    public Boolean defined(String name) {
        return functions.containsKey(name);
    }

    public Map<String, Integer> getArities() {
        return arities;
    }

    public Map<String, Procedure> getProcedures() {
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
                pp.evaluate(expression.toProcedureCall());
            } else {
                throw new NodeTypeException(expression, expression.type(), NodeType.PROCCALL);
            }
        }
        return pp.build();
    }
}
