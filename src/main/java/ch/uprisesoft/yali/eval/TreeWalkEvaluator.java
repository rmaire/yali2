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
package ch.uprisesoft.yali.eval;

import ch.uprisesoft.yali.ast.node.ProcedureCall;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.List;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.word.BooleanWord;
import ch.uprisesoft.yali.ast.node.word.FloatWord;
import ch.uprisesoft.yali.ast.node.word.IntegerWord;
import ch.uprisesoft.yali.ast.node.word.NilWord;
import ch.uprisesoft.yali.ast.node.word.NoWord;
import ch.uprisesoft.yali.ast.node.word.QuotedWord;
import ch.uprisesoft.yali.ast.node.word.ReferenceWord;
import ch.uprisesoft.yali.ast.node.word.SymbolWord;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.procedures.FunctionNotFoundException;
import ch.uprisesoft.yali.scope.Scope;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class TreeWalkEvaluator implements Evaluator {

    private static final Logger logger = LoggerFactory.getLogger(TreeWalkEvaluator.class);

//    private Scope scope;
    private Deque<Scope> scopes = new ArrayDeque<>();
    private final Interpreter functions;
    protected Node result = Node.none();

    public TreeWalkEvaluator(Scope scope, Interpreter functions) {
//        this.scope = scope;
        scopes.push(scope);
        this.functions = functions;
    }

//    public void evaluate(java.util.List<Node> calls) {
//        for (Node expression : calls) {
//            ProcedureCall fc = expression.toProcedureCall();
//            evaluate(fc);
//        }
//    }
//
//    public void evaluate(java.util.List<Node> calls, Scope scope) {
////        Scope oldScope = this.scope;
////        this.scope = scope;
//
//        scopes.push(scope);
//        
//        for (Node expression : calls) {
//            ProcedureCall fc = expression.toProcedureCall();
//            evaluate(fc);
//        }
//
//        scopes.pop();
//    }

    @Override
    public void evaluate(List subject) {
        for (Node c : subject.getChildren()) {
            logger.debug("(eval) List: " + c.toString() + "/" + c.type());
        }
        result = subject;
    }

    @Override
    public void evaluate(BooleanWord subject) {
        logger.debug("(eval) Boolean word: " + subject.toString());
        result = subject;
    }

    @Override
    public void evaluate(FloatWord subject) {
        logger.debug("(eval) Float word: " + subject.toString());
        result = subject;
    }

    @Override
    public void evaluate(IntegerWord subject) {
        logger.debug("(eval) Integer word: " + subject.toString());
        result = subject;
    }

    @Override
    public void evaluate(NilWord subject) {
        logger.debug("(eval) Nil word");
        result = subject;
    }

    @Override
    public void evaluate(NoWord subject) {
        logger.debug("(eval) No word");
        result = subject;
    }

    @Override
    public void evaluate(QuotedWord subject) {
        logger.debug("(eval) Quoted word: " + subject.toString());
        result = subject;
    }

    @Override
    public void evaluate(SymbolWord subject) {
        logger.debug("(eval) Symbol word: " + subject.toString());
        result = subject;
    }

    @Override
    public void evaluate(ReferenceWord subject) {
        logger.debug("(eval) Reference word: " + subject.toString());
        Node value = scopes.peek().resolve(subject.toReferenceWord().getReference());
        result = value;
    }

    @Override
    public void evaluate(ProcedureCall funCall) {

        logger.debug("(eval) Function Call " + funCall.getName());
        if (!functions.defined(funCall.getName())) {
            throw new FunctionNotFoundException(funCall.getName());
        }

        Procedure funDef = functions.getProcedures().get(funCall.getName());

        java.util.List<Node> args = new ArrayList<>();

        // TODO differentiate between procedures and macros (called with parent scope)
        Scope callScope = new Scope(funDef.getName());
        callScope.setEnclosingScope(scopes.peek());

        int i = 0;
        for (Node c : funCall.getChildren()) {
            c.accept(this);
            if (i < funDef.getArity()) {
                callScope.define(
                        funDef.getArgs().get(i),
                        result
                );
                i++;
            }
            args.add(result);
        }

        logger.debug("(eval) dispatching " + funCall.getName());
        result = functions.apply(funCall.getName(), callScope, args);
        logger.debug("(eval) Function Call " + funCall.getName() + " end");
    }

    public Node getResult() {
        return result;
    }

    @Override
    public void evaluate(Procedure subject) {
        logger.debug("(eval) Function Definition " + subject.getName());
    }
}
