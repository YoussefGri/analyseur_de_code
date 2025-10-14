package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import analyseurdecode.parser.SourceParser;
import analyseurdecode.processor.StatisticsResult;
import analyseurdecode.processor.StatisticsService;
import analyseurdecode.visitors.ClassVisitor;
import analyseurdecode.visitors.CallGraphVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyseurGUI extends JFrame {
    // Couleurs du thème
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color LIGHT_TEXT = new Color(127, 140, 141);

    private JTextField folderField;
    private JTextField xField;
    private JButton browseButton, analyzeButton;
    private JTabbedPane tabbedPane;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // Champs pour le Graphe d'Appel
    private List<File> listAllJavaFiles = new ArrayList<>();
    private Map<String, MethodInfo> allMethodsMap;

    // Stocke la dernière liste de classes analysées
    private List<ClassInfo> lastAnalyzedClasses = null;

    public AnalyseurGUI() {
        setTitle("Analyseur de Code Java - Orienté Objet");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Panneau principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Panneau de contrôle en haut
        JPanel controlPanel = createControlPanel();

        // Zone à onglets
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(CARD_COLOR);

        // Panneau de statut en bas
        JPanel statusPanel = createStatusPanel();

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Message de bienvenue
        showWelcomeTab();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Sélection du dossier
        JPanel folderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        folderPanel.setBackground(CARD_COLOR);

        JLabel folderLabel = new JLabel("Dossier source :");
        folderLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        folderLabel.setForeground(TEXT_COLOR);

        folderField = new JTextField(50);
        folderField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        folderField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        browseButton = createStyledButton("Parcourir...", PRIMARY_COLOR);
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Sélectionner le dossier source Java");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                folderField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        folderPanel.add(folderLabel);
        folderPanel.add(folderField);
        folderPanel.add(browseButton);

        // Paramètres et analyse
        JPanel paramsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        paramsPanel.setBackground(CARD_COLOR);

        JLabel xLabel = new JLabel("Nombre minimum de méthodes :");
        xLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        xLabel.setForeground(TEXT_COLOR);

        xField = new JTextField(5);
        xField.setText("2");
        xField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        xField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        analyzeButton = createStyledButton("Lancer l'Analyse", ACCENT_COLOR);
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        analyzeButton.addActionListener(e -> runAnalysis());

        paramsPanel.add(xLabel);
        paramsPanel.add(xField);
        paramsPanel.add(Box.createHorizontalStrut(20));
        paramsPanel.add(analyzeButton);

        // Checkbox pour afficher le calcul de couplage
        JCheckBox showCouplingCheckBox = new JCheckBox("Afficher le calcul de couplage entre deux classes");
        showCouplingCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showCouplingCheckBox.setBackground(CARD_COLOR);
        showCouplingCheckBox.setForeground(TEXT_COLOR);

        // Panneau de couplage (masqué par défaut)
        JPanel couplingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        couplingPanel.setBackground(CARD_COLOR);
        couplingPanel.setVisible(false);
        JLabel couplingLabel = new JLabel("Couplage entre deux classes :");
        couplingLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        couplingLabel.setForeground(TEXT_COLOR);
        JTextField classAField = new JTextField(15);
        classAField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTextField classBField = new JTextField(15);
        classBField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton couplingButton = createStyledButton("Calculer le couplage", ACCENT_COLOR);
        JLabel couplingResultLabel = new JLabel("");
        couplingResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        couplingResultLabel.setForeground(PRIMARY_COLOR);
        couplingButton.addActionListener(e -> {
            String classA = classAField.getText().trim();
            String classB = classBField.getText().trim();
            if (classA.isEmpty() || classB.isEmpty()) {
                couplingResultLabel.setText("Veuillez saisir les deux noms de classes.");
                return;
            }
            List<ClassInfo> classes = getLastAnalyzedClasses();
            System.out.println("[createControlPanel - AnalyseurGUI] : " + classes);
            if (classes == null) {
                couplingResultLabel.setText("Veuillez d'abord lancer l'analyse.");
                return;
            }
            double coupling = new analyseurdecode.processor.StatisticsService().computeCouplingMetric(classes, classA, classB);
            couplingResultLabel.setText("Couplage entre " + classA + " et " + classB + " : " + coupling);
            System.out.println("Couplage entre " + classA + " et " + classB + " : " + coupling);
        });
        couplingPanel.add(couplingLabel);
        couplingPanel.add(classAField);
        couplingPanel.add(classBField);
        couplingPanel.add(couplingButton);
        couplingPanel.add(couplingResultLabel);

        // Gestion de la visibilité du panneau de couplage
        showCouplingCheckBox.addActionListener(e -> {
            couplingPanel.setVisible(showCouplingCheckBox.isSelected());
        });

        panel.add(folderPanel);
        panel.add(paramsPanel);
        panel.add(showCouplingCheckBox);
        panel.add(couplingPanel);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        statusLabel = new JLabel("Prêt");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(LIGHT_TEXT);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setVisible(false);
        progressBar.setForeground(PRIMARY_COLOR);

        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.EAST);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void showWelcomeTab() {
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Analyseur de Code Java");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Analyse statique orientée objet");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(LIGHT_TEXT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setSize(500, 300);
        instructionsArea.setText(
                "Instructions d'utilisation :\n\n" +
                        "1. Sélectionnez un dossier contenant des fichiers Java (.java)\n\n" +
                        "2. Définissez le nombre minimum de méthodes pour le filtrage\n\n" +
                        "3. Cliquez sur 'Lancer l'Analyse' pour commencer\n\n" +
                        "L'analyse génèrera :\n" +
                        "  - Un résumé statistique complet\n" +
                        "  - Le classement des classes par méthodes et attributs\n" +
                        "  - Une vue arborescente de la structure du code\n" +
                        "  - Le graphe d'appel des méthodes (relations d'appel)"
        );
        instructionsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instructionsArea.setForeground(TEXT_COLOR);
        instructionsArea.setBackground(CARD_COLOR);
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(instructionsArea);

        welcomePanel.add(contentPanel);
        tabbedPane.addTab("Accueil", welcomePanel);
    }

    // Permet d'accéder à la dernière analyse
    public List<ClassInfo> getLastAnalyzedClasses() {
        return lastAnalyzedClasses;
    }

    private void runAnalysis() {
        String folderPath = folderField.getText().trim();
        int X;

        try {
            X = Integer.parseInt(xField.getText().trim());
            if (X < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            showError("Le nombre de méthodes doit être un entier positif");
            return;
        }

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            showError("Le dossier source est invalide ou n'existe pas");
            return;
        }

        if (!folder.canRead()) {
            showError("Le dossier n'est pas accessible en lecture");
            return;
        }

        // Analyse
        SwingWorker<List<ClassInfo>, Void> worker = new SwingWorker<List<ClassInfo>, Void>() {
            @Override
            protected List<ClassInfo> doInBackground() throws Exception {
                statusLabel.setText("Analyse en cours...");
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
                analyzeButton.setEnabled(false);

                List<ClassInfo> classes = new ArrayList<>();
                SourceParser parser = new SourceParser();

                listAllJavaFiles.clear();
                allMethodsMap = new HashMap<>();

                parseDirectory(folder, parser, classes, folderPath);

                if (classes.isEmpty()) {
                    throw new Exception("Aucun fichier Java trouvé");
                }

                // Préparation de la Map des méthodes
                for (ClassInfo ci : classes) {
                    if (ci == null) continue;

                    String packageName = ci.getPackageName();
                    String className = ci.getName();
                    if (className == null) continue;

                    String classQName = (packageName == null || packageName.isEmpty() ? "" : packageName + ".") + className;

                    List<MethodInfo> methods = ci.getMethods();
                    if (methods == null) continue;

                    for (MethodInfo mi : methods) {
                        if (mi == null || mi.getName() == null) continue;
                        String methodQName = classQName + "." + mi.getName();
                        mi.setFullyQualifiedName(methodQName);
                        allMethodsMap.put(methodQName, mi);
                    }
                }

                // Analyse du Graphe d'Appel
                for (File file : listAllJavaFiles) {
                    try {
                        CompilationUnit cu = parser.parse(file, folderPath);
                        CallGraphVisitor cgVisitor = new CallGraphVisitor(allMethodsMap);
                        cu.accept(cgVisitor);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'analyse du graphe d'appel pour " + file.getName());
                    }
                }

                return classes;
            }

            @Override
            protected void done() {
                try {
                    List<ClassInfo> classes = get();
                    lastAnalyzedClasses = classes;
                    showStatistics(classes, X);
                    statusLabel.setText("Analyse terminée - " + classes.size() + " classes analysées");
                } catch (Exception ex) {
                    showError("Erreur lors de l'analyse : " + ex.getMessage());
                    statusLabel.setText("Erreur");
                } finally {
                    progressBar.setVisible(false);
                    analyzeButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void parseDirectory(File folder, SourceParser parser, List<ClassInfo> classes, String projectSourcePath) throws Exception {
        File[] files = folder.listFiles();
        if (files == null) {
            System.err.println("ATTENTION: Impossible de lire le contenu de: " + folder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                parseDirectory(file, parser, classes, projectSourcePath);
            } else if (file.getName().endsWith(".java")) {
                listAllJavaFiles.add(file);

                try {
                    CompilationUnit cu = parser.parse(file, projectSourcePath);
                    ClassInfo ci = new ClassInfo();
                    ClassVisitor visitor = new ClassVisitor(cu, ci);
                    cu.accept(visitor);

                    if (ci.getName() != null && !ci.getName().isEmpty()) {
                        classes.add(ci);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du parsing de " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }


    private void showStatistics(List<ClassInfo> classes, int X) {
        tabbedPane.removeAll();

        // Calcul des statistiques centralisé
        StatisticsService service = new StatisticsService();
        StatisticsResult stats = service.compute(classes, X);

        // Onglet Résumé
        JPanel summaryPanel = createSummaryPanel(stats);
        tabbedPane.addTab("Résumé", summaryPanel);

        // Onglet Top Classes
        JPanel topPanel = createTopClassesPanel(stats, X);
        tabbedPane.addTab("Top Classes", topPanel);

        // Onglet Arbre des classes
        JPanel treePanel = createClassTreePanel(classes);
        tabbedPane.addTab("Structure", treePanel);

        // Onglet Graphe d'Appel
        JPanel callGraphPanel = createCallGraphPanel(classes);
        tabbedPane.addTab("Graphe d'Appel", callGraphPanel);

        tabbedPane.setSelectedIndex(0);
    }

    private JPanel createSummaryPanel(StatisticsResult stats) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        gridPanel.setBackground(BACKGROUND_COLOR);

        gridPanel.add(createStatCard("Classes", String.valueOf(stats.totalClasses), "Nombre total de classes analysées"));
        gridPanel.add(createStatCard("Lignes de Code", String.valueOf(stats.totalLoc), "Total de lignes de code"));
        gridPanel.add(createStatCard("Méthodes", String.valueOf(stats.totalMethods), "Nombre total de méthodes"));
        gridPanel.add(createStatCard("Packages", String.valueOf(stats.totalPackages), "Nombre de packages distincts"));
        gridPanel.add(createStatCard("Moy. Méthodes/Classe", String.format("%.2f", stats.avgMethodsPerClass), "Moyenne de méthodes par classe"));
        gridPanel.add(createStatCard("Moy. Lignes/Méthode", String.format("%.2f", stats.avgLocPerMethod), "Moyenne de lignes par méthode"));
        gridPanel.add(createStatCard("Moy. Attributs/Classe", String.format("%.2f", stats.avgAttributesPerClass), "Moyenne d'attributs par classe"));
        gridPanel.add(createStatCard("Max Paramètres", String.valueOf(stats.maxParams), "Maximum de paramètres dans une méthode"));

        mainPanel.add(gridPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createStatCard(String title, String value, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(LIGHT_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(PRIMARY_COLOR);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        descLabel.setForeground(LIGHT_TEXT);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descLabel);

        return card;
    }


    private JPanel createTopClassesPanel(StatisticsResult stats, int X) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(createTableSection("Classes avec le plus de méthodes", stats.topMethodsClasses));
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createTableSection("Classes avec le plus d'attributs", stats.topAttributesClasses));
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createTableSection("Classes dans les deux catégories", stats.bothCategories));
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createTableSection("Classes avec plus de " + X + " méthodes", stats.classesMoreThanX));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel createTableSection(String title, List<ClassInfo> classes) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JTable table = createStyledTable(classes);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        scrollPane.setPreferredSize(new Dimension(0, Math.min(200, (classes.size() + 1) * 30)));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTable createStyledTable(List<ClassInfo> classes) {
        String[] columns = {"Classe", "Méthodes", "Attributs", "Package"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (ClassInfo c : classes) {
            model.addRow(new Object[]{
                    c.getName(),
                    c.getMethods().size(),
                    c.getAttributes().size(),
                    c.getPackageName()
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(52, 152, 219, 50));
        table.setSelectionForeground(TEXT_COLOR);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        return table;
    }

    private JPanel createClassTreePanel(List<ClassInfo> classes) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Projet Java");

        for (ClassInfo c : classes) {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode("[Classe] " + c.getName());

            DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Méthodes (" + c.getMethods().size() + ")");
            c.getMethods().forEach(m -> methodsNode.add(new DefaultMutableTreeNode(m)));
            classNode.add(methodsNode);

            DefaultMutableTreeNode attributesNode = new DefaultMutableTreeNode("Attributs (" + c.getAttributes().size() + ")");
            c.getAttributes().forEach(a -> attributesNode.add(new DefaultMutableTreeNode(a)));
            classNode.add(attributesNode);

            root.add(classNode);
        }

        JTree tree = new JTree(root);
        tree.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tree.setRowHeight(24);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setBackgroundNonSelectionColor(BACKGROUND_COLOR);
        renderer.setBackgroundSelectionColor(PRIMARY_COLOR);
        renderer.setBorderSelectionColor(PRIMARY_COLOR);
        tree.setCellRenderer(renderer);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCallGraphPanel(List<ClassInfo> classes) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(CARD_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel modeLabel = new JLabel("Mode d'affichage :");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        modeLabel.setForeground(TEXT_COLOR);

        JToggleButton textButton = new JToggleButton("Textuel");
        JToggleButton treeButton = new JToggleButton("Arborescent");
        JToggleButton graphButton = new JToggleButton("Graphique");
        JToggleButton dendrogramButton = new JToggleButton("Dendrogramme");

        ButtonGroup group = new ButtonGroup();
        group.add(textButton);
        group.add(treeButton);
        group.add(graphButton);
        group.add(dendrogramButton);
        textButton.setSelected(true);

        for (JToggleButton btn : new JToggleButton[]{textButton, treeButton, graphButton, dendrogramButton}) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        controlPanel.add(modeLabel);
        controlPanel.add(textButton);
        controlPanel.add(treeButton);
        controlPanel.add(graphButton);
        controlPanel.add(dendrogramButton);

        CardLayout cardLayout = new CardLayout();
        JPanel displayPanel = new JPanel(cardLayout);
        displayPanel.setBackground(BACKGROUND_COLOR);

        JComponent textDisplay = createTextualCallGraph(classes);
        displayPanel.add(textDisplay, "TEXT");

        JComponent treeDisplay = createTreeCallGraph(classes);
        displayPanel.add(treeDisplay, "TREE");

        JComponent graphDisplay = new CallGraphPanel(classes, allMethodsMap);
        displayPanel.add(graphDisplay, "GRAPH");

        JComponent dendrogramDisplay = createDendrogramPanel(classes);
        displayPanel.add(dendrogramDisplay, "DENDRO");

        textButton.addActionListener(e -> cardLayout.show(displayPanel, "TEXT"));
        treeButton.addActionListener(e -> cardLayout.show(displayPanel, "TREE"));
        graphButton.addActionListener(e -> cardLayout.show(displayPanel, "GRAPH"));
        dendrogramButton.addActionListener(e -> cardLayout.show(displayPanel, "DENDRO"));

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createTextualCallGraph(List<ClassInfo> classes) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setMargin(new Insets(15, 15, 15, 15));
        textArea.setBackground(CARD_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setLineWrap(false);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════\n");
        sb.append("                    GRAPHE D'APPEL DES MÉTHODES\n");
        sb.append("═══════════════════════════════════════════════════════════════════════\n\n");

        if (this.allMethodsMap == null || this.allMethodsMap.isEmpty() || listAllJavaFiles.isEmpty()) {
            sb.append("ERREUR: Analyse du graphe d'appel indisponible.\n\n");
            sb.append("Raisons possibles:\n");
            sb.append("  - Échec de la résolution des liens JDT\n");
            sb.append("  - Configuration incorrecte du classpath\n");
            sb.append("  - Vérifiez les chemins du JRE dans SourceParser.java\n");
        } else {
            int totalCalls = 0;
            int internalCalls = 0;
            int externalCalls = 0;

            for (ClassInfo ci : classes) {
                sb.append("─────────────────────────────────────────────────────────────────────\n");
                sb.append("CLASSE: ").append(ci.getName()).append("\n");
                sb.append("Package: ").append(ci.getPackageName() != null ? ci.getPackageName() : "(default)").append("\n");
                sb.append("─────────────────────────────────────────────────────────────────────\n\n");

                for (MethodInfo mi : ci.getMethods()) {
                    sb.append("  >> ").append(mi.getName()).append("()\n");

                    if (!mi.getCalledMethodsNames().isEmpty()) {
                        sb.append("     Appelle:\n");
                        for (String calledName : mi.getCalledMethodsNames()) {
                            totalCalls++;
                            boolean isInternal = allMethodsMap.containsKey(calledName);
                            if (isInternal) {
                                internalCalls++;
                                sb.append("       [INT] ");
                            } else {
                                externalCalls++;
                                sb.append("       [EXT] ");
                            }
                            sb.append(calledName).append("\n");
                        }
                    } else {
                        sb.append("     (Aucun appel détecté)\n");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }

            sb.append("═══════════════════════════════════════════════════════════════════════\n");
            sb.append("STATISTIQUES DU GRAPHE D'APPEL\n");
            sb.append("═══════════════════════════════════════════════════════════════════════\n");
            sb.append("Total d'appels détectés: ").append(totalCalls).append("\n");
            sb.append("Appels internes (projet): ").append(internalCalls).append("\n");
            sb.append("Appels externes (API/lib): ").append(externalCalls).append("\n");
            sb.append("\n[INT] = Méthode interne au projet\n");
            sb.append("[EXT] = Méthode externe (API Java, bibliothèques)\n");
        }

        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JComponent createTreeCallGraph(List<ClassInfo> classes) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Graphe d'Appel - Projet Java");

        if (this.allMethodsMap == null || this.allMethodsMap.isEmpty() || listAllJavaFiles.isEmpty()) {
            DefaultMutableTreeNode errorNode = new DefaultMutableTreeNode(
                    "ERREUR: Analyse indisponible - Vérifiez la configuration JDT"
            );
            root.add(errorNode);
        } else {
            for (ClassInfo ci : classes) {
                String classLabel = ci.getName() + " (" + ci.getMethods().size() + " méthodes)";
                DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(classLabel);

                for (MethodInfo mi : ci.getMethods()) {
                    String methodLabel = mi.getName() + "() - " + mi.getCalledMethodsNames().size() + " appels";
                    DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(methodLabel);

                    if (!mi.getCalledMethodsNames().isEmpty()) {
                        for (String calledName : mi.getCalledMethodsNames()) {
                            boolean isInternal = allMethodsMap.containsKey(calledName);
                            String prefix = isInternal ? "[Interne] " : "[Externe] ";
                            String callLabel = prefix + calledName;
                            DefaultMutableTreeNode calledNode = new DefaultMutableTreeNode(callLabel);
                            methodNode.add(calledNode);
                        }
                    } else {
                        methodNode.add(new DefaultMutableTreeNode("(Aucun appel)"));
                    }

                    classNode.add(methodNode);
                }

                root.add(classNode);
            }
        }

        JTree tree = new JTree(root);
        tree.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tree.setShowsRootHandles(true);
        tree.setRootVisible(true);
        tree.setRowHeight(24);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setBackgroundNonSelectionColor(CARD_COLOR);
        renderer.setBackgroundSelectionColor(PRIMARY_COLOR);
        renderer.setBorderSelectionColor(PRIMARY_COLOR);
        renderer.setTextSelectionColor(Color.WHITE);
        tree.setCellRenderer(renderer);

        for (int i = 0; i < 3 && i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        infoPanel.setBackground(new Color(236, 240, 241));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel legendLabel = new JLabel("Légende: [Interne] = Méthode du projet | [Externe] = API/Bibliothèque");
        legendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        legendLabel.setForeground(LIGHT_TEXT);
        infoPanel.add(legendLabel);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDendrogramPanel(List<ClassInfo> classes) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        if (classes == null || classes.isEmpty()) {
            panel.add(new JLabel("Aucune analyse disponible. Lancez l'analyse d'abord."), BorderLayout.CENTER);
            return panel;
        }
        try {
            analyseurdecode.processor.HierarchicalClusteringProcessor processor = new analyseurdecode.processor.HierarchicalClusteringProcessor(classes);
            analyseurdecode.processor.DendrogramNode root = processor.cluster();
            if (root == null) {
                panel.add(new JLabel("Impossible de générer le dendrogramme."), BorderLayout.CENTER);
            } else {
                JScrollPane scrollPane = new JScrollPane(new analyseurdecode.ui.DendrogramPanel(root));
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
                panel.add(scrollPane, BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            panel.add(new JLabel("Erreur lors du calcul du dendrogramme : " + ex.getMessage()), BorderLayout.CENTER);
        }
        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erreur",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AnalyseurGUI gui = new AnalyseurGUI();
            gui.setVisible(true);
        });
    }
}
