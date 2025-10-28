package analyseurdecode.spoon.analyzer;

import analyseurdecode.model.MethodInfo;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.reflect.code.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class MethodCallAnalyzer {

    private final Map<String, MethodInfo> allMethodsMap;

    public MethodCallAnalyzer(Map<String, MethodInfo> allMethodsMap) {
        this.allMethodsMap = allMethodsMap;
    }

    public void analyze(CtModel model) {
        for (CtType<?> type : model.getAllTypes()) {
            for (CtMethod<?> method : type.getMethods()) {
                analyzeMethodCallsInMethod(method);
            }
        }
    }

    private void analyzeMethodCallsInMethod(CtMethod<?> method) {
        CtType<?> declaringType = method.getDeclaringType();
        if (declaringType == null) return;

        String callerQName = getQualifiedName(declaringType, method);
        MethodInfo callerMethod = allMethodsMap.get(callerQName);
        if (callerMethod == null) return;

        List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> inv : invocations) {
            CtExecutableReference<?> execRef = inv.getExecutable();
            if (execRef == null || execRef.getDeclaringType() == null) continue;

            String calledClass = execRef.getDeclaringType().getSimpleName();
            String calledMethod = execRef.getSimpleName();

            String calledQName = findMethodQualifiedName(calledClass, calledMethod);
            if (calledQName != null) callerMethod.addCalledMethodName(calledQName);
        }
    }

    private String getQualifiedName(CtType<?> type, CtMethod<?> method) {
        String pkg = (type.getPackage() != null) ? type.getPackage().getQualifiedName() : "";
        return (pkg.isEmpty() ? "" : pkg + ".") + type.getSimpleName() + "." + method.getSimpleName();
    }

    private String findMethodQualifiedName(String className, String methodName) {
        return allMethodsMap.keySet().stream()
                .filter(qName -> qName.endsWith("." + className + "." + methodName))
                .findFirst()
                .orElse(className + "." + methodName);
    }
}
