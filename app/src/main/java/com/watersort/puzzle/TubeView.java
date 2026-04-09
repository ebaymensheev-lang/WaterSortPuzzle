package com.watersort.puzzle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Кастомный View — рисует одну колбу со всеми шариками.
 * Поддерживает выделение и анимацию подпрыгивания.
 */
public class TubeView extends View {

    // ── Данные ────────────────────────────────────────────────────────
    private Tube tube;
    private boolean selected = false;
    private float selectionOffset = 0f; // Смещение вверх при выборе

    // ── Краски ────────────────────────────────────────────────────────
    private final Paint tubePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tubeBorder   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ballPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPt  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint solvedPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Геометрия ─────────────────────────────────────────────────────
    private float ballRadius;
    private float tubeWidth;
    private float tubeHeight;
    private float tubeLeft, tubeTop;

    // ── Слушатель кликов ─────────────────────────────────────────────
    public interface OnTubeClickListener {
        void onTubeClick(TubeView tubeView);
    }
    private OnTubeClickListener listener;

    public TubeView(Context context) {
        super(context);
        setClickable(true);
        setupPaints();
        setOnClickListener(v -> { if (listener != null) listener.onTubeClick(this); });
    }

    private void setupPaints() {
        // Стекло колбы
        tubePaint.setColor(Color.parseColor("#CCE8F7"));
        tubePaint.setAlpha(120);
        tubePaint.setStyle(Paint.Style.FILL);

        // Рамка колбы
        tubeBorder.setColor(Color.parseColor("#AACCEE"));
        tubeBorder.setStyle(Paint.Style.STROKE);
        tubeBorder.setStrokeWidth(4f);

        // Блик на шарике
        highlightPt.setColor(Color.WHITE);
        highlightPt.setAlpha(90);
        highlightPt.setStyle(Paint.Style.FILL);

        // Тень
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(40);
        shadowPaint.setStyle(Paint.Style.FILL);

        // Рамка решённой колбы
        solvedPaint.setColor(Color.parseColor("#38C664"));
        solvedPaint.setStyle(Paint.Style.STROKE);
        solvedPaint.setStrokeWidth(6f);
        solvedPaint.setAlpha(200);
    }

    // ── Данные ────────────────────────────────────────────────────────

    public void setTube(Tube tube) {
        this.tube = tube;
        invalidate();
    }

    public Tube getTube() { return tube; }

    public void setOnTubeClickListener(OnTubeClickListener l) { this.listener = l; }

    // ── Выделение с анимацией подпрыгивания ───────────────────────────

    public void setSelected(boolean sel) {
        this.selected = sel;
        ValueAnimator anim = ValueAnimator.ofFloat(
            selectionOffset, sel ? -dpToPx(20) : 0f
        );
        anim.setDuration(180);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            selectionOffset = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.start();
    }

    public boolean isSelectedTube() { return selected; }

    // ── Рисование ─────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        float padding = w * 0.08f;
        tubeWidth  = w - padding * 2;
        ballRadius = tubeWidth / 2f * 0.85f;
        int maxBalls = tube != null ? tube.getMaxCapacity() : 4;
        tubeHeight = ballRadius * 2 * maxBalls + ballRadius * 0.6f;
        tubeLeft   = padding;
        tubeTop    = h - tubeHeight - dpToPx(8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tube == null) return;

        canvas.save();
        canvas.translate(0, selectionOffset);

        drawTube(canvas);
        drawBalls(canvas);

        canvas.restore();
    }

    private void drawTube(Canvas canvas) {
        float r = tubeWidth / 2f;
        RectF rect = new RectF(tubeLeft, tubeTop, tubeLeft + tubeWidth, tubeTop + tubeHeight);

        // Фон колбы (открыта сверху — рисуем U-форму)
        Path path = new Path();
        path.moveTo(tubeLeft, tubeTop);
        path.lineTo(tubeLeft, tubeTop + tubeHeight - r);
        path.arcTo(new RectF(tubeLeft, tubeTop + tubeHeight - tubeWidth,
                tubeLeft + tubeWidth, tubeTop + tubeHeight), 180, -180, false);
        path.lineTo(tubeLeft + tubeWidth, tubeTop);

        canvas.drawPath(path, tubePaint);

        // Рамка (зелёная если решена)
        Paint borderPaint = tube.isSolved() ? solvedPaint : tubeBorder;
        if (selected) {
            borderPaint = new Paint(borderPaint);
            borderPaint.setColor(Color.parseColor("#F5D920"));
            borderPaint.setStrokeWidth(6f);
        }
        canvas.drawPath(path, borderPaint);
    }

    private void drawBalls(Canvas canvas) {
        if (tube == null) return;
        int count = tube.getCount();
        float cx = tubeLeft + tubeWidth / 2f;

        for (int i = 0; i < count; i++) {
            Ball ball = tube.getBallAt(i);
            if (ball == null) continue;

            // Позиция: нижний шарик (i=0) рисуется внизу колбы
            float cy = tubeTop + tubeHeight - ballRadius * 0.5f
                     - (i * ballRadius * 2f) - ballRadius;

            // Тень
            canvas.drawCircle(cx + dpToPx(2), cy + dpToPx(2), ballRadius, shadowPaint);

            // Шарик
            ballPaint.setColor(ball.getColor());
            canvas.drawCircle(cx, cy, ballRadius, ballPaint);

            // Блик (создаёт 3D эффект)
            float hx = cx - ballRadius * 0.28f;
            float hy = cy - ballRadius * 0.28f;
            canvas.drawCircle(hx, hy, ballRadius * 0.32f, highlightPt);
        }
    }

    // ── Вспомогательное ──────────────────────────────────────────────

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
