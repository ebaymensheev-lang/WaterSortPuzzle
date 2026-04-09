package com.watersort.puzzle;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Генерирует уровни с нарастающей сложностью.
 */
public class LevelManager {

    public static final int TOTAL_LEVELS   = 20;
    public static final int BALLS_PER_TUBE = 4;

    // Палитра цветов (10 штук — добавляйте свои)
    private static final int[] COLORS = {
        Color.parseColor("#E84343"), // Красный
        Color.parseColor("#4085F5"), // Синий
        Color.parseColor("#38C664"), // Зелёный
        Color.parseColor("#F5D920"), // Жёлтый
        Color.parseColor("#AB52EE"), // Фиолетовый
        Color.parseColor("#FF9A1A"), // Оранжевый
        Color.parseColor("#F570B8"), // Розовый
        Color.parseColor("#1ADAE6"), // Голубой
        Color.parseColor("#996633"), // Коричневый
        Color.parseColor("#EBEBEB"), // Белый
    };

    // ── Конфигурация уровня ───────────────────────────────────────────
    public static class LevelConfig {
        public int colorCount;   // Сколько разных цветов
        public int emptyTubes;  // Пустых колб для манёвров
        public int tubeCount;   // Всего колб = colorCount + emptyTubes

        public LevelConfig(int colorCount, int emptyTubes) {
            this.colorCount = colorCount;
            this.emptyTubes = emptyTubes;
            this.tubeCount  = colorCount + emptyTubes;
        }
    }

    // ── Параметры сложности по уровню ────────────────────────────────
    public LevelConfig getConfig(int level) {
        if      (level <= 3)  return new LevelConfig(3, 1);
        else if (level <= 6)  return new LevelConfig(4, 1);
        else if (level <= 10) return new LevelConfig(5, 2);
        else if (level <= 15) return new LevelConfig(6, 2);
        else                  return new LevelConfig(Math.min(6 + (level - 15), 8), 2);
    }

    // ── Генерация колб с перемешанными шариками ───────────────────────
    public List<Tube> generateLevel(int level) {
        LevelConfig cfg = getConfig(level);
        Random rnd = new Random(level * 1337L); // seed = уровень, для воспроизводимости

        // Собираем все шарики
        List<Ball> allBalls = new ArrayList<>();
        for (int c = 0; c < cfg.colorCount; c++) {
            for (int b = 0; b < BALLS_PER_TUBE; b++) {
                allBalls.add(new Ball(COLORS[c]));
            }
        }

        // Перемешиваем (Фишер–Йейтс)
        for (int i = allBalls.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Ball tmp = allBalls.get(i);
            allBalls.set(i, allBalls.get(j));
            allBalls.set(j, tmp);
        }

        // Раскладываем по колбам
        List<Tube> tubes = new ArrayList<>();
        int idx = 0;
        for (int t = 0; t < cfg.colorCount; t++) {
            Tube tube = new Tube(BALLS_PER_TUBE);
            for (int b = 0; b < BALLS_PER_TUBE; b++) {
                tube.addBall(allBalls.get(idx++));
            }
            tubes.add(tube);
        }

        // Пустые колбы
        for (int e = 0; e < cfg.emptyTubes; e++) {
            tubes.add(new Tube(BALLS_PER_TUBE));
        }

        // Перемешиваем порядок колб
        Collections.shuffle(tubes, rnd);
        return tubes;
    }
}
