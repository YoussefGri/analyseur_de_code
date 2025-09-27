package hai702.tp3.demo.model;

public class CommandVider extends Command {


    public CommandVider(Bidon bidon) {
        super(bidon);
    }

    @Override
    public void execute() {
        volumedeplace = bidon.getVolumeCourant();
        bidon.vider();
    }

    @Override
    public void undo() {
        bidon.setVolumeCourant(volumedeplace);
    }
}
