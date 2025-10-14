package analyseurdecode.ui;

import analyseurdecode.processor.DendrogramNode;
import analyseurdecode.processor.Cluster;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Set;

public class DendrogramPanel extends JPanel {
    private final DendrogramNode root;
    private static final int NODE_WIDTH = 80;
    private static final int NODE_HEIGHT = 30;
    private static final int VERTICAL_GAP = 50;
    private static final int HORIZONTAL_GAP = 30;

    // Couleurs améliorées
    private static final Color NODE_COLOR = new Color(52, 152, 219);
    private static final Color NODE_BORDER = new Color(41, 128, 185);
    private static final Color LEAF_COLOR = new Color(46, 204, 113);
    private static final Color LEAF_BORDER = new Color(39, 174, 96);
    private static final Color LINE_COLOR = new Color(149, 165, 166);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(44, 62, 80);

    public DendrogramPanel(DendrogramNode root) {
        this.root = root;
        setBackground(Color.WHITE);
        updatePreferredSize();
    }

    private void updatePreferredSize() {
        int width = (int) computeSubtreeWidth(root);
        int height = computeDepth(root) * (NODE_HEIGHT + VERTICAL_GAP) + 60;
        setPreferredSize(new Dimension(Math.max(width, 600), Math.max(height, 400)));
    }

    private int computeDepth(DendrogramNode node) {
        if (node == null) return 0;
        if (node.leafCluster != null) return 1;
        return 1 + Math.max(computeDepth(node.leftNode), computeDepth(node.rightNode));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updatePreferredSize();
        if (root != null) {
            int treeWidth = (int) computeSubtreeWidth(root);
            int startX = Math.max(getWidth() / 2, treeWidth / 2 + 20);
            int startY = 40;
            drawNode(g, root, startX, startY, treeWidth);
        } else {
            g.setColor(Color.RED);
            g.drawString("Aucun dendrogramme à afficher.", 20, 40);
        }
    }

    // Calcule la largeur totale nécessaire pour dessiner le sous-arbre
    private double computeSubtreeWidth(DendrogramNode node) {
        if (node == null) return 0;
        if (node.leafCluster != null) {
            return NODE_WIDTH + HORIZONTAL_GAP;
        } else {
            double left = computeSubtreeWidth(node.leftNode);
            double right = computeSubtreeWidth(node.rightNode);
            return left + right;
        }
    }

    // Dessine le nœud à la position x, y, en espaçant selon la largeur du sous-arbre
    private void drawNode(Graphics g, DendrogramNode node, int x, int y, double subtreeWidth) {
        if (node.leafCluster != null) {
            drawCluster(g, node.leafCluster, x, y);
        } else {
            double leftWidth = computeSubtreeWidth(node.leftNode);
            double rightWidth = computeSubtreeWidth(node.rightNode);
            int childY = y + VERTICAL_GAP;
            int leftX = (int) (x - subtreeWidth / 2 + leftWidth / 2);
            int rightX = (int) (x + subtreeWidth / 2 - rightWidth / 2);
            // Lignes vers enfants
            g.setColor(Color.GRAY);
            g.drawLine(x, y + NODE_HEIGHT, leftX, childY);
            g.drawLine(x, y + NODE_HEIGHT, rightX, childY);
            // Nœud courant
            g.setColor(new Color(41,128,185));
            g.fillRect(x - NODE_WIDTH / 2, y, NODE_WIDTH, NODE_HEIGHT);
            g.setColor(Color.WHITE);
            g.drawString(String.format("c=%.2f", node.coupling), x - NODE_WIDTH / 2 + 8, y + NODE_HEIGHT / 2 + 5);
            // Enfants
            drawNode(g, node.leftNode, leftX, childY, leftWidth);
            drawNode(g, node.rightNode, rightX, childY, rightWidth);
        }
    }

    private void drawCluster(Graphics g, Cluster cluster, int x, int y) {
        g.setColor(new Color(46,204,113));
        g.fillRect(x - NODE_WIDTH / 2, y, NODE_WIDTH, NODE_HEIGHT);
        g.setColor(Color.BLACK);
        Set<String> names = cluster.getClassNames();
        String label = String.join(", ", names);
        g.drawString(label, x - NODE_WIDTH / 2 + 8, y + NODE_HEIGHT / 2 + 5);
    }
}