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
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.word.BooleanWord;
import ch.uprisesoft.yali.ast.node.word.Word;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.scope.Scope;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import ch.uprisesoft.yali.runtime.procedures.ProcedureProvider;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Logic implements ProcedureProvider {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logic.class);

    private Interpreter it;

    public Node equal(Scope scope, java.util.List<Node> args) {
        Word left = (Word) it.eval(args.get(0));
        Word right = (Word) it.eval(args.get(1));

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            return Word.bool(left.getInteger().equals(right.getInteger()));
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            return Word.bool(left.getFloat().equals(right.getFloat()));
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            return Word.bool(Double.valueOf(left.getInteger().toString()).equals(right.getFloat()));
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            return Word.bool(left.getFloat().equals(Double.valueOf(right.getInteger().toString())));
        } else if (left.type().equals(NodeType.QUOTE) && right.type().equals(NodeType.QUOTE)) {
            return Word.bool(left.getQuote().equals(right.getQuote()));
        } else if (left.type().equals(NodeType.SYMBOL) && right.type().equals(NodeType.SYMBOL)) {
            return Word.bool(left.getSymbol().equals(right.getSymbol()));
        } else if (left.type().equals(NodeType.BOOLEAN) && right.type().equals(NodeType.BOOLEAN)) {
            return Word.bool(left.getBoolean().equals(right.getBoolean()));
        }

        return Word.bool(Boolean.FALSE);
    }

    public Node inequal(Scope scope, java.util.List<Node> args) {
        Node result = equal(scope, args);
        if (result.toBooleanWord().getBoolean()) {
            return new BooleanWord(Boolean.FALSE);
        } else {
            return new BooleanWord(Boolean.TRUE);
        }
    }

    public Node greater(Scope scope, java.util.List<Node> args) {
        Word left = (Word) it.eval(args.get(0));
        Word right = (Word) it.eval(args.get(1));
        
        if(!(left.type().equals(NodeType.FLOAT) || left.type().equals(NodeType.INTEGER))) {
            throw new NodeTypeException(Node.symbol("greater?"), left.type(), NodeType.NUMBER);
        }
        
        if(!(right.type().equals(NodeType.FLOAT) || right.type().equals(NodeType.INTEGER))) {
            throw new NodeTypeException(Node.symbol("greater?"), right.type(), NodeType.NUMBER);
        }

        Word result = Node.bool(Boolean.FALSE);

        logger.debug("(Logic) Greater-Expression. Left: " + left.toString() + "/" + left.type() + ", Right: " + right.toString() + "/" + right.type());

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            result = Word.bool(left.getInteger() > right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            logger.debug("(Logic) Evaluating Float : " + left.getFloat() + " is less than Float " + right.getFloat());
            result = Word.bool(left.getFloat() > right.getFloat());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            result = Word.bool(left.getInteger() > right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            result = Word.bool(left.getFloat() > right.getInteger());
        }

        logger.debug("(Logic) Expression is greater: " + result.getBoolean());
        return result;
    }

    public Node less(Scope scope, java.util.List<Node> args) {
        Word left = (Word) it.eval(args.get(0));
        Word right = (Word) it.eval(args.get(1));

        Word result = Node.bool(Boolean.FALSE);

        logger.debug("(Logic) Less-Expression. Left: " + left.toString() + "/" + left.type() + ", Right: " + right.toString() + "/" + right.type());

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            result = Word.bool(left.getInteger() < right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            logger.debug("(Logic) Evaluating Float : " + left.getFloat() + " is less than Float " + right.getFloat());
            result = Word.bool(left.getFloat() < right.getFloat());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            result = Word.bool(left.getInteger() < right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            result = Word.bool(left.getFloat() < right.getInteger());
        }

        logger.debug("(Logic) Expression is less: " + result.getBoolean());
        return result;
    }

    public Node greaterorequal(Scope scope, java.util.List<Node> args) {
        Word left = (Word) it.eval(args.get(0));
        Word right = (Word) it.eval(args.get(1));

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            return Word.bool(left.getInteger() >= right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            return Word.bool(left.getFloat() >= right.getFloat());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            return Word.bool(left.getInteger() >= right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            return Word.bool(left.getFloat() >= right.getInteger());
        }

        return Word.bool(Boolean.FALSE);
    }

    public Node lessorequal(Scope scope, java.util.List<Node> args) {
        Word left = (Word) it.eval(args.get(0));
        Word right = (Word) it.eval(args.get(1));

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            return Word.bool(left.getInteger() <= right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            return Word.bool(left.getFloat() <= right.getFloat());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            return Word.bool(left.getInteger() <= right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            return Word.bool(left.getFloat() <= right.getInteger());
        }

        return Word.bool(Boolean.FALSE);
    }

    @Override
    public Interpreter registerProcedures(Interpreter it) {
        this.it = it;

        it.env().define(new Procedure("equal?", (scope, val) -> this.equal(scope, val), "fst", "snd"));
        it.env().define(new Procedure("notequal?", (scope, val) -> this.inequal(scope, val), "fst", "snd"));
        it.env().define(new Procedure("less?", (scope, val) -> this.less(scope, val), "fst", "snd"));
        it.env().define(new Procedure("greater?", (scope, val) -> this.greater(scope, val), "fst", "snd"));
        it.env().define(new Procedure("greaterequal?", (scope, val) -> this.greaterorequal(scope, val), "fst", "snd"));
        it.env().define(new Procedure("lessequal?", (scope, val) -> this.lessorequal(scope, val), "fst", "snd"));

        return it;
    }
}
