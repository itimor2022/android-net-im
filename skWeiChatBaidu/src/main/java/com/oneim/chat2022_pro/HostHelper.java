package com.oneim.chat2022_pro;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HostHelper {

    private volatile static HostHelper sInstance;

//    private List<HostCallback> callbacks = new LinkedList<>();

    static {
        System.loadLibrary("chat2022_pro");
    }

    public static HostHelper getInstance() {
        if (sInstance == null) {
            synchronized (HostHelper.class) {
                if (sInstance == null) {
                    sInstance = new HostHelper();
                }
            }
        }

        return sInstance;
    }

    private HostHelper() {
    }

//    public void registerCallback(HostCallback hostCallback){
//        callbacks.add(hostCallback);
//    }
//
//    public void unRegisterCallback(HostCallback hostCallback){
//        if (callbacks.contains(hostCallback)){
//            callbacks.remove(hostCallback);
//        }
//    }

    /**
     * 执行
     *
     * @param imgUrl
     * @param savePath
     */
    public void excuteHost(String imgUrl, String savePath, HostCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveFile(imgUrl,savePath,callback);
            }
        }).start();
    }

    private void saveFile(String imgUrl, String savePath, HostCallback callback) {
        InputStream is = null;
        BufferedInputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL(imgUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            is = connection.getInputStream();
            inputStream = new BufferedInputStream(is);

            //将图片数据保存到byte中
            byteArrayOutputStream = new ByteArrayOutputStream();
            int bytes;
            byte[] buffer = new byte[1024];
            while ((bytes = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytes);
            }

            byte[] imageData = byteArrayOutputStream.toByteArray();

            File file = new File(savePath + File.separator + "host.jpg");
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(imageData);

            String result = getHost(file.getAbsolutePath());
            if (callback != null){
                callback.hostCallback(result);
            }

        } catch (MalformedURLException e) {
            if (callback != null){
                callback.hostCallback(getHost(null));
            }
        } catch (IOException e) {
            if (callback != null){
                callback.hostCallback(getHost(null));
            }
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }

                if (inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取域名
     *
     * @param path 图片保存路径
     * @return
     */
    public native String getHost(String path);

//    public void release() {
//        callbacks.clear();
//    }

    public interface HostCallback {
        void hostCallback(String hostList);
    }
}
