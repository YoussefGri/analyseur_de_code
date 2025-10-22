package analyseurdecode.parser;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import analyseurdecode.model.AttributeInfo;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.*;

public class SpoonSourceParser {

    private Launcher launcher;
    private CtModel model;
    private Map<String, MethodInfo> allMethodsMap;

    public SpoonSourceParser() {
        this.launcher = new Launcher();
        this.allMethodsMap = new HashMap<>();
    }

    //Parse un dossier de sources Java et retourne la liste des classes

    public List<ClassInfo> parseDirectory(String projectPath) {
        List<ClassInfo> classes = new ArrayList<>();

        try {
            // Configuration du launcher Spoon
            launcher.addInputResource(projectPath);
            launcher.getEnvironment().setNoClasspath(true);
            launcher.getEnvironment().setComplianceLevel(11);
            launcher.getEnvironment().setCommentEnabled(false);
            launcher.buildModel();

            model = launcher.getModel();

            // Première passe : extraire toutes les classes et méthodes
            for (CtType<?> type : model.getAllTypes()) {
                if (type instanceof CtClass || type instanceof CtInterface) {
                    ClassInfo classInfo = extractClassInfo(type);
                    if (classInfo != null) {
                        classes.add(classInfo);
                    }
                }
            }

            // Deuxième passe : analyser les appels de méthodes
            analyzeMethodCalls();

            System.out.println("✓ Spoon: " + classes.size() + " classes analysées");
            System.out.println("✓ Spoon: " + allMethodsMap.size() + " méthodes extraites");

        } catch (Exception e) {
            System.err.println("Erreur lors du parsing avec Spoon: " + e.getMessage());
            e.printStackTrace();
        }

        return classes;
    }

    //Extrait les informations d'une classe

    private ClassInfo extractClassInfo(CtType<?> type) {
        try {
            ClassInfo classInfo = new ClassInfo();

            // Nom de la classe
            classInfo.setName(type.getSimpleName());

            // Package
            CtPackage pkg = type.getPackage();
            if (pkg != null) {
                classInfo.setPackageName(pkg.getQualifiedName());
            }

            // Lignes de code (approximation)
            int loc = countLines(type);
            classInfo.setLoc(loc);

            // Extraire les méthodes
            Set<CtMethod<?>> methods = type.getMethods();
            for (CtMethod<?> method : methods) {
                MethodInfo methodInfo = extractMethodInfo(method, classInfo);
                if (methodInfo != null) {
                    classInfo.addMethod(methodInfo);
                }
            }

            // Extraire les attributs
            List<CtField<?>> fields = type.getFields();
            for (CtField<?> field : fields) {
                AttributeInfo attrInfo = extractAttributeInfo(field);
                if (attrInfo != null) {
                    classInfo.addAttribute(attrInfo);
                }
            }

            return classInfo;

        } catch (Exception e) {
            System.err.println("Erreur extraction classe " + type.getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    // Extrait les informations d'une méthode

    private MethodInfo extractMethodInfo(CtMethod<?> method, ClassInfo classInfo) {
        try {
            String methodName = method.getSimpleName();

            // Lignes de code de la méthode
            int methodLoc = countLines(method);

            // Nombre de paramètres
            int paramCount = method.getParameters().size();

            // Types des paramètres
            List<String> paramTypes = new ArrayList<>();
            for (CtParameter<?> param : method.getParameters()) {
                paramTypes.add(param.getType().getSimpleName());
            }

            MethodInfo methodInfo = new MethodInfo(methodName, methodLoc, paramCount, paramTypes);

            // Nom qualifié complet
            String packageName = classInfo.getPackageName();
            String className = classInfo.getName();
            String fullQName = (packageName != null && !packageName.isEmpty() ? packageName + "." : "")
                    + className + "." + methodName;

            methodInfo.setFullyQualifiedName(fullQName);

            // Stocker dans la map globale
            allMethodsMap.put(fullQName, methodInfo);

            return methodInfo;

        } catch (Exception e) {
            System.err.println("Erreur extraction méthode: " + e.getMessage());
            return null;
        }
    }

    //Extrait les informations d'un attribut

    private AttributeInfo extractAttributeInfo(CtField<?> field) {
        try {
            String fieldName = field.getSimpleName();
            String fieldType = field.getType().getSimpleName();
            return new AttributeInfo(fieldName, fieldType);
        } catch (Exception e) {
            return null;
        }
    }

    //Analyse tous les appels de méthodes dans le projet

    private void analyzeMethodCalls() {
        for (CtType<?> type : model.getAllTypes()) {
            // Parcourir toutes les méthodes de la classe
            Set<CtMethod<?>> methods = type.getMethods();
            for (CtMethod<?> method : methods) {
                analyzeMethodCallsInMethod(method);
            }
        }
    }

    //Analyse les appels dans une méthode spécifique

    private void analyzeMethodCallsInMethod(CtMethod<?> method) {
        try {
            // Construire le nom qualifié de la méthode appelante
            CtType<?> declaringType = method.getDeclaringType();
            if (declaringType == null) return;

            String packageName = "";
            CtPackage pkg = declaringType.getPackage();
            if (pkg != null) {
                packageName = pkg.getQualifiedName();
            }

            String callerQName = (packageName.isEmpty() ? "" : packageName + ".")
                    + declaringType.getSimpleName() + "." + method.getSimpleName();

            MethodInfo callerMethod = allMethodsMap.get(callerQName);
            if (callerMethod == null) return;

            // Trouver tous les appels de méthodes
            List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));

            for (CtInvocation<?> invocation : invocations) {
                CtExecutableReference<?> execRef = invocation.getExecutable();
                if (execRef == null) continue;

                // Récupérer le type déclarant de la méthode appelée
                CtTypeReference<?> declaringTypeRef = execRef.getDeclaringType();
                if (declaringTypeRef == null) continue;

                String calledClassName = declaringTypeRef.getSimpleName();
                String calledMethodName = execRef.getSimpleName();

                // Essayer de construire le nom qualifié
                String calledQName = findMethodQualifiedName(calledClassName, calledMethodName);

                if (calledQName != null) {
                    callerMethod.addCalledMethodName(calledQName);
                }
            }

        } catch (Exception e) {
            // Ignorer les erreurs pour continuer l'analyse
        }
    }

    //Trouve le nom qualifié d'une méthode

    private String findMethodQualifiedName(String className, String methodName) {
        // Chercher dans la map des méthodes
        for (String qName : allMethodsMap.keySet()) {
            if (qName.contains("." + className + "." + methodName)) {
                return qName;
            }
        }

        // Si non trouvé, retourner un nom externe
        return className + "." + methodName;
    }

    // Compte les lignes de code (approximation)

    private int countLines(CtElement element) {
        try {
            if (element.getPosition() != null && element.getPosition().isValidPosition()) {
                return element.getPosition().getEndLine() - element.getPosition().getLine() + 1;
            }
        } catch (Exception e) {
            // Position non disponible
        }
        return 1; // Valeur par défaut
    }

    // Retourne la map de toutes les méthodes

    public Map<String, MethodInfo> getAllMethodsMap() {
        return allMethodsMap;
    }

    //Retourne le modèle Spoon

    public CtModel getModel() {
        return model;
    }
}