/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.scope;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.Procedure;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rma
 */

public class Environment {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    private List<Scope> scopeStack = new ArrayList<>();

    {
        scopeStack.add(new Scope("global"));
    }

    public void push(Scope scope) {
        scopeStack.add(scope);
    }

    public Scope peek() {
        return scopeStack.get(scopeStack.size() - 1);
    }

    public Scope pop() {
        Scope ret = scopeStack.get(scopeStack.size() - 1);
        scopeStack.remove(scopeStack.size() - 1);
        return ret;
    }

    public void make(String name, Node value) {

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).defined(name)) {
                logger.debug("(Scope) defining variable " + name + " in scope " + scopeStack.get(i).getScopeName());
                scopeStack.get(i).define(name.toLowerCase(), value);
                return;
            }
        }

        logger.debug("(Scope) defining variable " + name + " in scope " + scopeStack.get(0).getScopeName());
        scopeStack.get(0).define(name.toLowerCase(), value);
    }

    public void local(String name) {
        logger.debug("(Scope) Reserve local variable " + name + " in scope " + peek().getScopeName());
        peek().local(name.toLowerCase());
    }
    
    public Node resolve(String name) {

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).defined(name)) {
                return scopeStack.get(i).resolve(name);
            }
        }

        return Node.none();
    }

    public Boolean resolveable(String name) {

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).defined(name)) {
                return true;
            }
        }

        return false;
    }
}
