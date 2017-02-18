package eu.thedarken.audiobug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class InfoDialogFragment extends DialogFragment {
    public static InfoDialogFragment newInstance() {
        InfoDialogFragment f = new InfoDialogFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    public void showDialog(FragmentActivity a) {
        show(a.getSupportFragmentManager(), InfoDialogFragment.class.getSimpleName());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        String version;
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "?";
        }
        dialog.setMessage(getActivity().getString(R.string.hello, version));
        return dialog;
    }
}
