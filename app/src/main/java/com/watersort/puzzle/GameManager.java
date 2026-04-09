package com.watersort.puzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Вся игровая логика: ходы, Undo, проверка победы.
 * Не зависит от UI — легко тестировать.
 */
public class GameManager {

    // ── Снимок состояния для Undo ─────────────────────────────────────
    private static class GameSnapshot {
        final List<List<Ball>> tubeSnapshots;
        GameSnapshot(List<Tube> tubes) {
            tubeSnapshots = new ArrayList<>();
            for (Tube t : tubes) {
                tubeSnapshots.add(t.getBallsCopy());
            }
        }
    }

    // ── Поля ─────────────────────────────────────────────────────────
    private final LevelManager levelManager = new LevelManager();
    private List<Tube> tubes = new ArrayList<>();
    private final Stack<GameSnapshot> undoStack = new Stack<>();

    private int  currentLevel   = 1;
    private int  selectedIndex  = -1; // -1 = ничего не выбрано
    private boolean levelComplete = false;

    // Слушатель событий для Activity
    public interface GameListener {
        void onMoveSuccess(int fromIndex, int toIndex);
        void onMoveInvalid();
        void onSelectionChanged(int selectedIndex);
        void onLevelComplete();
        void onLevelLoaded(int level, List<Tube> tubes);
    }
    private GameListener listener;

    public void setListener(GameListener l) { this.listener = l; }

    // ── Загрузка уровня ──────────────────────────────────────────────

    public void loadLevel(int level) {
        currentLevel  = level;
        levelComplete = false;
        selectedIndex = -1;
        undoStack.clear();
        tubes = levelManager.generateLevel(level);
        if (listener != null) listener.onLevelLoaded(level, tubes);
    }

    public void restartLevel() { loadLevel(currentLevel); }

    public void nextLevel() {
        int next = (currentLevel % LevelManager.TOTAL_LEVELS) + 1;
        loadLevel(next);
    }

    // ── Клик по колбе ────────────────────────────────────────────────

    public void onTubeClicked(int index) {
        if (levelComplete) return;
        if (index < 0 || index >= tubes.size()) return;

        if (selectedIndex == -1) {
            // Первый клик — выбрать исходную колбу
            if (!tubes.get(index).isEmpty()) {
                selectedIndex = index;
                if (listener != null) listener.onSelectionChanged(selectedIndex);
            }
        } else if (selectedIndex == index) {
            // Клик по той же — сброс выбора
            selectedIndex = -1;
            if (listener != null) listener.onSelectionChanged(-1);
        } else {
            // Второй клик — попытка хода
            tryMove(selectedIndex, index);
        }
    }

    // ── Выполнение хода ──────────────────────────────────────────────

    private void tryMove(int fromIdx, int toIdx) {
        Tube from = tubes.get(fromIdx);
        Tube to   = tubes.get(toIdx);
        Ball ball = from.peekTop();

        if (ball == null || !to.canReceive(ball)) {
            // Невалидный ход — если кликнули на колбу с шариками, выбираем её
            selectedIndex = (!to.isEmpty()) ? toIdx : -1;
            if (listener != null) {
                listener.onMoveInvalid();
                listener.onSelectionChanged(selectedIndex);
            }
            return;
        }

        // Сохраняем состояние для Undo
        undoStack.push(new GameSnapshot(tubes));

        // Перемещаем шарик
        Ball moved = from.removeTop();
        to.addBall(moved);

        int prevFrom = fromIdx;
        int prevTo   = toIdx;
        selectedIndex = -1;

        if (listener != null) {
            listener.onMoveSuccess(prevFrom, prevTo);
            listener.onSelectionChanged(-1);
        }

        // Проверяем победу
        if (checkWin()) {
            levelComplete = true;
            if (listener != null) listener.onLevelComplete();
        }
    }

    // ── Отмена хода ──────────────────────────────────────────────────

    public void undoMove() {
        if (undoStack.isEmpty()) return;
        GameSnapshot snap = undoStack.pop();
        for (int i = 0; i < tubes.size() && i < snap.tubeSnapshots.size(); i++) {
            tubes.get(i).restoreFrom(snap.tubeSnapshots.get(i));
        }
        selectedIndex = -1;
        if (listener != null) {
            listener.onSelectionChanged(-1);
            listener.onLevelLoaded(currentLevel, tubes); // Перерисовать всё
        }
    }

    // ── Подсказка ────────────────────────────────────────────────────

    /** Возвращает пару [from, to] для первого найденного хода, или null */
    public int[] findHint() {
        for (int i = 0; i < tubes.size(); i++) {
            if (tubes.get(i).isEmpty()) continue;
            Ball top = tubes.get(i).peekTop();
            for (int j = 0; j < tubes.size(); j++) {
                if (i == j) continue;
                if (tubes.get(j).canReceive(top)) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    // ── Проверка победы ──────────────────────────────────────────────

    private boolean checkWin() {
        for (Tube t : tubes) {
            if (!t.isSolved()) return false;
        }
        return true;
    }

    // ── Геттеры ──────────────────────────────────────────────────────

    public List<Tube> getTubes()       { return tubes; }
    public int        getCurrentLevel(){ return currentLevel; }
    public int        getSelectedIndex(){ return selectedIndex; }
    public boolean    isLevelComplete() { return levelComplete; }
    public boolean    canUndo()         { return !undoStack.isEmpty(); }
}
