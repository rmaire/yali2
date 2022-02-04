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

import ch.uprisesoft.yali.scope.Environment;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class List extends Node {

    public List() {
        super(NodeType.LIST);
    }
    
    public List(java.util.List<Node> nodes) {
        super(NodeType.LIST);
        super.addChildren(nodes);
    }
    
    @Override
    public Node evaluate(Environment env){
        return this;
    }

    public void reverse() {
        Collections.reverse(children);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(
                children
                        .stream()
                        .map(e -> e.toString())
                        .collect(Collectors.joining(" ")
                        ));
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        
        if (obj == null || !(obj instanceof List)) {
            return false;
        }
        
        final List other = (List) obj;
        
        if(this.children.size() != other.children.size()) {
            return false;
        }

        if (this.hashCode() == other.hashCode()) {
            return true;
        }

        return false;
    }
}
