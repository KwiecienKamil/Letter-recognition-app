import java.io.Serializable;

public class Warstwa implements Serializable {
    Neuron[] neurony;
    int liczbaNeuronow;
    int liczbaWejsc;
    double[] wyjscia;

    public Warstwa(int liczbaNeuronow, int liczbaWejsc) {
        this.liczbaNeuronow = liczbaNeuronow;
        this.liczbaWejsc = liczbaWejsc;
        this.neurony = new Neuron[liczbaNeuronow];
        for (int i = 0; i < liczbaNeuronow; i++) {
            neurony[i] = new Neuron(liczbaWejsc);
        }
        this.wyjscia = new double[liczbaNeuronow];
    }

    public double[] propaguj(double[] wejscia) {
        if (wejscia.length != liczbaWejsc) {
            throw new IllegalArgumentException("Nieprawidłowy rozmiar wektora wejściowego dla warstwy.");
        }
        for (int i = 0; i < liczbaNeuronow; i++) {
            wyjscia[i] = neurony[i].aktywuj(wejscia);
        }
        return wyjscia;
    }
}
