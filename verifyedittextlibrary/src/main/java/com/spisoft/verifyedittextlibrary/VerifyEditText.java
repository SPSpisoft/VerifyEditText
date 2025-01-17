package com.spisoft.verifyedittextlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spisoft.verificationedittextlibrary.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 *
 *
 * @author spisoft
 */
public class VerifyEditText extends LinearLayout {

    private final int DEFAULT_INPUT_COUNT = 4;
    private final int DEFAULT_LINE_HEIGHT = 1;
    private final int DEFAULT_INPUT_SPACE = 15;
    private final int DEFAULT_LINE_SPACE = 8;
    private final int DEFAULT_TEXT_SIZE = 20;

    private boolean EnableEdit = true;
    private Context context;
    private List<HelperEditText> editTextList;
    private List<View> underlineList;
    private int currentPosition = 0;
    private inputCompleteListener inputCompleteListener;
    private @ColorInt
    int lineFocusColor = ContextCompat.getColor(getContext(), android.R.color.holo_blue_light);
    private @ColorInt
    int lineDefaultColor = ContextCompat.getColor(getContext(), R.color.colorDefault);
    /**
     */
    private boolean isAllLineLight = false;
    /**
     */
    private int inputCount = DEFAULT_INPUT_COUNT;
    /**
     */
    private int lineHeight;
    /**
     */
    private int inputSpace;
    /**
     */
    private int lineSpace;
    /**
     */
    private float textSize = DEFAULT_TEXT_SIZE;
    /**
     */
    private @DrawableRes
    int mCursorDrawable = R.drawable.edit_cursor_shape;
    private int textColor = Color.GRAY;

    public VerifyEditText(Context context) {
        this(context, null);
    }

    public VerifyEditText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void SetEnableEditText(boolean enableEdit){
        this.EnableEdit = enableEdit;
        for (HelperEditText et : editTextList) {
            if(et != null) {
                et.setEnabled(this.EnableEdit);
            }
        }
    }

    public boolean GetEnableEditText(){
        return this.EnableEdit;
    }

    public VerifyEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerifyEditText);
        if (typedArray != null) {
            inputCount = typedArray.getInteger(R.styleable.VerifyEditText_inputCount, DEFAULT_INPUT_COUNT);
            lineHeight = (int) typedArray.getDimension(R.styleable.VerifyEditText_underlineHeight, dp2px(DEFAULT_LINE_HEIGHT));
            inputSpace = (int) typedArray.getDimension(R.styleable.VerifyEditText_inputSpace, dp2px(DEFAULT_INPUT_SPACE));
            lineSpace = (int) typedArray.getDimension(R.styleable.VerifyEditText_underlineSpace, dp2px(DEFAULT_LINE_SPACE));
            textSize = typedArray.getDimension(R.styleable.VerifyEditText_mTextSize, DEFAULT_TEXT_SIZE);
            lineFocusColor = typedArray.getColor(R.styleable.VerifyEditText_focusColor, ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));
            lineDefaultColor = typedArray.getColor(R.styleable.VerifyEditText_defaultColor, ContextCompat.getColor(getContext(), R.color.colorDefault));
            mCursorDrawable = typedArray.getResourceId(R.styleable.VerifyEditText_cursorDrawable, R.drawable.edit_cursor_shape);
            typedArray.recycle();
        }
        initView();
    }

    private void initView() {
        if (inputCount <= 0) {
            return;
        }

        editTextList = new ArrayList<>();
        underlineList = new ArrayList<>();

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        for (int i = 0; i < inputCount; i++) {

            LayoutParams flParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            flParams.setMargins(i == 0 ? 0 : inputSpace, 0, 0, 0);
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setLayoutParams(flParams);

            FrameLayout.LayoutParams etParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            HelperEditText editText = new HelperEditText(context);
            editText.setBackground(null);
            editText.setPadding(0, 0, 0, lineSpace);
            editText.setMaxLines(1);
            editText.setTextSize(textSize);
            editText.setTextColor(textColor);
            InputFilter[] filters = {new InputFilter.LengthFilter(1)};
            editText.setFilters(filters);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setGravity(Gravity.CENTER);
            try {
                Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                f.setAccessible(true);
                f.set(editText, mCursorDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            editText.setLayoutParams(etParams);
            frameLayout.addView(editText);

            FrameLayout.LayoutParams lineParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight);
            lineParams.gravity = Gravity.BOTTOM;
            View underline = new View(context);
            underline.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDefault));
            underline.setLayoutParams(lineParams);
            frameLayout.addView(underline);

            addView(frameLayout);
            editTextList.add(editText);
            underlineList.add(underline);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && currentPosition < editTextList.size() - 1) {
                    currentPosition++;
                    editTextList.get(currentPosition).requestFocus();
                }
                if (isInputComplete() && inputCompleteListener != null) {
                    inputCompleteListener.inputComplete(VerifyEditText.this, getContent());
                }
            }
        };

        OnFocusChangeListener onFocusChangeListener = (v, hasFocus) -> {
            for (int i = 0; i < editTextList.size(); i++) {
                if (editTextList.get(i).isFocused()) {
                    currentPosition = i;
//                    ShiftToLast(currentPosition);
                }
                if (!isAllLineLight) {
                    underlineList.get(i).setBackgroundColor(lineDefaultColor);
                }
            }
            if (!isAllLineLight) {
                underlineList.get(currentPosition).setBackgroundColor(lineFocusColor);
            }
        };

        @SuppressLint("ClickableViewAccessibility")
        OnTouchListener touchListener = (v, motion) -> {
            for (int i = 0; i < editTextList.size(); i++) {
                if(editTextList.get(i).getText().toString().isEmpty()){
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                    editTextList.get(i).requestFocus();
                    assert inputMethodManager != null;
                    inputMethodManager.showSoftInput(editTextList.get(i), 0);
                    return true;
                }
            }
            return false;
        };

        OnKeyListener keyListener = (v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL)
            {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return true;
                }
                if (editTextList.get(currentPosition).getText().toString().isEmpty()) {
                    if (currentPosition <= 0) {
                        return true;
                    }
                    for (int position = currentPosition; position >= 0; position--) {
                        currentPosition = position;
                        if (!editTextList.get(position).getText().toString().isEmpty()) {
                            break;
                        }
                    }
                }
                editTextList.get(currentPosition).requestFocus();
                editTextList.get(currentPosition).getText().clear();
                return true;
            }else {
                if (!editTextList.get(currentPosition).getText().toString().isEmpty()
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && (event.getKeyCode() >= KeyEvent.KEYCODE_0 && event.getKeyCode() <= KeyEvent.KEYCODE_9)) {
//                    editTextList.get(currentPosition).setText("");
                    StringBuilder builder = new StringBuilder();
                    builder.append((char) event.getUnicodeChar());
                    editTextList.get(currentPosition).setText(builder.toString());
                    return true;
                }
            }
            return false;
        };

        for (HelperEditText et : editTextList) {
            et.addTextChangedListener(textWatcher);
            et.setOnFocusChangeListener(onFocusChangeListener);
            et.setOnKeyListener(keyListener);
            et.setOnTouchListener(touchListener);
        }

        editTextList.get(0).requestFocus();
    }

    private void ShiftToLast(int mPosition) {
        while (mPosition > 0 && editTextList.get(mPosition-1).getText().toString().isEmpty()){
            editTextList.get(mPosition).clearFocus();
            --mPosition;
            editTextList.get(mPosition).requestFocus();
        }
//        editTextList.get(mPosition).clearFocus();
    }

    public String getContent() {
        if (editTextList == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (HelperEditText et : editTextList) {
            builder.append(et.getText().toString());
        }
        return builder.toString();
    }

    public void resetContent() {
        StringBuilder builder = new StringBuilder();
        for (HelperEditText et : editTextList) {
            et.setText("");
//            builder.append(et.getText().toString());
        }
//        return builder.toString();
    }

    /**
     *
     */
    public boolean isInputComplete() {
        if (editTextList == null) {
            return false;
        }
        for (EditText et : editTextList) {
            if (et.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    public void setAllLineLight(boolean flag) {
        isAllLineLight = flag;
        if (isAllLineLight) {
            for (View v : underlineList) {
                v.setBackgroundColor(lineFocusColor);
            }
        }
    }

    /**
     */
    public interface inputCompleteListener {
        void inputComplete(VerifyEditText et, String content);
    }

    public void setInputCompleteListener(VerifyEditText.inputCompleteListener inputCompleteListener) {
        this.inputCompleteListener = inputCompleteListener;
    }

    public int dp2px(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public int getLineFocusColor() {
        return lineFocusColor;
    }

    public void setLineFocusColor(int lineFocusColor) {
        this.lineFocusColor = lineFocusColor;
    }

    public int getLineDefaultColor() {
        return lineDefaultColor;
    }

    public void setLineDefaultColor(int lineDefaultColor) {
        this.lineDefaultColor = lineDefaultColor;
    }

    public VerifyEditText SetTextColor(int color) {
        this.textColor = color;
        for (HelperEditText et : editTextList) {
            if(et != null) {
                et.setTextColor(this.textColor);
            }
        }
        return this;
    }

    public VerifyEditText SetText(String text) {
        for (int i=0; i<editTextList.size() ; i++) {
            HelperEditText et = editTextList.get(i);
            if(et != null) {
                et.setText(text.substring(i), TextView.BufferType.EDITABLE);
            }
        }
        return this;
    }

    public int getInputCount() {
        return inputCount;
    }

    public void setInputCount(int inputCount) {
        this.inputCount = inputCount;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getInputSpace() {
        return inputSpace;
    }

    public void setInputSpace(int inputSpace) {
        this.inputSpace = inputSpace;
    }

    public int getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(int lineSpace) {
        this.lineSpace = lineSpace;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getmCursorDrawable() {
        return mCursorDrawable;
    }

    public void setmCursorDrawable(int mCursorDrawable) {
        this.mCursorDrawable = mCursorDrawable;
    }

    public boolean isAllLineLight() {
        return isAllLineLight;
    }
}
