package com.vpnapp.upstream.horizontalselectorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class HorizontalSelectorView extends LinearLayout {

    public enum Type {
        TEXT(0), IMAGE(1);

        private int id;

        Type(int id) {
            this.id = id;
        }

        static Type fromId(int id) {
            for (Type type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No color with passed id");
        }

        public int getId() {
            return id;
        }
    }

    public enum Buttons {
        ARROWS(0);

        private int id;

        Buttons(int id) {
            this.id = id;
        }

        static Buttons fromId(int id) {
            for (Buttons type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No color with passed id");
        }

        public int getId() {
            return id;
        }
    }

    public interface OnValueChangeListener {
        void valueChanged(String entryValue);
    }

    private OnValueChangeListener listener;
    private OnValueChangeListener bindingListener;

    private Context context;
    private int title_id;
    private int entries_id;
    private String[] entry_values;
    private int entryValueIndex = 0;
    private int textColorId;
    private int buttonsColorId;

    private Type type;
    private Buttons buttons;

    private TextView title_tw;
    private View value_view;

    public HorizontalSelectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        loadAttrs(attrs);
        initializeView();
    }

    private void loadAttrs(AttributeSet attrs) {

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HorizontalSelectorViewAttrs, 0, 0);
        title_id = array.getResourceId(R.styleable.HorizontalSelectorViewAttrs_horizontalSelectorTitle, View.NO_ID);

        entries_id = array.getResourceId(R.styleable.HorizontalSelectorViewAttrs_android_entries, View.NO_ID);

        CharSequence[] charSequencesEntryValues = array.getTextArray(R.styleable.HorizontalSelectorViewAttrs_android_entryValues);
        entry_values = new String[charSequencesEntryValues.length];
        for (int i = 0; i < charSequencesEntryValues.length; i++) {
            entry_values[i] = charSequencesEntryValues[i].toString();
        }

        type = Type.fromId(array.getInt(R.styleable.HorizontalSelectorViewAttrs_horizontalSelectorType, 0));
        buttons = Buttons.fromId(array.getInt(R.styleable.HorizontalSelectorViewAttrs_horizontalSelectorButtons, 0));

        textColorId = array.getColor(R.styleable.HorizontalSelectorViewAttrs_horizontalSelectorTextColor, View.NO_ID);
        buttonsColorId = array.getColor(R.styleable.HorizontalSelectorViewAttrs_horizontalSelectorButtonColor, getThemeAccentColor(context));

        array.recycle();
    }

    public static int getThemeAccentColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorAccent, value, true);
        return value.data;
    }

    private void initializeView() {

        if (type == Type.TEXT) {
            LayoutInflater.from(context).inflate(R.layout.horizontal_selector_textview, this);

        } else {
            LayoutInflater.from(context).inflate(R.layout.horizontal_selector_imageview, this);
        }

        title_tw = findViewById(R.id.title);
        ImageButton prev = findViewById(R.id.prev);
        prev.setOnClickListener(clickListener);
        setDrawableTint(prev, R.drawable.horizontal_selector_chevron_left, buttonsColorId);

        ImageButton next = findViewById(R.id.next);
        next.setOnClickListener(clickListener);
        setDrawableTint(next, R.drawable.horizontal_selector_chevron_right, buttonsColorId);

        value_view = findViewById(R.id.entry);

        fillView();
    }

    private void setDrawableTint(ImageButton imageButton, int res, int color) {
        if (color != View.NO_ID) {
            Drawable mWrappedDrawable = ContextCompat.getDrawable(context, res).mutate();
            mWrappedDrawable = DrawableCompat.wrap(mWrappedDrawable);
            DrawableCompat.setTint(mWrappedDrawable, color);
            DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);

            imageButton.setImageDrawable(mWrappedDrawable);
        } else {
            imageButton.setImageDrawable(ContextCompat.getDrawable(context, res));
        }
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (entry_values.length != 1) {
                if(view.getId() == R.id.prev) {
                    if (entryValueIndex == 0)
                        entryValueIndex = entry_values.length - 1;
                    else
                        entryValueIndex--;
                } else if(view.getId() == R.id.next) {
                    if (entryValueIndex == entry_values.length - 1)
                        entryValueIndex = 0;
                    else
                        entryValueIndex++;
                }
                fillView();
                onEntryValueChanged(entry_values[entryValueIndex]);
            }
        }
    };

    private void onEntryValueChanged(String value) {
        if (listener != null)
            listener.valueChanged(value);

        if (bindingListener != null)
            bindingListener.valueChanged(value);
    }

    private void fillView() {
        if (title_id != View.NO_ID) {
            title_tw.setText(context.getResources().getString(title_id));

            if (textColorId != View.NO_ID)
                title_tw.setTextColor(textColorId);
        } else
            title_tw.setVisibility(View.GONE);

        String[] entries = context.getResources().getStringArray(entries_id);

        if (value_view instanceof TextView) {
            TextView tw = (TextView) value_view;

            if (textColorId != View.NO_ID)
                tw.setTextColor(textColorId);

            tw.setText(entries[entryValueIndex]);
        } else {
            ImageView iw = (ImageView) value_view;
            int id = context.getResources().getIdentifier(entries[entryValueIndex], "drawable", context.getPackageName());
            iw.setImageResource(id);
        }
    }

    public void setCurrentEntryValue(@NonNull String value) {
        if (value != null) {
            int newEntryValueIndex = -1;

            for (int i = 0; i < entry_values.length; i++) {
                if (value.equals(entry_values[i])) {
                    newEntryValueIndex = i;
                    break;
                }
            }

            if (newEntryValueIndex != -1) {
                this.entryValueIndex = newEntryValueIndex;
                fillView();
            }
        }
    }

    @NonNull
    public String getCurrentEntryValue() {
        return entry_values[entryValueIndex];
    }

    boolean isTheSame(@NonNull String entryValue) {
        return getCurrentEntryValue().equals(entryValue);
    }

    public void setOnValueChangeListener(@Nullable OnValueChangeListener listener) {
        this.listener = listener;
    }

    void setBindingListener(@NonNull OnValueChangeListener listener) {
        this.bindingListener = listener;
    }

    public void updateLanguage(Context context) {
        this.context = context;
        fillView();
    }

}
