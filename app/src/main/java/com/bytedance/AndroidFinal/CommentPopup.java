package com.bytedance.AndroidFinal;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;

public class CommentPopup extends CenterPopupView {

    public CommentPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.comment_popup;
    }

    TextView tv_confirm;
    EditText et_input;
    public Boolean confirmClicked = false;

    @Override
    protected void onCreate() {
        super.onCreate();
        tv_confirm = findViewById(R.id.tv_confirm);
        et_input = findViewById(R.id.et_input);
        tv_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmClicked = true;
                dismiss();
            }
        });
    }

    public void setContent(String content) {
        et_input.setText(content);
        et_input.setSelection(content.length());
    }

    public String getContent() {
        return et_input.getText().toString();
    }
}
