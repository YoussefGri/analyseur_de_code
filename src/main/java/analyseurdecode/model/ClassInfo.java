package analyseurdecode.model;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private String name;
    private String packageName;
    private List<MethodInfo> methods = new ArrayList<>();
    private List<AttributeInfo> attributes = new ArrayList<>();
    private int loc;

    public void setName(String name) { this.name = name; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setLoc(int loc) { this.loc = loc; }

    public void addMethod(MethodInfo m) { methods.add(m); }
    public void addAttribute(AttributeInfo a) { attributes.add(a); }

    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public List<MethodInfo> getMethods() { return methods; }
    public List<AttributeInfo> getAttributes() { return attributes; }
    public int getLoc() { return loc; }

    @Override
    public String toString() {
        return name + " (Méthodes=" + methods.size() + ", Attributs=" + attributes.size() + ")";
    }
}
