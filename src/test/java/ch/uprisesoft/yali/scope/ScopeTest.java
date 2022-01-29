/*
 * Copyright 2021 rma.
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
package ch.uprisesoft.yali.scope;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.word.NilWord;
import ch.uprisesoft.yali.ast.node.word.SymbolWord;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rma
 */
public class ScopeTest {

    public ScopeTest() {
    }

    @Test
    public void testBasicScoping() {
        Scope rootScope = new Scope("root");
        rootScope.define("test", new NilWord());

        assertTrue(rootScope.resolve("test") != null);
        assertThat(rootScope.resolve("test").type(), is(NodeType.NIL));
    }
    
    @Test
    public void testScopingCase() {
        Scope rootScope = new Scope("root");
        rootScope.define("TEST", new NilWord());

        assertTrue(rootScope.resolve("test") != null);
        assertThat(rootScope.resolve("test").type(), is(NodeType.NIL));
    }

    @Test
    public void testScopePreservesValue() {
        Scope rootScope = new Scope("root");
        rootScope.define("test", new SymbolWord("testit"));

        assertThat(rootScope.resolve("test").type(), is(NodeType.SYMBOL));
        assertThat(rootScope.resolve("test").toSymbolWord().getSymbol(), is("testit"));

    }

    @Test
    public void testNestedScope() {
        Scope rootScope = new Scope("root");
        Scope childScope = new Scope("nested");
        childScope.setEnclosingScope(rootScope);
        
        childScope.local("test");
        childScope.define("test", new SymbolWord("testit"));

        assertThat(childScope.resolve("test").type(), is(NodeType.SYMBOL));
        assertThat(childScope.resolve("test").toSymbolWord().getSymbol(), is("testit"));

        assertThat(rootScope.defined("test"), is(false));
    }

    @Test
    public void testNestedResolve() {
        Scope rootScope = new Scope("root");
        Scope childScope = new Scope("nested");
        childScope.setEnclosingScope(rootScope);
        rootScope.define("test", new SymbolWord("testit"));

        assertThat(childScope.resolve("test").type(), is(NodeType.SYMBOL));
        assertThat(childScope.resolve("test").toSymbolWord().getSymbol(), is("testit"));

        assertThat(rootScope.defined("test"), is(true));
    }

    @Test
    public void testNestedShadowing() {
        Scope rootScope = new Scope("root");
        Scope childScope = new Scope("nested");
        childScope.setEnclosingScope(rootScope);
        rootScope.define("test", new SymbolWord("test"));
        childScope.local("test");
        childScope.define("test", new SymbolWord("test 2"));
        
        assertThat(rootScope.resolve("test").type(), is(NodeType.SYMBOL));
        assertThat(rootScope.resolve("test").toSymbolWord().getSymbol(), is("test"));
        
        assertThat(childScope.resolve("test").type(), is(NodeType.SYMBOL));
        assertThat(childScope.resolve("test").toSymbolWord().getSymbol(), is("test 2"));
    }
}
