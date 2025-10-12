/*package analyseurdecode.model;

public class MethodInfo {
    private String name;
    private int loc;
    private int parameters;

    public MethodInfo(String name, int loc, int parameters) {
        this.name = name;
        this.loc = loc;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public int getLoc() { return loc; }
    public int getParameters() { return parameters; }

    @Override
    public String toString() {
        return name + " (LOC=" + loc + ", params=" + parameters + ")";
    }
}
*/

package analyseurdecode.model;

import java.util.HashSet;
import java.util.Set;

public class MethodInfo {
    private String name;
    private int loc;
    private int parameters;
    private String fullyQualifiedName;
    private Set<String> calledMethodsNames = new HashSet<>();

    public MethodInfo(String name, int loc, int parameters) {
        this.name = name;
        this.loc = loc;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public int getLoc() { return loc; }
    public int getParameters() { return parameters; }
    
    public String getFullyQualifiedName() { return fullyQualifiedName; }
    public void setFullyQualifiedName(String fullyQualifiedName) { this.fullyQualifiedName = fullyQualifiedName; }
    
    public Set<String> getCalledMethodsNames() { return calledMethodsNames; }
    public void addCalledMethodName(String calledMethodName) { this.calledMethodsNames.add(calledMethodName); }

    @Override
    public String toString() {
        return name + " (LOC=" + loc + ", params=" + parameters + ")";
    }
}