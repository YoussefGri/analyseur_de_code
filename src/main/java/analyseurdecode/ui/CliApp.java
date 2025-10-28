package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo; // Nouveau
import analyseurdecode.jdt.parser.SourceParser;
import analyseurdecode.jdt.visitors.ClassVisitor;
import analyseurdecode.jdt.visitors.CallGraphVisitor; // Nouveau
import analyseurdecode.jdt.processor.StatisticsProcessor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliApp {

	// fichiers
	private List<File> listAllJavaFiles = new ArrayList<>();

	public void run(String sourceFolder, int X) throws Exception {
		List<ClassInfo> classes = new ArrayList<>();
		SourceParser parser = new SourceParser();

		parseDirectory(new File(sourceFolder), parser, classes);

		Map<String, MethodInfo> allMethodsMap = new HashMap<>();
		for (ClassInfo ci : classes) {
			// Construit le nom qualifié complet (package.Classe.méthode)
			String classQName = (ci.getPackageName().isEmpty() ? "" : ci.getPackageName() + ".") + ci.getName();
			for (MethodInfo mi : ci.getMethods()) {
				String methodQName = classQName + "." + mi.getName();
				mi.setFullyQualifiedName(methodQName);
				allMethodsMap.put(methodQName, mi);
			}
		}

		for (File file : listAllJavaFiles) {
			CompilationUnit cu = parser.parse(file, sourceFolder);
			CallGraphVisitor cgVisitor = new CallGraphVisitor(allMethodsMap);
			cu.accept(cgVisitor);
		}

		new StatisticsProcessor().computeStatistics(classes, X);

		// Interaction utilisateur pour le couplage
		java.util.Scanner scanner = new java.util.Scanner(System.in);
		System.out.print("Entrez le nom de la première classe pour le couplage : ");
		String classA = scanner.nextLine().trim();
		System.out.print("Entrez le nom de la seconde classe pour le couplage : ");
		String classB = scanner.nextLine().trim();
		new StatisticsProcessor().printCouplingMetric(classes, classA, classB);

		displayCallGraph(classes, allMethodsMap);
	}

	private void parseDirectory(File folder, SourceParser parser, List<ClassInfo> classes) throws Exception {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				parseDirectory(file, parser, classes);
			} else if (file.getName().endsWith(".java")) {
				listAllJavaFiles.add(file); // Stocker le fichier pour la Phase 3

				CompilationUnit cu = parser.parse(file, null);
				ClassInfo ci = new ClassInfo();
				ClassVisitor visitor = new ClassVisitor(cu, ci);
				cu.accept(visitor);
				classes.add(ci);
			}
		}
	}

	private void displayCallGraph(List<ClassInfo> classes, Map<String, MethodInfo> allMethodsMap) {
		System.out.println("\n==================================");
		System.out.println("Graphe d'Appel de l'Application");
		System.out.println("==================================");
		for (ClassInfo ci : classes) {
			for (MethodInfo mi : ci.getMethods()) {
				System.out.println("➡️ " + mi.getFullyQualifiedName() + " appelle:");
				if (!mi.getCalledMethodsNames().isEmpty()) {
					for (String calledName : mi.getCalledMethodsNames()) {
						String status = allMethodsMap.containsKey(calledName) ? " (Interne)" : " (Externe/API)";
						System.out.println("   -> " + calledName + status);
					}
				} else {
					System.out.println("   -> (aucun appel)");
				}
			}
		}
	}
}