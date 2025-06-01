package net.gamrath.junitpredict;

import java.lang.reflect.InvocationTargetException;

interface UI {
    Prediction promptForPrediction();

    void displayHitOrMiss(boolean hit) throws InterruptedException, InvocationTargetException;
}
