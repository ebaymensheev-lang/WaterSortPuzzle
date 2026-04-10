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

    private final Paint tubePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ballPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TubeView(Context context) {
        super(context);

        tubePaint.setColor(Color.parseColor("#CCE8F7"));
        tubePaint.setAlpha(120);
        tubePaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(Color.parseColor("#AACCEE"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        highlightPaint.setColor(Color.WHITE);
        highlightPaint.setAlpha(80);
        highlightPaint.setStyle(Paint.Style.FILL);
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
        float ballRadius = w / 2f * 0.8f;
        float tubeW = w * 0.84f;
        float left = (w - tubeW) / 2f;
        float bottom = h - dp(4);
        float tubeH = ballRadius * 2 * tube.getMaxCapacity() + ballRadius * 0.5f;
        float top = bottom - tubeH;

        // Рисуем колбу (U-форма)
        float r = tubeW / 2f;
        Path path = new Path();
        path.moveTo(left, top);
        path.lineTo(left, bottom - r);
        path.arcTo(new RectF(left, bottom - tubeW, left + tubeW, bottom), 180, -180, false);
        path.lineTo(left + tubeW, top);

        // Смещение если выбрана
        float offsetY = selected ? -dp(20) : 0;
        canvas.save();
        canvas.translate(0, offsetY);

        canvas.drawPath(path, tubePaint);

        borderPaint.setColor(selected
            ? Color.parseColor("#F5D920")
            : Color.parseColor("#AACCEE"));
        canvas.drawPath(path, borderPaint);

        // Рисуем шарики
        float cx = w / 2f;
        int count = tube.getCount();
        for (int i = 0; i < count; i++) {
            Ball ball = tube.getBallAt(i);
            if (ball == null) continue;
            float cy = bottom - ballRadius * 0.5f - (i * ballRadius * 2f) - ballRadius;
            ballPaint.setColor(ball.getColor());
            canvas.drawCircle(cx, cy, ballRadius, ballPaint);
            canvas.drawCircle(cx - ballRadius * 0.25f, cy - ballRadius * 0.25f,
                ballRadius * 0.3f, highlightPaint);
        }

        canvas.restore();
    }

    private float dp(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
