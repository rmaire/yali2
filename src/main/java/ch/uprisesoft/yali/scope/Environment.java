/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.scope;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.eval.TreeWalkEvaluator;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.runtime.procedures.FunctionNotFoundException;
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
public class Environment {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    private Map<String, Procedure> procedures = new HashMap<>();

    private List<Scope> scopeStack = new ArrayList<>();
    private List<Procedure> callStack = new ArrayList<>();

    {
        scopeStack.add(new Scope("global"));
    }

    public Node apply(Call call) {

        logger.debug("(FunctionDispatcher) dispatch function " + call.name() + " with scope " + peek().getScopeName());

        if (!procedures.containsKey(call.name())) {
            throw new FunctionNotFoundException(call.name());
        }

        Procedure procedure = procedures.get(call.name());

        callStack.add(procedure);

        // TODO check last function call for recursion
        Node result = Node.nil();

        if (!procedure.isMacro()) {
            Scope newScope = new Scope(procedure.getName());
            push(newScope);
        }

        // TODO differentiate from macros
        if (procedure.isNative() || procedure.isMacro()) {

            logger.debug("(FunctionDispatcher) native function");

            result = procedure.getNativeCall().apply(peek(), call.args());

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
            pop();
        }

        callStack.remove(callStack.size() - 1);

        return result;
    }

    public void define(Procedure function) {
        procedures.put(function.getName(), function);
//        arities.put(function.getName(), function.getArity());
    }

    public Boolean defined(String name) {
        return procedures.containsKey(name);
    }

    public Map<String, Procedure> getProcedures() {
        return procedures;
    }

    public void alias(String original, String alias) {
        if (!(procedures.containsKey(original))) {
            throw new FunctionNotFoundException(original);
        }

        procedures.put(alias, procedures.get(original));
    }

    public void push(Scope scope) {
        scopeStack.add(scope);
    }

    public Scope peek() {
        return scopeStack.get(scopeStack.size() - 1);
    }

    public Scope pop() {
        Scope ret = scopeStack.get(scopeStack.size() - 1);
        scopeStack.remove(scopeStack.size() - 1);
        return ret;
    }

    public void make(String name, Node value) {

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

    public void local(String name) {
        logger.debug("(Scope) Reserve local variable " + name + " in scope " + peek().getScopeName());
        peek().local(name.toLowerCase());
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
}
