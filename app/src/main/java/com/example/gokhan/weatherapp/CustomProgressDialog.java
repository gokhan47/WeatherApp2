package com.example.gokhan.weatherapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;

/**
 * Created by GOKHAN on 2/23/2016.
 */
public class CustomProgressDialog extends ProgressDialog {


    Dialog dialog;
    Context ctx;
    boolean cancelable;

    /**
     * Creates a custom progress dialog
     *
     * //@param   context      The context of the activity/fragment
     * @param   cancelable   Set to true if user can cancel action
     */
    public CustomProgressDialog (Context ctx, boolean cancelable) {
        super(ctx);
        this.cancelable = cancelable;
        this.ctx = ctx;
    }


    public void startCustomProgressDialog () {
        if(dialog !=null) dialog.dismiss();dialog = null;
        dialog = new Dialog(ctx, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.setCanceledOnTouchOutside(false);
        if (cancelable) {
            dialog.setCancelable(true);
        } else {
            dialog.setCancelable(false);
        }
        if(!dialog.isShowing())
            dialog.show();

    }


    public void stopCustomProgressDialog () {
        if (dialog !=null){
            if(dialog.isShowing()){
                dialog.dismiss();
            }
        }

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}