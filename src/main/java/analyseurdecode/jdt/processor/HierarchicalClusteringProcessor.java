package analyseurdecode.jdt.processor;

import analyseurdecode.model.ClassInfo;
import java.util.*;

public class HierarchicalClusteringProcessor {
    private final List<ClassInfo> classes;
    private final StatisticsService statsService;
    private final Map<String, Cluster> clusterMap = new HashMap<>();
    private final Map<String, DendrogramNode> dendroMap = new HashMap<>();

    public HierarchicalClusteringProcessor(List<ClassInfo> classes) {
        this.classes = classes;
        this.statsService = new StatisticsService();
    }

    public DendrogramNode cluster() {
        // Initialisation : chaque classe dans un cluster
        List<Cluster> clusters = new ArrayList<>();
        for (ClassInfo ci : classes) {
            Cluster c = new Cluster(ci.getName());
            clusters.add(c);
            clusterMap.put(ci.getName(), c);
        }
        // Dendrogramme initial : chaque classe est une feuille
        for (Cluster c : clusters) {
            dendroMap.put(c.getClassNames().iterator().next(), null);
        }
        // Matrice de couplage
        int n = clusters.size();
        double[][] couplingMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    couplingMatrix[i][j] = statsService.computeCouplingMetric(classes,
                        clusters.get(i).getClassNames().iterator().next(),
                        clusters.get(j).getClassNames().iterator().next());
                } else {
                    couplingMatrix[i][j] = -1;
                }
            }
        }
        // Clustering hiérarchique
        while (clusters.size() > 1) {
            // Trouver les deux clusters les plus couplés
            double maxCoupling = -1;
            int idx1 = -1, idx2 = -1;
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double c = couplingMatrix[i][j];
                    if (c > maxCoupling) {
                        maxCoupling = c;
                        idx1 = i;
                        idx2 = j;
                    }
                }
            }
            if (idx1 == -1 || idx2 == -1) break;
            // Fusionner les deux clusters
            Cluster c1 = clusters.get(idx1);
            Cluster c2 = clusters.get(idx2);
            Set<String> mergedNames = new HashSet<>(c1.getClassNames());
            mergedNames.addAll(c2.getClassNames());
            Cluster merged = new Cluster(mergedNames);
            DendrogramNode leftNode = dendroMap.get(c1.getClassNames().iterator().next());
            DendrogramNode rightNode = dendroMap.get(c2.getClassNames().iterator().next());
            DendrogramNode leftNodeFinal = leftNode != null ? leftNode : new DendrogramNode(c1);
            DendrogramNode rightNodeFinal = rightNode != null ? rightNode : new DendrogramNode(c2);
            DendrogramNode newNode = new DendrogramNode(leftNodeFinal, rightNodeFinal, maxCoupling);
            // Mise à jour
            clusters.remove(idx2);
            clusters.remove(idx1);
            clusters.add(merged);
            dendroMap.put(merged.getClassNames().iterator().next(), newNode);
            // Recalculer la matrice de couplage
            n = clusters.size();
            double[][] newMatrix = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        newMatrix[i][j] = -1;
                    } else {
                        // Couplage moyen entre tous les membres des deux clusters
                        double sum = 0;
                        int count = 0;
                        for (String a : clusters.get(i).getClassNames()) {
                            for (String b : clusters.get(j).getClassNames()) {
                                if (!a.equals(b)) {
                                    double val = statsService.computeCouplingMetric(classes, a, b);
                                    sum += val;
                                    count++;
                                }
                            }
                        }
                        newMatrix[i][j] = count > 0 ? sum / count : 0;
                    }
                }
            }
            couplingMatrix = newMatrix;
        }
        // Retourner le dendrogramme final
        if (clusters.size() == 1) {
            return dendroMap.get(clusters.get(0).getClassNames().iterator().next());
        }
        return null;
    }
}
