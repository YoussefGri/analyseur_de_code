package hai702.tp3.demo.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

import java.util.Scanner;

public class Partie {

    private ArrayList<Command> commandeRealisés;

    private ArrayList<Command> commandeAnnulées;

    private ArrayList<Bidon> bidons;

    private final double volumeCible;

    public Partie(int nbBidons, int[] capacites, double volumeCible) {
        this.volumeCible = volumeCible;
        this.commandeRealisés = new ArrayList<>();
        this.bidons = new ArrayList<>();

        for (int i = 0; i < nbBidons; i++) {
            bidons.add(new Bidon(capacites[i], 0));
        }
    }


    public ArrayList<Bidon> getBidons() {
        return bidons;
    }

    public void setBidons(ArrayList<Bidon> bidons) {
        this.bidons = bidons;
    }

    public ArrayList<Command> getCommandeRealisés() {
        return commandeRealisés;
    }

    public void setCommandeRealisés(ArrayList<Command> commandeRealisés) {
        this.commandeRealisés = commandeRealisés;
    }


    public Boolean estGagnée(){
        return bidons.get(0).getVolumeCourant() == volumeCible;
    }



    public void executeCommand(Command cmd) {
        cmd.execute();
        commandeRealisés.add(cmd);
    }

    public void undoLastCommand() {
        if (!commandeRealisés.isEmpty()) {
            
            Command lastCommand = commandeRealisés.remove(commandeRealisés.size() - 1);
            lastCommand.undo();
        }
    }

    public void jouer() {
        Scanner scanner = new Scanner(System.in);

        while (!estGagnée()) {
            System.out.println("\nÉtat actuel des bidons:");
            for (int i = 0; i < bidons.size(); i++) {
                System.out.println("Bidon " + (i+1) + ": " + bidons.get(i));
            }

            System.out.println("\nVolume cible à atteindre dans le premier bidon: " + volumeCible);
            System.out.println("\nActions disponibles:");
            System.out.println("1. Remplir un bidon");
            System.out.println("2. Vider un bidon");
            System.out.println("3. Transvaser un bidon dans un autre");
            System.out.println("4. Annuler la dernière action");

            int choix = scanner.nextInt();

            try {
                switch (choix) {
                    case 1:
                        System.out.println("Quel bidon remplir? (1-" + bidons.size() + ")");
                        int bidonARemplir = scanner.nextInt() - 1;
                        if (bidonARemplir >= 0 && bidonARemplir < bidons.size()) {
                            executeCommand(new CommandRemplir(bidons.get(bidonARemplir)));
                        }
                        break;

                    case 2:
                        System.out.println("Quel bidon vider? (1-" + bidons.size() + ")");
                        int bidonAVider = scanner.nextInt() - 1;
                        if (bidonAVider >= 0 && bidonAVider < bidons.size()) {
                            executeCommand(new CommandVider(bidons.get(bidonAVider)));
                        }
                        break;

                    case 3:
                        System.out.println("Bidon source? (1-" + bidons.size() + ")");
                        int source = scanner.nextInt() - 1;
                        System.out.println("Bidon destination? (1-" + bidons.size() + ")");
                        int destination = scanner.nextInt() - 1;
                        if (source >= 0 && source < bidons.size() &&
                                destination >= 0 && destination < bidons.size() &&
                                source != destination) {
                            executeCommand(new CommandTransverser(bidons.get(source), bidons.get(destination)));
                        }
                        break;

                    case 4:
                        undoLastCommand();
                        break;

                    default:
                        System.out.println("Action non valide");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Une erreur s'est produite: " + e.getMessage());
            }
        }

        System.out.println("\nFélicitations! Vous avez gagné!");
        System.out.println("Solution trouvée: " + getSolutionString());
    }

    private String getSolutionString() {
        StringBuilder solution = new StringBuilder("[");
        for (int i = 0; i < commandeRealisés.size(); i++) {
            Command cmd = commandeRealisés.get(i);
            if (cmd instanceof CommandRemplir) {
                solution.append("RemplirBidon-").append(bidons.indexOf(cmd.bidon) + 1);
            } else if (cmd instanceof CommandVider) {
                solution.append("ViderBidon-").append(bidons.indexOf(cmd.bidon) + 1);
            } else if (cmd instanceof CommandTransverser) {
                CommandTransverser trans = (CommandTransverser) cmd;
                solution.append("TransvaserBidon-")
                        .append(bidons.indexOf(cmd.bidon) + 1)
                        .append("-")
                        .append(bidons.indexOf(trans.getCible()) + 1);
            }

            if (i < commandeRealisés.size() - 1) {
                solution.append(", ");
            }
        }
        return solution.append("]").toString();
    }



}
