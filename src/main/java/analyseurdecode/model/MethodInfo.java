package analyseurdecode.model;

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
