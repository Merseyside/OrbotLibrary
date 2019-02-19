package com.vpnapp.upstream.horizontalselectorview;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

public class HorizontalSelectorViewBinder {

    @BindingAdapter(value = "horizontalSelectorEntryValueAttrChanged") // AttrChanged required postfix
    public static void setListener(HorizontalSelectorView view, final InverseBindingListener listener) {
        if (listener != null) {
            view.setBindingListener(new HorizontalSelectorView.OnValueChangeListener() {
                @Override
                public void valueChanged(String entryValue) {
                    listener.onChange();
                }
            });
        }
    }

    @BindingAdapter("horizontalSelectorEntryValue")
    public static void setHorizontalSelectorEntryValue(HorizontalSelectorView view, String value) {
        if (!view.isTheSame(value)) {
            view.setCurrentEntryValue(value);
        }
    }

    @InverseBindingAdapter(attribute = "horizontalSelectorEntryValue")
    public static String getHorizontalSelectorEntryValue(HorizontalSelectorView view) {
        return view.getCurrentEntryValue();
    }
}
