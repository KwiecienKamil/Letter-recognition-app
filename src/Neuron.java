public class Neuron {
    double[] wagi;
    double bias;
    double output;


    public Neuron(int liczbaWejsc) {
        wagi = new double[liczbaWejsc];
        for (int i = 0; i < liczbaWejsc; i++) {
            wagi[i] = Math.random() - 0.5;
        }
        bias = Math.random() - 0.5;
    }

    public double aktywuj(double[] wejscia) {
        double suma = 0.0;
        for (int i = 0; i < wagi.length; i++) {
            suma += wagi[i] * wejscia[i];
        }
        suma += bias;
        output = 1.0 / (1.0 + Math.exp(-suma));
        return output;
    }
}
