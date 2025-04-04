import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;

public class Trener {

    Siec siec;

    private List<double[]> daneUczaceWejscia;

    private List<double[]> daneUczaceWyjscia;

    private List<double[]> daneTestoweWejscia;
    private List<double[]> daneTestoweWyjscia;

    private double wspolczynnikUczenia;

    private final char[] litery = {'F', 'E', 'U'};

    private static final String PLIK_UCZACY = "dane_uczace.txt";
    private static final String PLIK_TESTOWY = "dane_testowe.txt";

    public Trener(Siec siec, double wspolczynnikUczenia) {
        this.siec = siec;
        this.wspolczynnikUczenia = wspolczynnikUczenia;
        this.daneUczaceWejscia = new ArrayList<>();
        this.daneUczaceWyjscia = new ArrayList<>();
        this.daneTestoweWejscia = new ArrayList<>();
        this.daneTestoweWyjscia = new ArrayList<>();
    }

    public void dodajDoUczacych(double[] wektorWejsc, char litera) {
        
        daneUczaceWejscia.add(wektorWejsc);
        daneUczaceWyjscia.add(literaNaWyjscie(litera));

 
        try (PrintWriter pw = new PrintWriter(new FileWriter(PLIK_UCZACY, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(Character.toUpperCase(litera));
            for (double val : wektorWejsc) {
                sb.append(' ');

                sb.append(String.format(Locale.US, "%.3f", val));
            }
            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dodajDoTestowych(double[] wektorWejsc, char litera) {
        daneTestoweWejscia.add(wektorWejsc);
        daneTestoweWyjscia.add(literaNaWyjscie(litera));

        try (PrintWriter pw = new PrintWriter(new FileWriter(PLIK_TESTOWY, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(Character.toUpperCase(litera));
            for (double val : wektorWejsc) {
                sb.append(' ');
                sb.append(String.format(Locale.US, "%.3f", val));
            }
            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double[] literaNaWyjscie(char litera) {
        double[] wy = new double[litery.length];
        Arrays.fill(wy, 0.0);
        for (int i = 0; i < litery.length; i++) {
            if (litery[i] == Character.toUpperCase(litera)) {
                wy[i] = 1.0;
                break;
            }
        }
        return wy;
    }


    private void uczPrzyklad(double[] wektorWejsc, double[] wektorDocelowy) {
        
        double[] wejscieAktualne = wektorWejsc;
        double[][] wyjsciaWarstw = new double[siec.liczbaWarstw() + 1][];
        wyjsciaWarstw[0] = wejscieAktualne;
        for (int l = 0; l < siec.warstwy.length; l++) {
            double[] wyjscia = siec.warstwy[l].propaguj(wejscieAktualne);
            wyjsciaWarstw[l + 1] = wyjscia;
            wejscieAktualne = wyjscia;
        }
        double[] wyjscieSieci = wyjsciaWarstw[siec.warstwy.length];

       
        int L = siec.warstwy.length;
        double[][] deltaWarstwy = new double[L][];
        int idxWyj = L - 1;
        Warstwa warstwaWyjsciowa = siec.warstwy[idxWyj];
        deltaWarstwy[idxWyj] = new double[warstwaWyjsciowa.liczbaNeuronow];
        for (int i = 0; i < warstwaWyjsciowa.liczbaNeuronow; i++) {
            double output = wyjscieSieci[i];
            double target = wektorDocelowy[i];
            double blad = target - output;
            deltaWarstwy[idxWyj][i] = blad * output * (1.0 - output);
        }

        
        for (int l = L - 2; l >= 0; l--) {
            Warstwa warstwa = siec.warstwy[l];
            Warstwa warstwaPo = siec.warstwy[l + 1];
            deltaWarstwy[l] = new double[warstwa.liczbaNeuronow];
            for (int j = 0; j < warstwa.liczbaNeuronow; j++) {
                double outputJ = wyjsciaWarstw[l + 1][j];
                double sumaBlad = 0.0;
                for (int k = 0; k < warstwaPo.liczbaNeuronow; k++) {
                    sumaBlad += warstwaPo.neurony[k].wagi[j] * deltaWarstwy[l + 1][k];
                }
                deltaWarstwy[l][j] = sumaBlad * outputJ * (1.0 - outputJ);
            }
        }

        
        for (int l = 0; l < L; l++) {
            Warstwa warstwa = siec.warstwy[l];
            double[] wejscieDoWarstwy = wyjsciaWarstw[l];
            for (int i = 0; i < warstwa.liczbaNeuronow; i++) {
                for (int j = 0; j < warstwa.liczbaWejsc; j++) {
                    warstwa.neurony[i].wagi[j] += wspolczynnikUczenia * deltaWarstwy[l][i] * wejscieDoWarstwy[j];
                }
                warstwa.neurony[i].bias += wspolczynnikUczenia * deltaWarstwy[l][i];
            }
        }
    }

    public void ucz(int epoki) {
        if (daneUczaceWejscia.isEmpty()) {
            return;
        }
        for (int e = 0; e < epoki; e++) {
            for (int idx = 0; idx < daneUczaceWejscia.size(); idx++) {
                double[] in = daneUczaceWejscia.get(idx);
                double[] target = daneUczaceWyjscia.get(idx);
                uczPrzyklad(in, target);
            }
        }
    }


    public double testuj() {
        if (daneTestoweWejscia.isEmpty()) {
            return 0.0;
        }
        int poprawne = 0;
        for (int idx = 0; idx < daneTestoweWejscia.size(); idx++) {
            double[] in = daneTestoweWejscia.get(idx);
            double[] expectedOut = daneTestoweWyjscia.get(idx);
            double[] wynik = siec.propagujSiec(in);
            int maxIndex = 0;
            double maxVal = wynik[0];
            for (int i = 1; i < wynik.length; i++) {
                if (wynik[i] > maxVal) {
                    maxVal = wynik[i];
                    maxIndex = i;
                }
            }
            int expectedIndex = -1;
            for (int i = 0; i < expectedOut.length; i++) {
                if (expectedOut[i] > 0.9) {
                    expectedIndex = i;
                    break;
                }
            }
            if (maxIndex == expectedIndex) {
                poprawne++;
            }
        }
        return (poprawne * 100.0) / daneTestoweWejscia.size();
    }

    public void wczytajDane(String plikUczacy, String plikTestowy) throws IOException {
        
        daneUczaceWejscia.clear();
        daneUczaceWyjscia.clear();
        daneTestoweWejscia.clear();
        daneTestoweWyjscia.clear();

        
        File fileTrain = new File(plikUczacy);
        if (fileTrain.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileTrain))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] tokens = line.split("\\s+");
                    if (tokens.length != 65) continue; 
                    char lit = tokens[0].charAt(0);
                    double[] wej = new double[64];
                    for (int i = 1; i < 65; i++) {

                        wej[i-1] = Double.parseDouble(tokens[i]);
                    }
                    daneUczaceWejscia.add(wej);
                    daneUczaceWyjscia.add(literaNaWyjscie(lit));
                }
            }
        }
        
        File fileTest = new File(plikTestowy);
        if (fileTest.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileTest))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] tokens = line.split("\\s+");
                    if (tokens.length != 65) continue;
                    char lit = tokens[0].charAt(0);
                    double[] wej = new double[64];
                    for (int i = 1; i < 65; i++) {
                        wej[i-1] = Double.parseDouble(tokens[i]);
                    }
                    daneTestoweWejscia.add(wej);
                    daneTestoweWyjscia.add(literaNaWyjscie(lit));
                }
            }
        }
    }

    public int liczbaTestowych() {
        return daneTestoweWejscia.size();
    }

    public int liczbaUczacych() {
        return daneUczaceWejscia.size();
    }
}
