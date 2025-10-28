package analyseurdecode.model;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class MethodInfo {
    private String name;
    private int loc;
    private int parameters;
    private String fullyQualifiedName;
    private Set<String> calledMethodsNames = new HashSet<>();
    private List<String> parameterTypes = new ArrayList<>();

    public MethodInfo(String name, int loc, int parameters) {
        this.name = name;
        this.loc = loc;
        this.parameters = parameters;
    }
    public MethodInfo(String name, int loc, int parameters, List<String> parameterTypes) {
        this.name = name;
        this.loc = loc;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    public String getName() { return name; }
    public int getLoc() { return loc; }
    public int getParameters() { return parameters; }
    
    public String getFullyQualifiedName() { return fullyQualifiedName; }
    public void setFullyQualifiedName(String fullyQualifiedName) { this.fullyQualifiedName = fullyQualifiedName; }
    
    public Set<String> getCalledMethodsNames() { return calledMethodsNames; }
    public void addCalledMethodName(String calledMethodName) { this.calledMethodsNames.add(calledMethodName); }
    public List<String> getParameterTypes() { return parameterTypes; }

    @Override
    public String toString() {
        return name + " (LOC=" + loc + ", params=" + parameters + ")";
    }
}