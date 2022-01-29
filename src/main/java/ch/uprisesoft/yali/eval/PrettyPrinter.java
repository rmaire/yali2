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
import java.util.stream.Collectors;
import org.ainslec.picocog.PicoWriter;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class PrettyPrinter implements Evaluator {

    private PicoWriter pw = new PicoWriter();

    @Override
    public void evaluate(List subject) {
        pw.writeln("[");
        pw.indentRight();
        for (Node c : subject.getChildren()) {
            c.accept(this);
        }
        pw.indentLeft();
        pw.writeln("]");
    }

    @Override
    public void evaluate(BooleanWord subject) {
        pw.writeln(subject.toString());
    }

    @Override
    public void evaluate(FloatWord subject) {
        pw.writeln(subject.toString());
    }

    @Override
    public void evaluate(NilWord subject) {
        pw.writeln(subject.toString());
    }

    @Override
    public void evaluate(NoWord subject) {
        pw.writeln("none");
    }

    @Override
    public void evaluate(QuotedWord subject) {
        pw.writeln("\"" + subject.toString());
    }

    @Override
    public void evaluate(ProcedureCall subject) {
        pw.writeln(subject.getName() + "/" + subject.getArity() + " => ");
        pw.indentRight();
        for (Node c : subject.getChildren()) {
            c.accept(this);
        }
        pw.indentLeft();
    }

    @Override
    public void evaluate(Procedure subject) {
        pw.write("to " + subject.getName() + " ");
        pw.writeln(subject
                .getArgs()
                .stream()
                .map(s -> ":" + s)
                .collect(Collectors.joining(" "))
        );
        pw.indentRight();
        for (Node c : subject.getChildren()) {
            c.accept(this);
        }
//        if (subject.isNative()) {
//            pw.writeln("(native)");
//        } else {
//            for (Node c : subject.getChildren()) {
//                c.accept(this);
//            }
//        }
        pw.indentLeft();
        pw.writeln("end");
    }

    @Override
    public void evaluate(IntegerWord subject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void evaluate(ReferenceWord subject) {
        pw.writeln(":" + subject.toString());
    }

    @Override
    public void evaluate(SymbolWord subject) {
        pw.writeln(subject.toString());
    }

    public void evaluate(Node node) {
        switch (node.type()) {
            case LIST:
                evaluate(node.toList());
                break;
            case BOOLEAN:
                evaluate(node.toBooleanWord());
                break;
            case FLOAT:
                evaluate(node.toFloatWord());
                break;
            case PROCCALL:
                evaluate(node.toProcedureCall());
                break;
            case PROCEDURE:
                evaluate(node.toProcedureDef());
                break;
            case INTEGER:
                evaluate(node.toIntegerWord());
                break;
            case NIL:
                evaluate(node.toNilWord());
                break;
            case NONE:
                evaluate(node.toNoWord());
                break;
            case QUOTE:
                evaluate(node.toQuotedWord());
                break;
            case REFERENCE:
                evaluate(node.toReferenceWord());
                break;
            case SYMBOL:
                evaluate(node.toSymbolWord());
                break;
        }
    }

    public String build() {
        return pw.toString();
    }
}
