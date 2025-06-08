# Java Neural Network - Letter Recognition (F, E, U)

This is a simple Java Swing application that uses a feedforward neural network to recognize handwritten letters: **F**, **E**, and **U**. It includes a drawing interface, training functionality, and testing capability using backpropagation.

## ✨ Features

- Neural network built from scratch (no external ML libraries)
- GUI with drawing panel for letter input
- Train/test your own samples
- Recognizes letters **F**, **E**, and **U**
- Persistence of training and testing data

## 🧠 How It Works

- The drawing panel captures user input on an 8x8 grid.
- Each grid cell is turned into a grayscale feature (black pixel ratio).
- A 3-layer neural network is used:
  - Input layer: 64 neurons (8x8 grid)
  - Hidden layer: 16 neurons
  - Output layer: 3 neurons (F, E, U)
- Uses sigmoid activation and backpropagation for training.

## 📚 Usage

- Draw a letter in the center panel.  
- Select the corresponding letter using the radio buttons.  
- Click **"Dodaj do uczącego"** to add to training set.  
- Click **"Dodaj do testowego"** to add to test set.  
- Click **"Ucz"** to train the network.  
- Click **"Testuj"** to evaluate performance.  
- Click **"Rozpoznaj"** to recognize a newly drawn letter.  

## 💾 Data Files

- Training data is saved to `dane_uczace.txt`  
- Test data is saved to `dane_testowe.txt`  
- Format: one letter per line with its 64 normalized features  

## 🔧 Requirements

- Java SE 8 or higher  
- No third-party dependencies  

