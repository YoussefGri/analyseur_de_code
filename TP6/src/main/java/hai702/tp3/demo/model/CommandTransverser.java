package hai702.tp3.demo.model;

public class CommandTransverser extends Command {

    private Bidon cible;

    public CommandTransverser(Bidon bidon, Bidon cible ) {
        super(bidon);
        this.cible = cible;
    }

    @Override
    public void execute() {
        double espaceDisponible = cible.getVolumeMax() - cible.getVolumeCourant();
        volumedeplace = Math.min(bidon.getVolumeCourant(), espaceDisponible);

        bidon.setVolumeCourant(bidon.getVolumeCourant() - volumedeplace);
        cible.setVolumeCourant(cible.getVolumeCourant() + volumedeplace);
    }

    @Override
    public void undo() {
        bidon.setVolumeCourant(bidon.getVolumeCourant() + volumedeplace);
        cible.setVolumeCourant(cible.getVolumeCourant() - volumedeplace);
    }


    public Bidon getCible() {
        return cible;
    }
}
