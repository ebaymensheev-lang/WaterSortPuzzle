package com.watersort.puzzle;

import android.animation.ObjectAnimator;
import android.graphics.Color;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#181C2E"));
        setContentView(root);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER);
        topBar.setBackgroundColor(Color.parseColor("#1E2438"));
        topBar.setPadding(dp(16), dp(14), dp(16), dp(14));
        root.addView(topBar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        levelText = new TextView(this);
        levelText.setTextColor(Color.WHITE);
        levelText.setTextSize(22f);
        levelText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        levelText.setGravity(Gravity.CENTER);
        topBar.addView(levelText, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tubeContainer = new LinearLayout(this);
        tubeContainer.setOrientation(LinearLayout.HORIZONTAL);
        tubeContainer.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        tubeContainer.setPadding(dp(8), dp(16), dp(8), dp(8));
        root.addView(tubeContainer, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        winBanner = new TextView(this);
        winBanner.setTextColor(Color.parseColor("#F5D920"));
        winBanner.setTextSize(22f);
        winBanner.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        winBanner.setGravity(Gravity.CENTER);
        winBanner.setBackgroundColor(Color.parseColor("#CC1E2438"));
        winBanner.setPadding(dp(24), dp(16), dp(24), dp(16));
        winBanner.setVisibility(View.GONE);
        root.addView(winBanner, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.VERTICAL);
        bottomBar.setBackgroundColor(Color.parseColor("#1E2438"));
        bottomBar.setPadding(dp(12), dp(8), dp(12), dp(20));
        root.addView(bottomBar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout row1 = makeRow();
        bottomBar.addView(row1, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button btnRestart = makeButton("↺  Restart", "#4085F5");
        Button btnUndo    = makeButton("↩  Undo",    "#6E7191");
        row1.addView(btnRestart, new LinearLayout.LayoutParams(0, dp(52), 1f));
        row1.addView(space());
        row1.addView(btnUndo, new LinearLayout.LayoutParams(0, dp(52), 1f));

        LinearLayout row2 = makeRow();
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.topMargin = dp(8);
        bottomBar.addView(row2, rp);
        Button btnHint = makeButton("💡  Hint", "#E68A00");
        Button btnNext = makeButton("⏭  Next", "#38C664");
        row2.addView(btnHint, new LinearLayout.LayoutParams(0, dp(52), 1f));
        row2.addView(space());
        row2.addView(btnNext, new LinearLayout.LayoutParams(0, dp(52), 1f));

        btnRestart.setOnClickListener(v -> gameManager.restartLevel());
        btnUndo.setOnClickListener(v -> gameManager.undoMove());
        btnHint.setOnClickListener(v -> showHint());
        btnNext.setOnClickListener(v -> gameManager.nextLevel());

        gameManager.setListener(this);
        gameManager.loadLevel(1);
    }

    @Override
    public void onLevelLoaded(int level, List<Tube> tubes) {
        levelText.setText("Level " + level);
        winBanner.setVisibility(View.GONE);
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
    public void onMoveSuccess(int f, int t) {
        if (f < tubeViews.size()) tubeViews.get(f).invalidate();
        if (t < tubeViews.size()) tubeViews.get(t).invalidate();
    }

    @Override
    public void onMoveInvalid() {
        int sel = gameManager.getSelectedIndex();
        if (sel >= 0 && sel < tubeViews.size()) {
            ObjectAnimator shake = ObjectAnimator.ofFloat(
                tubeViews.get(sel), "translationX", 0, -14, 14, -8, 8, 0);
            shake.setDuration(280);
            shake.start();
        }
    }

    @Override
    public void onLevelComplete() {
        winBanner.setText("🎉 Level " + gameManager.getCurrentLevel() + " Complete! Нажмите Next!");
        winBanner.setVisibility(View.VISIBLE);
        winBanner.setAlpha(0f);
        winBanner.animate().alpha(1f).setDuration(400).start();
        for (int i = 0; i < tubeViews.size(); i++) {
            final TubeView tv = tubeViews.get(i);
            tv.postDelayed(() -> {
                ObjectAnimator b = ObjectAnimator.ofFloat(tv, "translationY", 0, -dp(30), 0);
                b.setInterpolator(new BounceInterpolator());
                b.setDuration(600);
                b.start();
            }, i * 80L);
        }
    }

    private void buildTwoRows(List<Tube> tubes) {
        tubeContainer.setOrientation(LinearLayout.VERTICAL);
        tubeContainer.setGravity(Gravity.CENTER);
        int half = (tubes.size() + 1) / 2;
        LinearLayout row1 = makeRow();
        row1.setGravity(Gravity.CENTER);
        LinearLayout row2 = makeRow();
        row2.setGravity(Gravity.CENTER);
        for (int i = 0; i < half; i++) addTubeView(row1, tubes.get(i), i);
        for (int i = half; i < tubes.size(); i++) addTubeView(row2, tubes.get(i), i);
        tubeContainer.addView(row1);
        tubeContainer.addView(row2);
    }

    private void addTubeView(LinearLayout parent, Tube tube, int index) {
        TubeView tv = new TubeView(this);
        tv.setTube(tube);
        tv.setTag(index);
        tv.setOnTubeClickListener(v -> {
            int idx = (int) v.getTag();
            gameManager.onTubeClicked(idx);
        });
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(72), dp(260));
        p.setMargins(dp(4), 0, dp(4), 0);
        parent.addView(tv, p);
        tubeViews.add(tv);
    }

    private void showHint() {
        int[] hint = gameManager.findHint();
        if (hint == null) {
            Toast.makeText(this, "Нет ходов!", Toast.LENGTH_SHORT).show();
            return;
        }
        blinkTube(tubeViews.get(hint[0]), 0);
        blinkTube(tubeViews.get(hint[1]), 250);
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
}
