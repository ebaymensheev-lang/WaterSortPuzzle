package com.watersort.puzzle;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#181C2E"));
        root.setGravity(Gravity.CENTER);
        setContentView(root);

        TextView text = new TextView(this);
        text.setTextColor(Color.WHITE);
        text.setTextSize(20f);
        text.setGravity(Gravity.CENTER);

        try {
            // Только создаём TubeView и вешаем listener
            Tube tube = new Tube(4);
            tube.addBall(new Ball(Color.RED));
            tube.addBall(new Ball(Color.BLUE));

            TubeView tv = new TubeView(this);
            tv.setTube(tube);
            tv.setOnTubeClickListener(v -> {
                text.setText("Клик работает! ✅");
            });

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                dp(80), dp(280));
            root.addView(tv, p);
            text.setText("TubeView + клик готов ✅");

        } catch (Exception e) {
            text.setText("ОШИБКА: " + e.getMessage());
        }

        root.addView(text);
    }

    private int dp(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
