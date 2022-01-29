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
package ch.uprisesoft.yali.ast.node.word;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;

public abstract class Word extends Node {

    protected Double floatWord;
    protected Integer integerWord;
    protected String stringWord;
    protected String symbolWord;
    protected String quoteWord;
    protected String referenceWord;
    protected Boolean booleanWord;
    protected String nilWord = "nil";

    public Word(NodeType type) {
        super(type);
    }

    public NodeType getType() {
        return type;
    }

    public String getString() {
        return stringWord;
    }
    
    public String getQuote() {
        return quoteWord;
    }

    public Boolean getBoolean() {
        return booleanWord;
    }

    public Double getFloat() {
        return floatWord;
    }

    public Integer getInteger() {
        return integerWord;
    }

    public String getNil() {
        return nilWord;
    }
    
    public String getSymbol() {
        return symbolWord;
    }
    
    public String getReference() {
        return referenceWord;
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (floatWord == null ? 0 : floatWord.hashCode());
        hash = 31 * hash + (integerWord == null ? 0 : integerWord.hashCode());
        hash = 31 * hash + (quoteWord == null ? 0 : quoteWord.hashCode());
        hash = 31 * hash + (stringWord == null ? 0 : stringWord.hashCode());
        hash = 31 * hash + (booleanWord == null ? 0 : booleanWord.hashCode());
        hash = 31 * hash + (referenceWord == null ? 0 : referenceWord.hashCode());
        hash = 31 * hash + (symbolWord == null ? 0 : symbolWord.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        final Word other = (Word) obj;
        
        if(this.type.equals(NodeType.NIL) || this.type.equals(NodeType.NONE) || other.type.equals(NodeType.NIL) || other.type.equals(NodeType.NONE)) {
            return false;
        }
        
        if (this.type.equals(NodeType.INTEGER) && other.type.equals(NodeType.INTEGER)) {
            return this.integerWord.equals(other.integerWord);
        }
        
        if (this.type.equals(NodeType.NAME) && other.type.equals(NodeType.NAME)) {
            return this.stringWord.equals(other.stringWord);
        }
        
        if (this.type.equals(NodeType.FLOAT) && other.type.equals(NodeType.FLOAT)) {
            return this.floatWord.equals(other.floatWord);
        }
        
        if (this.type.equals(NodeType.FLOAT) && other.type.equals(NodeType.INTEGER)) {
            return this.floatWord.equals(Double.valueOf(other.integerWord));
        }

        if (this.type.equals(NodeType.INTEGER) && other.type.equals(NodeType.FLOAT)) {
            return Double.valueOf(this.integerWord).equals(other.floatWord);
        }
        
        if (this.type.equals(NodeType.SYMBOL) && other.type.equals(NodeType.QUOTE)) {
            return this.getString().equals(other.getQuote());
        }
        
        if (this.type.equals(NodeType.QUOTE) && other.type.equals(NodeType.SYMBOL)) {
            return this.getQuote().equals(other.getString());
        }
        
        if (this.type.equals(NodeType.QUOTE) && other.type.equals(NodeType.QUOTE)) {
            return this.getQuote().equals(other.getQuote());
        }
        
        if (this.type.equals(NodeType.SYMBOL) && other.type.equals(NodeType.SYMBOL)) {
            return this.getString().equals(other.getString());
        }
        
        if (((this.type.equals(NodeType.QUOTE) || this.type.equals(NodeType.SYMBOL)) && (other.type.equals(NodeType.INTEGER) || other.type.equals(NodeType.FLOAT)))
                || ((this.type.equals(NodeType.INTEGER) || this.type.equals(NodeType.FLOAT)) && (other.type.equals(NodeType.QUOTE) || other.type.equals(NodeType.SYMBOL)))) {
            return this.toString().equals(other.toString());
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        return true;
    }
}
