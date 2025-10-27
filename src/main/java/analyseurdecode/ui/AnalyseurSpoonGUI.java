package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import analyseurdecode.parser.SpoonSourceParser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.*;
import java.util.List;
import analyseurdecode.model.Module;
import analyseurdecode.processor.DendrogramNode;
import analyseurdecode.processor.HierarchicalClusteringProcessor;
import analyseurdecode.processor.ModuleIdentifier;

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

    private List<ClassInfo> analyzedClasses;
    private Map<String, Map<String, Double>> couplingMatrix;
    private int totalRelations;

    public AnalyseurSpoonGUI() {
        setTitle("Analyseur de Code Java - Mode Spoon");
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

        JLabel spoonLabel = new JLabel("Mode Spoon - Analyse de Couplage entre Classes");
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

        JLabel titleLabel = new JLabel("Analyseur Spoon");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Analyse de Couplage entre Classes");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(LIGHT_TEXT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setText(
                "Bienvenue dans l'Analyseur de Couplage Spoon!\n\n" +
                        "Cette application analyse le couplage entre les classes Java.\n\n" +
                        "Métrique de Couplage :\n" +
                        "   Couplage(A,B) = Nombre de relations entre méthodes de A et B\n" +
                        "                   ──────────────────────────────────────────\n" +
                        "                   Nombre total de relations dans l'application\n\n" +
                        "   Où une relation = un appel entre A.mi() et B.mj()\n\n" +
                        "Fonctionnalités :\n" +
                        "   - Extraction automatique des classes et méthodes\n" +
                        "   - Construction du graphe d'appel\n" +
                        "   - Calcul de la matrice de couplage\n" +
                        "   - Génération du graphe pondéré\n" +
                        "   - Identification des classes fortement couplées\n\n" +
                        "Instructions :\n\n" +
                        "1. Sélectionnez un dossier contenant des fichiers Java\n\n" +
                        "2. Définissez le nombre minimum de méthodes (filtrage)\n\n" +
                        "3. Cliquez sur 'Lancer l'Analyse'\n\n" +
                        "4. Consultez les résultats dans les onglets :\n" +
                        "   - Résumé : statistiques globales\n" +
                        "   - Matrice de Couplage : valeurs détaillées\n" +
                        "   - Graphe : visualisation du couplage\n" +
                        "   - Top Couplages : classement des couplages"
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

        final int minMethods = X;
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Démarrage de l'analyse...");
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
                analyzeButton.setEnabled(false);

                publish("Parsing du code source avec Spoon...");
                SpoonSourceParser parser = new SpoonSourceParser();
                analyzedClasses = parser.parseDirectory(folderPath);

                publish("Filtrage des classes (min " + minMethods + " méthodes)...");
                analyzedClasses = filterClasses(analyzedClasses, minMethods);

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

        Map<String, Map<String, Integer>> relationCounts = new HashMap<>();

        for (ClassInfo classA : analyzedClasses) {
            String classAName = classA.getName();
            relationCounts.putIfAbsent(classAName, new HashMap<>());

            for (MethodInfo method : classA.getMethods()) {
                for (String calledMethodName : method.getCalledMethodsNames()) {
                    String classBName = findClassForMethod(calledMethodName);
                    if (classBName != null && !classBName.equals(classAName)) {
                        relationCounts.get(classAName).merge(classBName, 1, Integer::sum);
                        totalRelations++;
                    }
                }
            }
        }

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
        while (tabbedPane.getTabCount() > 1) {
            tabbedPane.removeTabAt(1);
        }

        tabbedPane.addTab("Résumé", createSummaryPanel());
        tabbedPane.addTab("Matrice de Couplage", createCouplingMatrixPanel());
        tabbedPane.addTab("Graphe de Couplage", createCouplingGraphPanel());
        tabbedPane.addTab("Top Couplages", createTopCouplingsPanel());
        // Nouvel onglet Dendrogramme
        tabbedPane.addTab("Dendrogramme", createDendrogramTabPanel(analyzedClasses));

        tabbedPane.setSelectedIndex(1);
    }

    private JPanel createSummaryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        gridPanel.setBackground(BACKGROUND_COLOR);

        int totalMethods = analyzedClasses.stream()
                .mapToInt(c -> c.getMethods().size())
                .sum();

        int totalAttributes = analyzedClasses.stream()
                .mapToInt(c -> c.getAttributes().size())
                .sum();

        double avgMethodsPerClass = analyzedClasses.isEmpty() ? 0 :
                (double) totalMethods / analyzedClasses.size();

        gridPanel.add(createStatCard("Classes analysées",
                String.valueOf(analyzedClasses.size()),
                "Nombre total de classes"));
        gridPanel.add(createStatCard("Méthodes totales",
                String.valueOf(totalMethods),
                "Nombre total de méthodes"));
        gridPanel.add(createStatCard("Relations totales",
                String.valueOf(totalRelations),
                "Appels entre méthodes"));
        gridPanel.add(createStatCard("Attributs totaux",
                String.valueOf(totalAttributes),
                "Nombre total d'attributs"));
        gridPanel.add(createStatCard("Moy. Méthodes/Classe",
                String.format("%.2f", avgMethodsPerClass),
                "Moyenne de méthodes"));
        gridPanel.add(createStatCard("Couplages détectés",
                String.valueOf(couplingMatrix.values().stream()
                        .mapToInt(Map::size).sum()),
                "Paires de classes couplées"));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

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

    private JPanel createCouplingMatrixPanel() {
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

        JToggleButton tableButton = new JToggleButton("Tableau");
        JToggleButton textButton = new JToggleButton("Textuel");

        ButtonGroup group = new ButtonGroup();
        group.add(tableButton);
        group.add(textButton);
        tableButton.setSelected(true);

        for (JToggleButton btn : new JToggleButton[]{tableButton, textButton}) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        controlPanel.add(modeLabel);
        controlPanel.add(tableButton);
        controlPanel.add(textButton);

        CardLayout cardLayout = new CardLayout();
        JPanel displayPanel = new JPanel(cardLayout);
        displayPanel.setBackground(BACKGROUND_COLOR);

        JComponent tableDisplay = createMatrixTable();
        displayPanel.add(tableDisplay, "TABLE");

        JComponent textDisplay = createMatrixText();
        displayPanel.add(textDisplay, "TEXT");

        tableButton.addActionListener(e -> cardLayout.show(displayPanel, "TABLE"));
        textButton.addActionListener(e -> cardLayout.show(displayPanel, "TEXT"));

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createMatrixTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = new String[analyzedClasses.size() + 1];
        columns[0] = "Classe";
        for (int i = 0; i < analyzedClasses.size(); i++) {
            columns[i + 1] = analyzedClasses.get(i).getName();
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (ClassInfo classA : analyzedClasses) {
            Object[] row = new Object[analyzedClasses.size() + 1];
            row[0] = classA.getName();

            for (int i = 0; i < analyzedClasses.size(); i++) {
                ClassInfo classB = analyzedClasses.get(i);
                if (classA.getName().equals(classB.getName())) {
                    row[i + 1] = "X";
                } else {
                    Double coupling = getCoupling(classA.getName(), classB.getName());
                    row[i + 1] = coupling > 0 ? coupling : 0.0;
                }
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(35);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(52, 152, 219, 50));
        table.setSelectionForeground(TEXT_COLOR);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        for (int i = 1; i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(100);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);

                if (column == 0) {
                    setBackground(new Color(236, 240, 241));
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setForeground(TEXT_COLOR);
                    setText(value != null ? value.toString() : "");
                } else {
                    if ("X".equals(value)) {
                        setBackground(new Color(230, 230, 230));
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        setForeground(SECONDARY_COLOR);
                        setText("X");
                    } else if (value instanceof Double) {
                        double coupling = (Double) value;
                        setText(String.format("%.4f", coupling));
                        if (coupling > 0) {
                            setBackground(PRIMARY_COLOR);
                            setForeground(Color.WHITE);
                        } else {
                            setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                        }
                        setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    } else {
                        setText("0.0000");
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                    }
                }

                if (isSelected) {
                    setBackground(new Color(52, 152, 219));
                    setForeground(Color.WHITE);
                }

                return this;
            }
        };

        for (int i = 0; i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JLabel infoLabel = new JLabel("Les valeurs représentent Couplage(Ligne, Colonne) = Relations(A→B) / Total Relations");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(LIGHT_TEXT);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JComponent createMatrixText() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(CARD_COLOR);
        textArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("              MATRICE DE COUPLAGE ENTRE CLASSES\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        for (ClassInfo classA : analyzedClasses) {
            boolean hasConnections = false;
            StringBuilder classSb = new StringBuilder();
            classSb.append("Classe: ").append(classA.getName()).append("\n");

            for (ClassInfo classB : analyzedClasses) {
                if (!classA.getName().equals(classB.getName())) {
                    double coupling = getCoupling(classA.getName(), classB.getName());
                    if (coupling > 0) {
                        hasConnections = true;
                        classSb.append(String.format("  → %s : %.6f\n", classB.getName(), coupling));
                    }
                }
            }

            if (hasConnections) {
                sb.append(classSb).append("\n");
            }
        }

        sb.append("═══════════════════════════════════════════════════════════════\n");

        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCouplingGraphPanel() {
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
        JToggleButton graphButton = new JToggleButton("Graphique");
       // JToggleButton dotButton = new JToggleButton("DOT");

        ButtonGroup group = new ButtonGroup();
        group.add(textButton);
        group.add(graphButton);
        //group.add(dotButton);
        textButton.setSelected(true);

        for (JToggleButton btn : new JToggleButton[]{textButton, graphButton}) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        controlPanel.add(modeLabel);
        controlPanel.add(textButton);
        controlPanel.add(graphButton);
        //controlPanel.add(dotButton);

        CardLayout cardLayout = new CardLayout();
        JPanel displayPanel = new JPanel(cardLayout);
        displayPanel.setBackground(BACKGROUND_COLOR);

        JComponent textDisplay = createTextualCouplingGraph();
        displayPanel.add(textDisplay, "TEXT");

        JComponent graphDisplay = new CouplingGraphVisualPanel(analyzedClasses, couplingMatrix);
        displayPanel.add(graphDisplay, "GRAPH");

        JComponent dotDisplay = createDotCouplingGraph();
        displayPanel.add(dotDisplay, "DOT");

        textButton.addActionListener(e -> cardLayout.show(displayPanel, "TEXT"));
        graphButton.addActionListener(e -> cardLayout.show(displayPanel, "GRAPH"));
        //dotButton.addActionListener(e -> cardLayout.show(displayPanel, "DOT"));

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createTextualCouplingGraph() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(new Color(44, 62, 80));

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════\n");
        sb.append("                GRAPHE DE COUPLAGE ENTRE LES CLASSES\n");
        sb.append("═══════════════════════════════════════════════════════════════════════\n\n");

        boolean found = false;
        for (String classA : couplingMatrix.keySet()) {
            for (String classB : couplingMatrix.get(classA).keySet()) {
                double coupling = couplingMatrix.get(classA).get(classB);
                if (coupling > 0) {
                    found = true;
                    sb.append(classA)
                            .append(" <--> ")
                            .append(classB)
                            .append(" : Couplage = ")
                            .append(String.format("%.4f", coupling))
                            .append("\n");
                }
            }
        }

        if (!found) {
            sb.append("Aucun couplage détecté entre les classes du projet.\n");
        }

        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createDotCouplingGraph() {
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
        sb.append("  - Rouge (épais) : Couplage fort (> 0.1)\n");
        sb.append("  - Orange (moyen) : Couplage moyen (0.05 - 0.1)\n");
        sb.append("  - Gris (fin) : Couplage faible (< 0.05)\n");
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

    // Classe interne pour l'affichage graphique du couplage
    private class CouplingGraphVisualPanel extends JPanel {
        private List<ClassInfo> classes;
        private Map<String, Map<String, Double>> coupling;
        private Map<String, Point> nodePositions;

        public CouplingGraphVisualPanel(List<ClassInfo> classes, Map<String, Map<String, Double>> coupling) {
            this.classes = classes;
            this.coupling = coupling;
            this.nodePositions = new HashMap<>();
            setBackground(Color.WHITE);
            calculateNodePositions();
        }

        private void calculateNodePositions() {
            int n = classes.size();
            int centerX = 400;
            int centerY = 300;
            int radius = Math.min(centerX, centerY) - 100;

            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n - Math.PI / 2;
                int x = centerX + (int) (radius * Math.cos(angle));
                int y = centerY + (int) (radius * Math.sin(angle));
                nodePositions.put(classes.get(i).getName(), new Point(x, y));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dessiner les arêtes (couplages)
            for (String classA : coupling.keySet()) {
                Point posA = nodePositions.get(classA);
                if (posA == null) continue;

                for (String classB : coupling.get(classA).keySet()) {
                    Point posB = nodePositions.get(classB);
                    if (posB == null) continue;

                    double couplingValue = coupling.get(classA).get(classB);
                    if (couplingValue > 0) {
                        // Déterminer la couleur et l'épaisseur
                        Color edgeColor;
                        int thickness;
                        if (couplingValue > 0.1) {
                            edgeColor = new Color(231, 76, 60); // Rouge
                            thickness = 3;
                        } else if (couplingValue > 0.05) {
                            edgeColor = new Color(230, 126, 34); // Orange
                            thickness = 2;
                        } else {
                            edgeColor = new Color(149, 165, 166); // Gris
                            thickness = 1;
                        }

                        g2d.setColor(edgeColor);
                        g2d.setStroke(new BasicStroke(thickness));
                        g2d.drawLine(posA.x, posA.y, posB.x, posB.y);

                        // Dessiner le poids au milieu de l'arête
                        int midX = (posA.x + posB.x) / 2;
                        int midY = (posA.y + posB.y) / 2;
                        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                        g2d.setColor(TEXT_COLOR);
                        String label = String.format("%.3f", couplingValue);
                        g2d.drawString(label, midX - 15, midY - 5);
                    }
                }
            }

            // Dessiner les nœuds (classes)
            for (ClassInfo cls : classes) {
                Point pos = nodePositions.get(cls.getName());
                if (pos == null) continue;

                int nodeSize = 60;
                int x = pos.x - nodeSize / 2;
                int y = pos.y - nodeSize / 2;

                // Ombre
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval(x + 3, y + 3, nodeSize, nodeSize);

                // Nœud
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillOval(x, y, nodeSize, nodeSize);

                // Bordure
                g2d.setColor(PRIMARY_COLOR.darker());
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x, y, nodeSize, nodeSize);

                // Nom de la classe
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                String name = cls.getName();
                if (name.length() > 8) {
                    name = name.substring(0, 7) + "...";
                }
                int textWidth = fm.stringWidth(name);
                g2d.drawString(name, pos.x - textWidth / 2, pos.y + 5);

                // Nombre de méthodes
                g2d.setFont(new Font("Arial", Font.PLAIN, 8));
                String methods = cls.getMethods().size() + " m";
                int methodsWidth = g2d.getFontMetrics().stringWidth(methods);
                g2d.drawString(methods, pos.x - methodsWidth / 2, pos.y + 15);
            }

            // Légende
            drawLegend(g2d);
        }

        private void drawLegend(Graphics2D g2d) {
            int x = 20;
            int y = getHeight() - 100;

            g2d.setColor(new Color(255, 255, 255, 230));
            g2d.fillRoundRect(x - 5, y - 5, 180, 85, 10, 10);
            g2d.setColor(new Color(189, 195, 199));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(x - 5, y - 5, 180, 85, 10, 10);

            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.setColor(TEXT_COLOR);
            g2d.drawString("Légende - Couplage", x, y + 10);

            // Fort
            g2d.setColor(new Color(231, 76, 60));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(x, y + 25, x + 30, y + 25);
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("Fort (> 0.1)", x + 35, y + 28);

            // Moyen
            g2d.setColor(new Color(230, 126, 34));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x, y + 45, x + 30, y + 45);
            g2d.setColor(TEXT_COLOR);
            g2d.drawString("Moyen (0.05-0.1)", x + 35, y + 48);

            // Faible
            g2d.setColor(new Color(149, 165, 166));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(x, y + 65, x + 30, y + 65);
            g2d.setColor(TEXT_COLOR);
            g2d.drawString("Faible (< 0.05)", x + 35, y + 68);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 600);
        }
    }

    private JPanel createTopCouplingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<CouplingEntry> couplings = new ArrayList<>();
        for (String classA : couplingMatrix.keySet()) {
            for (String classB : couplingMatrix.get(classA).keySet()) {
                double value = couplingMatrix.get(classA).get(classB);
                if (value > 0) {
                    couplings.add(new CouplingEntry(classA, classB, value));
                }
            }
        }

        couplings.sort((a, b) -> Double.compare(b.coupling, a.coupling));

        String[] columns = {"Rang", "Classe A", "Classe B", "Couplage", "Niveau"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

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
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(236, 240, 241));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel infoLabel = new JLabel(String.format("%d couplages détectés - Total relations: %d",
                couplings.size(), totalRelations));
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoLabel.setForeground(TEXT_COLOR);

        infoPanel.add(infoLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    private double getCoupling(String classA, String classB) {
        if (couplingMatrix.containsKey(classA) && couplingMatrix.get(classA).containsKey(classB)) {
            return couplingMatrix.get(classA).get(classB);
        }
        return 0.0;
    }

    // ===== Dendrogramme & Modules (via clustering hiérarchique) =====
    private JPanel createDendrogramTabPanel(List<ClassInfo> classes) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(CARD_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel modeLabel = new JLabel("Affichage :");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        modeLabel.setForeground(TEXT_COLOR);

        JToggleButton graphButton = new JToggleButton("Graphe");
        JToggleButton modulesButton = new JToggleButton("Modules avec CP");
        ButtonGroup group = new ButtonGroup();
        group.add(graphButton);
        group.add(modulesButton);
        graphButton.setSelected(true);

        for (JToggleButton btn : new JToggleButton[]{graphButton, modulesButton}) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        controlPanel.add(modeLabel);
        controlPanel.add(graphButton);
        controlPanel.add(modulesButton);

        CardLayout cardLayout = new CardLayout();
        JPanel displayPanel = new JPanel(cardLayout);
        displayPanel.setBackground(BACKGROUND_COLOR);

        JComponent dendroView = createDendrogramPanel(classes);
        displayPanel.add(dendroView, "GRAPH");

        JComponent modulesView = createModulesPanel(classes);
        displayPanel.add(modulesView, "MODULES");

        graphButton.addActionListener(e -> cardLayout.show(displayPanel, "GRAPH"));
        modulesButton.addActionListener(e -> cardLayout.show(displayPanel, "MODULES"));

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createDendrogramPanel(List<ClassInfo> classes) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        if (classes == null || classes.isEmpty()) {
            panel.add(new JLabel("Aucune analyse disponible. Lancez l'analyse d'abord."), BorderLayout.CENTER);
            return panel;
        }
        try {
            HierarchicalClusteringProcessor processor = new HierarchicalClusteringProcessor(classes);
            DendrogramNode root = processor.cluster();
            if (root == null) {
                panel.add(new JLabel("Impossible de générer le dendrogramme."), BorderLayout.CENTER);
            } else {
                JScrollPane scrollPane = new JScrollPane(new DendrogramPanel(root));
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
                panel.add(scrollPane, BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            panel.add(new JLabel("Erreur lors du calcul du dendrogramme : " + ex.getMessage()), BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel createModulesPanel(List<ClassInfo> classes) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        if (classes == null || classes.isEmpty()) {
            mainPanel.add(new JLabel("Aucune analyse disponible. Lancez l'analyse d'abord."), BorderLayout.CENTER);
            return mainPanel;
        }

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(CARD_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel cpLabel = new JLabel("Seuil de couplage (CP) :");
        cpLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cpLabel.setForeground(TEXT_COLOR);

        JTextField cpField = new JTextField("0.01", 8);
        cpField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton identifyButton = createStyledButton("Identifier les modules", PRIMARY_COLOR);

        controlPanel.add(cpLabel);
        controlPanel.add(cpField);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(identifyButton);

        JPanel modulesDisplayPanel = new JPanel();
        modulesDisplayPanel.setLayout(new BoxLayout(modulesDisplayPanel, BoxLayout.Y_AXIS));
        modulesDisplayPanel.setBackground(BACKGROUND_COLOR);
        modulesDisplayPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel initialMessage = new JLabel("<html><div style='text-align: center; padding: 50px;'>" +
                "<h2 style='color: #2ecc71;'>Identification de Modules</h2>" +
                "<p style='margin-top: 20px;'>Cliquez sur 'Identifier les modules' pour lancer l'analyse.</p>" +
                "<p style='margin-top: 10px; color: #7f8c8d;'>Le seuil de couplage (CP) détermine la force minimale du couplage<br>" +
                "entre les classes d'un même module.</p>" +
                "<br><b>Valeurs suggérées:</b><br>" +
                "CP = 0.01 : modules très larges (couplage faible accepté)<br>" +
                "CP = 0.05 : modules moyens<br>" +
                "CP = 0.10 : modules compacts (couplage fort requis)" +
                "</div></html>");
        initialMessage.setHorizontalAlignment(JLabel.CENTER);
        modulesDisplayPanel.add(initialMessage);

        JScrollPane scrollPane = new JScrollPane(modulesDisplayPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        identifyButton.addActionListener(e -> {
            try {
                double cp = Double.parseDouble(cpField.getText().trim());
                if (cp < 0 || cp > 1) throw new NumberFormatException();

                modulesDisplayPanel.removeAll();
                JLabel loadingLabel = new JLabel("Identification des modules en cours...");
                loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                loadingLabel.setForeground(PRIMARY_COLOR);
                loadingLabel.setHorizontalAlignment(JLabel.CENTER);
                modulesDisplayPanel.add(loadingLabel);
                modulesDisplayPanel.revalidate();
                modulesDisplayPanel.repaint();

                SwingWorker<List<Module>, Void> worker = new SwingWorker<List<Module>, Void>() {
                    @Override
                    protected List<Module> doInBackground() {
                        HierarchicalClusteringProcessor clusteringProcessor = new HierarchicalClusteringProcessor(classes);
                        DendrogramNode root = clusteringProcessor.cluster();
                        if (root == null) return Collections.emptyList();
                        ModuleIdentifier identifier = new ModuleIdentifier(classes, cp);
                        return identifier.identifyModules(root);
                    }

                    @Override
                    protected void done() {
                        try {
                            List<Module> modules = get();
                            modulesDisplayPanel.removeAll();
                            if (modules.isEmpty()) {
                                JPanel errorPanel = createErrorPanel(cp);
                                modulesDisplayPanel.add(errorPanel);
                            } else {
                                displayModulesGraphically(modulesDisplayPanel, modules, classes);
                            }
                            modulesDisplayPanel.revalidate();
                            modulesDisplayPanel.repaint();
                        } catch (Exception ex) {
                            modulesDisplayPanel.removeAll();
                            JLabel errorLabel = new JLabel("<html><div style='text-align: center; color: #e74c3c;'>" +
                                    "<h3>Erreur lors de l'identification</h3>" +
                                    "<p>" + ex.getMessage() + "</p></div></html>");
                            errorLabel.setHorizontalAlignment(JLabel.CENTER);
                            modulesDisplayPanel.add(errorLabel);
                            modulesDisplayPanel.revalidate();
                            modulesDisplayPanel.repaint();
                        }
                    }
                };
                worker.execute();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel,
                        "Le seuil de couplage doit être un nombre entre 0 et 1.",
                        "Erreur de saisie",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private void displayModulesGraphically(JPanel container, List<Module> modules, List<ClassInfo> allClasses) {
        JPanel statsPanel = createModuleStatsPanel(modules, allClasses);
        container.add(statsPanel);
        container.add(Box.createVerticalStrut(20));

        Color[] moduleColors = {
                new Color(52, 152, 219),
                new Color(46, 204, 113),
                new Color(155, 89, 182),
                new Color(241, 196, 15),
                new Color(230, 126, 34),
                new Color(231, 76, 60),
                new Color(26, 188, 156),
                new Color(52, 73, 94)
        };

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            Color moduleColor = moduleColors[i % moduleColors.length];
            JPanel moduleCard = createModuleCard(module, i + 1, moduleColor, allClasses);
            container.add(moduleCard);
            container.add(Box.createVerticalStrut(15));
        }
    }

    private JPanel createModuleCard(Module module, int moduleNumber, Color themeColor, List<ClassInfo> allClasses) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeColor, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(themeColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("MODULE " + moduleNumber);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel couplingLabel = new JLabel(String.format("Couplage: %.4f", module.getCouplingScore()));
        couplingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        couplingLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(couplingLabel, BorderLayout.EAST);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(CARD_COLOR);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel classCountLabel = new JLabel("Classes (" + module.getClassNames().size() + "):");
        classCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        classCountLabel.setForeground(TEXT_COLOR);
        classCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(classCountLabel);
        bodyPanel.add(Box.createVerticalStrut(8));

        JPanel classesGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        classesGrid.setBackground(CARD_COLOR);
        classesGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String className : module.getClassNames()) {
            JPanel classBox = createClassBox(className, themeColor, allClasses);
            classesGrid.add(classBox);
        }

        bodyPanel.add(classesGrid);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(bodyPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createClassBox(String className, Color themeColor, List<ClassInfo> allClasses) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(236, 240, 241));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel nameLabel = new JLabel(className);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(themeColor.darker());
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ClassInfo classInfo = allClasses.stream()
                .filter(c -> c.getName().equals(className))
                .findFirst()
                .orElse(null);

        if (classInfo != null) {
            JLabel methodsLabel = new JLabel(classInfo.getMethods().size() + " méthodes");
            methodsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            methodsLabel.setForeground(LIGHT_TEXT);
            methodsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel attrsLabel = new JLabel(classInfo.getAttributes().size() + " attributs");
            attrsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            attrsLabel.setForeground(LIGHT_TEXT);
            attrsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            box.add(nameLabel);
            box.add(Box.createVerticalStrut(5));
            box.add(methodsLabel);
            box.add(attrsLabel);
        } else {
            box.add(nameLabel);
        }

        return box;
    }

    private JPanel createModuleStatsPanel(List<Module> modules, List<ClassInfo> allClasses) {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        Set<String> coveredClasses = new HashSet<>();
        for (Module module : modules) {
            coveredClasses.addAll(module.getClassNames());
        }

        double avgModuleSize = modules.stream().mapToInt(Module::size).average().orElse(0.0);
        double avgCoupling = modules.stream().mapToDouble(Module::getCouplingScore).average().orElse(0.0);
        double coverage = allClasses.isEmpty() ? 0.0 : (coveredClasses.size() * 100.0) / allClasses.size();

        statsPanel.add(createStatCard("Modules identifiés", String.valueOf(modules.size()), "Nombre total de modules"));
        statsPanel.add(createStatCard("Couverture", String.format("%.1f%%", coverage), "Classes dans des modules"));
        statsPanel.add(createStatCard("Taille moyenne", String.format("%.1f", avgModuleSize), "Classes par module"));
        statsPanel.add(createStatCard("Couplage moyen", String.format("%.4f", avgCoupling), "Force de cohésion"));

        return statsPanel;
    }

    private JPanel createErrorPanel(double cp) {
        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        errorPanel.setBackground(BACKGROUND_COLOR);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("Aucun module identifié");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(231, 76, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center; margin-top: 20px;'>" +
                "Aucun module ne satisfait les contraintes avec CP = " + cp + "<br><br>" +
                "<b>Suggestions:</b><br>" +
                "• Réduire le seuil de couplage (CP)<br>" +
                "• Vérifier que les classes ont des relations d'appel<br>" +
                "• Essayer CP = 0.001 pour des modules plus larges" +
                "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorPanel.add(titleLabel);
        errorPanel.add(Box.createVerticalStrut(15));
        errorPanel.add(messageLabel);
        return errorPanel;
    }
    // ===== Fin Dendrogramme & Modules =====

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

