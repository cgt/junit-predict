package net.gamrath.junitpredict;

interface UI {
    Prediction promptForPrediction();

    void displayHitOrMiss(boolean hit);
}
