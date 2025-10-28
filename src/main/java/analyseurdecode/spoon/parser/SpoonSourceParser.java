package analyseurdecode.spoon.parser;

import analyseurdecode.spoon.launcher.SpoonLauncherConfig;
import analyseurdecode.spoon.builder.SpoonModelBuilder;
import analyseurdecode.spoon.extractor.ClassExtractor;
import analyseurdecode.spoon.analyzer.MethodCallAnalyzer;
import analyseurdecode.model.*;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;

import java.util.*;

public class SpoonSourceParser {

    private Map<String, MethodInfo> allMethodsMap = new HashMap<>();

    public List<ClassInfo> parseDirectory(String projectPath) {
        System.out.println("\n=== [1] Configuration du launcher ===");
        Launcher launcher = SpoonLauncherConfig.createConfiguredLauncher(projectPath);

        System.out.println("\n=== [2] Construction du modèle ===");
        CtModel model = SpoonModelBuilder.buildModel(launcher);

        System.out.println("\n=== [3] Extraction des entités ===");
        List<ClassInfo> classes = new ArrayList<>();
        ClassExtractor classExtractor = new ClassExtractor(allMethodsMap);

        for (CtType<?> type : model.getAllTypes()) {
            if (type instanceof CtClass || type instanceof CtInterface) {
                classes.add(classExtractor.extractClassInfo(type));
            }
        }

        System.out.println("\n=== [4] Analyse des appels de méthodes ===");
        MethodCallAnalyzer analyzer = new MethodCallAnalyzer(allMethodsMap);
        analyzer.analyze(model);

        System.out.printf("\n[Spoon] %d classes analysées, %d méthodes extraites\n",
                classes.size(), allMethodsMap.size());

        return classes;
    }

    public Map<String, MethodInfo> getAllMethodsMap() {
        return allMethodsMap;
    }
}
