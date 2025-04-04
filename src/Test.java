import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;  // <-- ważne, aby móc używać IOException

public class Test extends JFrame {
    private DrawingPanel drawingPanel;
    private JButton recognizeButton;
    private JButton addTrainButton;
    private JButton addTestButton;
    private JButton trainButton;
    private JButton testButton;
    private JButton clearButton;
    private JRadioButton radioF;
    private JRadioButton radioE;
    private JRadioButton radioU;
    private JTextField resultField;
    private Trener trener;

    private class DrawingPanel extends JPanel {
        private BufferedImage image;
        private Graphics2D g2;
        private int panelSize;
        private int cellCount = 8;
        private int cellSize;
        private int lastX = -1, lastY = -1;

        public DrawingPanel(int size) {
            this.panelSize = size;
            this.cellSize = panelSize / cellCount;
            setPreferredSize(new Dimension(panelSize, panelSize));
            image = new BufferedImage(panelSize, panelSize, BufferedImage.TYPE_INT_RGB);
            g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, panelSize, panelSize);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    lastX = e.getX();
                    lastY = e.getY();
                }
                public void mouseReleased(MouseEvent e) {
                    lastX = -1;
                    lastY = -1;
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    if (lastX != -1 && lastY != -1) {
                        g2.drawLine(lastX, lastY, x, y);
                    } else {
                        g2.drawLine(x, y, x, y);
                    }
                    lastX = x;
                    lastY = y;
                    repaint();
                }
            });
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
            g.setColor(Color.LIGHT_GRAY);
            for (int i = 1; i < cellCount; i++) {
                int pos = i * cellSize;
                g.drawLine(pos, 0, pos, panelSize);
                g.drawLine(0, pos, panelSize, pos);
            }
        }

        public void clear() {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, panelSize, panelSize);
            g2.setColor(Color.BLACK);
            repaint();
        }

        public double[] getInputData() {
            double[] data = new double[cellCount * cellCount];
            for (int r = 0; r < cellCount; r++) {
                for (int c = 0; c < cellCount; c++) {
                    int startX = c * cellSize;
                    int startY = r * cellSize;
                    int blackCount = 0;
                    for (int y = startY; y < startY + cellSize; y++) {
                        for (int x = startX; x < startX + cellSize; x++) {
                            int rgb = image.getRGB(x, y);
                            if (rgb != Color.WHITE.getRGB()) {
                                blackCount++;
                            }
                        }
                    }
                    double fraction = (double) blackCount / (cellSize * cellSize);
                    data[r * cellCount + c] = fraction;
                }
            }
            return data;
        }
    }

    public Test() {
        super("Rozpoznawanie liter F, E, U - Sieć neuronowa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inicjalizacja sieci
        Siec siec = new Siec(64, new int[]{16, 3});
        trener = new Trener(siec, 0.1);

        // Wczytanie danych (jeśli pliki istnieją)
        try {
            trener.wczytajDane("dane_uczace.txt", "dane_testowe.txt");
        } catch (IOException e) {
            System.err.println("Błąd wczytywania danych: " + e.getMessage());
        }

        drawingPanel = new DrawingPanel(240);
        add(drawingPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        radioF = new JRadioButton("F");
        radioE = new JRadioButton("E");
        radioU = new JRadioButton("U");
        ButtonGroup group = new ButtonGroup();
        group.add(radioF);
        group.add(radioE);
        group.add(radioU);
        radioF.setSelected(true);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(new JLabel("Litera:"));
        radioPanel.add(radioF);
        radioPanel.add(radioE);
        radioPanel.add(radioU);
        controlPanel.add(radioPanel);

        addTrainButton = new JButton("Dodaj do uczącego");
        addTestButton = new JButton("Dodaj do testowego");
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.add(addTrainButton);
        addPanel.add(addTestButton);
        controlPanel.add(addPanel);

        recognizeButton = new JButton("Rozpoznaj");
        resultField = new JTextField(10);
        resultField.setEditable(false);
        JPanel recognizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recognizePanel.add(recognizeButton);
        recognizePanel.add(new JLabel("Wynik:"));
        recognizePanel.add(resultField);
        controlPanel.add(recognizePanel);

        trainButton = new JButton("Ucz");
        testButton = new JButton("Testuj");
        JPanel trainTestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        trainTestPanel.add(trainButton);
        trainTestPanel.add(testButton);
        controlPanel.add(trainTestPanel);

        clearButton = new JButton("Wyczyść");
        JPanel clearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clearPanel.add(clearButton);
        controlPanel.add(clearPanel);

        add(controlPanel, BorderLayout.EAST);

        
        recognizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double[] input = drawingPanel.getInputData();
                double[] output = trener.siec.propagujSiec(input);
                int maxIndex = 0;
                for (int i = 1; i < output.length; i++) {
                    if (output[i] > output[maxIndex]) {
                        maxIndex = i;
                    }
                }
                char recognizedLetter;
                if (maxIndex == 0) recognizedLetter = 'F';
                else if (maxIndex == 1) recognizedLetter = 'E';
                else recognizedLetter = 'U';

                if (output[maxIndex] < 0.5) {
                    resultField.setText("Inna litera");
                } else {
                    resultField.setText(String.valueOf(recognizedLetter));
                }
            }
        });

        addTrainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double[] input = drawingPanel.getInputData();
                char litera = radioF.isSelected() ? 'F' : (radioE.isSelected() ? 'E' : 'U');
                trener.dodajDoUczacych(input, litera);
                drawingPanel.clear();
            }
        });

        addTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double[] input = drawingPanel.getInputData();
                char litera = radioF.isSelected() ? 'F' : (radioE.isSelected() ? 'E' : 'U');
                trener.dodajDoTestowych(input, litera);
                drawingPanel.clear();
            }
        });

        trainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trener.ucz(500);
                JOptionPane.showMessageDialog(Test.this,
                        "Trening zakończony.",
                        "Ucz",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double accuracy = trener.testuj();
                String msg;
                if (trener.liczbaTestowych() == 0) {
                    msg = "Brak danych testowych.";
                } else {
                    msg = String.format("Skuteczność sieci: %.2f%%", accuracy);
                }
                JOptionPane.showMessageDialog(Test.this,
                        msg,
                        "Wynik testowania",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingPanel.clear();
                resultField.setText("");
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Test();
            }
        });
    }
}
