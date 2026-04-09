package com.watersort.puzzle;

/**
 * Шарик — хранит только цвет.
 */
public class Ball {

    private final int color; // Android цвет (0xFFRRGGBB)

    public Ball(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
