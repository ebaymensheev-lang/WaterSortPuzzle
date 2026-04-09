package com.watersort.puzzle;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GameManager.GameListener {

    private final GameManager gameManager = new GameManager();
    private LinearLayout tubeContainer;
    private final List<TubeView> tubeViews = new ArrayList<>();
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#181C2E"));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        setContentView(root);

        statusText = new TextView(this);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(18f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setText("Загрузка...");
        root.addView(statusText);

        tubeContainer = new LinearLayout(this);
        tubeContainer.setOrientation(LinearLayout.HORIZONTAL);
        tubeContainer.setGravity(Gravity.CENTER);
        root.addView(tubeContainer, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        try {
            statusText.setText("Шаг 1: setListener...");
            gameManager.setListener(this);
            statusText.setText("Шаг 2: loadLevel...");
            gameManager.loadLevel(1);
            statusText.setText("Шаг 3: готово ✅");
        } catch (Exception e) {
            statusText.setText("ОШИБКА: " + e.getMessage());
        }
    }

    @Override
    public void onLevelLoaded(int level, List<Tube> tubes) {
        try {
            statusText.setText("onLevelLoaded: " + tubes.size() + " труб...");
            tubeContainer.removeAllViews();
            tubeViews.clear();
            for (int i = 0; i < tubes.size(); i++) {
                TubeView tv = new TubeView(this);
                tv.setTube(tubes.get(i));
                final int idx = i;
                tv.setOnTubeClickListener(v -> gameManager.onTubeClicked(idx));
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(72), dp(260));
                p.setMargins(dp(4), 0, dp(4), 0);
                tubeContainer.addView(tv, p);
                tubeViews.add(tv);
                statusText.setText("Добавлена труба " + (i+1));
            }
            statusText.setText("Все трубы OK ✅ Level " + level);
        } catch (Exception e) {
            statusText.setText("ОШИБКА в onLevelLoaded: " + e.getMessage());
        }
    }

    @Override public void onSelectionChanged(int i) {}
    @Override public void onMoveSuccess(int f, int t) {}
    @Override public void onMoveInvalid() {}
    @Override public void onLevelComplete() {
        statusText.setText("Уровень пройден! 🎉");
    }

    private int dp(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
