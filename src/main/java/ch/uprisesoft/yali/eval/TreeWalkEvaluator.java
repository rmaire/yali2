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

import ch.uprisesoft.yali.ast.node.Call;
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
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class TreeWalkEvaluator implements Evaluator {

    private static final Logger logger = LoggerFactory.getLogger(TreeWalkEvaluator.class);

    private final Interpreter it;
    protected Node result = Node.none();

    public TreeWalkEvaluator(Interpreter it) {
        this.it = it;
    }

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
        Node value = it.resolve(subject.toReferenceWord().getReference());
        result = value;
    }

    @Override
    public void evaluate(Call call) {

        logger.debug("(eval) Function Call " + call.name());
        if (!it.defined(call.name())) {
            throw new FunctionNotFoundException(call.name());
        }

        Procedure funDef = it.getProcedures().get(call.name());

        java.util.List<Node> args = new ArrayList<>();

        // TODO differentiate between procedures and macros (called with parent scope)

        
        int i = 0;
        for (Node c : call.getChildren()) {
            c.accept(this);
            if (i < funDef.getArity()) {
                it.make(
                        funDef.getArgs().get(i),
                        result
                );
                i++;
            }
            args.add(result);
        }
        
        call.args(args);

        logger.debug("(eval) dispatching " + call.name());
        result = it.apply(call);
        logger.debug("(eval) Function Call " + call.name() + " end");
        
    }

    public Node getResult() {
        return result;
    }

    @Override
    public void evaluate(Procedure subject) {
        logger.debug("(eval) Function Definition " + subject.getName());
    }
}
