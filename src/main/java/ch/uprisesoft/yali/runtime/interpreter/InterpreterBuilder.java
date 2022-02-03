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

    private OutputObserver oo;
    private InputGenerator ig;

    public InterpreterBuilder() {
    }

    public InterpreterBuilder withStdLib(OutputObserver oo, InputGenerator ig) {
        this.oo = oo;
        this.ig = ig;
        return this;
    }

    public Interpreter build() {
        Interpreter it = new Interpreter();

        if (oo != null && ig != null) {
            it.loadStdLib(oo, ig);
        } else if (oo != null && ig == null) {
            it.loadStdLib();
        }

        return it;
    }
}
