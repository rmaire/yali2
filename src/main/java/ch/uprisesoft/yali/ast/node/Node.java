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
package ch.uprisesoft.yali.ast.node;

import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.word.BooleanWord;
import ch.uprisesoft.yali.ast.node.word.FloatWord;
import ch.uprisesoft.yali.ast.node.word.IntegerWord;
import ch.uprisesoft.yali.ast.node.word.NilWord;
import ch.uprisesoft.yali.ast.node.word.NoWord;
import ch.uprisesoft.yali.ast.node.word.QuotedWord;
import ch.uprisesoft.yali.ast.node.word.ReferenceWord;
import ch.uprisesoft.yali.ast.node.word.SymbolWord;
import ch.uprisesoft.yali.eval.Evaluator;
import ch.uprisesoft.yali.lexer.Token;
import java.util.ArrayList;
//import org.ainslec.picocog.PicoWriter;

public abstract class Node {

    protected int line = 0;
    protected int col = 0;
    protected NodeType type;
    protected java.util.List<Node> children = new ArrayList<>();
    protected Token token;

    public Node(NodeType type) {
        this.type = type;
    }

    public void setPosInSource(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
        this.line = token.getLine();
        this.col = token.getPos();
    }

    public int getLine() {
        return token.getLine();
    }

    public int getCol() {
        return token.getPos();
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public void addChildren(java.util.List<Node> nodes) {
        children.addAll(nodes);
    }

    public java.util.List<Node> getChildren() {
        return children;
    }

    public NodeType type() {
        return type;
    }

    @Override
    public abstract String toString();

    public abstract void accept(Evaluator evaluator);

    public BooleanWord toBooleanWord() throws NodeTypeException {
        if (this.type() != NodeType.BOOLEAN) {
            throw new NodeTypeException(this, this.type(), NodeType.BOOLEAN);
        }
        return (BooleanWord) this;
    }

    public FloatWord toFloatWord(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.FLOAT) {
            throw new NodeTypeException(this, this.type(), NodeType.FLOAT);
        }
        return (FloatWord) this;
    }

    public IntegerWord toIntegerWord(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.INTEGER) {
            throw new NodeTypeException(this, this.type(), NodeType.INTEGER);
        }
        return (IntegerWord) this;
    }

    public NilWord toNilWord(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.NIL) {
            throw new NodeTypeException(this, this.type(), NodeType.NIL);
        }
        return (NilWord) this;
    }

    public QuotedWord toQuotedWord(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.QUOTE) {
            throw new NodeTypeException(this, this.type(), NodeType.QUOTE);
        }
        return (QuotedWord) this;
    }

    public SymbolWord toSymbolWord(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.SYMBOL) {
            throw new NodeTypeException(this, this.type(), NodeType.SYMBOL);
        }
        return (SymbolWord) this;
    }

    public ReferenceWord toReferenceWord(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.REFERENCE) {
            throw new NodeTypeException(this, this.type(), NodeType.REFERENCE);
        }
        return (ReferenceWord) this;
    }

    public Call toCall(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.PROCCALL) {
            throw new NodeTypeException(this, this.type(), NodeType.PROCCALL);
        }
        return (Call) this;
    }

    public Procedure toProcedureDef(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.PROCEDURE) {
            throw new NodeTypeException(this, this.type(), NodeType.PROCEDURE);
        }
        return (Procedure) this;
    }

    public List toList(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.LIST) {
            throw new NodeTypeException(this, this.type(), NodeType.LIST);
        }
        return (List) this;
    }

    public NoWord toNoWord(  Node this) throws NodeTypeException {
        if (this.type() != NodeType.NONE) {
            throw new NodeTypeException(this, this.type(), NodeType.NONE);
        }
        return (NoWord) this;
    }

    public static BooleanWord bool(Boolean bool) {
        return new BooleanWord(bool);
    }

    public static SymbolWord string(String str) {
        return new SymbolWord(str);
    }

    public static SymbolWord symbol(String str) {
        return new SymbolWord(str);
    }

    public static FloatWord flt(Double f) {
        return new FloatWord(f);
    }

    public static IntegerWord integer(Integer i) {
        return new IntegerWord(i);
    }

    public static NilWord nil() {
        return new NilWord();
    }

    public static NoWord none() {
        return new NoWord();
    }

    public static QuotedWord quote(String quote) {
        return new QuotedWord(quote);
    }

    public static ReferenceWord reference(String reference) {
        return new ReferenceWord(reference);
    }

    public static List list(java.util.List<Node> list) {
        List l = new List();
        l.addChildren(list);
        return l;
    }
}
