package analyseurdecode.ui;

import javax.swing.*;
import java.awt.*;


public class AnalyseurLauncher {

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(230, 126, 34);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> showModeSelectionDialog());
    }

    private static void showModeSelectionDialog() {
        JDialog dialog = new JDialog((Frame) null, "Analyseur de Code Java", true);
        dialog.setSize(1600, 800);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // En-tête
        JPanel headerPanel = createHeader();

        // Zone de choix
        JPanel choicePanel = createChoicePanel(dialog);

        // Pied de page
        JPanel footerPanel = createFooter();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(choicePanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private static JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JLabel titleLabel = new JLabel("Analyseur de Code Java");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choisissez votre mode d'analyse");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitleLabel);

        return panel;
    }

    private static JPanel createChoicePanel(JDialog dialog) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 30, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Carte Mode Standard
        JPanel standardCard = createModeCard(
                "Mode Standard",
                "Utilise JDT Eclipse",
                new String[]{
                        " Graphe d'appel des méthodes",
                        " Analyse de couplage",
                        " Clustering hiérarchique",
                        " Identification de modules",
                        " Dendrogramme"
                },
                PRIMARY_COLOR,
                () -> {
                    dialog.dispose();
                    launchStandardMode();
                }
        );

        // Carte Mode Spoon
        JPanel spoonCard = createModeCard(
                "Mode Spoon",
                "Utilise Spoon Framework",
                new String[]{
                        " Analyse avec Spoon",
                        " Parser moderne et performant",
                        " Mêmes fonctionnalités",
                        " API plus simple",
                        " Nécessite dépendance Maven"
                },
                ACCENT_COLOR,
                () -> {
                    dialog.dispose();
                    launchSpoonMode();
                }
        );

        panel.add(standardCard);
        panel.add(spoonCard);

        return panel;
    }

    private static JPanel createModeCard(String title, String subtitle,
                                         String[] features, Color themeColor,
                                         Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeColor, 3),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // En-tête de la carte
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(themeColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        // Liste des fonctionnalités
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setBackground(CARD_COLOR);
        featuresPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            featureLabel.setForeground(TEXT_COLOR);
            featureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            featuresPanel.add(featureLabel);
            featuresPanel.add(Box.createVerticalStrut(8));
        }

        // Bouton de lancement
        JButton launchButton = new JButton("Lancer");
        launchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        launchButton.setBackground(themeColor);
        launchButton.setForeground(Color.WHITE);
        launchButton.setFocusPainted(false);
        launchButton.setBorderPainted(false);
        launchButton.setOpaque(true);
        launchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        launchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        launchButton.setMaximumSize(new Dimension(200, 40));

        launchButton.addActionListener(e -> action.run());

        launchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                launchButton.setBackground(themeColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                launchButton.setBackground(themeColor);
            }
        });

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(featuresPanel, BorderLayout.CENTER);
        card.add(launchButton, BorderLayout.SOUTH);

        return card;
    }

    private static JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel infoLabel = new JLabel(
                "<html><center> <b>Conseil:</b> Utilisez le Mode Standard si vous n'avez pas configuré Spoon<br>" +
                        "Les deux modes offrent les mêmes fonctionnalités d'analyse</center></html>"
        );
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(infoLabel);

        return panel;
    }

    private static void launchStandardMode() {
        System.out.println("=== Lancement du Mode Standard (JDT Eclipse) ===");
        SwingUtilities.invokeLater(() -> {
            AnalyseurJdtGUI gui = new AnalyseurJdtGUI();
            gui.setVisible(true);
        });
    }

    private static void launchSpoonMode() {
        System.out.println("=== Lancement du Mode Spoon ===");

        // Vérifier si Spoon est disponible
        try {
            Class.forName("spoon.Launcher");

            SwingUtilities.invokeLater(() -> {
                AnalyseurSpoonGUI gui = new AnalyseurSpoonGUI();
                gui.setVisible(true);
            });

        } catch (ClassNotFoundException e) {
            showSpoonNotFoundDialog();
        }
    }

    private static void showSpoonNotFoundDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(" Spoon n'est pas disponible");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(WARNING_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea messageArea = new JTextArea(
                "Spoon n'a pas été trouvé dans le classpath.\n\n" +
                        "Pour utiliser le Mode Spoon, ajoutez cette dépendance Maven :\n\n" +
                        "<dependency>\n" +
                        "    <groupId>fr.inria.gforge.spoon</groupId>\n" +
                        "    <artifactId>spoon-core</artifactId>\n" +
                        "    <version>10.4.2</version>\n" +
                        "</dependency>\n\n" +
                        "Ou téléchargez le JAR depuis:\n" +
                        "https://github.com/INRIA/spoon"
        );
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        messageArea.setEditable(false);
        messageArea.setBackground(new Color(236, 240, 241));
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(messageArea);

        int choice = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Dépendance manquante",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.OK_OPTION) {
            // Relancer le selecteur
            showModeSelectionDialog();
        }
    }
}
