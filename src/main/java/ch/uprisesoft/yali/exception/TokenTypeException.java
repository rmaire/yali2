/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.exception;

import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.lexer.TokenType;

/**
 *
 * @author rma
 */
public class TokenTypeException  extends RuntimeException {
    
    private TokenType expected;
    private TokenType received;
    
    public TokenTypeException(TokenType expected, TokenType received) {
        super("Expected token of type " + expected + ", but received node of type " + received);
        this.expected = expected;
        this.received = received;
    }

    public TokenType getExpected() {
        return expected;
    }

    public TokenType getReceived() {
        return received;
    }
    
    
}