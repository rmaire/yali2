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
package ch.uprisesoft.yali.exception;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class NodeTypeException extends RuntimeException {
    
    private java.util.List<NodeType> expected = new ArrayList<>();
    private NodeType received;
    
    private Node node;
    
    public NodeTypeException(Node node, NodeType received, NodeType... expected) {
        super("Expected node of type " + Stream.of(expected).map(nodetype -> nodetype.toString()).collect(Collectors.joining(", ")) + ", but received node of type " + received);
        for(NodeType e: expected) {
            this.expected.add(e);
        }
        
        this.received = received;
        this.node = node;
    }

    public java.util.List<NodeType> getExpected() {
        return expected;
    }

    public NodeType getReceived() {
        return received;
    }

    public Node getNode() {
        return node;
    }
}
