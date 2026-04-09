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
        text.setText("Игра работает! ✅");
        text.setTextColor(Color.WHITE);
        text.setTextSize(32f);

        layout.addView(text);
        setContentView(layout);
    }
}
