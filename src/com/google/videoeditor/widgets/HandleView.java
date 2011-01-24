/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.videoeditor.widgets;

import com.google.videoeditor.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * The view that represents a resize handle
 */
public class HandleView extends ImageView {
    // Instance variables
    private final Drawable mArrowLeft;
    private final Drawable mArrowRight;
    private MoveListener mListener;
    private float mStartMoveX, mLastMoveX;
    private boolean mMoveStarted;
    private boolean mBeginLimitReached, mEndLimitReached;
    private int mLastDeltaX;

    /**
     * Move listener
     */
    public interface MoveListener {
        /**
         * The move begins
         *
         * @param view The view
         */
        public void onMoveBegin(HandleView view);

        /**
         * Move is in progress
         *
         * @param view The view
         * @param left The left edge left position
         * @param delta The offset relative to the left of the view
         */
        public boolean onMove(HandleView view, int left, int delta);

        /**
         * The move ended
         *
         * @param view The view
         * @param left The left edge left position
         * @param delta The offset relative to the left of the view
         */
        public void onMoveEnd(HandleView view, int left, int delta);
    }

    /*
     * {@inheritDoc}
     */
    public HandleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Prepare the handle arrows
        final Resources resources = getResources();
        mArrowLeft = resources.getDrawable(R.drawable.handle_left_arrow);
        mArrowRight = resources.getDrawable(R.drawable.handle_right_arrow);
    }

    /*
     * {@inheritDoc}
     */
    public HandleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /*
     * {@inheritDoc}
     */
    public HandleView(Context context) {
        this(context, null, 0);
    }

    /**
     * @param listener The listener
     */
    public void setListener(MoveListener listener) {
        mListener = listener;
    }

    /**
     * Set the movement limits
     *
     * @param beginLimitReached true if the begin limit was reached
     * @param endLimitReached true if the end limit was reached
     */
    public void setLimitReached(boolean beginLimitReached, boolean endLimitReached) {
        // Check if anything has changed
        if (beginLimitReached == mBeginLimitReached && endLimitReached == mEndLimitReached) {
            return;
        }

        mBeginLimitReached = beginLimitReached;
        mEndLimitReached = endLimitReached;

        invalidate();
    }

    /**
     * End the move
     */
    public void endMove() {
        if (mMoveStarted) {
            endActionMove(mLastMoveX);
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (isEnabled()) {
                    // The ScrollView will not get the touch events
                    getParent().requestDisallowInterceptTouchEvent(true);
                    if (mListener != null) {
                        mListener.onMoveBegin(this);
                    }

                    mStartMoveX = ev.getX();
                    mMoveStarted = true;
                } else {
                    mMoveStarted = false;
                }

                mLastDeltaX = -10000;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mMoveStarted && isEnabled()) {
                    final int deltaX = Math.round((ev.getX() - mStartMoveX));
                    if (deltaX != mLastDeltaX) {
                        mLastDeltaX = deltaX;

                        if (mListener != null) {
                            if (getId() == R.id.handle_left) {
                                mListener.onMove(this, getLeft(), deltaX + getWidth());
                            } else {
                                mListener.onMove(this, getLeft(), deltaX);
                            }
                        }

                        mLastMoveX = ev.getX();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                endActionMove(ev.getX());
                break;
            }

            default: {
                break;
            }
        }

        return true;
    }

    /**
     * End the move (if it was in progress)
     *
     * @param eventX The event horizontal position
     */
    private void endActionMove(float eventX) {
        if (mMoveStarted) {
            mMoveStarted = false;

            final int deltaX = Math.round((eventX - mStartMoveX));
            if (getId() == R.id.handle_left) {
                mListener.onMoveEnd(this, getLeft(), deltaX + getWidth());
            } else {
                mListener.onMoveEnd(this, getLeft(), deltaX);
            }
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mBeginLimitReached) {
            mArrowLeft.setBounds(0, 0, mArrowLeft.getIntrinsicWidth(),
                    mArrowLeft.getIntrinsicHeight());
            mArrowLeft.draw(canvas);
        }

        if (!mEndLimitReached) {
            mArrowRight.setBounds(getWidth() - mArrowRight.getIntrinsicWidth(), 0, getWidth(),
                    mArrowRight.getIntrinsicHeight());
            mArrowRight.draw(canvas);
        }
    }
}
