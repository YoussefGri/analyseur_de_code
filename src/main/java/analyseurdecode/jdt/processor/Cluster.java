package analyseurdecode.jdt.processor;

import java.util.HashSet;
import java.util.Set;

public class Cluster {
    private Set<String> classNames = new HashSet<>();

    public Cluster(String className) {
        classNames.add(className);
    }
    public Cluster(Set<String> classNames) {
        this.classNames.addAll(classNames);
    }
    public Set<String> getClassNames() {
        return classNames;
    }
    public void merge(Cluster other) {
        this.classNames.addAll(other.classNames);
    }
    @Override
    public String toString() {
        return classNames.toString();
    }
}

