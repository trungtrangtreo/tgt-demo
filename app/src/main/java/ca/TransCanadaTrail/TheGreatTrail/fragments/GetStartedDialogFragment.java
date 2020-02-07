package ca.TransCanadaTrail.TheGreatTrail.fragments;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.TransCanadaTrail.TheGreatTrail.R;


public class GetStartedDialogFragment extends DialogFragment {

    private static final String TAG = "GetStartedDialogFragment";
    private GetStartedDialogIF getStartedDialogIF;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_started_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        final Dialog dialog = new Dialog(getActivity());
        Window window = dialog.getWindow();

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }

    public void setGetStartedDialogIF(GetStartedDialogIF getStartedDialogIF) {
        this.getStartedDialogIF = getStartedDialogIF;
    }

    @OnClick(R.id.dismiss_btn)
    public void onClickDismiss() {
        dismiss();
    }

    @OnClick(R.id.start_btn)
    public void onClickStart() {
        if (getStartedDialogIF == null)
            return;

        getStartedDialogIF.onClickGetStarted();
        dismiss();
    }

    public void show(FragmentManager fm) {
        fm.beginTransaction()
                .add(this, TAG)
                .commitAllowingStateLoss();
    }

    public interface GetStartedDialogIF {
        void onClickGetStarted();
    }
}
