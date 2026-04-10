package com.watersort.puzzle;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GameManager.GameListener {

    private final GameManager gameManager = new GameManager();
    private LinearLayout tubeContainer;
    private TextView levelText;
    private final List<TubeView> tubeViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#181C2E"));
        setContentView(root);

        levelText = new TextView(this);
        levelText.setTextColor(Color.WHITE);
        levelText.setTextSize(22f);
        levelText.setGravity(Gravity.CENTER);
        levelText.setPadding(0, dp(16), 0, dp(16));
        root.addView(levelText, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tubeContainer = new LinearLayout(this);
        tubeContainer.setOrientation(LinearLayout.HORIZONTAL);
        tubeContainer.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        root.addView(tubeContainer, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Button btnRestart = new Button(this);
        btnRestart.setText("↺ Restart");
        btnRestart.setBackgroundColor(Color.parseColor("#4085F5"));
        btnRestart.setTextColor(Color.WHITE);
        btnRestart.setOnClickListener(v -> gameManager.restartLevel());
        root.addView(btnRestart, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        gameManager.setListener(this);
        gameManager.loadLevel(1);
    }

    @Override
    public void onLevelLoaded(int level, List<Tube> tubes) {
        levelText.setText("Level " + level);
        tubeContainer.removeAllViews();
        tubeViews.clear();
        for (int i = 0; i < tubes.size(); i++) {
            TubeView tv = new TubeView(this);
            tv.setTube(tubes.get(i));
            tv.setTag(i);
            tv.setOnTubeClickListener(v -> {
                int idx = (int) v.getTag();
                gameManager.onTubeClicked(idx);
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(72), dp(260));
            p.setMargins(dp(4), 0, dp(4), 0);
            tubeContainer.addView(tv, p);
            tubeViews.add(tv);
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
    public void onMoveInvalid() {}

    @Override
    public void onLevelComplete() {
        levelText.setText("🎉 Level Complete!");
    }

    private int dp(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
