package analyseurdecode.model;

import java.util.HashSet;
import java.util.Set;

/*
  Représente un module/composant/service identifié dans l'application.
  Un module est un ensemble de classes fortement couplées.
 */
public class Module {
    private Set<String> classNames;
    private double couplingScore;
    private String name;

    public Module(Set<String> classNames) {
        this.classNames = new HashSet<>(classNames);
        this.couplingScore = 0.0;
        this.name = generateDefaultName();
    }

    public Module(Set<String> classNames, double couplingScore) {
        this.classNames = new HashSet<>(classNames);
        this.couplingScore = couplingScore;
        this.name = generateDefaultName();
    }

    private String generateDefaultName() {
        if (classNames.isEmpty()) {
            return "Module_Empty";
        }
        if (classNames.size() == 1) {
            return "Module_" + classNames.iterator().next();
        }
        return "Module_" + classNames.size() + "_classes";
    }

    public Set<String> getClassNames() {
        return new HashSet<>(classNames);
    }

    public double getCouplingScore() {
        return couplingScore;
    }

    public void setCouplingScore(double couplingScore) {
        this.couplingScore = couplingScore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return classNames.size();
    }

    public boolean contains(String className) {
        return classNames.contains(className);
    }

    public boolean hasOverlapWith(Module other) {
        for (String className : classNames) {
            if (other.contains(className)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name + " {classes=" + classNames.size() + ", coupling=" +
                String.format("%.4f", couplingScore) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return classNames.equals(module.classNames);
    }

    @Override
    public int hashCode() {
        return classNames.hashCode();
    }
}