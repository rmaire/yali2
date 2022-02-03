/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.runtime.interpreter;

import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import ch.uprisesoft.yali.scope.Scope;

public class InterpreterBuilder {

    private Scope variables;
    private boolean stdLib = false;
    private OutputObserver oo;
    private InputGenerator ig;

    public InterpreterBuilder() {
    }

    public InterpreterBuilder withVariables(Scope scope) {
        this.variables = scope;
        return this;
    }

    public InterpreterBuilder withStdLib(OutputObserver oo) {
        this.oo = oo;
        return this;
    }

    public InterpreterBuilder withStdLib(OutputObserver oo, InputGenerator ig) {
        this.oo = oo;
        this.ig = ig;
        return this;
    }

    public Interpreter build() {
        if (variables == null) {
            variables = new Scope("global");
        }

        Interpreter it = new Interpreter();

        if (oo != null && ig != null) {
            it.loadStdLib(oo, ig);
        } else if (oo != null && ig == null) {
            it.loadStdLib();
        }

        return it;
    }
}
