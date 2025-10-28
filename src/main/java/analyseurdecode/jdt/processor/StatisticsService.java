package analyseurdecode.jdt.processor;

import analyseurdecode.model.AttributeInfo;
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

    public double computeCouplingMetric(List<ClassInfo> classes, String classA, String classB) {
        int abRelationships = 0;
        int totalRelationships = 0;
        java.util.Map<String, String> methodToClass = new java.util.HashMap<>();
        java.util.Set<String> classNames = new java.util.HashSet<>();
        for (ClassInfo ci : classes) {
            classNames.add(ci.getName());
            for (MethodInfo mi : ci.getMethods()) {
                methodToClass.put(ci.getName() + "." + mi.getName(), ci.getName());
            }
        }
        // Couplage via les attributs
        for (ClassInfo ci : classes) {
            String sourceClass = ci.getName();
            for (AttributeInfo attr : ci.getAttributes()) {
                String targetClass = attr.getType();
                if (classNames.contains(targetClass) && !sourceClass.equals(targetClass)) {
                    totalRelationships++;
                    if ((sourceClass.equals(classA) && targetClass.equals(classB)) ||
                        (sourceClass.equals(classB) && targetClass.equals(classA))) {
                        abRelationships++;
                    }
                }
            }
        }
        // Couplage via les appels de méthodes
        for (ClassInfo ci : classes) {
            for (MethodInfo mi : ci.getMethods()) {
                String sourceClass = ci.getName();
                for (String called : mi.getCalledMethodsNames()) {
                    String targetClass = null;
                    if (called.startsWith("COUPLING:")) {
                        targetClass = called.substring("COUPLING:".length());
                        if (!classNames.contains(targetClass)) continue;
                    } else {
                        targetClass = methodToClass.getOrDefault(called, null);
                    }
                    if (targetClass != null && !sourceClass.equals(targetClass)) {
                        totalRelationships++;
                        if ((sourceClass.equals(classA) && targetClass.equals(classB)) ||
                            (sourceClass.equals(classB) && targetClass.equals(classA))) {
                            abRelationships++;
                        }
                    }
                }
            }
        }
        // Couplage via les paramètres de méthodes
        for (ClassInfo ci : classes) {
            String sourceClass = ci.getName();
            for (MethodInfo mi : ci.getMethods()) {
                for (String paramType : mi.getParameterTypes()) {
                    // Type direct
                    if (classNames.contains(paramType) && !sourceClass.equals(paramType)) {
                        totalRelationships++;
                        if ((sourceClass.equals(classA) && paramType.equals(classB)) ||
                            (sourceClass.equals(classB) && paramType.equals(classA))) {
                            abRelationships++;
                        }
                    }
                    // Type générique
                    if (paramType.contains("<") && paramType.contains(">")) {
                        String genericType = paramType.substring(paramType.indexOf('<')+1, paramType.indexOf('>')).trim();
                        if (classNames.contains(genericType) && !sourceClass.equals(genericType)) {
                            totalRelationships++;
                            if ((sourceClass.equals(classA) && genericType.equals(classB)) ||
                                (sourceClass.equals(classB) && genericType.equals(classA))) {
                                abRelationships++;
                            }
                        }
                    }
                }
            }
        }
        if (totalRelationships == 0) return 0.0;
        double ratio = (double) abRelationships / totalRelationships;
        return Math.round(ratio * 1000.0) / 1000.0;
    }
}
