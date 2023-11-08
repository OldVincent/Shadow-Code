package edu.upc.shadowcode.views;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.databinding.FragmentLongTextBinding;

public class LongTextFragment extends DialogFragment {

    public LongTextFragment() {
    }

    private String contentTitle;
    private String contentHtml;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private FragmentLongTextBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLongTextBinding.inflate(inflater);

        if (contentHtml != null){
            binding.textDialogDescription.setText(
                    Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_COMPACT));
        }
        if (contentTitle != null) {
            binding.textDialogTitle.setText(contentTitle);
        }

        binding.buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void setContent(String title, String html){
        contentTitle = title;
        contentHtml = html;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics screen = Controller.getContext().getResources().getDisplayMetrics();
        Window window = requireDialog().getWindow();
        WindowManager.LayoutParams parameters = window.getAttributes();
        parameters.gravity = Gravity.CENTER;
        parameters.width = (int)(screen.widthPixels - 45 * screen.density);
        parameters.height = (int)(screen.heightPixels - 45 * screen.density);
        requireDialog().setCanceledOnTouchOutside(false);
        window.setAttributes(parameters);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }
}