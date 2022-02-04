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
import ch.uprisesoft.yali.ast.node.word.FloatWord;
import ch.uprisesoft.yali.ast.node.word.IntegerWord;
import ch.uprisesoft.yali.ast.node.word.Word;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.scope.Scope;
import java.util.ArrayList;
import java.util.List;
import ch.uprisesoft.yali.runtime.procedures.ProcedureProvider;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Arithmetic implements ProcedureProvider {

    private boolean checkArgs(Node node) {
        if (!(node.type().equals(NodeType.FLOAT) || node.type().equals(NodeType.INTEGER))) {
            return false;
        }

        return true;
    }

    public Node add(Scope scope, java.util.List<Node> args) {
        Word left = (Word) args.get(0);
        Word right = (Word) args.get(1);

        if (!checkArgs(left)) {
            throw new NodeTypeException(Node.symbol("+"), left.type(), NodeType.NUMBER);
        }
        if (!checkArgs(right)) {
            throw new NodeTypeException(Node.symbol("+"), right.type(), NodeType.NUMBER);
        }

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            return new IntegerWord(left.getInteger() + right.getInteger());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getInteger() + right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            return new FloatWord(left.getFloat() + right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getFloat() + right.getFloat());
        }

        return Node.none();
    }

    public Node sub(Scope scope, java.util.List<Node> args) {
        Word left = (Word) args.get(0);
        Word right = (Word) args.get(1);

        if (!checkArgs(left)) {
            throw new NodeTypeException(Node.symbol("-"), left.type(), NodeType.NUMBER);
        }
        if (!checkArgs(right)) {
            throw new NodeTypeException(Node.symbol("-"), right.type(), NodeType.NUMBER);
        }

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            return new IntegerWord(left.getInteger() - right.getInteger());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getInteger() - right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            return new FloatWord(left.getFloat() - right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getFloat() - right.getFloat());
        }
        return Node.none();
    }

    public Node mul(Scope scope, java.util.List<Node> args) {
        Word left = (Word) args.get(0);
        Word right = (Word) args.get(1);

        if (!checkArgs(left)) {
            throw new NodeTypeException(Node.symbol("*"), left.type(), NodeType.NUMBER);
        }
        if (!checkArgs(right)) {
            throw new NodeTypeException(Node.symbol("*"), right.type(), NodeType.NUMBER);
        }

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            return new IntegerWord(left.getInteger() * right.getInteger());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getInteger() * right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            return new FloatWord(left.getFloat() * right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getFloat() * right.getFloat());
        }
        return Node.none();
    }

    public Node div(Scope scope, java.util.List<Node> args) {
        Word left = (Word) args.get(0);
        Word right = (Word) args.get(1);

        if (!checkArgs(left)) {
            throw new NodeTypeException(Node.symbol("/"), left.type(), NodeType.NUMBER);
        }
        if (!checkArgs(right)) {
            throw new NodeTypeException(Node.symbol("/"), right.type(), NodeType.NUMBER);
        }

        if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.INTEGER)) {
            return new IntegerWord(left.getInteger() / right.getInteger());
        } else if (left.type().equals(NodeType.INTEGER) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getInteger() / right.getFloat());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.INTEGER)) {
            return new FloatWord(left.getFloat() / right.getInteger());
        } else if (left.type().equals(NodeType.FLOAT) && right.type().equals(NodeType.FLOAT)) {
            return new FloatWord(left.getFloat() / right.getFloat());
        }
        return Node.none();
    }
    
    public Node mod(Scope scope, java.util.List<Node> args) {
        Word left = (Word) args.get(0);
        Word right = (Word) args.get(1);

        if (!left.type().equals(NodeType.INTEGER)) {
            throw new NodeTypeException(left, left.type(), NodeType.INTEGER);
        }
        if (!right.type().equals(NodeType.INTEGER)) {
            throw new NodeTypeException(right, right.type(), NodeType.NUMBER);
        }

        return new IntegerWord(left.getInteger() % right.getInteger());
    }
    
    public Node integer(Scope scope, java.util.List<Node> args) {
        Word arg = (Word) args.get(0);

        if (arg.type().equals(NodeType.INTEGER)) {
            return arg;
        } else if (arg.type().equals(NodeType.INTEGER)) {
            return Node.integer(arg.toFloatWord().getFloat().intValue());
        }
        else {
            throw new NodeTypeException(arg, arg.type(), NodeType.FLOAT, NodeType.INTEGER);
        }
    }
    
    public Node round(Scope scope, java.util.List<Node> args) {
        Word arg = (Word) args.get(0);

        if (arg.type().equals(NodeType.INTEGER)) {
            return arg;
        } else if (arg.type().equals(NodeType.INTEGER)) {
            return Node.integer((int)Math.round(arg.toFloatWord().getFloat()));
        }
        else {
            throw new NodeTypeException(arg, arg.type(), NodeType.FLOAT, NodeType.INTEGER);
        }
    }

    @Override
    public Interpreter registerProcedures(Interpreter it) {
        Arithmetic builtins = new Arithmetic();

        it.env().define(new Procedure("add", (scope, val) -> builtins.add(scope, val), "fst", "snd"));
        it.env().define(new Procedure("mul", (scope, val) -> builtins.mul(scope, val), "fst", "snd"));
        it.env().define(new Procedure("sub", (scope, val) -> builtins.sub(scope, val), "fst", "snd"));
        it.env().define(new Procedure("div", (scope, val) -> builtins.div(scope, val), "fst", "snd"));
        it.env().define(new Procedure("mod", (scope, val) -> builtins.mod(scope, val), "fst", "snd"));
        it.env().define(new Procedure("integer", (scope, val) -> builtins.integer(scope, val), "val"));
        it.env().define(new Procedure("round", (scope, val) -> builtins.round(scope, val), "val"));

        return it;
    }
}
