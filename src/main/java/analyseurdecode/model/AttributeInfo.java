package analyseurdecode.model;

public class AttributeInfo {
    private String name;
    private String type;

    public AttributeInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
