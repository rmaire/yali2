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
package ch.uprisesoft.yali.runtime.procedures.builtin;

import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.scope.Scope;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.uprisesoft.yali.runtime.procedures.ProcedureProvider;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Control implements ProcedureProvider {

    private static final Logger logger = LoggerFactory.getLogger(Control.class);
    private Interpreter it;

    // Only here so we don't have to strip it out as edge cases in the Reader. Does nothing.
    public Node alias(Scope scope, java.util.List<Node> args) {
        return Node.nil();
    }
    
    public Node thing(Scope scope, java.util.List<Node> args) {
       
        String name = "";
        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }
        
        logger.debug("(Control) resolvong variable " + name + " in scope " + scope.getScopeName());

        Node value = it.resolve(name);

        return value;
    }
    
    public Node local(Scope scope, java.util.List<Node> args) {
        String name = "";
        Scope workScope = scope;
        
        // Variable should be defined in calling scope, not in make scope
        
        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }
        
        logger.debug("(Control) defining variable " + name + " in scope " + workScope.getScopeName());

        it.env().local(name);

        return Node.nil();
    }
    
    public Node make(Scope scope, java.util.List<Node> args) {
        Node newVar = null;
        String name = "";
        Scope workScope = scope;
        
        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }
        
        logger.debug("(Control) defining variable " + name + " in scope " + workScope.getScopeName());

        newVar = args.get(1);
        it.env().make(name, newVar);

        return newVar;
    }
    
    public Node localmake(Scope scope, java.util.List<Node> args) {
        Node newVar = null;
        String name = "";
        Scope workScope = scope;
        
        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }
        
        logger.debug("(Control) defining variable " + name + " in scope " + workScope.getScopeName());

        newVar = args.get(1);
        it.env().local(name);
        it.env().make(name, newVar);

        return newVar;
    }

    public Node ifexpr(Scope scope, java.util.List<Node> args) {
        logger.debug("(Control) start ifexpr");
        Node result = Node.none();
        Node condition = it.eval(args.get(0));
        Node iftrue = args.get(1);

        if (!condition.type().equals(NodeType.BOOLEAN)) {
            throw new NodeTypeException(condition, condition.type(), NodeType.BOOLEAN);
        }

        if (condition.toBooleanWord().getBoolean()) {
            logger.debug("(Control) condition is true");
            result = it.eval(String.join(" ", it.stringify(iftrue)));
        } else {
            logger.debug("(Control) condition is false");
            result = Node.nil();
        }

        logger.debug("(Control) end ifexpr");
        return result;
    }

    public Node ifelseexpr(Scope scope, java.util.List<Node> args) {
        logger.debug("(Control) start ifelseexpr");
        Node result = Node.none();
        Node condition = it.eval(args.get(0));
        Node iftrue = args.get(1);
        Node iffalse = args.get(2);

        if (!condition.type().equals(NodeType.BOOLEAN)) {
            throw new NodeTypeException(condition, condition.type(), NodeType.BOOLEAN);
        }

        if (condition.toBooleanWord().getBoolean()) {
            logger.debug("(Control) condition is true");
            result = it.eval(String.join(" ", it.stringify(iftrue)));
        } else {
            logger.debug("(Control) condition is false");
            result = it.eval(String.join(" ", it.stringify(iffalse)));
        }

        logger.debug("(Control) end ifelseexpr");
        return result;
    }

    public Node repeat(Scope scope, java.util.List<Node> args) {

        Scope workScope = scope;
//        if (scope.getEnclosingScope().isPresent()) {
//            workScope = scope.getEnclosingScope().get();
//        }

        Node control = args.get(0);
        Node block = args.get(1);

        if (!control.type().equals(NodeType.INTEGER)) {
            Node res = Node.none();
            try {
                res = it.eval(control);
            } catch (Exception ex) {
                System.out.println(ex.toString() + " -> repeat 1");
            }

            if (res.type().equals(NodeType.INTEGER)) {
                control = res;
            } else {
                throw new NodeTypeException(res, res.type(), NodeType.INTEGER);
            }
        }

        if (!block.type().equals(NodeType.LIST)) {
            throw new NodeTypeException(block, block.type(), NodeType.LIST);
        }

        Integer idx = control.toIntegerWord().getInteger();
        Node result = Node.nil();

        for (int i = 0; i < idx; i++) {
            try {
                result = it.eval(String.join(" ", it.stringify(block)));
            } catch (Exception ex) {
                System.out.println(ex.toString() + " -> repeat 2");
            }
        }

        return result;
    }

    public Node run(Scope scope, java.util.List<Node> args) {

        Scope workScope = scope;
//        if (scope.getEnclosingScope().isPresent()) {
//            workScope = scope.getEnclosingScope().get();
//        }
        
        Node result = Node.none();
        try {
            result = it.eval(String.join(" ", it.stringify(args.get(0))));
        } catch (Exception ex) {
            System.out.println(ex.toString() + " -> run");
        }
        return result;
    }

    public Node output(Scope scope, java.util.List<Node> args) {
        Node res = Node.none();
        try {
            res = it.eval(args.get(0));
        } catch (Exception ex) {
            System.out.println(ex.toString() + " -> output");
        }
        return res;
    }
    
    public Node stop(Scope scope, java.util.List<Node> args) {
        return Node.nil();
    }

    @Override
    public Interpreter registerProcedures(Interpreter interpreter) {
        this.it = interpreter;

        it.env().define(new Procedure("alias", (scope, val) -> this.alias(scope, val), "original", "alias"));
        it.env().define(new Procedure("thing", (scope, val) -> this.thing(scope, val), "name"));
        it.env().define(new Procedure("make", (scope, val) -> this.make(scope, val), "name", "value").macro());
        it.env().define(new Procedure("local", (scope, val) -> this.local(scope, val), "name").macro());
        it.env().define(new Procedure("localmake", (scope, val) -> this.localmake(scope, val), "name", "value").macro());
        it.env().define(new Procedure("repeat", (scope, val) -> this.repeat(scope, val), "control", "block").macro());
        it.env().define(new Procedure("run", (scope, val) -> this.run(scope, val), "block").macro());
        it.env().define(new Procedure("output", (scope, val) -> this.output(scope, val), "block"));
        it.env().define(new Procedure("stop", (scope, val) -> this.output(scope, val)));
        it.env().define(new Procedure("ifelse", (scope, val) -> this.ifelseexpr(scope, val), "condition", "iftrue", "iffalse").macro());
        it.env().define(new Procedure("if",(scope, val) -> this.ifexpr(scope, val),  "condition", "iftrue").macro());

        return it;
    }
}
