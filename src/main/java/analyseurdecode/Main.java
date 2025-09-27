package analyseurdecode;

import analyseurdecode.ui.CliApp;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -jar analyseur_de_code.jar <dossier_source> <X>");
            return;
        }

        String folder = args[0];
        int X = Integer.parseInt(args[1]);

        new CliApp().run(folder, X);
    }
}
