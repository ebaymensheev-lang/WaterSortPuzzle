package com.watersort.puzzle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class TubeView extends View {

    public interface OnTubeClickListener {
        void onTubeClick(TubeView tubeView);
    }

    private Tube tube;
    private boolean selected = false;
    private OnTubeClickListener listener;

    // Анимация песка
    private float animOffset = 0f;      // смещение песка при анимации
    private boolean isAnimating = false;
    private ValueAnimator sandAnimator;

    // Краски
    private final Paint glassPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glassBorder  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sandPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shinePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint solvedPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TubeView(Context context) {
        super(context);

        // Стекло колбы
        glassPaint.setColor(Color.parseColor("#D0E8F5"));
        glassPaint.setAlpha(100);
        glassPaint.setStyle(Paint.Style.FILL);

        // Рамка колбы
        glassBorder.setColor(Color.parseColor("#90B8D0"));
        glassBorder.setStyle(Paint.Style.STROKE);
        glassBorder.setStrokeWidth(dp(2.5f));

        // Блик
        shinePaint.setColor(Color.WHITE);
        shinePaint.setAlpha(60);
        shinePaint.setStyle(Paint.Style.FILL);

        // Тень
        shadowPaint.setColor(Color.parseColor("#55000000"));
        shadowPaint.setStyle(Paint.Style.FILL);

        // Решённая колба
        solvedPaint.setColor(Color.parseColor("#FFD93D"));
        solvedPaint.setStyle(Paint.Style.STROKE);
        solvedPaint.setStrokeWidth(dp(3f));

        // Выбранная колба
        selectedPaint.setColor(Color.parseColor("#54C6EB"));
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(dp(3.5f));

        // Частицы песка
        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setAlpha(180);
    }

    public void setTube(Tube tube) { this.tube = tube; invalidate(); }
    public Tube getTube() { return tube; }
    public void setOnTubeClickListener(OnTubeClickListener l) { this.listener = l; }

    @Override
    public void setSelected(boolean sel) {
        this.selected = sel;
        if (sel) startSandAnimation();
        else stopSandAnimation();
        invalidate();
    }

    // Анимация песка — колышется когда выбрана
    private void startSandAnimation() {
        if (sandAnimator != null) sandAnimator.cancel();
        sandAnimator = ValueAnimator.ofFloat(0f, (float)(Math.PI * 2));
        sandAnimator.setDuration(1200);
        sandAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sandAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        sandAnimator.addUpdateListener(a -> {
            animOffset = (float) a.getAnimatedValue();
            invalidate();
        });
        sandAnimator.start();
        isAnimating = true;
    }

    private void stopSandAnimation() {
        if (sandAnimator != null) sandAnimator.cancel();
        animOffset = 0f;
        isAnimating = false;
        invalidate();
    }

    // Анимация пересыпания (вызывается снаружи)
    public void animatePour(Runnable onComplete) {
        ValueAnimator pour = ValueAnimator.ofFloat(0f, 1f);
        pour.setDuration(500);
        pour.addUpdateListener(a -> {
            animOffset = (float) a.getAnimatedValue() * 20f;
            invalidate();
        });
        pour.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                animOffset = 0f;
                invalidate();
                if (onComplete != null) onComplete.run();
            }
        });
        pour.start();
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

        float tubeW  = w - dp(10);
        float left   = (w - tubeW) / 2f;
        float right  = left + tubeW;
        float bottom = h - dp(6);
        float r      = tubeW / 2f;
        float tubeH  = h - dp(20);
        float top    = bottom - tubeH;

        float offsetY = selected ? -dp(20) : 0f;
        canvas.save();
        canvas.translate(0, offsetY);

        // Путь колбы (U-форма)
        Path tubePath = new Path();
        tubePath.moveTo(left, top);
        tubePath.lineTo(left, bottom - r);
        tubePath.arcTo(new RectF(left, bottom - tubeW, right, bottom), 180, -180, false);
        tubePath.lineTo(right, top);

        // Тень
        canvas.save();
        canvas.translate(dp(3), dp(3));
        shadowPaint.setAlpha(40);
        canvas.drawPath(tubePath, shadowPaint);
        canvas.restore();

        // Фон колбы
        canvas.drawPath(tubePath, glassPaint);

        // Рисуем слои песка
        drawSand(canvas, left, top, right, bottom, tubeW, tubeH, r);

        // Стекло поверх (полупрозрачное)
        Paint glassOverlay = new Paint(glassPaint);
        glassOverlay.setAlpha(40);
        canvas.drawPath(tubePath, glassOverlay);

        // Рамка
        if (selected) {
            canvas.drawPath(tubePath, selectedPaint);
        } else if (tube.isSolved()) {
            canvas.drawPath(tubePath, solvedPaint);
        } else {
            canvas.drawPath(tubePath, glassBorder);
        }

        // Блик слева (имитация стекла)
        float blinkW = tubeW * 0.18f;
        RectF blinkRect = new RectF(left + dp(4), top + dp(8),
            left + dp(4) + blinkW, bottom - r - dp(4));
        Paint blinkPaint = new Paint(shinePaint);
        blinkPaint.setAlpha(50);
        canvas.drawRoundRect(blinkRect, blinkW / 2f, blinkW / 2f, blinkPaint);

        canvas.restore();
    }

    private void drawSand(Canvas canvas, float left, float top, float right,
                           float bottom, float tubeW, float tubeH, float r) {
        int count = tube.getCount();
        if (count == 0) return;

        int maxCap = tube.getMaxCapacity();
        float layerH = (tubeH - r) / maxCap;

        // Обрезаем рисунок по форме колбы
        canvas.save();
        Path clipPath = new Path();
        clipPath.moveTo(left + dp(2), top);
        clipPath.lineTo(left + dp(2), bottom - r);
        clipPath.arcTo(new RectF(left + dp(2), bottom - tubeW + dp(2),
            right - dp(2), bottom - dp(2)), 180, -180, false);
        clipPath.lineTo(right - dp(2), top);
        canvas.clipPath(clipPath);

        // Рисуем каждый слой песка снизу вверх
        for (int i = 0; i < count; i++) {
            Ball ball = tube.getBallAt(i);
            if (ball == null) continue;

            float layerBottom = bottom - dp(2) - i * layerH;
            float layerTop    = layerBottom - layerH;

            // Волнистая верхняя граница песка
            float wave = isAnimating && i == count - 1
                ? (float)(Math.sin(animOffset) * dp(3))
                : 0f;

            // Основной цвет песка
            sandPaint.setColor(ball.getColor());
            sandPaint.setAlpha(230);

            RectF sandRect = new RectF(left + dp(2), layerTop + wave,
                right - dp(2), layerBottom);
            canvas.drawRect(sandRect, sandPaint);

            // Частицы песка (зернистость)
            drawSandParticles(canvas, ball.getColor(), sandRect);

            // Тёмная полоска между слоями
            if (i < count - 1) {
                Paint separator = new Paint(Paint.ANTI_ALIAS_FLAG);
                separator.setColor(Color.parseColor("#33000000"));
                separator.setStyle(Paint.Style.FILL);
                canvas.drawRect(left + dp(2), layerBottom - dp(2),
                    right - dp(2), layerBottom, separator);
            }

            // Блик на верхнем слое
            if (i == count - 1) {
                Paint topShine = new Paint(Paint.ANTI_ALIAS_FLAG);
                topShine.setColor(Color.WHITE);
                topShine.setAlpha(30);
                topShine.setStyle(Paint.Style.FILL);
                canvas.drawRect(left + dp(2), layerTop + wave,
                    right - dp(2), layerTop + wave + dp(4), topShine);
            }
        }

        canvas.restore();
    }

    private void drawSandParticles(Canvas canvas, int baseColor, RectF area) {
        // Рисуем случайные точки для имитации зёрен песка
        int lighter = lightenColor(baseColor, 1.4f);
        int darker  = darkenColor(baseColor, 0.7f);

        particlePaint.setAlpha(120);

        // Используем детерминированные позиции (не случайные, чтобы не мигало)
        float[] offsets = {0.15f, 0.35f, 0.55f, 0.75f, 0.25f, 0.65f, 0.45f, 0.85f};
        float[] vOffsets = {0.2f, 0.5f, 0.7f, 0.3f, 0.8f, 0.4f, 0.6f, 0.1f};

        for (int p = 0; p < 8; p++) {
            float px = area.left + area.width() * offsets[p];
            float py = area.top  + area.height() * vOffsets[p];
            float pr = dp(1.2f);

            particlePaint.setColor(p % 2 == 0 ? lighter : darker);
            canvas.drawCircle(px, py, pr, particlePaint);
        }
    }

    private int lightenColor(int color, float factor) {
        int r = Math.min(255, (int)(Color.red(color)   * factor));
        int g = Math.min(255, (int)(Color.green(color) * factor));
        int b = Math.min(255, (int)(Color.blue(color)  * factor));
        return Color.rgb(r, g, b);
    }

    private int darkenColor(int color, float factor) {
        return Color.rgb(
            (int)(Color.red(color)   * factor),
            (int)(Color.green(color) * factor),
            (int)(Color.blue(color)  * factor)
        );
    }

    private float dp(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (sandAnimator != null) sandAnimator.cancel();
    }
}
