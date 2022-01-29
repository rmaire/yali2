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
package ch.uprisesoft.yali.parser;

import ch.uprisesoft.yali.ast.node.ProcedureCall;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.List;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.word.BooleanWord;
import ch.uprisesoft.yali.ast.node.word.FloatWord;
import ch.uprisesoft.yali.ast.node.word.NilWord;
import ch.uprisesoft.yali.ast.node.word.QuotedWord;
import ch.uprisesoft.yali.ast.node.word.ReferenceWord;
import ch.uprisesoft.yali.ast.node.word.SymbolWord;
import ch.uprisesoft.yali.lexer.Token;
import ch.uprisesoft.yali.lexer.TokenType;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.word.IntegerWord;
import ch.uprisesoft.yali.exception.TokenTypeException;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import org.ainslec.picocog.PicoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reader {

    private static final Logger logger = LoggerFactory.getLogger(Reader.class);
    private PicoWriter pw = new PicoWriter();

    private java.util.List<Token> tokens;
    private final Interpreter functions;

    private int current = 0;
    private boolean inParens = false;

//    public Reader() {
//        this(new ProcedureDispatcher());
//    }

    public Reader(Interpreter functions) {
        this.functions = functions;
    }

    public Interpreter getFunctions() {
        return functions;
    }

    public Node read(java.util.List<Token> tokens) {
        logger.debug("Parsing start");
        this.tokens = tokens;

        parseFunctionHeaders();
        parseFunctionAliases();

        Node program = new List();
        while (!isAtEnd()) {
            Node expression = parseExpression();
            logger.debug(pw.toString());
            pw = new PicoWriter();
            if (!expression.type().equals(NodeType.NONE) && !expression.type().equals(NodeType.PROCEDURE)) {
                program.addChild(expression);
            }

        }
        logger.debug("Parsing end");
        return program;
    }

    private void parseFunctionHeaders() {
        logger.debug("Header parsing start");
        while (!isAtEnd()) {
            if (match(TokenType.TO)) {
                pw.writeln("\nFunction Header start");
                pw.indentRight();

                Token defStartToken = previous();
                Procedure fun = new Procedure(consume(TokenType.SYMBOL).getLexeme());
                java.util.List<String> args = new java.util.ArrayList<>();
                while (!check(TokenType.NEWLINE)) {
                    args.add(consume(TokenType.REFERENCE).getLexeme().substring(1));
                }
                fun.setArgs(args);
                advance();

                fun.setPosInSource(defStartToken.getLine(), defStartToken.getPos());
                functions.define(fun);

                pw.indentRight();
                pw.writeln(fun.header());
                pw.indentLeft();

                pw.indentLeft();
                pw.writeln("\nFunction Header end");
            }
            advance();
        }
        current = 0;
        logger.debug("Header parsing end");
    }
    
    private void parseFunctionAliases() {
    logger.debug("Alias parsing start");
        while (!isAtEnd()) {
            if (match(TokenType.SYMBOL) && previous().getLexeme().toLowerCase().equals("alias")) {
                pw.writeln("\nAlias start");
                pw.indentRight();
                
                String original = consume(TokenType.QUOTE).getLexeme().substring(1);
                String alias = consume(TokenType.QUOTE).getLexeme().substring(1);
                
                functions.alias(original, alias);
                
                advance();

                pw.indentRight();
                pw.writeln("Alias " + original + " -> " + alias);
                pw.indentLeft();

                pw.indentLeft();
                pw.writeln("Alias end");
            }
            advance();
        }
        current = 0;
        logger.debug("Alias parsing end");
    }

    private Node parseExpression() {
        logger.debug("Expression parsing start");
        pw.writeln("\nExpression start");
        pw.indentRight();

        Node node = expression();

        pw.indentLeft();
        pw.writeln("Expression end");
        logger.debug("Expression parsing end");

        return node;
    }

    private Node expression() {
        if (match(TokenType.NEWLINE)) {
            return Node.none();
        }
        return funBody();
    }

    private Node funBody() {
        Node node = Node.none();

        if (match(TokenType.TO)) {

            Procedure fun = functions.getFunctions().get(consume(TokenType.SYMBOL).getLexeme());
            pw.indentRight();
            logger.debug("Fundef Body parsing start");
            pw.writeln("Fundef Body start: " + fun.getName());

            while (!check(TokenType.NEWLINE)) {
                advance();
            }
            advance();

            while (!check(TokenType.END)) {
                fun.addChild(expression());
                
                // Check for unclosed function body
                if(peek().type().equals(TokenType.EOF)) {
                    throw new TokenTypeException(TokenType.END, TokenType.EOF);
                }
                
                advance();
            }
            consume(TokenType.END);

            fun.setSource(previous().getLexeme());

            match(TokenType.NEWLINE);

            functions.define(fun);

            pw.indentLeft();
            pw.writeln("Fundef Body end: " + fun.getName());
            logger.debug("Fundef Body parsing end");

            node = fun;
        } else {
            node = funCall();
        }

        return node;
    }

    private Node funCall() {
        Node node = Node.none();

        if (current().type().equals(TokenType.SYMBOL) && functions.getArities().containsKey(current().getLexeme().toLowerCase())) {

            pw.writeln("Funcall start: " + current().getLexeme());
            pw.indentRight();

            String name = current().getLexeme();
            int arity = functions.getArities().get(name);
            advance();

            node = new ProcedureCall(name, arity);
            node.setPosInSource(current().getLine(), current().getPos());

            if (inParens) {
                while (!check(TokenType.RIGHT_PAREN)) {
                    node.addChild(expression());
                }
            } else {
                for (int i = 0; i < arity; i++) {
                    node.addChild(expression());
                }
            }

            pw.indentLeft();
            pw.writeln("Funcall end: " + name);

        } else {
            node = equality();
        }

        return node;
    }

    private Node equality() {
        Node node = comparison();

        if (match(TokenType.EQUAL, TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = comparison();

            if (operator.equals(TokenType.EQUAL) || operator.equals(TokenType.EQUAL_EQUAL)) {
                node = new ProcedureCall("equal?", 2);
            }

            if (operator.equals(TokenType.BANG_EQUAL)) {
                node = new ProcedureCall("notequal?", 2);
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());
        }

        return node;
    }

    private Node comparison() {
        Node node = term();

        while (match(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = term();

            pw.writeln("Logic Expression start: " + operator);
            pw.indentRight();

            if (operator.equals(TokenType.LESS)) {
                node = new ProcedureCall("less?", 2);
            }

            if (operator.equals(TokenType.GREATER)) {
                node = new ProcedureCall("greater?", 2);
            }

            if (operator.equals(TokenType.LESS_EQUAL)) {
                node = new ProcedureCall("lessequal?", 2);
            }

            if (operator.equals(TokenType.GREATER_EQUAL)) {
                node = new ProcedureCall("greaterequal?", 2);
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());

            pw.indentLeft();
            pw.writeln("Logic Expression end: " + operator);
        }

        return node;
    }

    private Node term() {
        Node node = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = factor();

            if (operator.equals(TokenType.PLUS)) {
                node = new ProcedureCall("add", 2);
            }

            if (operator.equals(TokenType.MINUS)) {
                node = new ProcedureCall("sub", 2);
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());
        }

        return node;
    }

    private Node factor() {
        Node node = word();

        while (match(TokenType.STAR, TokenType.SLASH)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = word();

            if (operator.equals(TokenType.STAR)) {
                node = new ProcedureCall("mul", 2);
            }

            if (operator.equals(TokenType.SLASH)) {
                node = new ProcedureCall("div", 2);
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());
        }

        return node;
    }

    private Node word() {
        Node node = Node.none();

        pw.writeln("Word start");
        pw.indentRight();

        if (match(TokenType.FALSE)) {
            pw.indentRight();
            pw.writeln("False");
            pw.indentLeft();
            node = new BooleanWord(Boolean.FALSE);
            node.setToken(previous());
        } else if (match(TokenType.TRUE)) {
            pw.indentRight();
            pw.writeln("True");
            pw.indentLeft();
            node = new BooleanWord(Boolean.TRUE);
            node.setToken(previous());
        } else if (match(TokenType.NIL)) {
            pw.indentRight();
            pw.writeln("Nil");
            pw.indentLeft();
            node = new NilWord();
            node.setToken(previous());
        } else if (match(TokenType.NUMBER)) {
            Token token = previous();

            if (token.getLexeme().contains(".")) {
                node = new FloatWord(Double.parseDouble(token.getLexeme()));
            } else {
                node = new IntegerWord(Integer.parseInt(token.getLexeme()));
            }

            pw.indentRight();
            pw.writeln("Number: " + node.toString() + " " + token.getLexeme());
            pw.indentLeft();

            node.setToken(previous());
        } else if (match(TokenType.SYMBOL)) {
            node = new SymbolWord(previous().getLexeme());
            pw.indentRight();
            pw.writeln("Symbol: " + node.toString() + " " + previous().toString());
            pw.indentLeft();
            node.setToken(previous());
        } else if (match(TokenType.QUOTE)) {
            node = new QuotedWord(previous().getLexeme().substring(1));
            node.setToken(previous());
            pw.indentRight();
            pw.writeln("Quote: " + node.toString() + " " + previous().toString());
            pw.indentLeft();
        } else if (match(TokenType.REFERENCE)) {
            node = new ReferenceWord(previous().getLexeme().substring(1));

            pw.indentRight();
            pw.writeln("Reference: " + node.toString() + " " + previous().toString());
            pw.indentLeft();

            node.setToken(previous());
        } else if (match(TokenType.LEFT_BRACKET)) {
            logger.debug("Start parsing top level List");
            node = parseList();
            logger.debug("End parsing top level List");
        } else if (match(TokenType.LEFT_PAREN)) {
            inParens = true;
            node = expression();
            node.setPosInSource(previous().getLine(), previous().getPos());
            consume(TokenType.RIGHT_PAREN);
            inParens = false;
        }

        pw.indentLeft();
        pw.writeln("Word end: " + node.toString());

        return node;
    }

    private List parseList() {
        pw.writeln("List start");
        pw.indentRight();
        ch.uprisesoft.yali.ast.node.List list = new ch.uprisesoft.yali.ast.node.List();
        list.setPosInSource(previous().getLine(), previous().getPos());

        while (!check(TokenType.RIGHT_BRACKET) && !isAtEnd()) {
            if (match(TokenType.LEFT_BRACKET)) {
                logger.debug("Start parsing nested List");
                List nestedList = parseList();
                logger.debug("List: " + nestedList.toString());
                list.addChild(nestedList);
                logger.debug("End parsing nested List");
                break;
            }

            pw.writeln("Symbol: " + current().getLexeme());
            list.addChild(new SymbolWord(current().getLexeme()));
            advance();
        }
        consume(TokenType.RIGHT_BRACKET);

        pw.writeln("List: " + list.toString());
        pw.indentLeft();
        pw.writeln("List end");

        return list;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token current() {
        return tokens.get(current);
    }

    private Token consume(TokenType type) throws TokenTypeException {
        if (check(type)) {
            return advance();
        }
        throw new TokenTypeException(type, peek().type());
    }
}
