package hai702.tp3.demo.model;

public class CommandRemplir extends Command {

    public CommandRemplir(Bidon bidon) {
        super(bidon);
    }

    @Override
    public void execute() {
    volumedeplace = bidon.getVolumeMax() -bidon.getVolumeCourant();
    bidon.remplir();
    }

    @Override
    public void undo() {
        bidon.setVolumeCourant(bidon.getVolumeCourant() - volumedeplace);

    }


}
