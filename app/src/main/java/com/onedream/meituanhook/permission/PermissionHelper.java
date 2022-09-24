package com.onedream.meituanhook.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    public static boolean hasPermission(Context context, String permission) {
        int targetSdkVersion = 0;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = ContextCompat.checkSelfPermission(context, permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(context, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }


    public static void processRequestPermissionResult(Activity activity, @NonNull String[] permissions, @NonNull int[] grantResults, WeDoXPermissionListener fanPermissionListener) {
        //记录点击了不再提醒的未授权权限
        List<String> forceDeniedPermissions = new ArrayList<>();
        //记录点击了普通的未授权权限
        List<String> normalDeniedPermissions = new ArrayList<>();
        List<String> grantedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            int grantResult = grantResults[i];
            String permission = permissions[i];
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                //授权通过 nothing to do
                grantedPermissions.add(permission);
            } else {
                //授权拒绝
                if (checkPermissionIsForceDenied(activity, permission)) {
                    forceDeniedPermissions.add(permission);
                } else {
                    normalDeniedPermissions.add(permission);
                }
            }
        }
        if (forceDeniedPermissions.size() == 0 && normalDeniedPermissions.size() == 0) {
            //全部授权通过
            if (null != fanPermissionListener) {
                fanPermissionListener.permissionRequestSuccess();
            }
        } else {
            if (null != fanPermissionListener) {
                fanPermissionListener.permissionRequestFail(grantedPermissions.toArray(new String[grantedPermissions.size()]),
                        normalDeniedPermissions.toArray(new String[normalDeniedPermissions.size()]),
                        forceDeniedPermissions.toArray(new String[forceDeniedPermissions.size()]));
            }
        }
    }

    public interface WeDoXPermissionListener {
        /*
         * 授权全部通过
         */
        void permissionRequestSuccess();

        /*
         * 授权未通过
         * @param grantedPermissions 已通过的权限
         * @param deniedPermissions 拒绝的权限
         * @param forceDeniedPermissions 永久拒绝的权限（也就是用户点击了不再提醒的那些权限）
         */
        void permissionRequestFail(String[] grantedPermissions, String[] deniedPermissions, String[] forceDeniedPermissions);
    }


    /**
     * 判断该权限是否被用户设置为【不再询问】
     */
    public static boolean checkPermissionIsForceDenied(Activity activity, String permission) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }


}
