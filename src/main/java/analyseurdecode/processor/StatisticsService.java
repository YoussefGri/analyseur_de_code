package analyseurdecode.processor;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsService {
    public StatisticsResult compute(List<ClassInfo> classes, int X) {
        StatisticsResult result = new StatisticsResult();
        result.totalClasses = classes.size();
        result.totalMethods = classes.stream().mapToInt(c -> c.getMethods().size()).sum();
        result.totalLoc = classes.stream().mapToInt(ClassInfo::getLoc).sum();
        result.totalAttributes = classes.stream().mapToInt(c -> c.getAttributes().size()).sum();
        result.totalPackages = (int) classes.stream().map(ClassInfo::getPackageName).distinct().count();
        result.avgMethodsPerClass = result.totalClasses > 0 ? (double) result.totalMethods / result.totalClasses : 0;
        result.avgLocPerMethod = result.totalMethods > 0 ?
            (double) classes.stream().flatMap(c -> c.getMethods().stream()).mapToInt(MethodInfo::getLoc).sum() / result.totalMethods
            : 0;
        result.avgAttributesPerClass = result.totalClasses > 0 ? (double) result.totalAttributes / result.totalClasses : 0;
        result.maxParams = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .mapToInt(MethodInfo::getParameters)
            .max().orElse(0);
        int topCount = Math.max(1, result.totalClasses / 10);
        result.topMethodsClasses = new ArrayList<>(classes);
        result.topMethodsClasses.sort((a, b) -> b.getMethods().size() - a.getMethods().size());
        result.topMethodsClasses = result.topMethodsClasses.subList(0, topCount);
        result.topAttributesClasses = new ArrayList<>(classes);
        result.topAttributesClasses.sort((a, b) -> b.getAttributes().size() - a.getAttributes().size());
        result.topAttributesClasses = result.topAttributesClasses.subList(0, topCount);
        result.bothCategories = new ArrayList<>(result.topMethodsClasses);
        result.bothCategories.retainAll(result.topAttributesClasses);
        result.classesMoreThanX = classes.stream()
            .filter(c -> c.getMethods().size() > X)
            .collect(Collectors.toList());
        return result;
    }
}
