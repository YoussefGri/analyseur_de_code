package analyseurdecode.spoon.extractor;


import analyseurdecode.model.*;
import spoon.reflect.declaration.*;
import java.util.*;

public class ClassExtractor {

    private final MethodExtractor methodExtractor;
    private final AttributeExtractor attributeExtractor;

    public ClassExtractor(Map<String, MethodInfo> allMethodsMap) {
        this.methodExtractor = new MethodExtractor(allMethodsMap);
        this.attributeExtractor = new AttributeExtractor();
    }

    public ClassInfo extractClassInfo(CtType<?> type) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(type.getSimpleName());

        CtPackage pkg = type.getPackage();
        if (pkg != null) classInfo.setPackageName(pkg.getQualifiedName());

        int loc = countLines(type);
        classInfo.setLoc(loc);

        for (CtMethod<?> method : type.getMethods()) {
            MethodInfo methodInfo = methodExtractor.extractMethodInfo(method, classInfo);
            if (methodInfo != null) classInfo.addMethod(methodInfo);
        }

        for (CtField<?> field : type.getFields()) {
            AttributeInfo attrInfo = attributeExtractor.extractAttributeInfo(field);
            if (attrInfo != null) classInfo.addAttribute(attrInfo);
        }

        return classInfo;
    }

    private int countLines(CtElement element) {
        if (element.getPosition() != null && element.getPosition().isValidPosition()) {
            return element.getPosition().getEndLine() - element.getPosition().getLine() + 1;
        }
        return 1;
    }
}
