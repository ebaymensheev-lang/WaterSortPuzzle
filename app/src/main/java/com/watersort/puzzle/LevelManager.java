package com.watersort.puzzle;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LevelManager {

    public static final int TOTAL_LEVELS   = 50;
    public static final int BALLS_PER_TUBE = 4;

    // 10 СТРОГО РАЗНЫХ цветов — никаких похожих!
    private static final int[] COLORS = {
        Color.parseColor("#E63946"), // Красный
        Color.parseColor("#2196F3"), // Синий
        Color.parseColor("#FFD600"), // Жёлтый
        Color.parseColor("#2ECC40"), // Зелёный
        Color.parseColor("#FF6D00"), // Оранжевый
        Color.parseColor("#9B59B6"), // Фиолетовый
        Color.parseColor("#00BCD4"), // Бирюзовый
        Color.parseColor("#FF4081"), // Малиновый
        Color.parseColor("#795548"), // Коричневый
        Color.parseColor("#FFFFFF"), // Белый
    };

    public static class LevelConfig {
        public int colorCount;
        public int emptyTubes;
        public int tubeCount;
        public LevelConfig(int colorCount, int emptyTubes) {
            this.colorCount = colorCount;
            this.emptyTubes = emptyTubes;
            this.tubeCount  = colorCount + emptyTubes;
        }
    }

    public LevelConfig getConfig(int level) {
        if      (level <= 5)  return new LevelConfig(3, 1);
        else if (level <= 10) return new LevelConfig(4, 1);
        else if (level <= 20) return new LevelConfig(5, 2);
        else if (level <= 35) return new LevelConfig(6, 2);
        else if (level <= 45) return new LevelConfig(7, 2);
        else                  return new LevelConfig(Math.min(8, COLORS.length), 2);
    }

    public List<Tube> generateLevel(int level) {
        LevelConfig cfg = getConfig(level);

        // Пробуем до 200 раз найти решаемый уровень
        for (int attempt = 0; attempt < 200; attempt++) {
            List<Tube> tubes = generateByReverseSolve(cfg,
                new Random(level * 7331L + attempt * 13L));
            if (tubes != null && hasAtLeastOneMove(tubes)) {
                return tubes;
            }
        }

        // Гарантированный запасной вариант
        return generateGuaranteed(cfg);
    }

    // Генерация обратным решением — начинаем с решённого и перемешиваем
    private List<Tube> generateByReverseSolve(LevelConfig cfg, Random rnd) {
        // 1. Решённое состояние
        List<Tube> tubes = new ArrayList<>();
        for (int c = 0; c < cfg.colorCount; c++) {
            Tube t = new Tube(BALLS_PER_TUBE);
            for (int b = 0; b < BALLS_PER_TUBE; b++) {
                t.addBall(new Ball(COLORS[c]));
            }
            tubes.add(t);
        }
        for (int e = 0; e < cfg.emptyTubes; e++) {
            tubes.add(new Tube(BALLS_PER_TUBE));
        }

        // 2. Перемешиваем обратными ходами
        int totalMoves = cfg.colorCount * BALLS_PER_TUBE * 4;
        int lastFrom = -1;
        int lastTo   = -1;

        for (int m = 0; m < totalMoves; m++) {
            List<int[]> moves = new ArrayList<>();
            for (int from = 0; from < tubes.size(); from++) {
                if (tubes.get(from).isEmpty()) continue;
                // Не ходим обратно тем же шариком
                if (from == lastTo) continue;
                for (int to = 0; to < tubes.size(); to++) {
                    if (from == to) continue;
                    if (tubes.get(to).isFull()) continue;
                    // Не возвращаем только что сделанный ход
                    if (from == lastTo && to == lastFrom) continue;
                    moves.add(new int[]{from, to});
                }
            }
            if (moves.isEmpty()) break;

            int[] move = moves.get(rnd.nextInt(moves.size()));
            Ball ball = tubes.get(move[0]).removeTop();
            if (ball != null) {
                tubes.get(move[1]).addBall(ball);
                lastFrom = move[0];
                lastTo   = move[1];
            }
        }

        // Проверяем что уровень не уже решён
        boolean allSolved = true;
        for (Tube t : tubes) {
            if (!t.isSolved()) { allSolved = false; break; }
        }
        if (allSolved) return null;

        return tubes;
    }

    // Проверка — есть ли хотя бы один возможный ход
    private boolean hasAtLeastOneMove(List<Tube> tubes) {
        for (int from = 0; from < tubes.size(); from++) {
            if (tubes.get(from).isEmpty()) continue;
            Ball top = tubes.get(from).peekTop();
            for (int to = 0; to < tubes.size(); to++) {
                if (from == to) continue;
                if (tubes.get(to).canReceive(top)) return true;
            }
        }
        return false;
    }

    // Гарантированный простой уровень (запасной)
    private List<Tube> generateGuaranteed(LevelConfig cfg) {
        // Просто перекладываем по одному шарику из каждой трубы в пустую
        List<Tube> tubes = new ArrayList<>();
        for (int c = 0; c < cfg.colorCount; c++) {
            Tube t = new Tube(BALLS_PER_TUBE);
            for (int b = 0; b < BALLS_PER_TUBE; b++) {
                t.addBall(new Ball(COLORS[c]));
            }
            tubes.add(t);
        }
        // Добавляем пустые
        for (int e = 0; e < cfg.emptyTubes; e++) {
            tubes.add(new Tube(BALLS_PER_TUBE));
        }

        // Перемешиваем верхние шарики между трубами
        Random rnd = new Random(42);
        for (int i = 0; i < cfg.colorCount * 2; i++) {
            int from = rnd.nextInt(cfg.colorCount);
            int to   = cfg.colorCount; // первая пустая
            if (!tubes.get(from).isEmpty() && !tubes.get(to).isFull()) {
                Ball b = tubes.get(from).removeTop();
                tubes.get(to).addBall(b);
            }
        }
        return tubes;
    }
}
