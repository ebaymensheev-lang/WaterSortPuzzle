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

    private final Paint boxPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint boxBorder   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint candyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wrapPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shinePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint solvedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TubeView(Context context) {
        super(context);

        boxPaint.setColor(Color.parseColor("#2A2D3E"));
        boxPaint.setStyle(Paint.Style.FILL);

        boxBorder.setColor(Color.parseColor("#4A4D6E"));
        boxBorder.setStyle(Paint.Style.STROKE);
        boxBorder.setStrokeWidth(3f);

        shinePaint.setColor(Color.WHITE);
        shinePaint.setAlpha(40);
        shinePaint.setStyle(Paint.Style.FILL);

        shadowPaint.setColor(Color.parseColor("#88000000"));
        shadowPaint.setStyle(Paint.Style.FILL);

        solvedPaint.setColor(Color.parseColor("#FFD93D"));
        solvedPaint.setStyle(Paint.Style.STROKE);
        solvedPaint.setStrokeWidth(4f);

        dividerPaint.setColor(Color.parseColor("#1AFFFFFF"));
        dividerPaint.setStyle(Paint.Style.STROKE);
        dividerPaint.setStrokeWidth(1f);

        wrapPaint.setStyle(Paint.Style.FILL);
        wrapPaint.setAlpha(180);
    }

    public void setTube(Tube tube) { this.tube = tube; invalidate(); }
    public Tube getTube() { return tube; }
    public void setOnTubeClickListener(OnTubeClickListener l) { this.listener = l; }

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

        // Размеры коробки
        float boxW  = w - dp(8);
        float slotH = (h - dp(16)) / (float) tube.getMaxCapacity();
        float boxH  = slotH * tube.getMaxCapacity();
        float left  = (w - boxW) / 2f;
        float top   = h - dp(8) - boxH;
        float right = left + boxW;
        float bottom = top + boxH;

        float offsetY = selected ? -dp(18) : 0;
        canvas.save();
        canvas.translate(0, offsetY);

        // Тень коробки
        shadowPaint.setAlpha(60);
        RectF shadowRect = new RectF(left + dp(4), top + dp(4), right + dp(4), bottom + dp(4));
        canvas.drawRoundRect(shadowRect, dp(8), dp(8), shadowPaint);

        // Тело коробки
        RectF boxRect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(boxRect, dp(8), dp(8), boxPaint);

        // Рисуем слоты с конфетами (снизу вверх)
        int count = tube.getCount();
        for (int i = 0; i < tube.getMaxCapacity(); i++) {
            float slotTop    = top + i * slotH;
            float slotBottom = slotTop + slotH;
            RectF slotRect = new RectF(left + dp(4), slotTop + dp(3),
                right - dp(4), slotBottom - dp(3));

            // Фон слота (темнее)
            Paint slotBg = new Paint(Paint.ANTI_ALIAS_FLAG);
            slotBg.setColor(Color.parseColor("#1A1C2A"));
            slotBg.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(slotRect, dp(4), dp(4), slotBg);

            // Конфета в слоте (индекс 0 = нижний, рисуем снизу вверх)
            int ballIndex = tube.getMaxCapacity() - 1 - i;
            if (ballIndex < count) {
                Ball ball = tube.getBallAt(ballIndex);
                if (ball != null) {
                    drawCandyInSlot(canvas, ball.getColor(), slotRect);
                }
            }

            // Разделитель между слотами
            if (i < tube.getMaxCapacity() - 1) {
                canvas.drawLine(left + dp(8), slotBottom,
                    right - dp(8), slotBottom, dividerPaint);
            }
        }

        // Рамка коробки
        Paint borderToDraw = tube.isSolved() ? solvedPaint : boxBorder;
        if (selected) {
            Paint selPaint = new Paint(boxBorder);
            selPaint.setColor(Color.parseColor("#54C6EB"));
            selPaint.setStrokeWidth(4f);
            canvas.drawRoundRect(boxRect, dp(8), dp(8), selPaint);
        } else {
            canvas.drawRoundRect(boxRect, dp(8), dp(8), borderToDraw);
        }

        // Блик сверху (стеклянный эффект)
        RectF shineRect = new RectF(left + dp(6), top + dp(4),
            right - dp(6), top + dp(16));
        canvas.drawRoundRect(shineRect, dp(4), dp(4), shinePaint);

        canvas.restore();
    }

    // Рисуем порцию конфет в фантиках в одном слоте
    private void drawCandyInSlot(Canvas canvas, int color, RectF slot) {
        float sw = slot.width();
        float sh = slot.height();
        float cx = slot.left + sw / 2f;
        float cy = slot.top  + sh / 2f;

        // 4 маленькие конфеты в фантиках расположены 2x2
        float candyW = sw * 0.38f;
        float candyH = sh * 0.36f;
        float gapX   = sw * 0.08f;
        float gapY   = sh * 0.06f;

        float[] xs = {
            cx - candyW / 2f - gapX,
            cx + gapX,
            cx - candyW / 2f - gapX,
            cx + gapX
        };
        float[] ys = {
            cy - candyH / 2f - gapY,
            cy - candyH / 2f - gapY,
            cy + gapY,
            cy + gapY
        };

        for (int i = 0; i < 4; i++) {
            drawWrappedCandy(canvas, xs[i], ys[i], candyW, candyH, color);
        }
    }

    // Рисуем одну конфету в фантике (прямоугольник с закрученными концами)
    private void drawWrappedCandy(Canvas canvas, float x, float y,
                                   float w, float h, int color) {
        float r = h * 0.35f; // радиус скругления тела

        // Тело конфеты
        candyPaint.setColor(color);
        RectF body = new RectF(x + w * 0.2f, y, x + w * 0.8f, y + h);
        canvas.drawRoundRect(body, r, r, candyPaint);

        // Блик на теле
        Paint shine = new Paint(Paint.ANTI_ALIAS_FLAG);
        shine.setColor(Color.WHITE);
        shine.setAlpha(60);
        shine.setStyle(Paint.Style.FILL);
        RectF shineRect = new RectF(
            body.left + w * 0.06f, body.top + h * 0.08f,
            body.right - w * 0.1f, body.top + h * 0.3f);
        canvas.drawRoundRect(shineRect, r * 0.5f, r * 0.5f, shine);

        // Левый фантик (скрученный конец)
        drawWrapEnd(canvas, x, y + h * 0.15f, w * 0.22f, h * 0.7f, color, true);

        // Правый фантик
        drawWrapEnd(canvas, x + w * 0.78f, y + h * 0.15f, w * 0.22f, h * 0.7f, color, false);
    }

    // Рисуем скрученный конец фантика
    private void drawWrapEnd(Canvas canvas, float x, float y,
                              float w, float h, int color, boolean left) {
        // Более тёмный цвет для фантика
        int darkColor = darkenColor(color, 0.7f);
        wrapPaint.setColor(darkColor);
        wrapPaint.setAlpha(200);

        Path path = new Path();
        if (left) {
            // Левый конец — сужается влево
            path.moveTo(x + w, y);
            path.lineTo(x, y + h * 0.5f);
            path.lineTo(x + w, y + h);
            path.close();
        } else {
            // Правый конец — сужается вправо
            path.moveTo(x, y);
            path.lineTo(x + w, y + h * 0.5f);
            path.lineTo(x, y + h);
            path.close();
        }
        canvas.drawPath(path, wrapPaint);

        // Линии на фантике (имитация скрутки)
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setAlpha(40);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(0.8f));

        if (left) {
            canvas.drawLine(x + w * 0.3f, y + h * 0.2f,
                x + w * 0.7f, y + h * 0.5f, linePaint);
            canvas.drawLine(x + w * 0.3f, y + h * 0.8f,
                x + w * 0.7f, y + h * 0.5f, linePaint);
        } else {
            canvas.drawLine(x + w * 0.7f, y + h * 0.2f,
                x + w * 0.3f, y + h * 0.5f, linePaint);
            canvas.drawLine(x + w * 0.7f, y + h * 0.8f,
                x + w * 0.3f, y + h * 0.5f, linePaint);
        }
    }

    // Затемнить цвет
    private int darkenColor(int color, float factor) {
        int r = (int) (Color.red(color)   * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color)  * factor);
        return Color.rgb(r, g, b);
    }

    private float dp(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
