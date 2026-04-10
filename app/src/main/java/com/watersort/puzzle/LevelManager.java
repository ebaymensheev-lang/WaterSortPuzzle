package com.watersort.puzzle;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LevelManager {

    public static final int TOTAL_LEVELS   = 50;
    public static final int BALLS_PER_TUBE = 4;

    // 🍭 Конфетная палитра цветов
    private static final int[] COLORS = {
        Color.parseColor("#FF6B9D"), // Клубничная конфета
        Color.parseColor("#54C6EB"), // Мятная конфета
        Color.parseColor("#FFD93D"), // Лимонная конфета
        Color.parseColor("#6BCB77"), // Яблочная конфета
        Color.parseColor("#FF9A3C"), // Апельсиновая конфета
        Color.parseColor("#C77DFF"), // Виноградная конфета
        Color.parseColor("#FF4D6D"), // Вишнёвая конфета
        Color.parseColor("#4CC9F0"), // Черничная конфета
        Color.parseColor("#F9C74F"), // Банановая конфета
        Color.parseColor("#90BE6D"), // Арбузная конфета
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
        else                  return new LevelConfig(Math.min(6 + (level - 35) / 5, 9), 2);
    }

    public List<Tube> generateLevel(int level) {
        LevelConfig cfg = getConfig(level);
        Random rnd = new Random(level * 7331L);

        List<Ball> allBalls = new ArrayList<>();
        for (int c = 0; c < cfg.colorCount; c++) {
            for (int b = 0; b < BALLS_PER_TUBE; b++) {
                allBalls.add(new Ball(COLORS[c]));
            }
        }

        for (int i = allBalls.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Ball tmp = allBalls.get(i);
            allBalls.set(i, allBalls.get(j));
            allBalls.set(j, tmp);
        }

        List<Tube> tubes = new ArrayList<>();
        int idx = 0;
        for (int t = 0; t < cfg.colorCount; t++) {
            Tube tube = new Tube(BALLS_PER_TUBE);
            for (int b = 0; b < BALLS_PER_TUBE; b++) {
                tube.addBall(allBalls.get(idx++));
            }
            tubes.add(tube);
        }

        for (int e = 0; e < cfg.emptyTubes; e++) {
            tubes.add(new Tube(BALLS_PER_TUBE));
        }

        Collections.shuffle(tubes, rnd);
        return tubes;
    }
}
