/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.helper;

import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.parser.Reader;
import ch.uprisesoft.yali.runtime.functions.builtin.LogicTest;
import ch.uprisesoft.yali.runtime.procedures.builtin.MockTurtleManager;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.interpreter.InterpreterBuilder;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rmaire
 */
public class ObjectMother {
    
    private Interpreter i;
    private Reader p;
    private Lexer l;
   
    public ObjectMother(OutputObserver oo, InputGenerator ig) {
        this.i = new InterpreterBuilder().build();
        this.i.loadStdLib(oo, ig);
        
        MockTurtleManager mtm = new MockTurtleManager();
        mtm.registerProcedures(i);
        
        this.p = new Reader(i);
        this.l = new Lexer();
    }
    
    public ObjectMother() {
        
        OutputObserver oo = new OutputObserver() {
            private final Logger logger = LoggerFactory.getLogger(LogicTest.class);
            
            @Override
            public void inform(String output) {
                logger.debug(output);
            }
        };
        
        InputGenerator ig = new InputGenerator() {
            @Override
            public String request() {
                return "requestedinput";
            }

            @Override
            public String requestLine() {
                return "requestedinputline";
            }
        };
        
        this.i = new InterpreterBuilder().build();
        this.i.loadStdLib(oo, ig);
        
        MockTurtleManager mtm = new MockTurtleManager();
        mtm.registerProcedures(i);
        
        this.p = new Reader(i);
        this.l = new Lexer();
    }


    public Interpreter getInterpreter() {
        return i;
    }

    public Reader getParser() {
        return p;
    }

    public Lexer getLexer() {
        return l;
    }
}
