package www.androidghost.com.batteryandtheftalarm;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
/**
 * Created by HackMe on 03-01-2018.
 */
public class AutoPermissionDialog extends DialogFragment implements View.OnClickListener
{
    SharedPreferences mSharedPreferencesPermission;
    Button btnLatter,btnPer;
    Dialog dialog;
    AlertDialog.Builder builder;
    final ViewGroup group=null;
    View view;
    Context context;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        builder=new AlertDialog.Builder(context);

        view= LayoutInflater.from(context).inflate(R.layout.permission_dialog,group);
        builder.setView(view);

        btnLatter = view.findViewById(R.id.btnLatter);
        btnPer= view.findViewById(R.id.btn_permission);
        btnPer.setOnClickListener(this);
        btnLatter.setOnClickListener(this);

        dialog=builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if( dialog.getWindow()!=null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId()==R.id.btnLatter)
        {
            dismiss();
        }
        else if(view.getId()==R.id.btn_permission) {

            mSharedPreferencesPermission=context.getSharedPreferences("permission",Context.MODE_PRIVATE);
            SharedPreferences.Editor mEditor=mSharedPreferencesPermission.edit();
            mEditor.putBoolean("autoStart",true);
            mEditor.apply();

            if(Build.BRAND.equalsIgnoreCase("xiaomi") )
            {
                try
                {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                    startActivity(intent);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            else if(Build.BRAND.equalsIgnoreCase("Letv"))
            {
                try
                {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
                    startActivity(intent);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if(Build.BRAND.equalsIgnoreCase("Honor"))
            {
                try
                {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                    startActivity(intent);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            else if(Build.BRAND.equalsIgnoreCase("oppo"))
            {
                try
                {
                    Intent intent = new Intent();
                    intent.setClassName("com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                    startActivity(intent);
                }
                catch (Exception e)
                {
                    try

                    {
                        Intent intent = new Intent();
                        intent.setClassName("com.oppo.safe",
                                "com.oppo.safe.permission.startup.StartupAppListActivity");
                        startActivity(intent);

                    }
                    catch (Exception ex) {
                        try {
                            Intent intent = new Intent();
                            intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity");
                            startActivity(intent);
                        }
                        catch (Exception exx) {
                            exx.printStackTrace();
                        }
                    }
                }
            }
            else if(Build.BRAND.equalsIgnoreCase("vivo")) {
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                    startActivity(intent);
                }
                catch (Exception e) {
                    try {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                        startActivity(intent);
                    }
                    catch (Exception ex) {
                        try {
                            Intent intent = new Intent();
                            intent.setClassName("com.iqoo.secure",
                                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                            startActivity(intent);
                        }
                        catch (Exception exx) {
                            ex.printStackTrace();

                        }
                    }
                }
            }
            dismiss();
        }
    }
}
