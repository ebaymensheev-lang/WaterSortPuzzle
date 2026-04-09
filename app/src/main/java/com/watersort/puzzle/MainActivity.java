package com.watersort.puzzle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GameManager.GameListener {

    // ── Игровая логика ────────────────────────────────────────────────
    private final GameManager gameManager = new GameManager();

    // ── UI элементы ───────────────────────────────────────────────────
    private TextView      levelText;
    private LinearLayout  tubeContainer;
    private Button        btnRestart, btnUndo, btnHint, btnSkip;
    private View          winBanner;
    private TextView      winLevelText;

    // ── Вьюхи колб ───────────────────────────────────────────────────
    private final List<TubeView> tubeViews = new ArrayList<>();

    // ── Звук ─────────────────────────────────────────────────────────
    private SoundPool soundPool;
    private int       soundPop;

    // ═══════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSound();
        buildUI();

        gameManager.setListener(this);
        gameManager.loadLevel(1);
    }

    // ── Построение UI программно ──────────────────────────────────────

    private void buildUI() {
        // Корневой layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#181C2E"));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        setContentView(root);

        // ── Верхняя панель ─────────────────────────────────────────
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER);
        topBar.setBackgroundColor(Color.parseColor("#1E2438"));
        topBar.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams topParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root.addView(topBar, topParams);

        levelText = new TextView(this);
        levelText.setTextColor(Color.WHITE);
        levelText.setTextSize(22f);
        levelText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        levelText.setGravity(Gravity.CENTER);
        topBar.addView(levelText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // ── Поле с колбами ─────────────────────────────────────────
        tubeContainer = new LinearLayout(this);
        tubeContainer.setOrientation(LinearLayout.HORIZONTAL);
        tubeContainer.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        tubeContainer.setPadding(dp(8), dp(24), dp(8), dp(8));
        LinearLayout.LayoutParams fieldParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(tubeContainer, fieldParams);

        // ── Баннер победы (скрыт) ──────────────────────────────────
        winBanner = buildWinBanner(root);
        root.addView(winBanner);

        // ── Нижняя панель кнопок ───────────────────────────────────
        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.VERTICAL);
        bottomBar.setBackgroundColor(Color.parseColor("#1E2438"));
        bottomBar.setPadding(dp(12), dp(8), dp(12), dp(16));
        root.addView(bottomBar,
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Ряд 1: Restart + Undo
        LinearLayout row1 = makeButtonRow();
        bottomBar.addView(row1,
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        btnRestart = makeButton("↺  Restart", "#4085F5");
        btnUndo    = makeButton("↩  Undo",    "#6E7191");
        row1.addView(btnRestart, new LinearLayout.LayoutParams(0, dp(52), 1f));
        row1.addView(makeSpace(dp(8)), null);
        row1.addView(btnUndo,    new LinearLayout.LayoutParams(0, dp(52), 1f));

        // Ряд 2: Hint + Skip
        LinearLayout row2 = makeButtonRow();
        row2.setPadding(0, dp(8), 0, 0);
        bottomBar.addView(row2,
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        btnHint = makeButton("💡  Hint (Ad)", "#E68A00");
        btnSkip = makeButton("⏭  Skip (Ad)", "#E68A00");
        row2.addView(btnHint, new LinearLayout.LayoutParams(0, dp(52), 1f));
        row2.addView(makeSpace(dp(8)), null);
        row2.addView(btnSkip, new LinearLayout.LayoutParams(0, dp(52), 1f));

        // Слушатели кнопок
        btnRestart.setOnClickListener(v -> gameManager.restartLevel());
        btnUndo.setOnClickListener(v -> gameManager.undoMove());
        btnHint.setOnClickListener(v -> showHintAd());
        btnSkip.setOnClickListener(v -> showSkipAd());
    }

    private View buildWinBanner(ViewGroup parent) {
        LinearLayout banner = new LinearLayout(this);
        banner.setOrientation(LinearLayout.VERTICAL);
        banner.setGravity(Gravity.CENTER);
        banner.setBackgroundColor(Color.parseColor("#CC1E2438"));
        banner.setPadding(dp(24), dp(20), dp(24), dp(20));
        banner.setVisibility(View.GONE);

        winLevelText = new TextView(this);
        winLevelText.setTextColor(Color.parseColor("#F5D920"));
        winLevelText.setTextSize(28f);
        winLevelText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        winLevelText.setGravity(Gravity.CENTER);
        banner.addView(winLevelText);

        Button btnNext = makeButton("▶  Next Level", "#38C664");
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(dp(260), dp(56));
        p.topMargin = dp(16);
        banner.addView(btnNext, p);
        btnNext.setOnClickListener(v -> {
            banner.setVisibility(View.GONE);
            gameManager.nextLevel();
        });

        return banner;
    }

    // ── GameManager.GameListener ──────────────────────────────────────

    @Override
    public void onLevelLoaded(int level, List<Tube> tubes) {
        levelText.setText("Level  " + level);
        winBanner.setVisibility(View.GONE);
        buildTubeViews(tubes);
    }

    @Override
    public void onSelectionChanged(int selectedIndex) {
        for (int i = 0; i < tubeViews.size(); i++) {
            tubeViews.get(i).setSelected(i == selectedIndex);
        }
    }

    @Override
    public void onMoveSuccess(int fromIndex, int toIndex) {
        soundPool.play(soundPop, 0.8f, 0.8f, 0, 0, 1.0f + (float) Math.random() * 0.3f);
        tubeViews.get(fromIndex).invalidate();
        tubeViews.get(toIndex).invalidate();
    }

    @Override
    public void onMoveInvalid() {
        // Вибрация: короткое покачивание кнопки
        if (!tubeViews.isEmpty()) {
            View v = tubeViews.get(gameManager.getSelectedIndex() == -1 ? 0 : gameManager.getSelectedIndex());
            ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX", 0, -14, 14, -8, 8, 0);
            shake.setDuration(280);
            shake.start();
        }
    }

    @Override
    public void onLevelComplete() {
        winLevelText.setText("🎉  Level " + gameManager.getCurrentLevel() + " Complete!");
        winBanner.setVisibility(View.VISIBLE);
        winBanner.setAlpha(0f);
        winBanner.animate().alpha(1f).setDuration(400).start();

        // Анимируем все колбы
        for (int i = 0; i < tubeViews.size(); i++) {
            final TubeView tv = tubeViews.get(i);
            tv.postDelayed(() -> {
                ObjectAnimator bounce = ObjectAnimator.ofFloat(tv, "translationY", 0, -dp(30), 0);
                bounce.setInterpolator(new BounceInterpolator());
                bounce.setDuration(600);
                bounce.start();
            }, i * 80L);
        }
    }

    // ── Построение TubeView ───────────────────────────────────────────

    private void buildTubeViews(List<Tube> tubes) {
        tubeContainer.removeAllViews();
        tubeViews.clear();

        int count = tubes.size();
        // Если колб много — разбиваем на 2 ряда
        if (count > 6) {
            buildTwoRowLayout(tubes);
            return;
        }

        for (int i = 0; i < count; i++) {
            addTubeView(tubeContainer, tubes.get(i), i);
        }
    }

    private void buildTwoRowLayout(List<Tube> tubes) {
        tubeContainer.setOrientation(LinearLayout.VERTICAL);
        tubeContainer.setGravity(Gravity.CENTER);

        int half = (tubes.size() + 1) / 2;

        LinearLayout row1 = new LinearLayout(this);
        row1.setGravity(Gravity.CENTER);
        for (int i = 0; i < half; i++) addTubeView(row1, tubes.get(i), i);

        LinearLayout row2 = new LinearLayout(this);
        row2.setGravity(Gravity.CENTER);
        row2.setPadding(0, dp(12), 0, 0);
        for (int i = half; i < tubes.size(); i++) addTubeView(row2, tubes.get(i), i);

        tubeContainer.addView(row1);
        tubeContainer.addView(row2);
    }

    private void addTubeView(LinearLayout parent, Tube tube, int index) {
        TubeView tv = new TubeView(this);
        tv.setTube(tube);
        tv.setOnTubeClickListener(v -> gameManager.onTubeClicked(index));

        int size = dp(72);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, dp(260));
        params.setMargins(dp(4), 0, dp(4), 0);
        parent.addView(tv, params);
        tubeViews.add(tv);
    }

    // ── Монетизация (заглушки) ────────────────────────────────────────

    private void showHintAd() {
        // TODO: показать рекламу, потом вызвать showHint()
        showHint();
    }

    private void showHint() {
        int[] hint = gameManager.findHint();
        if (hint == null) {
            Toast.makeText(this, "Нет доступных ходов!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Мигаем нужными колбами
        blinkTube(tubeViews.get(hint[0]), 0);
        blinkTube(tubeViews.get(hint[1]), 200);
    }

    private void blinkTube(TubeView tv, long delay) {
        tv.postDelayed(() -> {
            tv.setSelected(true);
            tv.postDelayed(() -> {
                tv.setSelected(false);
                tv.postDelayed(() -> {
                    tv.setSelected(true);
                    tv.postDelayed(() -> tv.setSelected(false), 300);
                }, 200);
            }, 300);
        }, delay);
    }

    private void showSkipAd() {
        // TODO: показать рекламу, потом вызвать gameManager.nextLevel()
        new AlertDialog.Builder(this)
            .setTitle("Пропустить уровень?")
            .setMessage("В реальной игре здесь будет реклама.")
            .setPositiveButton("Пропустить", (d, w) -> gameManager.nextLevel())
            .setNegativeButton("Отмена", null)
            .show();
    }

    // ── Звук ─────────────────────────────────────────────────────────

    private void setupSound() {
        AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        soundPool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build();

        // Генерируем короткий "pop" PCM звук программно
        soundPop = generatePopSound();
    }

    private int generatePopSound() {
        // Простой синусоидальный чирп 600 → 900 Гц, 80мс
        int sampleRate = 22050;
        int frames = sampleRate / 12; // ~83мс
        short[] pcm = new short[frames];
        for (int i = 0; i < frames; i++) {
            float t    = (float) i / sampleRate;
            float freq = 600f + 300f * t * 12;
            float env  = (float) Math.sin(Math.PI * i / frames);
            pcm[i] = (short) (env * 12000 * Math.sin(2 * Math.PI * freq * t));
        }
        byte[] bytes = new byte[pcm.length * 2];
        for (int i = 0; i < pcm.length; i++) {
            bytes[2 * i]     = (byte) (pcm[i] & 0xFF);
            bytes[2 * i + 1] = (byte) ((pcm[i] >> 8) & 0xFF);
        }
        // Записываем во временный WAV-файл
        try {
            java.io.File tmp = java.io.File.createTempFile("pop", ".wav", getCacheDir());
            writeWav(tmp, bytes, sampleRate);
            return soundPool.load(tmp.getAbsolutePath(), 1);
        } catch (Exception e) {
            return 0;
        }
    }

    private void writeWav(java.io.File file, byte[] pcm, int sr) throws Exception {
        int dataLen = pcm.length;
        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
        // WAV header (44 байта)
        fos.write(new byte[]{'R','I','F','F'});
        writeInt(fos, 36 + dataLen);
        fos.write(new byte[]{'W','A','V','E','f','m','t',' '});
        writeInt(fos, 16);
        writeShort(fos, (short)1);  // PCM
        writeShort(fos, (short)1);  // моно
        writeInt(fos, sr);
        writeInt(fos, sr * 2);
        writeShort(fos, (short)2);
        writeShort(fos, (short)16);
        fos.write(new byte[]{'d','a','t','a'});
        writeInt(fos, dataLen);
        fos.write(pcm);
        fos.close();
    }

    private void writeInt(java.io.OutputStream os, int v) throws Exception {
        os.write(v & 0xFF); os.write((v>>8)&0xFF); os.write((v>>16)&0xFF); os.write((v>>24)&0xFF);
    }
    private void writeShort(java.io.OutputStream os, short v) throws Exception {
        os.write(v & 0xFF); os.write((v>>8)&0xFF);
    }

    // ── Вспомогательные UI хелперы ────────────────────────────────────

    private Button makeButton(String text, String hexColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14f);
        btn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        btn.setBackgroundColor(Color.parseColor(hexColor));
        btn.setAllCaps(false);
        btn.setPadding(dp(8), 0, dp(8), 0);
        return btn;
    }

    private LinearLayout makeButtonRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        return row;
    }

    private View makeSpace(int size) {
        View v = new View(this);
        v.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        return v;
    }

    private int dp(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) soundPool.release();
    }
}
