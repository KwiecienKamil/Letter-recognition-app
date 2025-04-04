import java.io.Serializable;
import java.util.Arrays;

public class Siec implements Serializable {
    int liczbaWejsc;
    Warstwa[] warstwy;


    public Siec(int liczbaWejsc, int[] warstwyKonfiguracja) {
        this.liczbaWejsc = liczbaWejsc;
        this.warstwy = new Warstwa[warstwyKonfiguracja.length];
        int poprzednieWyjscia = liczbaWejsc;
        for (int i = 0; i < warstwyKonfiguracja.length; i++) {
            this.warstwy[i] = new Warstwa(warstwyKonfiguracja[i], poprzednieWyjscia);
            poprzednieWyjscia = warstwyKonfiguracja[i];
        }
    }

    public double[] propagujSiec(double[] wejscie) {
        double[] aktywacje = wejscie;
        for (int i = 0; i < warstwy.length; i++) {
            aktywacje = warstwy[i].propaguj(aktywacje);
        }
        return aktywacje;
    }

    public int liczbaWarstw() {
        return warstwy.length;
    }
}
