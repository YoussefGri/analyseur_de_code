package hai702.tp3.demo.model;

public class Bidon {
    private double volumeMax;
    private double volumeCourant;


    public Bidon(double volumeMax, double volumeCourant) {
        this.volumeMax = volumeMax;
        this.volumeCourant = volumeCourant;
    }

    public double getVolumeMax() {
        return volumeMax;
    }

    public void setVolumeMax(double volumeMax) {
        this.volumeMax = volumeMax;
    }

    public double getVolumeCourant() {
        return volumeCourant;
    }

    public void setVolumeCourant(double volumeCourant) {
        this.volumeCourant = volumeCourant;
    }


    public void vider(){
        this.volumeCourant=0;
    }

    public void remplir(){
        this.volumeCourant=this.volumeMax;
    }

    public void transverser(Bidon bidoncible) {
        if (this.volumeCourant > 0) {
            double espaceDisponible = bidoncible.volumeMax - bidoncible.volumeCourant;
            double volumeTransfere = Math.min(this.volumeCourant, espaceDisponible);

            this.volumeCourant -= volumeTransfere;
            bidoncible.volumeCourant += volumeTransfere;

            System.out.println("Transfert de " + volumeTransfere + " unités de liquide.");
        } else {
            System.out.println("Le bidon source est vide.");
        }
    }

    @Override
    public String toString() {
        return "Bidon{" +
                "volumeMax=" + volumeMax +
                ", volumeCourant=" + volumeCourant +
                '}';
    }

}
