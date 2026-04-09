package com.watersort.puzzle;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#181C2E"));
        layout.setGravity(Gravity.CENTER);

        TextView text = new TextView(this);

        try {
            LevelManager lm = new LevelManager();
            java.util.List<Tube> tubes = lm.generateLevel(1);

            // Пробуем создать TubeView
            TubeView tv = new TubeView(this);
            tv.setTube(tubes.get(0));

            text.setText("TubeView OK ✅");
            layout.addView(text);
            layout.addView(tv, new LinearLayout.LayoutParams(dp(80), dp(280)));

        } catch (Exception e) {
            text.setText("ОШИБКА в TubeView:\n" + e.getMessage());
            layout.addView(text);
        }

        text.setTextColor(Color.WHITE);
        text.setTextSize(24f);
        text.setGravity(Gravity.CENTER);
        setContentView(layout);
    }

    private int dp(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
