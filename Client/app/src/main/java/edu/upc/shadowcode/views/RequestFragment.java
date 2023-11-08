package edu.upc.shadowcode.views;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.FragmentRequestBinding;

public class RequestFragment extends DialogFragment {

    public RequestFragment() {
    }

    public interface RequestHandler {
        void confirm();
        void cancel();
    }

    public static class Request {
        @NotNull
        public final String title;
        @NotNull
        public final String description;
        public final RequestHandler handler;

        public Request(@NotNull String requestTitle,
                       @NotNull String requestDescription,
                       RequestHandler requestHandler) {
            title = requestTitle;
            description = requestDescription;
            handler = requestHandler;
        }
    }

    private static final LinkedList<Request> requests = new LinkedList<>();

    public static void addRequest(Request request) {
        requests.addLast(request);
    }

    public static void displayRequest(Request request) {
        requests.addLast(request);
        display();
    }

    private static boolean showing = false;
    public static void display() {
        if (showing) {
            return;
        }
        FragmentManager fragmentManager = MainActivity.get().getSupportFragmentManager();
        RequestFragment dialogFragment = new RequestFragment();
        dialogFragment.show(fragmentManager, "dialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showing = true;
    }

    private FragmentRequestBinding binding;

    private Request currentRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRequestBinding.inflate(inflater);

        binding.buttonRequestConfirm.setOnClickListener(onConfirm);
        binding.buttonRequestCancel.setOnClickListener(onCancel);

        nextRequest();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showing = false;
    }

    private void nextRequest() {
        currentRequest = requests.peekFirst();
        if (currentRequest == null) {
            dismiss();
            return;
        }
        requests.removeFirst();

        binding.textRequestTitle.setText(
                Html.fromHtml(currentRequest.title, Html.FROM_HTML_MODE_COMPACT));
        binding.textRequestDescription.setText(
                Html.fromHtml(currentRequest.title, Html.FROM_HTML_MODE_COMPACT));
    }

    public View.OnClickListener onConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (currentRequest != null && currentRequest.handler != null) {
                currentRequest.handler.confirm();
            }
            nextRequest();
        }
    };

    public View.OnClickListener onCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (currentRequest != null && currentRequest.handler != null) {
                currentRequest.handler.cancel();
            }
            nextRequest();
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics screen = Controller.getContext().getResources().getDisplayMetrics();
        Window window = requireDialog().getWindow();
        WindowManager.LayoutParams parameters = window.getAttributes();
        parameters.gravity = Gravity.CENTER;
        requireDialog().setCanceledOnTouchOutside(false);
        window.setAttributes(parameters);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }
}