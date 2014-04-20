package eu.thedarken.audiobug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class FollowMeDialogFragment extends DialogFragment {
    public static FollowMeDialogFragment newInstance() {
        FollowMeDialogFragment f = new FollowMeDialogFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    public void showDialog(FragmentActivity a) {
        show(a.getSupportFragmentManager(), FollowMeDialogFragment.class.getSimpleName());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setIcon(R.drawable.ic_action_twitter);
        dialog.setTitle(getActivity().getString(R.string.follow_darken));
        dialog.setMessage(getActivity().getString(R.string.keep_up_with_recent_developments));

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Google+", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/116634499773478773276"));
                startActivity(i);
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Twitter", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.twitter.com/d4rken"));
                startActivity(i);
            }
        });

        return dialog;
    }
}
