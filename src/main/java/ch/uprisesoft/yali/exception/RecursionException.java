/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.exception;

import ch.uprisesoft.yali.ast.node.Node;

/**
 *
 * @author rma
 */
public class RecursionException extends RuntimeException {
    
    private String function;
    private Node node;

    public RecursionException(String function, Node node) {
        super("Too many recursions in procedure " + function);
        this.function = function;
        this.node = node;
    }

    public String getFunction() {
        return function;
    }
    
    public Node getNode() {
        return node;
    }
    
}
