package analyseurdecode.jdt.visitors;

import analyseurdecode.model.MethodInfo;
import org.eclipse.jdt.core.dom.*;
import java.util.Map;

public class CallGraphVisitor extends ASTVisitor {
    private final Map<String, MethodInfo> methodMap;
    private MethodInfo currentMethod; 

   
    public CallGraphVisitor(Map<String, MethodInfo> methodMap) {
        this.methodMap = methodMap;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        IMethodBinding binding = node.resolveBinding();
        currentMethod = null;
        
        if (binding != null) {
            String qName = binding.getDeclaringClass().getQualifiedName() + "." + node.getName().getIdentifier();
            currentMethod = methodMap.get(qName);
        }
        
        return true; 
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        currentMethod = null;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (currentMethod != null) {
            IMethodBinding binding = node.resolveMethodBinding();
            
            if (binding != null && binding.getDeclaringClass() != null) {
                String calledClass = binding.getDeclaringClass().getQualifiedName();
                String calledMethod = binding.getName();
                String calledQualifiedName = calledClass + "." + calledMethod;

                currentMethod.addCalledMethodName(calledQualifiedName);
            }
        }
        return super.visit(node); 
    }
}