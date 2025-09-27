package analyseurdecode.processor;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import analyseurdecode.model.AttributeInfo;

import java.util.*;
import java.util.stream.Collectors;

public class StatisticsProcessor {

    public void computeStatistics(List<ClassInfo> classes, int X) {
        int totalClasses = classes.size();
        int totalMethods = classes.stream().mapToInt(c -> c.getMethods().size()).sum();
        int totalLoc = classes.stream().mapToInt(ClassInfo::getLoc).sum();
        int totalAttributes = classes.stream().mapToInt(c -> c.getAttributes().size()).sum();
        int totalPackages = (int) classes.stream().map(ClassInfo::getPackageName).distinct().count();

        double avgMethodsPerClass = totalClasses > 0 ? (double) totalMethods / totalClasses : 0;
        double avgLocPerMethod = totalMethods > 0 ?
            (double) classes.stream().flatMap(c -> c.getMethods().stream()).mapToInt(MethodInfo::getLoc).sum() / totalMethods
            : 0;
        double avgAttributesPerClass = totalClasses > 0 ? (double) totalAttributes / totalClasses : 0;

        int maxParams = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .mapToInt(MethodInfo::getParameters)
            .max().orElse(0);

        int topCount = Math.max(1, totalClasses / 10);

        List<ClassInfo> topMethodsClasses = new ArrayList<>(classes);
        topMethodsClasses.sort((a, b) -> b.getMethods().size() - a.getMethods().size());
        topMethodsClasses = topMethodsClasses.subList(0, topCount);

        List<ClassInfo> topAttributesClasses = new ArrayList<>(classes);
        topAttributesClasses.sort((a, b) -> b.getAttributes().size() - a.getAttributes().size());
        topAttributesClasses = topAttributesClasses.subList(0, topCount);

        List<ClassInfo> bothCategories = new ArrayList<>(topMethodsClasses);
        bothCategories.retainAll(topAttributesClasses);

        List<ClassInfo> classesMoreThanX = classes.stream()
            .filter(c -> c.getMethods().size() > X)
            .collect(Collectors.toList());

        System.out.println("Nombre de classes : " + totalClasses);
        System.out.println("Nombre de lignes de code : " + totalLoc);
        System.out.println("Nombre total de méthodes : " + totalMethods);
        System.out.println("Nombre total de packages : " + totalPackages);
        System.out.println("Nombre moyen de méthodes par classe : " + avgMethodsPerClass);
        System.out.println("Nombre moyen de lignes par méthode : " + avgLocPerMethod);
        System.out.println("Nombre moyen d'attributs par classe : " + avgAttributesPerClass);
        System.out.println("Max params dans toutes les méthodes : " + maxParams);
        System.out.println("Top classes par méthodes : " + topMethodsClasses);
        System.out.println("Top classes par attributs : " + topAttributesClasses);
        System.out.println("Classes dans les deux catégories : " + bothCategories);
        System.out.println("Classes avec plus de " + X + " méthodes : " + classesMoreThanX);
    }
}