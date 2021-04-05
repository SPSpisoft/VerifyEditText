package com.spisoft.verifyedittextlibrary;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;


public class HelperEditText extends android.support.v7.widget.AppCompatEditText {

    private OnKeyListener keyListener;

    public HelperEditText(Context context) {
        super(context);
    }

    public HelperEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HelperEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new MyInputConnection(super.onCreateInputConnection(outAttrs),
                true);
    }

    private class MyInputConnection extends InputConnectionWrapper {

        public MyInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (keyListener != null) {
                keyListener.onKey(HelperEditText.this, event.getKeyCode(), event);
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 || afterLength == 0 || beforeLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }

    }

    public void setDeleteEventListener(OnKeyListener listener) {
        keyListener = listener;
    }

}
