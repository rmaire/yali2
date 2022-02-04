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
import ch.uprisesoft.yali.ast.node.List;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.word.BooleanWord;
import ch.uprisesoft.yali.ast.node.word.QuotedWord;
import ch.uprisesoft.yali.ast.node.word.SymbolWord;
import ch.uprisesoft.yali.ast.node.word.Word;
import java.util.ArrayList;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.scope.Scope;
import java.util.Collections;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import ch.uprisesoft.yali.runtime.procedures.ProcedureProvider;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Data implements ProcedureProvider {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Data.class);

    private Interpreter it;

    // Constructors
    public Node reverse(Scope scope, java.util.List<Node> args) {
        if (args.get(0).type().equals(NodeType.LIST)) {
            List resultList = new List();
            java.util.List<Node> forwardList = args.get(0).getChildren();
            Collections.reverse(forwardList);
            resultList.addChildren(forwardList);
            return resultList;
        } else if (args.get(0).type().equals(NodeType.QUOTE)) {
            StringBuilder resultString = new StringBuilder(args.get(0).toQuotedWord().getQuote());
            return new QuotedWord(resultString.reverse().toString());
        } else if (args.get(0).type().equals(NodeType.SYMBOL)) {
            StringBuilder resultString = new StringBuilder(args.get(0).toSymbolWord().getSymbol());
            return new SymbolWord(resultString.reverse().toString());
        } else {
            throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST, NodeType.QUOTE, NodeType.SYMBOL);
        }
    }

    public Node fput(Scope scope, java.util.List<Node> args) {
        if (args.get(1).type().equals(NodeType.LIST)) {
            List resultList = new List();
            resultList.addChild(args.get(0));
            resultList.addChildren(args.get(1).getChildren());
            return resultList;
        } else if (args.get(1).type().equals(NodeType.QUOTE)) {
            StringBuilder resultString = new StringBuilder(args.get(0).toQuotedWord().getQuote()).append(args.get(1).toQuotedWord().getQuote());
            return new QuotedWord(resultString.toString());
        } else if (args.get(1).type().equals(NodeType.SYMBOL)) {
            StringBuilder resultString = new StringBuilder(args.get(0).toSymbolWord().getSymbol()).append(args.get(1).toSymbolWord().getSymbol());
            return new SymbolWord(resultString.toString());
        } else {
            throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST, NodeType.QUOTE, NodeType.SYMBOL);
        }
    }

    public Node lput(Scope scope, java.util.List<Node> args) {
        if (args.get(1).type().equals(NodeType.LIST)) {
            List resultList = new List();
            resultList.addChildren(args.get(1).getChildren());
            resultList.addChild(args.get(0));
            return resultList;
        } else if (args.get(1).type().equals(NodeType.QUOTE)) {
            StringBuilder resultString = new StringBuilder().append(args.get(1).toQuotedWord().getQuote()).append(args.get(0).toQuotedWord().getQuote());
            return new QuotedWord(resultString.toString());
        } else if (args.get(1).type().equals(NodeType.SYMBOL)) {
            StringBuilder resultString = new StringBuilder().append(args.get(1).toSymbolWord().getSymbol()).append(args.get(0).toSymbolWord().getSymbol());
            return new SymbolWord(resultString.toString());
        } else {
            throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST, NodeType.QUOTE, NodeType.SYMBOL);
        }
    }

    public Node word(Scope scope, java.util.List<Node> args) {
        String concatenated = "";

        for (Node arg : args) {
            concatenated += stringifyNode(arg);
        }

        return new SymbolWord(concatenated);
    }

    public String stringifyNode(Node node) {
        String concatenated = "";

        switch (node.type()) {
            case BOOLEAN:
            case FLOAT:
            case INTEGER:
            case QUOTE:
                concatenated += node.toString();
                break;
            case NIL:
                break;
            case PROCCALL:
                concatenated += stringifyNode(it.eval(node));
            case REFERENCE:
                concatenated += stringifyNode(it.eval(node));
            case LIST:
                throw new NodeTypeException(node, NodeType.SYMBOL, NodeType.LIST);
            case NONE:
                throw new NodeTypeException(node, NodeType.SYMBOL, NodeType.NONE);
        }

        return concatenated;
    }

    public Node sentence(Scope scope, java.util.List<Node> args) {
        List list = new List();
        list.addChildren(flatten(args));
        return list;
    }

    private java.util.List<Node> flatten(java.util.List<Node> list) {
        java.util.List<Node> flattened = new ArrayList<>();

        for (Node n : list) {
            if (n.type().equals(NodeType.LIST)) {
                flattened.addAll(flatten(n.getChildren()));
            } else {
                flattened.add(n);
            }
        }

        return flattened;
    }

    public Node list(Scope scope, java.util.List<Node> args) {
        List list = new List();

        for (Node n : args) {
            list.addChild(n);
        }

        return list;
    }

    public Node gensym(Scope scope, java.util.List<Node> args) {
        return new SymbolWord(UUID.randomUUID().toString().replace("-", ""));
    }

    // Selectors
    public Node first(Scope scope, java.util.List<Node> args) {
        Node first = args.get(0);

        switch (args.get(0).type()) {
            case LIST:
                first = first.getChildren().get(0);
                break;
            case QUOTE:
                first = new QuotedWord(first.toQuotedWord().toString().substring(0, 1));
                break;
            case SYMBOL:
                first = new SymbolWord(first.toSymbolWord().getSymbol().substring(0, 1));
                break;
            default:
                throw new NodeTypeException(first, args.get(0).type(), NodeType.LIST);
        }

        return first;
    }

    public Node last(Scope scope, java.util.List<Node> args) {

        Node last = args.get(0);

        switch (args.get(0).type()) {
            case LIST:
                last = last.getChildren().get(last.getChildren().size() - 1);
                break;
            case QUOTE:
                last = new QuotedWord(last.toQuotedWord().toString().substring(
                        last.toQuotedWord().toString().length() - 1,
                        last.toQuotedWord().toString().length()));
                break;
            case SYMBOL:
                last = new QuotedWord(args.get(0).toSymbolWord().getSymbol().substring(
                        last.toSymbolWord().getSymbol().length() - 1,
                        last.toSymbolWord().getSymbol().length()));
                break;
            default:
                throw new NodeTypeException(last, args.get(0).type(), NodeType.LIST);
        }

        return last;
    }

    public Node butfirst(Scope scope, java.util.List<Node> args) {

        Node butfirst = Node.none();

        switch (args.get(0).type()) {
            case LIST:
                butfirst = new List();
                butfirst.addChildren(args.get(0).getChildren().subList(1, args.get(0).getChildren().size()));
                break;
            case QUOTE:
                butfirst = new QuotedWord(args.get(0).toQuotedWord().toString().substring(
                        1,
                        args.get(0).toQuotedWord().toString().length()));
                break;
            case SYMBOL:
                butfirst = new QuotedWord(args.get(0).toSymbolWord().getSymbol().substring(
                        1,
                        args.get(0).toSymbolWord().getSymbol().length()));
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST);
        }

        return butfirst;
    }

    public Node butlast(Scope scope, java.util.List<Node> args) {

        Node butlast = Node.none();

        switch (args.get(0).type()) {
            case LIST:
                butlast = new List();
                butlast.addChildren(args.get(0).getChildren().subList(0, args.get(0).getChildren().size() - 1));
                break;
            case QUOTE:
                butlast = new QuotedWord(args.get(0).toQuotedWord().toString().substring(
                        0,
                        args.get(0).toQuotedWord().toString().length() - 1));
                break;
            case SYMBOL:
                butlast = new QuotedWord(args.get(0).toSymbolWord().getSymbol().substring(
                        0,
                        args.get(0).toSymbolWord().getSymbol().length() - 1));
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST);
        }

        return butlast;
    }

    public Node item(Scope scope, java.util.List<Node> args) {

        Node item = Node.none();
        Node index = it.eval(args.get(0));

        if (!index.type().equals(NodeType.INTEGER)) {
            throw new NodeTypeException(index, index.type(), NodeType.INTEGER);
        }

        switch (args.get(1).type()) {
            case LIST:
                item = args.get(1).getChildren().get(index.toIntegerWord().getInteger() - 1);
                break;
            case QUOTE:
                item = new QuotedWord(String.valueOf(
                        args.get(1).toQuotedWord().getQuote().charAt(
                                index.toIntegerWord().getInteger() - 1)));
                break;
            case SYMBOL:
                item = new SymbolWord(String.valueOf(
                        args.get(1).toSymbolWord().getSymbol().charAt(
                                index.toIntegerWord().getInteger() - 1)));
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.LIST);
        }

        return item;
    }

    // Mutators
    public Node setitem(Scope scope, java.util.List<Node> args) {

        Node index = it.eval(args.get(0));
        Node list = args.get(1);
        Node newVal = args.get(2);

        if (!index.type().equals(NodeType.INTEGER)) {
            throw new NodeTypeException(index, index.type(), NodeType.INTEGER);
        }

        if (!list.type().equals(NodeType.LIST)) {
            throw new NodeTypeException(list, list.type(), NodeType.LIST);
        }

        list.getChildren().set(index.toIntegerWord().getInteger() - 1, newVal);

        return list;
    }

    // Predicates
    public Node emptyp(Scope scope, java.util.List<Node> args) {

        Node list = args.get(0);
        Node empty = new BooleanWord(Boolean.FALSE);

        if (list.type().equals(NodeType.LIST) && list.getChildren().isEmpty()) {
            empty = new BooleanWord(Boolean.TRUE);
        }

        return empty;
    }

    public Node wordp(Scope scope, java.util.List<Node> args) {

        Node word = args.get(0);
        Node wordp = new BooleanWord(Boolean.FALSE);

        if (word.type().equals(NodeType.QUOTE) || word.type().equals(NodeType.SYMBOL) || word.type().equals(NodeType.INTEGER) || word.type().equals(NodeType.FLOAT)) {
            wordp = new BooleanWord(Boolean.TRUE);
        }

        return wordp;
    }

    public Node numberp(Scope scope, java.util.List<Node> args) {

        Node word = args.get(0);
        Node wordp = new BooleanWord(Boolean.FALSE);

        if (word.type().equals(NodeType.INTEGER) || word.type().equals(NodeType.FLOAT)) {
            wordp = new BooleanWord(Boolean.TRUE);
        }

        return wordp;
    }

    public Node listp(Scope scope, java.util.List<Node> args) {

        Node list = args.get(0);
        Node listp = new BooleanWord(Boolean.FALSE);

        if (list.type().equals(NodeType.LIST)) {
            listp = new BooleanWord(Boolean.TRUE);
        }

        return listp;
    }

    public Node equalp(Scope scope, java.util.List<Node> args) {

        Node fst = args.get(0);
        Node snd = args.get(1);
        Node equalp = new BooleanWord(fst.equals(snd));

        return equalp;
    }

    public Node memberp(Scope scope, java.util.List<Node> args) {

        Node member = args.get(0);
        Node list = args.get(1);

        Node result = Word.bool(false);

        if (!list.type().equals(NodeType.LIST)) {
            throw new NodeTypeException(list, list.type(), NodeType.LIST);
        }

        for (Node lm : list.getChildren()) {
            if (lm.hashCode() == member.hashCode()) {
                java.util.List<Node> subArgs = new ArrayList<>();
                subArgs.add(member);
                subArgs.add(lm);
                result = equalp(scope, subArgs);
            }
        }

        return result.toBooleanWord();
    }

    // Queries
    public Node count(Scope scope, java.util.List<Node> args) {

        Node element = args.get(0);

        if (element.type().equals(NodeType.LIST)) {
            return Node.integer(element.getChildren().size());
        }

        if (element.type().equals(NodeType.QUOTE)) {
            return Node.integer(element.toQuotedWord().getQuote().length());
        }

        if (element.type().equals(NodeType.SYMBOL)) {
            return Node.integer(element.toSymbolWord().getSymbol().length());
        }

        return Node.integer(0);
    }

    public Node lowercase(Scope scope, java.util.List<Node> args) {

        Node element = args.get(0);

        if (element.type().equals(NodeType.LIST)) {
            List result = new List();
            for (Node lm : element.getChildren()) {
                java.util.List<Node> subArgs = new ArrayList<>();
                subArgs.add(lm);
                result.addChild(lowercase(scope, subArgs));
            }
            return result;
        }

        if (element.type().equals(NodeType.QUOTE)) {
            return Node.quote(element.toQuotedWord().getQuote().toLowerCase());
        }

        if (element.type().equals(NodeType.SYMBOL)) {
            return Node.quote(element.toSymbolWord().getSymbol().toLowerCase());
        }

        return element;
    }

    public Node uppercase(Scope scope, java.util.List<Node> args) {

        Node element = args.get(0);

        if (element.type().equals(NodeType.LIST)) {
            List result = new List();
            for (Node lm : element.getChildren()) {
                java.util.List<Node> subArgs = new ArrayList<>();
                subArgs.add(lm);
                result.addChild(uppercase(scope, subArgs));
            }
            return result;
        }

        if (element.type().equals(NodeType.QUOTE)) {
            return Node.quote(element.toQuotedWord().getQuote().toUpperCase());
        }

        if (element.type().equals(NodeType.SYMBOL)) {
            return Node.quote(element.toSymbolWord().getSymbol().toUpperCase());
        }

        return element;
    }

    @Override
    public Interpreter registerProcedures(Interpreter it) {
        this.it = it;

        it.env().define(new Procedure("uppercase", (scope, val) -> this.uppercase(scope, val), "element"));
        it.env().define(new Procedure("lowercase", (scope, val) -> this.lowercase(scope, val), "element"));
        it.env().define(new Procedure("count", (scope, val) -> this.count(scope, val), "element"));
        it.env().define(new Procedure("equal?", (scope, val) -> this.equalp(scope, val), "fst", "snd"));
        it.env().define(new Procedure("member?", (scope, val) -> this.memberp(scope, val), "fst", "snd"));
        it.env().define(new Procedure("list?", (scope, val) -> this.listp(scope, val), "list"));
        it.env().define(new Procedure("number?", (scope, val) -> this.numberp(scope, val), "number"));
        it.env().define(new Procedure("word?", (scope, val) -> this.wordp(scope, val), "word"));
        it.env().define(new Procedure("empty?", (scope, val) -> this.emptyp(scope, val), "list"));
        it.env().define(new Procedure("setitem", (scope, val) -> this.setitem(scope, val), "index", "list", "newval"));
        it.env().define(new Procedure("item", (scope, val) -> this.item(scope, val), "index", "listorword"));
        it.env().define(new Procedure("butlast", (scope, val) -> this.butlast(scope, val), "listorword"));
        it.env().define(new Procedure("butfirst", (scope, val) -> this.butfirst(scope, val), "listorword"));
        it.env().define(new Procedure("last", (scope, val) -> this.last(scope, val), "listorword"));
        it.env().define(new Procedure("first", (scope, val) -> this.first(scope, val), "listorword"));
        it.env().define(new Procedure("reverse", (scope, val) -> this.reverse(scope, val), "list"));
        it.env().define(new Procedure("fput", (scope, val) -> this.fput(scope, val), "fst", "snd"));
        it.env().define(new Procedure("lput", (scope, val) -> this.lput(scope, val), "fst", "snd"));
        it.env().define(new Procedure("word", (scope, val) -> this.word(scope, val), "fst", "snd"));
        it.env().define(new Procedure("list", (scope, val) -> this.list(scope, val), "fst", "snd"));
        it.env().define(new Procedure("sentence", (scope, val) -> this.sentence(scope, val), "fst", "snd"));
        it.env().define(new Procedure("gensym", (scope, val) -> this.gensym(scope, val)));

        return it;
    }
}
