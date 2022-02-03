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

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.List;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.word.QuotedWord;
import ch.uprisesoft.yali.scope.Scope;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.uprisesoft.yali.runtime.procedures.ProcedureProvider;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Template implements ProcedureProvider {

    private static final Logger logger = LoggerFactory.getLogger(Template.class);
    private Interpreter it;

    public Node map(Scope scope, java.util.List<Node> args) {
        Node template = Node.none();

        if (args.get(0).type().equals(NodeType.LIST)) {
            template = args.get(0).toList();
        } else {
            throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST);
        }

        if (args.get(1).type().equals(NodeType.LIST)) {
            Node values = args.get(1).toList();

            java.util.List<Node> results = new ArrayList<>();

            for (int i = 0; i < values.getChildren().size(); i++) {
                Node val = values.getChildren().get(i);
                java.util.List<Node> realizedValues = new ArrayList<>();
                for (Node n : template.getChildren()) {
                    if (n.type().equals(NodeType.SYMBOL) && n.toSymbolWord().getSymbol().equals("?")) {
                        realizedValues.add(val);
                    } else {
                        realizedValues.add(n);
                    }
                }
                Call run = new Call("run");
                run.code(it.getProcedures().get("run"));
                run.addChild(new List(realizedValues));
                results.add(Node.symbol(it.eval(run).toString()));
            }
            return new List(results);

        } else if (args.get(1).type().equals(NodeType.QUOTE)) {
            String values = args.get(1).toQuotedWord().getQuote();

            String results = "";

            for (int i = 0; i < values.length(); i++) {
                Character val = values.charAt(i);
                java.util.List<Node> realizedValues = new ArrayList<>();
                for (Node n : template.getChildren()) {
                    if (n.type().equals(NodeType.SYMBOL) && n.toSymbolWord().getSymbol().equals("?")) {
                        realizedValues.add(Node.quote(String.valueOf(val)));
                    } else {
                        realizedValues.add(n);
                    }
                }
                Call run = new Call("run");
                run.code(it.getProcedures().get("run"));
                run.addChild(new List(realizedValues));
                results += it.eval(run).toString();
            }
            return new QuotedWord(results);

        } else {
            throw new NodeTypeException(args.get(1), args.get(1).type(), NodeType.LIST, NodeType.QUOTE);
        }
    }

    public Node filter(Scope scope, java.util.List<Node> args) {

        Node template = Node.none();

        if (args.get(0).type().equals(NodeType.LIST)) {
            template = args.get(0).toList();
        } else {
            throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST);
        }

        if (args.get(1).type().equals(NodeType.LIST)) {
            Node values = args.get(1).toList();

            java.util.List<Node> results = new ArrayList<>();

            for (int i = 0; i < values.getChildren().size(); i++) {
                Node val = values.getChildren().get(i);
                java.util.List<Node> realizedValues = new ArrayList<>();
                for (Node n : template.getChildren()) {
                    if (n.type().equals(NodeType.SYMBOL) && n.toSymbolWord().getSymbol().equals("?")) {
                        realizedValues.add(val);
                    } else {
                        realizedValues.add(n);
                    }
                }
                Call run = new Call("run");
                run.code(it.getProcedures().get("run"));
                run.addChild(new List(realizedValues));
                Node result = it.eval(run);

                if (!result.type().equals(NodeType.BOOLEAN)) {
                    throw new NodeTypeException(template, result.type(), NodeType.BOOLEAN);
                }

                if (result.toBooleanWord().getBoolean()) {
                    results.add(val);
                }
            }
            return new List(results);

        } else if (args.get(1).type().equals(NodeType.QUOTE)) {
            String values = args.get(1).toQuotedWord().getQuote();

            String results = "";

            for (int i = 0; i < values.length(); i++) {
                Character val = values.charAt(i);
                java.util.List<Node> realizedValues = new ArrayList<>();
                for (Node n : template.getChildren()) {
                    if (n.type().equals(NodeType.SYMBOL) && n.toSymbolWord().getSymbol().equals("?")) {
                        realizedValues.add(Node.symbol("\""+String.valueOf(val)));
                    } else {
                        realizedValues.add(n);
                    }
                }
                Call run = new Call("run");
                run.code(it.getProcedures().get("run"));
                run.addChild(new List(realizedValues));
                Node result = it.eval(run);

                if (!result.type().equals(NodeType.BOOLEAN)) {
                    throw new NodeTypeException(template, result.type(), NodeType.BOOLEAN);
                }

                if (result.toBooleanWord().getBoolean()) {
                    results += val;
                }
            }
            return new QuotedWord(results);

        } else {
            throw new NodeTypeException(args.get(1), args.get(1).type(), NodeType.LIST, NodeType.QUOTE);
        }
    }

    public Node find(Scope scope, java.util.List<Node> args) {
        Node template = Node.none();

        if (args.get(0).type().equals(NodeType.LIST)) {
            template = args.get(0).toList();
        } else {
            throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST);
        }

        if (args.get(1).type().equals(NodeType.LIST)) {
            Node values = args.get(1).toList();

            for (int i = 0; i < values.getChildren().size(); i++) {
                Node val = values.getChildren().get(i);
                java.util.List<Node> realizedValues = new ArrayList<>();
                for (Node n : template.getChildren()) {
                    if (n.type().equals(NodeType.SYMBOL) && n.toSymbolWord().getSymbol().equals("?")) {
                        realizedValues.add(val);
                    } else {
                        realizedValues.add(n);
                    }
                }
                Call run = new Call("run");
                run.code(it.getProcedures().get("run"));
                run.addChild(new List(realizedValues));
                Node result = it.eval(run);

                if (!result.type().equals(NodeType.BOOLEAN)) {
                    throw new NodeTypeException(template, result.type(), NodeType.BOOLEAN);
                }

                if (result.toBooleanWord().getBoolean()) {
                    return val;
                }
            }

        } else if (args.get(1).type().equals(NodeType.QUOTE)) {
            String values = args.get(1).toQuotedWord().getQuote();

            for (int i = 0; i < values.length(); i++) {
                Character val = values.charAt(i);
                java.util.List<Node> realizedValues = new ArrayList<>();
                for (Node n : template.getChildren()) {
                    if (n.type().equals(NodeType.SYMBOL) && n.toSymbolWord().getSymbol().equals("?")) {
                        realizedValues.add(Node.symbol("\""+String.valueOf(val)));
                    } else {
                        realizedValues.add(n);
                    }
                }
                Call run = new Call("run");
                run.code(it.getProcedures().get("run"));
                run.addChild(new List(realizedValues));
                Node result = it.eval(run);

                if (!result.type().equals(NodeType.BOOLEAN)) {
                    throw new NodeTypeException(template, result.type(), NodeType.BOOLEAN);
                }

                if (result.toBooleanWord().getBoolean()) {
                    return Node.quote(String.valueOf(val));
                }
            }

        } else {
            throw new NodeTypeException(args.get(1), args.get(1).type(), NodeType.LIST, NodeType.QUOTE);
        }
        
//        Node template = Node.none();
//        Node values = Node.none();
//
//        if (args.get(0).type().equals(NodeType.LIST)) {
//            template = args.get(0).toList();
//        } else {
//            throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST);
//        }
//
//        if (args.get(1).type().equals(NodeType.LIST)) {
//            values = args.get(1).toList();
//        } else {
//            throw new NodeTypeException(args.get(1), args.get(1).type(), NodeType.LIST);
//        }
//
//        for (int i = 0; i < values.getChildren().size(); i++) {
//            Node val = values.getChildren().get(i);
//            java.util.List<Node> realizedValues = new ArrayList<>();
//            for (Node n : template.getChildren()) {
//                if (n.type().equals(NodeType.SYMBOL) && n.toSymbolWord().getSymbol().equals("?")) {
//                    realizedValues.add(val);
//                } else {
//                    realizedValues.add(n);
//                }
//            }
//            Call run = new Call("run");
//            run.addChild(new List(realizedValues));
//            Node result = it.eval(run);
//
//            if (!result.type().equals(NodeType.BOOLEAN)) {
//                throw new NodeTypeException(template, result.type(), NodeType.BOOLEAN);
//            }
//
//            if (result.toBooleanWord().getBoolean()) {
//                return val;
//            }
//        }

        return Node.nil();
    }

    @Override
    public Interpreter registerProcedures(Interpreter interpreter) {
        this.it = interpreter;

        it.define(new Procedure("map", (scope, val) -> this.map(scope, val), "template", "values"));
        it.define(new Procedure("filter", (scope, val) -> this.filter(scope, val), "template", "values"));
        it.define(new Procedure("find", (scope, val) -> this.find(scope, val), "template", "values"));

        return it;
    }
}
