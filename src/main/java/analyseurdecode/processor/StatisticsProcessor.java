package analyseurdecode.processor;

import analyseurdecode.model.ClassInfo;

import java.util.List;

public class StatisticsProcessor {
    public void computeStatistics(List<ClassInfo> classes, int X) {
        StatisticsService service = new StatisticsService();
        StatisticsResult result = service.compute(classes, X);
        System.out.println("Nombre de classes : " + result.totalClasses);
        System.out.println("Nombre de lignes de code : " + result.totalLoc);
        System.out.println("Nombre total de méthodes : " + result.totalMethods);
        System.out.println("Nombre total de packages : " + result.totalPackages);
        System.out.println("Nombre moyen de méthodes par classe : " + result.avgMethodsPerClass);
        System.out.println("Nombre moyen de lignes par méthode : " + result.avgLocPerMethod);
        System.out.println("Nombre moyen d'attributs par classe : " + result.avgAttributesPerClass);
        System.out.println("Max params dans toutes les méthodes : " + result.maxParams);
        System.out.println("Top classes par méthodes : " + result.topMethodsClasses);
        System.out.println("Top classes par attributs : " + result.topAttributesClasses);
        System.out.println("Classes dans les deux catégories : " + result.bothCategories);
        System.out.println("Classes avec plus de " + X + " méthodes : " + result.classesMoreThanX);
    }

    public void printCouplingMetric(List<ClassInfo> classes, String classA, String classB) {
        StatisticsService service = new StatisticsService();
        double coupling = service.computeCouplingMetric(classes, classA, classB);
        System.out.println("Couplage entre " + classA + " et " + classB + " : " + coupling);
    }
}