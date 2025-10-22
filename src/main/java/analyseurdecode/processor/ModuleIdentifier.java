package analyseurdecode.processor;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.Module;

import java.util.*;

/*
  Identifie les modules/groupes de classes couplées à partir d'un dendrogramme.
  Contraintes:
  - Une application doit avoir au plus M/2 modules (M = nombre de classes)
  - Chaque module contient uniquement les classes d'une seule branche du dendrogramme
 - La moyenne du couplage de tous les couples de classes du module doit être > CP
 */
public class ModuleIdentifier {
    private final List<ClassInfo> classes;
    private final StatisticsService statsService;
    private final double couplingThreshold; // CP
    private final int maxModules; // M/2

    public ModuleIdentifier(List<ClassInfo> classes, double couplingThreshold) {
        this.classes = classes;
        this.statsService = new StatisticsService();
        this.couplingThreshold = couplingThreshold;
        this.maxModules = Math.max(1, classes.size() / 2);
    }

    /*
      Identifie les modules à partir du dendrogramme
      @param root Le nœud racine du dendrogramme généré par le clustering hiérarchique
      @return Liste des modules identifiés
     */
    public List<Module> identifyModules(DendrogramNode root) {
        if (root == null) {
            return new ArrayList<>();
        }

        List<Module> candidateModules = new ArrayList<>();

        // Parcours du dendrogramme pour identifier les branches candidates
        extractCandidateBranches(root, candidateModules);

        // Filtrage des modules selon les contraintes
        List<Module> validModules = filterValidModules(candidateModules);

        // Limitation au nombre maximum de modules (M/2)
        if (validModules.size() > maxModules) {
            validModules = selectBestModules(validModules, maxModules);
        }

        return validModules;
    }

    /*
      Extrait toutes les branches du dendrogramme comme modules candidats
     */
    private void extractCandidateBranches(DendrogramNode node, List<Module> candidateModules) {
        if (node == null) {
            return;
        }

        // Si c'est une feuille (classe individuelle)
        if (node.leafCluster != null) {
            Module module = new Module(node.leafCluster.getClassNames());
            module.setCouplingScore(0.0); // Une seule classe = pas de couplage interne
            candidateModules.add(module);
            return;
        }

        // Si c'est un nœud interne, créer un module avec toutes ses classes
        Set<String> allClasses = getAllClassesInSubtree(node);
        if (!allClasses.isEmpty()) {
            Module module = new Module(allClasses);
            double avgCoupling = calculateAverageCoupling(allClasses);
            module.setCouplingScore(avgCoupling);
            candidateModules.add(module);
        }

        // Continuer récursivement sur les sous-arbres
        extractCandidateBranches(node.leftNode, candidateModules);
        extractCandidateBranches(node.rightNode, candidateModules);
    }

    /*
      Récupère toutes les classes d'un sous-arbre du dendrogramme
     */
    private Set<String> getAllClassesInSubtree(DendrogramNode node) {
        Set<String> classes = new HashSet<>();

        if (node == null) {
            return classes;
        }

        if (node.leafCluster != null) {
            classes.addAll(node.leafCluster.getClassNames());
        } else {
            classes.addAll(getAllClassesInSubtree(node.leftNode));
            classes.addAll(getAllClassesInSubtree(node.rightNode));
        }

        return classes;
    }

    /*
      Calcule la moyenne du couplage entre tous les couples de classes d'un ensemble
     */
    private double calculateAverageCoupling(Set<String> classNames) {
        if (classNames.size() <= 1) {
            return 0.0;
        }

        List<String> classList = new ArrayList<>(classNames);
        double totalCoupling = 0.0;
        int pairCount = 0;

        for (int i = 0; i < classList.size(); i++) {
            for (int j = i + 1; j < classList.size(); j++) {
                double coupling = statsService.computeCouplingMetric(
                        classes,
                        classList.get(i),
                        classList.get(j)
                );
                totalCoupling += coupling;
                pairCount++;
            }
        }

        return pairCount > 0 ? totalCoupling / pairCount : 0.0;
    }

    /*
      Filtre les modules candidats selon les contraintes:
      - Couplage moyen > CP
      - Pas de chevauchement entre modules
     */
    private List<Module> filterValidModules(List<Module> candidateModules) {
        // Trier par couplage décroissant
        candidateModules.sort((m1, m2) -> Double.compare(m2.getCouplingScore(), m1.getCouplingScore()));

        List<Module> validModules = new ArrayList<>();
        Set<String> usedClasses = new HashSet<>();

        for (Module module : candidateModules) {
            // Vérifier le seuil de couplage
            if (module.getCouplingScore() <= couplingThreshold) {
                continue;
            }

            // Vérifier qu'il n'y a pas de chevauchement avec des modules déjà sélectionnés
            boolean hasOverlap = false;
            for (String className : module.getClassNames()) {
                if (usedClasses.contains(className)) {
                    hasOverlap = true;
                    break;
                }
            }

            if (!hasOverlap) {
                validModules.add(module);
                usedClasses.addAll(module.getClassNames());
            }
        }

        return validModules;
    }

    /*
      Sélectionne les meilleurs modules si le nombre dépasse M/2
      Stratégie: privilégier les modules avec le meilleur couplage et le plus de classes
     */
    private List<Module> selectBestModules(List<Module> validModules, int maxCount) {
        // Calculer un score combinant couplage et taille
        validModules.sort((m1, m2) -> {
            double score1 = m1.getCouplingScore() * Math.log(m1.getClassNames().size() + 1);
            double score2 = m2.getCouplingScore() * Math.log(m2.getClassNames().size() + 1);
            return Double.compare(score2, score1);
        });

        return validModules.subList(0, Math.min(maxCount, validModules.size()));
    }

    /*
      Génère un rapport textuel des modules identifiés
     */
    public String generateModuleReport(List<Module> modules) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════\n");
        sb.append("                    IDENTIFICATION DES MODULES\n");
        sb.append("═══════════════════════════════════════════════════════════════════════\n\n");
        sb.append("Paramètres:\n");
        sb.append("  - Seuil de couplage (CP): ").append(String.format("%.2f", couplingThreshold)).append("\n");
        sb.append("  - Nombre maximum de modules (M/2): ").append(maxModules).append("\n");
        sb.append("  - Nombre de classes total: ").append(classes.size()).append("\n\n");
        sb.append("─────────────────────────────────────────────────────────────────────\n");
        sb.append("Modules identifiés: ").append(modules.size()).append("\n");
        sb.append("─────────────────────────────────────────────────────────────────────\n\n");

        if (modules.isEmpty()) {
            sb.append("Aucun module valide identifié avec le seuil de couplage spécifié.\n");
            sb.append("Suggestions:\n");
            sb.append("  - Réduire le seuil de couplage (CP)\n");
            sb.append("  - Vérifier que les classes ont des relations d'appel\n");
        } else {
            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                sb.append("MODULE ").append(i + 1).append(":\n");
                sb.append("  Nombre de classes: ").append(module.getClassNames().size()).append("\n");
                sb.append("  Couplage moyen: ").append(String.format("%.4f", module.getCouplingScore())).append("\n");
                sb.append("  Classes:\n");

                for (String className : module.getClassNames()) {
                    sb.append("    - ").append(className).append("\n");
                }
                sb.append("\n");
            }

            // Statistiques globales
            Set<String> coveredClasses = new HashSet<>();
            for (Module module : modules) {
                coveredClasses.addAll(module.getClassNames());
            }

            sb.append("─────────────────────────────────────────────────────────────────────\n");
            sb.append("STATISTIQUES:\n");
            sb.append("  - Classes couvertes par modules: ").append(coveredClasses.size())
                    .append("/").append(classes.size()).append("\n");
            sb.append("  - Taux de couverture: ")
                    .append(String.format("%.1f%%", (coveredClasses.size() * 100.0 / classes.size()))).append("\n");

            double avgModuleSize = modules.stream()
                    .mapToInt(m -> m.getClassNames().size())
                    .average()
                    .orElse(0.0);
            sb.append("  - Taille moyenne des modules: ").append(String.format("%.1f", avgModuleSize)).append("\n");

            double avgCoupling = modules.stream()
                    .mapToDouble(Module::getCouplingScore)
                    .average()
                    .orElse(0.0);
            sb.append("  - Couplage moyen des modules: ").append(String.format("%.4f", avgCoupling)).append("\n");
        }

        sb.append("═══════════════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }

    // Getters
    public double getCouplingThreshold() {
        return couplingThreshold;
    }

    public int getMaxModules() {
        return maxModules;
    }
}