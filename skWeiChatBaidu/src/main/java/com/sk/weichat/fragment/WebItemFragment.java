package com.sk.weichat.fragment;


import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.util.UiUtils;
import com.sk.weichat.util.log.FileUtils;

import java.io.File;


/**
 * 导航1
 */
public class WebItemFragment extends EasyFragment implements View.OnClickListener {

    private WebView mWebView;
    private ProgressBar mLoadBar;
    private WebSettings mWs;
    public String homeUrl;
    public String title;
    private ValueCallback<Uri> mUploadCallBack;
    private ValueCallback<Uri[]> mUploadCallBackAboveL;
    private String mCameraFilePath;
    private static final int REQUEST_CODE_FILE_CHOOSER = 0x122;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public WebItemFragment() {
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_web_item;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
    }


    public boolean canGoBack(){
        if (mWebView != null && mWebView.canGoBack()){
            mWebView.goBack();
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {

        mWebView = findViewById(R.id.wv_web);
        mLoadBar = findViewById(R.id.pb_load_bar);

        mWs = mWebView.getSettings();
        // 设置可以支持缩放
        mWs.setSupportZoom(true);
        // 设置出现缩放工具
        mWs.setBuiltInZoomControls(true);
        //设置可在大视野范围内上下左右拖动，并且可以任意比例缩放
        mWs.setUseWideViewPort(true);
        //设置默认加载的可视范围是大视野范围
        mWs.setLoadWithOverviewMode(true);
        //自适应屏幕
        mWs.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //支持js
        mWs.setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webView, int progress) {
                mLoadBar.setProgress(progress);
            }
        });

        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                mLoadBar.setVisibility(View.GONE);

                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                String endCookie = cookieManager.getCookie(url);
                Log.i("XWEBVIEW", "onPageFinished: endCookie : " + endCookie);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();//同步cookie
                } else {
                    CookieManager.getInstance().flush();
                }
//                if (homeUrl.equalsIgnoreCase(url)) {
//                    LogUtil.d("回到首页");
//                    ivHome.setVisibility(View.GONE);
//                } else {
//                    if (ivHome.getVisibility() == View.GONE) {
//                        ivHome.setVisibility(View.VISIBLE);
//                    }
//                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，
                // 为false调用系统浏览器或第三方浏览器
                if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
                    return false;
                } else {
//                    try {
//                        Intent intent = new Intent();
//                        intent.setAction(Intent.ACTION_VIEW);
//                        intent.setData(Uri.parse(url));
//                        view.getContext().startActivity(intent);
//                    } catch (ActivityNotFoundException e) {
//                        Toast.makeText(view.getContext(), "手机还没有安装支持打开此网页的应用！", Toast.LENGTH_SHORT).show();
//                    }
                    return true;
                }
            }
        });

        mWebView.setWebChromeClient(webChromeClient);


        mWebView.loadUrl(homeUrl);

    }

    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient = new WebChromeClient() {
        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//            val localBuilder = AlertDialog.Builder(webView.context)
//            localBuilder.setMessage(message).setPositiveButton("确定", null)
//            localBuilder.setCancelable(false)
//            localBuilder.create().show()
//
//            //注意:
//            //必须要这一句代码:result.confirm()表示:
//            //处理结果为确定状态同时唤醒WebCore线程
//            //否则不能继续点击按钮
//            result.confirm()
//            return true
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> valueCallback) {
            mUploadCallBack = valueCallback;
            showFileChooser();
        }

        // For Android  >= 3.0
        public void openFileChooser(ValueCallback valueCallback, String acceptType) {
            mUploadCallBack = valueCallback;
            showFileChooser();
        }

        //For Android  >= 4.1
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            mUploadCallBack = valueCallback;
            showFileChooser();
        }

        // For Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mUploadCallBackAboveL = filePathCallback;
            showFileChooser();
            return true;
        }
    };


    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        int id = v.getId();
        switch (id) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_CHOOSER) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result == null && !TextUtils.isEmpty(mCameraFilePath)) {
                // 看是否从相机返回
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            if (result != null) {
                String path = FileUtils.getPath(getActivity(), result);
                if (!TextUtils.isEmpty(path)) {
                    File f = new File(path);
                    if (f.exists() && f.isFile()) {
                        Uri newUri = Uri.fromFile(f);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (mUploadCallBackAboveL != null) {
                                if (newUri != null) {
                                    mUploadCallBackAboveL.onReceiveValue(new Uri[]{newUri});
                                    mUploadCallBackAboveL = null;
                                    return;
                                }
                            }
                        } else if (mUploadCallBack != null) {
                            if (newUri != null) {
                                mUploadCallBack.onReceiveValue(newUri);
                                mUploadCallBack = null;
                                return;
                            }
                        }
                    }
                }
            }
            clearUploadMessage();
            return;
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack(); //goBack()表示返回WebView的上一页面
            return true;
        }
        return false;
    }

    /**
     * webview没有选择文件也要传null，防止下次无法执行
     */
    private void clearUploadMessage() {
        if (mUploadCallBackAboveL != null) {
            mUploadCallBackAboveL.onReceiveValue(null);
            mUploadCallBackAboveL = null;
        }
        if (mUploadCallBack != null) {
            mUploadCallBack.onReceiveValue(null);
            mUploadCallBack = null;
        }
    }

    private boolean isPermission() {
        boolean isSaves = false;
        for (String s : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), s) != PackageManager.PERMISSION_GRANTED) {
                isSaves = true;
            }
        }
        return isSaves;
    }

    /**
     * 打开选择文件/相机
     */
    private void showFileChooser() {

        if (isPermission()) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, 300);
            return;
        }

//        Intent intent1 = new Intent(Intent.ACTION_PICK, null);
//        intent1.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
        intent1.addCategory(Intent.CATEGORY_OPENABLE);
        intent1.setType("*/*");

        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCameraFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // android7.0注意uri的获取方式改变
            Uri photoOutputUri = FileProvider.getUriForFile(
                    getActivity(),
                    getContext().getApplicationInfo().packageName + ".fileProvider",
                    new File(mCameraFilePath));
            intent2.putExtra(MediaStore.EXTRA_OUTPUT, photoOutputUri);
        } else {
            intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
        }

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
        chooser.putExtra(Intent.EXTRA_INTENT, intent1);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent2});
        startActivityForResult(chooser, REQUEST_CODE_FILE_CHOOSER);
    }

}
