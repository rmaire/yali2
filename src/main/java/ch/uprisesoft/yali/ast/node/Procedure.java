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
import ch.uprisesoft.yali.runtime.procedures.FunctionType;
import ch.uprisesoft.yali.scope.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Procedure extends Node {

    private String name;
    private java.util.List<String> args = new ArrayList<>();
    private BiFunction<Scope, java.util.List<Node>, Node> nativeCall;
    private String source;

    FunctionType funType = FunctionType.YALI;

    public Procedure() {
        super(NodeType.PROCEDURE);
    }

    public Procedure(String name) {
        this();
        this.name = name;
    }
    
    public Procedure(String name, BiFunction<Scope, java.util.List<Node>, Node> nativeCall, String... args) {
        this();
        this.funType = FunctionType.NATIVE;
        this.name = name;
        this.nativeCall = nativeCall;
        for(String arg: args) {
            this.args.add(arg);
        }
    }

    public Procedure setNativeCall(BiFunction<Scope, java.util.List<Node>, Node> nativeCall) {
        this.funType = FunctionType.NATIVE;
        this.nativeCall = nativeCall;
        return this;
    }
    
    public Procedure macro() {
        this.funType = FunctionType.MACRO;
        return this;
    }

    public BiFunction<Scope, java.util.List<Node>, Node> getNativeCall() {
        return nativeCall;
    }

    public Boolean isNative() {
        return funType == FunctionType.NATIVE;
    }
    
    public Boolean isMacro() {
        return funType == FunctionType.MACRO;
    }
    
    public Boolean isUser() {
        return funType == FunctionType.YALI;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public int getArity() {
        return args.size();
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public void accept(Evaluator evaluator) {
        evaluator.evaluate(this.toProcedureDef());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("define \"").append(name).append(" [[");

        sb.append(String.join(" ", args));
        sb.append("][");
        for (Node n : children) {
            sb.append(n.toString()).append(" ");
        }
        sb.append("]]");

        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        for (Node n : children) {
            hash = 31 * hash + (n == null ? 0 : n.hashCode());
        }

        for (String arg : args) {
            hash = 31 * hash + arg.hashCode();
        }

//        if (nativeCall != null) {
//            hash = 31 * hash + nativeCall.hashCode();
//        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Procedure)) {
            return false;
        }

        final Procedure other = (Procedure) obj;

        if (this.hashCode() == other.hashCode()) {
            return true;
        }

        return true;
    }
    
    public String header() {
        StringBuffer header = new StringBuffer();
        
        header.append(this.name);
        for(String arg: args) {
            header.append(" :" + arg);
        }
        
        return header.toString();
    }

}
