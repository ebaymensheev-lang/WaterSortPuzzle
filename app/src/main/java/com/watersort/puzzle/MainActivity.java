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
        layout.setBackgroundColor(Color.parseColor("#181C2E"));
        layout.setGravity(Gravity.CENTER);

        TextView text = new TextView(this);

        try {
            GameManager gm = new GameManager();
            LevelManager lm = new LevelManager();
            java.util.List<Tube> tubes = lm.generateLevel(1);
            text.setText("GameManager OK ✅\nТруб: " + tubes.size());
        } catch (Exception e) {
            text.setText("ОШИБКА:\n" + e.getMessage());
        }

        text.setTextColor(Color.WHITE);
        text.setTextSize(24f);
        text.setGravity(Gravity.CENTER);
        layout.addView(text);
        setContentView(layout);
    }
}
