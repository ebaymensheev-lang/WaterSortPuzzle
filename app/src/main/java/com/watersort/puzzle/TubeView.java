package com.watersort.puzzle;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;

public class TubeView extends View {

    public interface OnTubeClickListener {
        void onTubeClick(TubeView tubeView);
    }

    private Tube tube;
    private boolean selected = false;
    private OnTubeClickListener listener;

    // Краски
    private final Paint jarPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint jarBorder   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint candyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shinePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint solvedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lidPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TubeView(Context context) {
        super(context);

        // Стекло банки
        jarPaint.setColor(Color.parseColor("#E8F4FD"));
        jarPaint.setAlpha(180);
        jarPaint.setStyle(Paint.Style.FILL);

        // Рамка банки
        jarBorder.setColor(Color.parseColor("#B8D4E8"));
        jarBorder.setStyle(Paint.Style.STROKE);
        jarBorder.setStrokeWidth(3f);

        // Блик на конфете
        shinePaint.setColor(Color.WHITE);
        shinePaint.setAlpha(120);
        shinePaint.setStyle(Paint.Style.FILL);

        // Тень
        shadowPaint.setColor(Color.parseColor("#44000000"));
        shadowPaint.setStyle(Paint.Style.FILL);

        // Рамка решённой банки
        solvedPaint.setColor(Color.parseColor("#FFD93D"));
        solvedPaint.setStyle(Paint.Style.STROKE);
        solvedPaint.setStrokeWidth(5f);

        // Крышка банки
        lidPaint.setStyle(Paint.Style.FILL);
    }

    public void setTube(Tube tube) {
        this.tube = tube;
        invalidate();
    }

    public Tube getTube() { return tube; }

    public void setOnTubeClickListener(OnTubeClickListener l) {
        this.listener = l;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null) listener.onTubeClick(this);
            return true;
        }
        return event.getAction() == MotionEvent.ACTION_DOWN;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tube == null) return;

        int w = getWidth();
        int h = getHeight();

        float candyRadius = w / 2f * 0.75f;
        float jarW  = w * 0.82f;
        float left  = (w - jarW) / 2f;
        float bottom = h - dp(8);
        float jarH  = candyRadius * 2.1f * tube.getMaxCapacity() + dp(8);
        float top   = bottom - jarH;
        float r     = jarW / 2f;

        // Смещение вверх если выбрана
        float offsetY = selected ? -dp(22) : 0;
        canvas.save();
        canvas.translate(0, offsetY);

        // Рисуем банку (прямоугольник с закруглённым дном)
        Path jarPath = new Path();
        jarPath.moveTo(left, top + dp(16));
        jarPath.lineTo(left, bottom - r);
        jarPath.arcTo(new RectF(left, bottom - jarW, left + jarW, bottom), 180, -180, false);
        jarPath.lineTo(left + jarW, top + dp(16));
        jarPath.close();

        // Тень банки
        canvas.save();
        canvas.translate(dp(3), dp(3));
        canvas.drawPath(jarPath, shadowPaint);
        canvas.restore();

        // Тело банки
        canvas.drawPath(jarPath, jarPaint);

        // Рисуем конфеты
        float cx = w / 2f;
        int count = tube.getCount();
        for (int i = 0; i < count; i++) {
            Ball ball = tube.getBallAt(i);
            if (ball == null) continue;

            float cy = bottom - candyRadius * 0.6f
                     - (i * candyRadius * 2.1f) - candyRadius;

            // Тень конфеты
            shadowPaint.setAlpha(50);
            canvas.drawCircle(cx + dp(2), cy + dp(2), candyRadius, shadowPaint);

            // Конфета
            candyPaint.setColor(ball.getColor());
            canvas.drawCircle(cx, cy, candyRadius, candyPaint);

            // Блик (делает конфету объёмной)
            canvas.drawCircle(
                cx - candyRadius * 0.28f,
                cy - candyRadius * 0.28f,
                candyRadius * 0.35f,
                shinePaint
            );

            // Маленький блик
            shinePaint.setAlpha(60);
            canvas.drawCircle(
                cx + candyRadius * 0.2f,
                cy + candyRadius * 0.2f,
                candyRadius * 0.15f,
                shinePaint
            );
            shinePaint.setAlpha(120);
        }

        // Рамка банки (поверх конфет)
        Paint borderToDraw = tube.isSolved() ? solvedPaint : jarBorder;
        if (selected) {
            Paint selPaint = new Paint(jarBorder);
            selPaint.setColor(Color.parseColor("#FF6B9D"));
            selPaint.setStrokeWidth(5f);
            canvas.drawPath(jarPath, selPaint);
        } else {
            canvas.drawPath(jarPath, borderToDraw);
        }

        // Крышка банки (горлышко)
        float lidW = jarW * 0.7f;
        float lidL = (w - lidW) / 2f;
        float lidH = dp(12);
        RectF lidRect = new RectF(lidL, top, lidL + lidW, top + lidH);

        if (tube.isSolved() && !tube.isEmpty()) {
            // Золотая крышка если решена
            lidPaint.setColor(Color.parseColor("#FFD93D"));
        } else {
            lidPaint.setColor(Color.parseColor("#B8D4E8"));
        }
        canvas.drawRoundRect(lidRect, dp(4), dp(4), lidPaint);

        // Блик на крышке
        Paint lidShine = new Paint(Paint.ANTI_ALIAS_FLAG);
        lidShine.setColor(Color.WHITE);
        lidShine.setAlpha(80);
        lidShine.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(
            new RectF(lidL + dp(4), top + dp(2), lidL + lidW - dp(4), top + lidH / 2f),
            dp(3), dp(3), lidShine
        );

        canvas.restore();
    }

    private float dp(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
