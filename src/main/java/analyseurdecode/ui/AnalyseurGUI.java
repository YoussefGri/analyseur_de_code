package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.parser.SourceParser;
import analyseurdecode.visitors.ClassVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyseurGUI extends JFrame {
    private JTextField folderField;
    private JTextField xField;
    private JButton browseButton, analyzeButton;
    private JTabbedPane tabbedPane;
    private JTable topMethodsTable, topAttributesTable, bothCategoriesTable, moreThanXTable;
    private JTree classTree;
    private JPanel summaryPanel;

    public AnalyseurGUI() {
        setTitle("Analyseur de code Java OO");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        folderField = new JTextField(40);
        browseButton = new JButton("Parcourir...");
        xField = new JTextField(3);
        analyzeButton = new JButton("Analyser");

        topPanel.add(new JLabel("Dossier source :"));
        topPanel.add(folderField);
        topPanel.add(browseButton);
        topPanel.add(new JLabel("NB méthodes Min :"));
        topPanel.add(xField);
        topPanel.add(analyzeButton);

        tabbedPane = new JTabbedPane();
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                folderField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        analyzeButton.addActionListener(e -> runAnalysis());
    }

    private void runAnalysis() {
        String folderPath = folderField.getText();
        int X;
        try {
            X = Integer.parseInt(xField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Le nombre de méthodes doit être un nombre entier", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Le dossier source est invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            List<ClassInfo> classes = new ArrayList<>();
            SourceParser parser = new SourceParser();
            parseDirectory(folder, parser, classes);
            showStatistics(classes, X);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'analyse : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
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

    private void showStatistics(List<ClassInfo> classes, int X) {
        tabbedPane.removeAll();
        // --- Summary Panel ---
        summaryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        int totalClasses = classes.size();
        int totalMethods = classes.stream().mapToInt(c -> c.getMethods().size()).sum();
        int totalLoc = classes.stream().mapToInt(ClassInfo::getLoc).sum();
        int totalAttributes = classes.stream().mapToInt(c -> c.getAttributes().size()).sum();
        int totalPackages = (int) classes.stream().map(ClassInfo::getPackageName).distinct().count();
        double avgMethodsPerClass = totalClasses > 0 ? (double) totalMethods / totalClasses : 0;
        double avgLocPerMethod = totalMethods > 0 ?
            (double) classes.stream().flatMap(c -> c.getMethods().stream()).mapToInt(m -> m.getLoc()).sum() / totalMethods
            : 0;
        double avgAttributesPerClass = totalClasses > 0 ? (double) totalAttributes / totalClasses : 0;
        int maxParams = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .mapToInt(m -> m.getParameters())
            .max().orElse(0);
        summaryPanel.add(createStatLabel("Nombre de classes", String.valueOf(totalClasses)));
        summaryPanel.add(createStatLabel("Nombre de lignes de code", String.valueOf(totalLoc)));
        summaryPanel.add(createStatLabel("Nombre total de méthodes", String.valueOf(totalMethods)));
        summaryPanel.add(createStatLabel("Nombre total de packages", String.valueOf(totalPackages)));
        summaryPanel.add(createStatLabel("Nombre moyen de méthodes par classe", String.format("%.2f", avgMethodsPerClass)));
        summaryPanel.add(createStatLabel("Nombre moyen de lignes par méthode", String.format("%.2f", avgLocPerMethod)));
        summaryPanel.add(createStatLabel("Nombre moyen d'attributs par classe", String.format("%.2f", avgAttributesPerClass)));
        summaryPanel.add(createStatLabel("Max params dans toutes les méthodes", String.valueOf(maxParams)));
        tabbedPane.addTab("Résumé", summaryPanel);
        // --- Top Classes Tables ---
        int topCount = Math.max(1, totalClasses / 10);
        List<ClassInfo> topMethodsClasses = new ArrayList<>(classes);
        topMethodsClasses.sort((a, b) -> b.getMethods().size() - a.getMethods().size());
        topMethodsClasses = topMethodsClasses.subList(0, topCount);
        List<ClassInfo> topAttributesClasses = new ArrayList<>(classes);
        topAttributesClasses.sort((a, b) -> b.getAttributes().size() - a.getAttributes().size());
        topAttributesClasses = topAttributesClasses.subList(0, topCount);
        List<ClassInfo> bothCategories = new ArrayList<>(topMethodsClasses);
        bothCategories.retainAll(topAttributesClasses);
        List<ClassInfo> classesMoreThanX = classes.stream()
            .filter(c -> c.getMethods().size() > X)
            .collect(Collectors.toList());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topPanel.add(createTableSection("Top classes par méthodes", topMethodsClasses));
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(createTableSection("Top classes par attributs", topAttributesClasses));
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(createTableSection("Classes dans les deux catégories", bothCategories));
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(createTableSection("Classes avec plus de " + X + " méthodes", classesMoreThanX));
        tabbedPane.addTab("Top classes", topPanel);
        // --- Tree View ---
        classTree = new JTree(buildClassTree(classes));
        JScrollPane treeScroll = new JScrollPane(classTree);
        tabbedPane.addTab("Arbre des classes", treeScroll);
    }

    private JPanel createStatLabel(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel l = new JLabel(label + ": ");
        l.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.BOLD, 18));
        v.setForeground(new Color(0, 102, 204));
        panel.add(l, BorderLayout.WEST);
        panel.add(v, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTableSection(String title, List<ClassInfo> classes) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JTable table = createClassTable(classes);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return panel;
    }

    private JTable createClassTable(List<ClassInfo> classes) {
        String[] columns = {"Classe", "Méthodes", "Attributs", "Package"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (ClassInfo c : classes) {
            model.addRow(new Object[]{c.getName(), c.getMethods().size(), c.getAttributes().size(), c.getPackageName()});
        }
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        return table;
    }

    private DefaultMutableTreeNode buildClassTree(List<ClassInfo> classes) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Projet Java");
        for (ClassInfo c : classes) {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(c.getName());
            DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Méthodes");
            c.getMethods().forEach(m -> methodsNode.add(new DefaultMutableTreeNode(m)));
            classNode.add(methodsNode);
            DefaultMutableTreeNode attributesNode = new DefaultMutableTreeNode("Attributs");
            c.getAttributes().forEach(a -> attributesNode.add(new DefaultMutableTreeNode(a)));
            classNode.add(attributesNode);
            root.add(classNode);
        }
        return root;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalyseurGUI().setVisible(true));
    }
}