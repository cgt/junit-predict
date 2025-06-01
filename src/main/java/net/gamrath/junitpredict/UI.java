package net.gamrath.junitpredict;

import java.lang.reflect.InvocationTargetException;

interface UI {
    Prediction promptForPrediction() throws InterruptedException, InvocationTargetException;

    void displayHitOrMiss(boolean hit) throws InterruptedException, InvocationTargetException;
}
