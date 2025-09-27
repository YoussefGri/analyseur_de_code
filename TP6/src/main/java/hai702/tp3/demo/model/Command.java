package hai702.tp3.demo.model;

public abstract class Command {
    protected Bidon bidon;
    protected double volumedeplace =0;

    public abstract void execute();
    public abstract void undo();

    public Command(Bidon bidon , int volumedeplace) {
        this.bidon = bidon;
        this.volumedeplace = volumedeplace;
    }
    public Command(Bidon bidon ){
        this.bidon = bidon;
    }

}
