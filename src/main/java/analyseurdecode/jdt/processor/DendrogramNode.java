package analyseurdecode.jdt.processor;

public class DendrogramNode {
    public Cluster leafCluster;
    public DendrogramNode leftNode;
    public DendrogramNode rightNode;
    public double coupling;

    // Feuille
    public DendrogramNode(Cluster leafCluster) {
        this.leafCluster = leafCluster;
    }
    // Nœud interne
    public DendrogramNode(DendrogramNode leftNode, DendrogramNode rightNode, double coupling) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.coupling = coupling;
    }
    @Override
    public String toString() {
        if (leafCluster != null) {
            return leafCluster.toString();
        } else {
            return "[" + leftNode + ", " + rightNode + ", c=" + coupling + "]";
        }
    }
}
