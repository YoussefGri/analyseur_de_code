package analyseurdecode.spoon.extractor;

import analyseurdecode.model.*;
import spoon.reflect.declaration.*;
import java.util.*;

public class MethodExtractor {

    private final Map<String, MethodInfo> allMethodsMap;

    public MethodExtractor(Map<String, MethodInfo> allMethodsMap) {
        this.allMethodsMap = allMethodsMap;
    }

    public MethodInfo extractMethodInfo(CtMethod<?> method, ClassInfo classInfo) {
        try {
            String methodName = method.getSimpleName();
            int loc = countLines(method);

            List<String> paramTypes = new ArrayList<>();
            for (CtParameter<?> param : method.getParameters())
                paramTypes.add(param.getType().getSimpleName());

            MethodInfo methodInfo = new MethodInfo(methodName, loc, paramTypes.size(), paramTypes);

            String fqName = buildQualifiedName(classInfo, methodName);
            methodInfo.setFullyQualifiedName(fqName);

            allMethodsMap.put(fqName, methodInfo);
            return methodInfo;

        } catch (Exception e) {
            return null;
        }
    }

    private String buildQualifiedName(ClassInfo classInfo, String methodName) {
        String pkg = classInfo.getPackageName();
        return (pkg != null && !pkg.isEmpty() ? pkg + "." : "") +
                classInfo.getName() + "." + methodName;
    }

    private int countLines(CtElement element) {
        if (element.getPosition() != null && element.getPosition().isValidPosition()) {
            return element.getPosition().getEndLine() - element.getPosition().getLine() + 1;
        }
        return 1;
    }
}
