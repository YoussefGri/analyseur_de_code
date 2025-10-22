package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import analyseurdecode.parser.SpoonSourceParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

//Interface graphique pour l'analyseur utilisant Spoon avec calcul de couplage

public class AnalyseurSpoonGUI extends JFrame {

    private static final Color PRIMARY_COLOR = new Color(46, 204, 113);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
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

    // Données d'analyse
    private List<ClassInfo> analyzedClasses;
    private Map<String, Map<String, Double>> couplingMatrix;
    private int totalRelations;

    public AnalyseurSpoonGUI() {
        setTitle("Analyseur de Code Java - Mode Spoon avec Couplage");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel controlPanel = createControlPanel();
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(CARD_COLOR);

        JPanel statusPanel = createStatusPanel();

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
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

        JPanel bannerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bannerPanel.setBackground(new Color(231, 250, 241));
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel spoonLabel = new JLabel("🥄 Mode Spoon - Analyse de Couplage entre Classes");
        spoonLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        spoonLabel.setForeground(PRIMARY_COLOR.darker());
        bannerPanel.add(spoonLabel);

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

        analyzeButton = createStyledButton("Lancer l'Analyse", PRIMARY_COLOR);
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        analyzeButton.addActionListener(e -> runAnalysisSpoon());

        paramsPanel.add(xLabel);
        paramsPanel.add(xField);
        paramsPanel.add(Box.createHorizontalStrut(20));
        paramsPanel.add(analyzeButton);

        panel.add(bannerPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(folderPanel);
        panel.add(paramsPanel);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        statusLabel = new JLabel("Prêt - Mode Spoon");
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

        JLabel titleLabel = new JLabel("🥄 Analyseur Spoon");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Analyse de Couplage entre Classes");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(LIGHT_TEXT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setText(
                "Bienvenue dans l'Analyseur de Couplage !\n\n" +
                        "Cette application analyse le couplage entre les classes Java.\n\n" +
                        "📊 Métrique de Couplage :\n" +
                        "   Couplage(A,B) = Nombre de relations entre méthodes de A et B\n" +
                        "                   ──────────────────────────────────────────\n" +
                        "                   Nombre total de relations dans l'application\n\n" +
                        "   Où une relation = un appel entre A.mi() et B.mj()\n\n" +
                        "✨ Fonctionnalités :\n" +
                        "   • Extraction automatique des classes et méthodes\n" +
                        "   • Construction du graphe d'appel\n" +
                        "   • Calcul de la matrice de couplage\n" +
                        "   • Génération du graphe pondéré\n" +
                        "   • Identification des classes fortement couplées\n\n" +
                        "📋 Instructions :\n\n" +
                        "1. Sélectionnez un dossier contenant des fichiers Java\n\n" +
                        "2. Définissez le nombre minimum de méthodes (filtrage)\n\n" +
                        "3. Cliquez sur 'Lancer l'Analyse'\n\n" +
                        "4. Consultez les résultats dans les onglets :\n" +
                        "   • Résumé : statistiques globales\n" +
                        "   • Matrice de Couplage : valeurs détaillées\n" +
                        "   • Graphe : visualisation du couplage\n" +
                        "   • Classes Couplées : top couplages"
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

    private void runAnalysisSpoon() {
        String folderPath = folderField.getText().trim();

        if (folderPath.isEmpty()) {
            showError("Veuillez sélectionner un dossier source");
            return;
        }

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

        // Lancer l'analyse dans un thread séparé
        final int minMethods = X;
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Démarrage de l'analyse...");
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
                analyzeButton.setEnabled(false);

                // 1. Parser le code source
                publish("Parsing du code source avec Spoon...");
                SpoonSourceParser parser = new SpoonSourceParser();
                analyzedClasses = parser.parseDirectory(folderPath);

                // 2. Filtrer les classes
                publish("Filtrage des classes (min " + minMethods + " méthodes)...");
                analyzedClasses = filterClasses(analyzedClasses, minMethods);

                // 3. Calculer le couplage
                publish("Calcul du couplage entre classes...");
                calculateCoupling();

                publish("Analyse terminée !");
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    statusLabel.setText(msg);
                }
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                analyzeButton.setEnabled(true);

                try {
                    get();
                    displayResults();
                    statusLabel.setText("Analyse terminée - " + analyzedClasses.size() + " classes analysées");
                } catch (Exception ex) {
                    showError("Erreur lors de l'analyse : " + ex.getMessage());
                    statusLabel.setText("Erreur lors de l'analyse");
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private List<ClassInfo> filterClasses(List<ClassInfo> classes, int minMethods) {
        List<ClassInfo> filtered = new ArrayList<>();
        for (ClassInfo cls : classes) {
            if (cls.getMethods().size() >= minMethods) {
                filtered.add(cls);
            }
        }
        return filtered;
    }

    private void calculateCoupling() {
        couplingMatrix = new HashMap<>();
        totalRelations = 0;

        // Compter toutes les relations entre méthodes
        Map<String, Map<String, Integer>> relationCounts = new HashMap<>();

        for (ClassInfo classA : analyzedClasses) {
            String classAName = classA.getName();
            relationCounts.putIfAbsent(classAName, new HashMap<>());

            for (MethodInfo method : classA.getMethods()) {
                for (String calledMethodName : method.getCalledMethodsNames()) {
                    // Trouver la classe qui contient cette méthode
                    String classBName = findClassForMethod(calledMethodName);
                    if (classBName != null && !classBName.equals(classAName)) {
                        relationCounts.get(classAName).merge(classBName, 1, Integer::sum);
                        totalRelations++;
                    }
                }
            }
        }

        // Calculer les métriques de couplage
        for (String classA : relationCounts.keySet()) {
            couplingMatrix.putIfAbsent(classA, new HashMap<>());
            for (String classB : relationCounts.get(classA).keySet()) {
                int relations = relationCounts.get(classA).get(classB);
                double coupling = totalRelations > 0 ? (double) relations / totalRelations : 0.0;
                couplingMatrix.get(classA).put(classB, coupling);
            }
        }
    }

    private String findClassForMethod(String methodFullName) {
        for (ClassInfo cls : analyzedClasses) {
            for (MethodInfo method : cls.getMethods()) {
                if (method.getFullyQualifiedName() != null &&
                        method.getFullyQualifiedName().equals(methodFullName)) {
                    return cls.getName();
                }
            }
        }
        return null;
    }

    private void displayResults() {
        // Supprimer les anciens onglets (sauf l'accueil)
        while (tabbedPane.getTabCount() > 1) {
            tabbedPane.removeTabAt(1);
        }

        // Ajouter les nouveaux onglets
        tabbedPane.addTab("📊 Résumé", createSummaryPanel());
        tabbedPane.addTab("📈 Matrice de Couplage", createCouplingMatrixPanel());
        tabbedPane.addTab("🔗 Graphe de Couplage", createCouplingGraphPanel());
        tabbedPane.addTab("⭐ Top Couplages", createTopCouplingsPanel());

        // Sélectionner l'onglet résumé
        tabbedPane.setSelectedIndex(1);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea summaryArea = new JTextArea();
        summaryArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        summaryArea.setEditable(false);
        summaryArea.setBackground(CARD_COLOR);
        summaryArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("              RÉSUMÉ DE L'ANALYSE DE COUPLAGE\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        sb.append("📦 Nombre de classes analysées : ").append(analyzedClasses.size()).append("\n");

        int totalMethods = analyzedClasses.stream()
                .mapToInt(c -> c.getMethods().size())
                .sum();
        sb.append("🔧 Nombre total de méthodes : ").append(totalMethods).append("\n");

        sb.append("🔗 Nombre total de relations : ").append(totalRelations).append("\n\n");

        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append("DÉTAILS DES CLASSES\n");
        sb.append("───────────────────────────────────────────────────────────────\n\n");

        for (ClassInfo cls : analyzedClasses) {
            sb.append(String.format("▸ %s\n", cls.getName()));
            sb.append(String.format("  • Package : %s\n", cls.getPackageName()));
            sb.append(String.format("  • Méthodes : %d\n", cls.getMethods().size()));
            sb.append(String.format("  • Attributs : %d\n", cls.getAttributes().size()));
            sb.append(String.format("  • LOC : %d\n", cls.getLoc()));

            // Compter les relations sortantes
            int outgoingRelations = 0;
            for (MethodInfo method : cls.getMethods()) {
                outgoingRelations += method.getCalledMethodsNames().size();
            }
            sb.append(String.format("  • Relations sortantes : %d\n\n", outgoingRelations));
        }

        sb.append("═══════════════════════════════════════════════════════════════\n");

        summaryArea.setText(sb.toString());
        summaryArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCouplingMatrixPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Créer le modèle de table
        String[] columns = new String[analyzedClasses.size() + 1];
        columns[0] = "Classe";
        for (int i = 0; i < analyzedClasses.size(); i++) {
            columns[i + 1] = analyzedClasses.get(i).getName();
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (ClassInfo classA : analyzedClasses) {
            Object[] row = new Object[analyzedClasses.size() + 1];
            row[0] = classA.getName();

            for (int i = 0; i < analyzedClasses.size(); i++) {
                ClassInfo classB = analyzedClasses.get(i);
                if (classA.getName().equals(classB.getName())) {
                    row[i + 1] = "-";
                } else {
                    Double coupling = getCoupling(classA.getName(), classB.getName());
                    row[i + 1] = coupling > 0 ? String.format("%.4f", coupling) : "0";
                }
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Consolas", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("💡 Les valeurs représentent Couplage(Ligne, Colonne) = Relations(A→B) / Total Relations");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(LIGHT_TEXT);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCouplingGraphPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea graphArea = new JTextArea();
        graphArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        graphArea.setEditable(false);
        graphArea.setBackground(CARD_COLOR);
        graphArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("           GRAPHE DE COUPLAGE PONDÉRÉ (Format DOT)\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        sb.append("digraph CouplingGraph {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [shape=box, style=filled, fillcolor=lightblue];\n\n");

        // Ajouter les nœuds
        sb.append("    // Nœuds (Classes)\n");
        for (ClassInfo cls : analyzedClasses) {
            sb.append(String.format("    \"%s\" [label=\"%s\\n(%d méthodes)\"];\n",
                    cls.getName(), cls.getName(), cls.getMethods().size()));
        }

        sb.append("\n    // Arêtes (Relations de couplage)\n");
        for (String classA : couplingMatrix.keySet()) {
            for (String classB : couplingMatrix.get(classA).keySet()) {
                double coupling = couplingMatrix.get(classA).get(classB);
                if (coupling > 0) {
                    // Déterminer l'épaisseur et la couleur selon le couplage
                    String penwidth = coupling > 0.1 ? "3.0" : coupling > 0.05 ? "2.0" : "1.0";
                    String color = coupling > 0.1 ? "red" : coupling > 0.05 ? "orange" : "gray";

                    sb.append(String.format("    \"%s\" -> \"%s\" [label=\"%.4f\", penwidth=%s, color=%s];\n",
                            classA, classB, coupling, penwidth, color));
                }
            }
        }

        sb.append("}\n\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("Légende :\n");
        sb.append("  • Rouge (épais) : Couplage fort (> 0.1)\n");
        sb.append("  • Orange (moyen) : Couplage moyen (0.05 - 0.1)\n");
        sb.append("  • Gris (fin) : Couplage faible (< 0.05)\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");

        graphArea.setText(sb.toString());
        graphArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(graphArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton copyButton = createStyledButton("Copier le graphe DOT", SECONDARY_COLOR);
        copyButton.addActionListener(e -> {
            graphArea.selectAll();
            graphArea.copy();
            graphArea.setCaretPosition(0);
            showInfo("Graphe copié ! Collez-le dans Graphviz Online pour la visualisation.");
        });
        buttonPanel.add(copyButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTopCouplingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Collecter tous les couplages
        List<CouplingEntry> couplings = new ArrayList<>();
        for (String classA : couplingMatrix.keySet()) {
            for (String classB : couplingMatrix.get(classA).keySet()) {
                double value = couplingMatrix.get(classA).get(classB);
                if (value > 0) {
                    couplings.add(new CouplingEntry(classA, classB, value));
                }
            }
        }

        // Trier par valeur décroissante
        couplings.sort((a, b) -> Double.compare(b.coupling, a.coupling));

        // Créer la table
        String[] columns = {"Rang", "Classe A", "Classe B", "Couplage", "Niveau"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        int rank = 1;
        for (CouplingEntry entry : couplings) {
            String level = entry.coupling > 0.1 ? "Fort" :
                    entry.coupling > 0.05 ? "Moyen" : "Faible";
            model.addRow(new Object[]{
                    rank++,
                    entry.classA,
                    entry.classB,
                    String.format("%.6f", entry.coupling),
                    level
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel(String.format("📊 %d couplages détectés - Total relations: %d",
                couplings.size(), totalRelations));
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoLabel.setForeground(TEXT_COLOR);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private double getCoupling(String classA, String classB) {
        if (couplingMatrix.containsKey(classA) && couplingMatrix.get(classA).containsKey(classB)) {
            return couplingMatrix.get(classA).get(classB);
        }
        return 0.0;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erreur",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Classe interne pour représenter une entrée de couplage
     */
    private static class CouplingEntry {
        String classA;
        String classB;
        double coupling;

        CouplingEntry(String classA, String classB, double coupling) {
            this.classA = classA;
            this.classB = classB;
            this.coupling = coupling;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AnalyseurSpoonGUI gui = new AnalyseurSpoonGUI();
            gui.setVisible(true);
        });
    }
}