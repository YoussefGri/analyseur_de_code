package analyseurdecode.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.IOException;

public class SourceParser {

    public CompilationUnit parse(File file) throws IOException {
        return StaticJavaParser.parse(file);
    }
}
