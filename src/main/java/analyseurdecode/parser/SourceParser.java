/*package analyseurdecode.parser;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SourceParser {

    public CompilationUnit parse(File file) throws IOException {
        String source = new String(Files.readAllBytes(file.toPath()));
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        return (CompilationUnit) parser.createAST(null);
    }
}*/

// Fichier : analyseurdecode.parser.SourceParser.java
package analyseurdecode.parser;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.JavaCore; 
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map; 

public class SourceParser {

    public CompilationUnit parse(File file, String projectSourcePath) throws IOException {
        String source = new String(Files.readAllBytes(file.toPath()));

        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setResolveBindings(true); 
        parser.setBindingsRecovery(true);

        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);
        parser.setUnitName(file.getName());

        // --- CONFIG ---
        
        String javaHome = System.getProperty("java.home");
        
        String jrePath = javaHome + File.separator + "jmods"; // pour java 17
        
        // Dossiers sources (Le chemin fourni par l'interface utilisateur)
        String[] sources = { projectSourcePath };
        String[] classpath = { jrePath }; 


        if (!new File(projectSourcePath).isDirectory()) {
             System.err.println("ERREUR PARSER: Dossier source INVALIDE: " + projectSourcePath);
             throw new IOException("Dossier source du projet analysé introuvable.");
        }
        if (!new File(jrePath).exists()) {
             System.err.println("ERREUR PARSER: Chemin JRE INVALIDE: " + jrePath);
             throw new IOException("Chemin JRE pour JDT introuvable. Vérifiez votre JDK/JRE.");
        }
        
        parser.setEnvironment(classpath, sources, new String[] {"UTF-8"}, true);

        return (CompilationUnit) parser.createAST(null);
    }
}