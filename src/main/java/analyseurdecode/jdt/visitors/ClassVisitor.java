package analyseurdecode.jdt.visitors;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import analyseurdecode.model.AttributeInfo;
import org.eclipse.jdt.core.dom.*;

public class ClassVisitor extends ASTVisitor {
    private final CompilationUnit cu;
    private final ClassInfo collector;

    public ClassVisitor(CompilationUnit cu, ClassInfo collector) {
        this.cu = cu;
        this.collector = collector;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        collector.setName(node.getName().getIdentifier());
        PackageDeclaration pkg = cu.getPackage();
        collector.setPackageName(pkg != null ? pkg.getName().getFullyQualifiedName() : "");

        // Attributs
        for (FieldDeclaration field : node.getFields()) {
            String typeName = field.getType().toString();
            for (Object fragObj : field.fragments()) {
                VariableDeclarationFragment frag = (VariableDeclarationFragment) fragObj;
                collector.addAttribute(new AttributeInfo(frag.getName().getIdentifier(), typeName));
            }
        }

        // Méthodes
        for (MethodDeclaration method : node.getMethods()) {
            int loc = cu.getLineNumber(method.getStartPosition() + method.getLength()) - cu.getLineNumber(method.getStartPosition()) + 1;
            int params = method.parameters().size();
            java.util.List<String> paramTypes = new java.util.ArrayList<>();
            for (Object paramObj : method.parameters()) {
                SingleVariableDeclaration param = (SingleVariableDeclaration) paramObj;
                String typeStr = param.getType().toString();
                paramTypes.add(typeStr);
            }
            collector.addMethod(new MethodInfo(method.getName().getIdentifier(), loc, params, paramTypes));
        }

        int classLoc = cu.getLineNumber(node.getStartPosition() + node.getLength()) - cu.getLineNumber(node.getStartPosition()) + 1;
        collector.setLoc(classLoc);
        return false;
    }
}