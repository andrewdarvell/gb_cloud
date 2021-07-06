package ru.darvell.gb.cloud.client;

@FunctionalInterface
public interface ProgressBarUpdater {
    void setProgress(double p);
}
