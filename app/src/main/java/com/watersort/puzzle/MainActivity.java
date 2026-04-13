package com.watersort.puzzle;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GameManager.GameListener {

    private final GameManager gameManager = new GameManager();
    private LinearLayout tubeContainer;
    private TextView levelText;
    private TextView winBanner;
    private final List<TubeView> tubeViews = new ArrayList<>();
    private ToneGenerator toneGenerator;
    private boolean isPouring = false; // блокировка во время анимации

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        } catch (Exception e) {
            toneGenerator = null;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1A1C2A"));
        setContentView(root);

        // Верхняя панель
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.VERTICAL);
        topBar.setGravity(Gravity.CENTER);
        topBar.setBackgroundColor(Color.parseColor("#2A2D3E"));
        topBar.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.addView(topBar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView titleText = new TextView(this);
        titleText.setText("🏖️ Sand Sort");
        titleText.setTextColor(Color.parseColor("#FFD93D"));
        titleText.setTextSize(26f);
        titleText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleText.setGravity(Gravity.CENTER);
        topBar.addView(titleText, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        levelText = new TextView(this);
        levelText.setTextColor(Color.parseColor("#AABBCC"));
        levelText.setTextSize(15f);
        levelText.setGravity(Gravity.CENTER);
        topBar.addView(levelText, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Поле колб
        tubeContainer = new LinearLayout(this);
        tubeContainer.setOrientation(LinearLayout.HORIZONTAL);
        tubeContainer.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        tubeContainer.setPadding(dp(8), dp(16), dp(8), dp(8));
        root.addView(tubeContainer, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Баннер победы
        winBanner = new TextView(this);
        winBanner.setTextColor(Color.WHITE);
        winBanner.setTextSize(20f);
        winBanner.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        winBanner.setGravity(Gravity.CENTER);
        winBanner.setBackgroundColor(Color.parseColor("#CC2A2D3E"));
        winBanner.setPadding(dp(24), dp(16), dp(24), dp(16));
        winBanner.setVisibility(View.GONE);
        root.addView(winBanner, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Нижняя панель
        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.VERTICAL);
        bottomBar.setBackgroundColor(Color.parseColor("#2A2D3E"));
        bottomBar.setPadding(dp(12), dp(8), dp(12), dp(20));
        root.addView(bottomBar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout row1 = makeRow();
        bottomBar.addView(row1, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button btnRestart = makeButton("↺  Restart", "#3A5A8A");
        Button btnUndo    = makeButton("↩  Undo",    "#5A3A8A");
        row1.addView(btnRestart, new LinearLayout.LayoutParams(0, dp(52), 1f));
        row1.addView(space());
        row1.addView(btnUndo, new LinearLayout.LayoutParams(0, dp(52), 1f));

        LinearLayout row2 = makeRow();
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.topMargin = dp(8);
        bottomBar.addView(row2, rp);
        Button btnHint = makeButton("💡  Hint", "#8A6A1A");
        Button btnNext = makeButton("⏭  Next",  "#1A6A3A");
        row2.addView(btnHint, new LinearLayout.LayoutParams(0, dp(52), 1f));
        row2.addView(space());
        row2.addView(btnNext, new LinearLayout.LayoutParams(0, dp(52), 1f));

        btnRestart.setOnClickListener(v -> { if (!isPouring) gameManager.restartLevel(); });
        btnUndo.setOnClickListener(v -> { if (!isPouring) gameManager.undoMove(); });
        btnHint.setOnClickListener(v -> { if (!isPouring) showHint(); });
        btnNext.setOnClickListener(v -> { if (!isPouring) gameManager.nextLevel(); });

        gameManager.setListener(this);

        int savedLevel = getSharedPreferences("sand_sort", MODE_PRIVATE)
            .getInt("level", 1);
        gameManager.loadLevel(savedLevel);
    }

    // Звук шороха песка — несколько тонов быстро
    private void playSandSound() {
        try {
            if (toneGenerator == null) return;
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 60);
            new android.os.Handler().postDelayed(() -> {
                try {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 40);
                } catch (Exception e) {}
            }, 80);
            new android.os.Handler().postDelayed(() -> {
                try {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 30);
                } catch (Exception e) {}
            }, 150);
        } catch (Exception e) {}
    }

    private void playWinSound() {
        try {
            if (toneGenerator == null) return;
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400);
        } catch (Exception e) {}
    }

    @Override
    public void onLevelLoaded(int level, List<Tube> tubes) {
        levelText.setText("Уровень " + level + " из " + LevelManager.TOTAL_LEVELS);
        winBanner.setVisibility(View.GONE);
        isPouring = false;
        tubeContainer.removeAllViews();
        tubeViews.clear();
        tubeContainer.setOrientation(LinearLayout.HORIZONTAL);
        tubeContainer.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        if (tubes.size() > 6) {
            buildTwoRows(tubes);
        } else {
            for (int i = 0; i < tubes.size(); i++)
                addTubeView(tubeContainer, tubes.get(i), i);
        }
    }

    @Override
    public void onSelectionChanged(int selectedIndex) {
        for (int i = 0; i < tubeViews.size(); i++)
            tubeViews.get(i).setSelected(i == selectedIndex);
    }

    @Override
    public void onMoveSuccess(int fromIndex, int toIndex) {
        isPouring = true;

        // Анимация — колба-источник поднимается и пересыпает
        TubeView fromView = fromIndex < tubeViews.size() ? tubeViews.get(fromIndex) : null;
        TubeView toView   = toIndex   < tubeViews.size() ? tubeViews.get(toIndex)   : null;

        playSandSound();

        // Анимируем наклон источника
        if (fromView != null) {
            ObjectAnimator tilt = ObjectAnimator.ofFloat(fromView, "rotation", 0f, -15f, 0f);
            tilt.setDuration(400);
            tilt.start();
        }

        // Анимируем получателя — песок "падает"
        if (toView != null) {
            toView.animatePour(() -> {
                isPouring = false;
                if (fromView != null) fromView.invalidate();
                toView.invalidate();
            });
        } else {
            isPouring = false;
        }
    }

    @Override
    public void onMoveInvalid() {
        int sel = gameManager.getSelectedIndex();
        if (sel >= 0 && sel < tubeViews.size()) {
            ObjectAnimator shake = ObjectAnimator.ofFloat(
                tubeViews.get(sel), "translationX", 0, -12, 12, -6, 6, 0);
            shake.setDuration(300);
            shake.start();
        }
    }

    @Override
    public void onLevelComplete() {
        playWinSound();

        int next = gameManager.getCurrentLevel() + 1;
        if (next <= LevelManager.TOTAL_LEVELS) {
            getSharedPreferences("sand_sort", MODE_PRIVATE)
                .edit().putInt("level", next).apply();
        }

        winBanner.setText("🎉 Уровень пройден! Нажмите Next!");
        winBanner.setVisibility(View.VISIBLE);
        winBanner.setAlpha(0f);
        winBanner.animate().alpha(1f).setDuration(500).start();

        for (int i = 0; i < tubeViews.size(); i++) {
            final TubeView tv = tubeViews.get(i);
            tv.postDelayed(() -> {
                ObjectAnimator b = ObjectAnimator.ofFloat(tv, "translationY", 0, -dp(25), 0);
                b.setInterpolator(new BounceInterpolator());
                b.setDuration(700);
                b.start();
            }, i * 100L);
        }
    }

    private void buildTwoRows(List<Tube> tubes) {
        tubeContainer.setOrientation(LinearLayout.VERTICAL);
        tubeContainer.setGravity(Gravity.CENTER);
        int half = (tubes.size() + 1) / 2;
        LinearLayout row1 = makeRow();
        row1.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        LinearLayout row2 = makeRow();
        row2.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        LinearLayout.LayoutParams r2p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        r2p.topMargin = dp(12);
        for (int i = 0; i < half; i++) addTubeView(row1, tubes.get(i), i);
        for (int i = half; i < tubes.size(); i++) addTubeView(row2, tubes.get(i), i);
        tubeContainer.addView(row1);
        tubeContainer.addView(row2, r2p);
    }

    private void addTubeView(LinearLayout parent, Tube tube, int index) {
        TubeView tv = new TubeView(this);
        tv.setTube(tube);
        tv.setTag(index);
        tv.setOnTubeClickListener(v -> {
            if (!isPouring) {
                int idx = (int) v.getTag();
                gameManager.onTubeClicked(idx);
            }
        });
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(68), dp(280));
        p.setMargins(dp(4), 0, dp(4), 0);
        parent.addView(tv, p);
        tubeViews.add(tv);
    }

    private void showHint() {
        int[] hint = gameManager.findHint();
        if (hint == null) {
            Toast.makeText(this, "Нет доступных ходов!", Toast.LENGTH_SHORT).show();
            return;
        }
        blinkTube(tubeViews.get(hint[0]), 0);
        blinkTube(tubeViews.get(hint[1]), 300);
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

    private Button makeButton(String text, String color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14f);
        btn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        btn.setBackgroundColor(Color.parseColor(color));
        btn.setAllCaps(false);
        return btn;
    }

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        return row;
    }

    private Space space() {
        Space v = new Space(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(8), dp(8)));
        return v;
    }

    private int dp(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
