package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.parser.SourceParser;
import analyseurdecode.visitors.ClassVisitor;
import analyseurdecode.processor.StatisticsProcessor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CliApp {

    public void run(String sourceFolder, int X) throws Exception {
        List<ClassInfo> classes = new ArrayList<>();
        SourceParser parser = new SourceParser();

        parseDirectory(new File(sourceFolder), parser, classes);

        new StatisticsProcessor().computeStatistics(classes, X);
    }

    private void parseDirectory(File folder, SourceParser parser, List<ClassInfo> classes) throws Exception {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                parseDirectory(file, parser, classes);
            } else if (file.getName().endsWith(".java")) {
                CompilationUnit cu = parser.parse(file);
                ClassInfo ci = new ClassInfo();
                ClassVisitor visitor = new ClassVisitor(cu, ci);
                cu.accept(visitor);
                classes.add(ci);
            }
        }
    }
}