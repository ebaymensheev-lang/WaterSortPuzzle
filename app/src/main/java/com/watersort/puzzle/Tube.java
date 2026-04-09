package com.watersort.puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * Колба — стек шариков с правилами приёма.
 */
public class Tube {

    private final List<Ball> balls = new ArrayList<>();
    private final int maxCapacity;

    public Tube(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    // ── Добавить шарик сверху ──────────────────────────────────────────
    public boolean addBall(Ball ball) {
        if (isFull()) return false;
        balls.add(ball);
        return true;
    }

    // ── Убрать верхний шарик ──────────────────────────────────────────
    public Ball removeTop() {
        if (isEmpty()) return null;
        return balls.remove(balls.size() - 1);
    }

    // ── Посмотреть верхний шарик (без удаления) ───────────────────────
    public Ball peekTop() {
        if (isEmpty()) return null;
        return balls.get(balls.size() - 1);
    }

    // ── Можно ли принять этот шарик? ──────────────────────────────────
    public boolean canReceive(Ball ball) {
        if (isFull()) return false;
        if (isEmpty()) return true;
        return peekTop().getColor() == ball.getColor();
    }

    // ── Колба решена? (пустая или все одного цвета и полная) ─────────
    public boolean isSolved() {
        if (isEmpty()) return true;
        if (balls.size() != maxCapacity) return false;
        int firstColor = balls.get(0).getColor();
        for (Ball b : balls) {
            if (b.getColor() != firstColor) return false;
        }
        return true;
    }

    public boolean isEmpty()  { return balls.isEmpty(); }
    public boolean isFull()   { return balls.size() >= maxCapacity; }
    public int     getCount() { return balls.size(); }
    public int     getMaxCapacity() { return maxCapacity; }

    // Копия списка для Undo
    public List<Ball> getBallsCopy() {
        return new ArrayList<>(balls);
    }

    // Восстановить из снимка
    public void restoreFrom(List<Ball> snapshot) {
        balls.clear();
        balls.addAll(snapshot);
    }

    // Шарик по индексу (0 = нижний)
    public Ball getBallAt(int index) {
        if (index < 0 || index >= balls.size()) return null;
        return balls.get(index);
    }
}
