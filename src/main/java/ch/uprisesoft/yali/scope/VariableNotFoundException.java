package ch.uprisesoft.yali.scope;

/**
 *
 * @author rma
 */


public class VariableNotFoundException extends RuntimeException {

    private String name;
    
    public VariableNotFoundException(String name) {
        super("Could not find variable " + name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
