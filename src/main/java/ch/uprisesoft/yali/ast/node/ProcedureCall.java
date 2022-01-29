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

import ch.uprisesoft.yali.eval.Evaluator;
import java.util.stream.Collectors;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class ProcedureCall extends Node {

    private final String name;
    private final int arity;

    public ProcedureCall(String name, int arity) {
        super(NodeType.PROCCALL);
        this.name = name;
        this.arity = arity;
    }

    public ProcedureCall(String name) {
        super(NodeType.PROCCALL);
        this.name = name;
        this.arity = -1;
    }

    public String getName() {
        return name;
    }

    public int getArity() {
        return arity;
    }

    public Boolean isExtraCall() {
        return arity < 0;
    }

    @Override
    public void accept(Evaluator evaluator) {
        evaluator.evaluate(this.toProcedureCall());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("(").append(name).append("");

        sb.append(
                children
                        .stream()
                        .map(e -> e.toString())
                        .collect(Collectors.joining(" ")
                        ));

        sb.append(")");

        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        hash = 31 * hash + arity;
        for (Node n : children) {
            hash = 31 * hash + (n == null ? 0 : n.hashCode());
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ProcedureCall)) {
            return false;
        }

        final ProcedureCall other = (ProcedureCall) obj;

        if (this.hashCode() == other.hashCode()) {
            return true;
        }

        return true;
    }

}
