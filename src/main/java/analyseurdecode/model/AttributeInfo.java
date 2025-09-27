package analyseurdecode.model;

public class AttributeInfo {
    private String name;

    public AttributeInfo(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }
}
