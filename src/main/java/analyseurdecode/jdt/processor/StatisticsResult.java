package analyseurdecode.jdt.processor;

import analyseurdecode.model.ClassInfo;
import java.util.List;

public class StatisticsResult {
    public int totalClasses;
    public int totalMethods;
    public int totalLoc;
    public int totalAttributes;
    public int totalPackages;
    public double avgMethodsPerClass;
    public double avgLocPerMethod;
    public double avgAttributesPerClass;
    public int maxParams;
    public List<ClassInfo> topMethodsClasses;
    public List<ClassInfo> topAttributesClasses;
    public List<ClassInfo> bothCategories;
    public List<ClassInfo> classesMoreThanX;
}
