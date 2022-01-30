/*
 * Copyright 2020 rma.
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
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.word.QuotedWord;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.InputReceiver;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import ch.uprisesoft.yali.runtime.io.OutputSubject;
import ch.uprisesoft.yali.scope.Scope;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.uprisesoft.yali.runtime.procedures.ProcedureProvider;

/**
 *
 * @author rma
 */
public class IO implements ProcedureProvider, OutputSubject, InputReceiver {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IO.class);

    private java.util.List<OutputObserver> observers = new ArrayList<>();
    private InputGenerator generator;
    private Interpreter it;

    public Node print(Scope scope, java.util.List<Node> args) {
        java.util.List<Node> concreteArgs = new ArrayList<>();
        
        for(Node arg: args) {
            switch (arg.type()) {
                case SYMBOL:
                    throw new NodeTypeException(arg, NodeType.SYMBOL, NodeType.BOOLEAN, NodeType.EXPRESSION, NodeType.FLOAT, NodeType.PROCCALL, NodeType.INTEGER, NodeType.LIST, NodeType.NIL, NodeType.QUOTE, NodeType.REFERENCE);
                case PROCCALL:
                case REFERENCE:
                    concreteArgs.add(it.eval(arg));
                    break;
                default:
                    concreteArgs.add(arg);
                    break;
            }
        }
        
        java.util.List<String> stringifiedArgs = new ArrayList<>();
        stringifiedArgs.addAll(it.stringify(concreteArgs));

        inform(String.join(" ", stringifiedArgs) + "\n");

        logger.debug(">>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<");
        logger.debug("(Communication) print called with " + String.join(" ", stringifiedArgs));
        logger.debug(">>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<");

        return Node.nil();
    }

    public Node show(Scope scope, java.util.List<Node> args) {
        java.util.List<String> stringifiedArgs = new ArrayList<>();
        stringifiedArgs.addAll(it.stringify(args));

        inform(String.join(" ", stringifiedArgs) + "\n");
        return Node.nil();
    }

    public Node type(Scope scope, java.util.List<Node> args) {
        java.util.List<String> stringifiedArgs = new ArrayList<>();
        stringifiedArgs.addAll(it.stringify(args));

        inform(String.join(" ", stringifiedArgs));
        return Node.nil();
    }

//    private java.util.List<String> stringify(java.util.List<Node> args, Boolean escapeLists) {
//        java.util.List<String> stringifiedArgs = new ArrayList<>();
//        for (Node arg : args) {
//            if (arg.type().equals(NodeType.LIST)) {
//                if (escapeLists) {
//                    stringifiedArgs.add("[");
//                }
//                stringifiedArgs.addAll(stringify(arg.getChildren(), escapeLists));
//                if (escapeLists) {
//                    stringifiedArgs.add("]");
//                }
//            } else {
//                logger.debug("(Communication) stringify " + arg.toString());
//                stringifiedArgs.add(arg.toString());
//            }
//        }
//        return stringifiedArgs;
//    }

    public Node readword(Scope scope, java.util.List<Node> args) {
        QuotedWord result = new QuotedWord(requestLine());
        return result;
    }

    public Node readlist(Scope scope, java.util.List<Node> args) {
        logger.debug("(Communication) Start readlist");

        StringBuffer list = new StringBuffer();
        list.append("[");
        list.append(requestLine());
        list.append("]");

        logger.debug("(Communication) Read " + list.toString());
        List result = (List) it.read(list.toString());

        logger.debug("(Communication) End readlist");
        return result;
    }

    @Override
    public Interpreter registerProcedures(Interpreter it) {
        this.it = it;
        
        it.defineProc(new Procedure("readword", (scope, val) -> this.readword(scope, val)));
        it.defineProc(new Procedure("readlist", (scope, val) -> this.readlist(scope, val)));
        it.defineProc(new Procedure("show", (scope, val) -> this.show(scope, val), "output"));
        it.defineProc(new Procedure("type", (scope, val) -> this.type(scope, val), "output"));
        it.defineProc(new Procedure("print", (scope, val) -> this.print(scope, val), "output"));

        return it;
    }

    @Override
    public void register(InputGenerator generator) {
        logger.debug("(Communication) Got Generator");
        this.generator = generator;
    }

    @Override
    public void register(OutputObserver observer) {
        logger.debug("(Communication) Got Observer");
        observers.add(observer);
    }

    private void inform(String output) {
        for (OutputObserver oo : observers) {
            oo.inform(output);
        }
    }

    private String request() {
        return generator.request();
    }

    private String requestLine() {
        logger.debug("(Communication) Start requestLine");
        if (generator == null) {
            logger.debug("(Communication) Nog Generator registered");
        }
        String line = generator.requestLine();
        if (line == null) {
            logger.debug("(Communication) No line read");
        }
        logger.debug("(Communication) Line: " + line);
        logger.debug("(Communication) End requestLine");
        return line;
    }
}
