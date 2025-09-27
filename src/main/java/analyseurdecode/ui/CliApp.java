package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.parser.SourceParser;
import analyseurdecode.visitors.ClassVisitor;
import analyseurdecode.processor.StatisticsProcessor;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CliApp {

    public void run(String sourceFolder, int X) throws Exception {
        List<ClassInfo> classes = new ArrayList<>();
        SourceParser parser = new SourceParser();
        ClassVisitor classVisitor = new ClassVisitor();

        parseDirectory(new File(sourceFolder), parser, classVisitor, classes);

        new StatisticsProcessor().computeStatistics(classes, X);
    }

    private void parseDirectory(File folder, SourceParser parser, ClassVisitor visitor, List<ClassInfo> classes) throws Exception {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                parseDirectory(file, parser, visitor, classes);
            } else if (file.getName().endsWith(".java")) {
                CompilationUnit cu = parser.parse(file);
                ClassInfo ci = new ClassInfo();
                visitor.visit(cu, ci);
                classes.add(ci);
            }
        }
    }
}
