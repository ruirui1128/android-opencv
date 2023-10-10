package com.mind.open.presenter;

import static android.content.Context.DOMAIN_VERIFICATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.mind.open.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * create by Rui on 2023-10-09
 * desc:
 */
public class RknnPresenter {

    private String mModelName = "yolov5s.rknn";
    private String platform = "rk3588";
    private String fileDirPath;

    private String engineFileName = "cp_xml.xml";

    private RknnPresenter() {
    }

    private Activity activity;

    public RknnPresenter(Activity activity) {
        this.activity = activity;
    }

    /**
     * 获取模型文件地址
     *
     * @return
     */
    public String getModelFilePath() {
        return fileDirPath + "/" + mModelName;
    }


    /**
     * 获取入库文件
     */
    public String getEngineFilePath() {
        return fileDirPath + "/" + engineFileName;
    }

    /**
     * 芯片类型判断 并将模型文件放在缓存文件夹中
     */
    public void loadPlatform() {
        fileDirPath = activity.getCacheDir().getAbsolutePath();
        platform = getPlatform();
        if (platform.equals("rk3588")) {
            createFile(mModelName, R.raw.model_epoch_v3);
        } else {
            Toast toast = Toast.makeText(activity, "Can not get platform use RK3588 instead.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

//        if (platform.equals("rk3588")) {
//            createFile(mModelName, R.raw.yolov5s_rk3588);
//        } else if (platform.equals("rk356x")) {
//            createFile(mModelName, R.raw.yolov5s_rk3566);
//        } else if (platform.equals("rk3562")) {
//            createFile(mModelName, R.raw.yolov5s_rk3562);
//        } else {
//            Toast toast = Toast.makeText(activity, "Can not get platform use RK3588 instead.", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
//            createFile(mModelName, R.raw.yolov5s_rk3588);
//        }

    }


    private void createFile(String fileName, int id) {
        String filePath = fileDirPath + "/" + fileName;
        try {
            File dir = new File(fileDirPath);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String engPath = getEngineFilePath();
            File engFile = new File(engPath);
            if (!engFile.exists()) {
                engFile.createNewFile();
            }


            // 目录存在，则将apk中raw中的需要的文档复制到该目录下
            File file = new File(filePath);

            if (!file.exists() || isFirstRun()) {

                InputStream ins = activity.getResources().openRawResource(id);// 通过raw得到数据资源
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[8192];
                int count = 0;

                while ((count = ins.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }

                fos.close();
                ins.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isFirstRun() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("setting", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }

        return isFirstRun;
    }


    private String getPlatform()//取平台版本
    {
        String platform = null;
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            platform = (String) getMethod.invoke(classType, new Object[]{"ro.board.platform"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return platform;
    }


}
