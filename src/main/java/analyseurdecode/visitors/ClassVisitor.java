package analyseurdecode.visitors;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import analyseurdecode.model.AttributeInfo;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ClassVisitor extends VoidVisitorAdapter<ClassInfo> {

    @Override
    public void visit(ClassOrInterfaceDeclaration c, ClassInfo collector) {
        super.visit(c, collector);

        collector.setName(c.getNameAsString());
        collector.setPackageName(
            c.findCompilationUnit()
             .flatMap(cu -> cu.getPackageDeclaration())
             .map(pd -> pd.getNameAsString())
             .orElse("")
        );

        // Attributs
        c.getFields().forEach(f ->
            f.getVariables().forEach(v ->
                collector.addAttribute(new AttributeInfo(v.getNameAsString()))
            )
        );

        // Méthodes
        c.getMethods().forEach(m -> {
            int loc = m.getEnd().map(e -> e.line - m.getBegin().get().line + 1).orElse(0);
            int params = m.getParameters().size();
            collector.addMethod(new MethodInfo(m.getNameAsString(), loc, params));
        });

        int classLoc = c.getEnd().map(e -> e.line - c.getBegin().get().line + 1).orElse(0);
        collector.setLoc(classLoc);
    }
}
