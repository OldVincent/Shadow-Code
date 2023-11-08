package edu.upc.shadowcode;

public abstract class Presenter<Binder> {
    private Binder binding;

    public Binder getBinding() {
        return binding;
    }

    public void attach(Binder fragmentBinding) {
        binding = fragmentBinding;
        onAttach();
    }

    public void detach() {
        onDetach();
        binding = null;
    }

    protected abstract void onAttach();

    protected abstract void onDetach();
}
