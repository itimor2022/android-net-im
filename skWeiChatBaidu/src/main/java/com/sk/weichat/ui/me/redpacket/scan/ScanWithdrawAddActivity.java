package com.sk.weichat.ui.me.redpacket.scan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ScanRecharge;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.me.BasicInfoEditActivity;
import com.sk.weichat.ui.mucfile.UploadingHelper;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.CameraUtil;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Header;
import okhttp3.Call;

/**
 * 添加提现账号
 */
public class ScanWithdrawAddActivity extends BaseActivity {
    private int mAddType;
    private EditText mAlipayNameEdit, mAlipayAccount; // 支付宝输入
    private EditText mBandCardOwnerNameEdit, mBandCardAccountEdit, mBandCardNameEdit, mBandCardSonNameEdit, mRemarkEdit; // 银行卡输入

    private ImageView wxIvQrCode; // 微信提现账户
    private EditText etWxName, etWxDesc;
    private String wxQrcodeImgUrl;

    private Spinner spUsdtAddrType;
    private  EditText etUsdtAddr, etUsdtName, etUsdtDesc;


    public static void start(Context context, int type) {
        Intent intent = new Intent(context, ScanWithdrawAddActivity.class);
        intent.putExtra("add_type", type);
        context.startActivity(intent);
    }

    private String getAddText(int type) {
        String txt = ScanWithdrawListActivity.addBtnMap.get(type);
        if (txt != null) return txt;
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_withdraw_add);
        mAddType = getIntent().getIntExtra("add_type", 1);// 1 支付宝 2 银行卡 3.微信 4.usdt
        initActionbar();
        initView();
        intEvent();
    }

    private void initActionbar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView mTvTitle = findViewById(R.id.tv_title_center);
        mTvTitle.setText(getAddText(mAddType));
    }

    private void initView() {
        switch (mAddType) {
            case 1:
                findViewById(R.id.ll1).setVisibility(View.VISIBLE);
                mAlipayNameEdit = findViewById(R.id.alipay_name_et);
                mAlipayAccount = findViewById(R.id.alipay_account_et);
                break;
            case 2:
                findViewById(R.id.ll2).setVisibility(View.VISIBLE);
                mBandCardOwnerNameEdit = findViewById(R.id.band_card_owner_name_et);
                mBandCardAccountEdit = findViewById(R.id.band_card_account_et);
                mBandCardNameEdit = findViewById(R.id.band_name_et);
                mBandCardSonNameEdit = findViewById(R.id.band_son_name_et);
                mRemarkEdit = findViewById(R.id.band_card_remark_et);
                break;
            case 3:
                findViewById(R.id.ll_wx).setVisibility(View.VISIBLE);
                wxIvQrCode = findViewById(R.id.wx_addwithdraw_qrcodeimg);
                etWxName = findViewById(R.id.wx_account_et);
                etWxDesc = findViewById(R.id.wx_account_withdrawaccout_desc);
                break;
            case 4:
                findViewById(R.id.ll_usdt).setVisibility(View.VISIBLE);
                spUsdtAddrType = findViewById(R.id.dialog_withdraw_manual_sp2);
                etUsdtAddr = findViewById(R.id.usdt_account_et);
                etUsdtName = findViewById(R.id.usdt_account_et_name);
                etUsdtDesc = findViewById(R.id.usdt_account_et_note);
                break;
            default:
                break;
        }
        ButtonColorChange.colorChange(mContext, findViewById(R.id.sure_band_btn));
    }

    private void showSelectPicDialog() {
        String[] items = new String[]{getString(R.string.photograph), getString(R.string.album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.tip_select_photo))
                .setSingleChoiceItems(items, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    takePhoto();
                                } else {
                                    selectPhoto();
                                }
                                dialog.dismiss();
                            }
                        });
        builder.show();
    }

    private Uri mNewPhotoUri;
    private File mCurrentFile;
    private static final int REQUEST_CODE_CAPTURE_CROP_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_CROP_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_PHOTO = 3;

    private void takePhoto() {
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_CROP_PHOTO);
    }

    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_CROP_PHOTO) {// 拍照返回再去裁减
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    Uri o = mNewPhotoUri;
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_CROP_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri o = Uri.fromFile(new File(CameraUtil.parsePickImageResult(data)));
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_CROP_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    // 上传二维码文件
                    uploadPic(mCurrentFile);
                } else {
                    ToastUtil.showToast(this, R.string.c_crop_failed);
                }
            }
        }
    }

    private UploadingHelper.OnUpFileListener onUpFileListener = new UploadingHelper.OnUpFileListener() {
        @Override
        public void onSuccess(String url, String filePath) {
            DialogHelper.dismissProgressDialog();
            wxQrcodeImgUrl = url;
            mNewPhotoUri = null;
            mCurrentFile = null;
            AvatarHelper.getInstance().displayUrl(wxQrcodeImgUrl, wxIvQrCode);
        }

        @Override
        public void onFailure(String err, String filePath) {
            DialogHelper.dismissProgressDialog();
            ToastUtil.showToast(ScanWithdrawAddActivity.this, R.string.upload_failed);
        }
    };

    private void uploadPic(File file) {
        if (!file.exists()) {
            // 文件不存在
            return;
        }
        // 显示正在上传的ProgressDialog
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String loginUserId = coreManager.getSelf().getUserId();
        final String accessToken = CoreManager.requireSelfStatus(MyApplication.getContext()).accessToken;
        UploadingHelper.upfile(accessToken, loginUserId, file, onUpFileListener);
    }

    private void intEvent() {
        findViewById(R.id.sure_band_btn).setOnClickListener(v -> {
            band();
        });
        findViewById(R.id.wx_addwithdraw_qrcodeimg).setOnClickListener(v -> {
            // 点击上传微信收款码（上传文件）
            showSelectPicDialog();
        });
    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        switch (mAddType) {
            case 1:
                String alipayName = mAlipayNameEdit.getText().toString().trim();
                String alipayAccount = mAlipayAccount.getText().toString().trim();
                if (TextUtils.isEmpty(alipayName) || TextUtils.isEmpty(alipayAccount)) {
                    ToastUtil.showToast(mContext, getString(R.string.must_edit_info_cannot_null));
                    return null;
                }
                params.put("type", String.valueOf(1));
                params.put("aliPayName", alipayName);
                params.put("aliPayAccount", alipayAccount);
                break;
            case 2:
                String bandCardOwnerName = mBandCardOwnerNameEdit.getText().toString().trim();
                String bandCardAccount = mBandCardAccountEdit.getText().toString().trim();
                String bandCardName = mBandCardNameEdit.getText().toString().trim();
                String bandCardSonName = mBandCardSonNameEdit.getText().toString().trim();
                String remark = mRemarkEdit.getText().toString().trim();
                if (TextUtils.isEmpty(bandCardOwnerName) || TextUtils.isEmpty(bandCardAccount) || TextUtils.isEmpty(bandCardName)) {
                    ToastUtil.showToast(mContext, getString(R.string.must_edit_info_cannot_null));
                    return null;
                }
                params.put("type", String.valueOf(2));
                params.put("cardName", bandCardOwnerName);
                params.put("bankCardNo", bandCardAccount);
                params.put("bankName", bandCardName);
                params.put("bankBranchName", bandCardSonName);
                params.put("desc", remark);
                break;
            case 3:
                String qrCodeImgUrl = wxQrcodeImgUrl;
                String wxName = etWxName.getText().toString();
                String wxdesc = etWxDesc.getText().toString();
                if (TextUtils.isEmpty(qrCodeImgUrl)) {
                    ToastUtil.showToast(mContext, getString(R.string.must_edit_info_wx_withdraw_qrcode));
                    return null;
                }
                if (TextUtils.isEmpty(wxName)) {
                    ToastUtil.showToast(mContext, getString(R.string.must_edit_info_cannot_null));
                    return null;
                }
                params.put("wxQrCode", qrCodeImgUrl);
                params.put("name", wxName);
                params.put("desc", wxdesc);
                params.put("type", "3");
                break;
            case 4:
                String addrType = spUsdtAddrType.getSelectedItem().toString();
                String usdtAddr = etUsdtAddr.getText().toString();
                String usdtName = etUsdtName.getText().toString();
                String usdtDesc = etUsdtDesc.getText().toString();
                if (TextUtils.isEmpty(usdtAddr) || TextUtils.isEmpty(usdtName)) {
                    ToastUtil.showToast(mContext, getString(R.string.must_edit_info_cannot_null));
                    return null;
                }
                params.put("usdtAddress", usdtAddr);
                params.put("usdtType", addrType);
                params.put("name", usdtName);
                params.put("desc", usdtDesc);
                params.put("type", "4");
                break;
            default:
                break;
        }
        return params;
    }

    private void band() {
        Map<String, String> params = getParams();
        if (params == null) return;
        DialogHelper.showDefaulteMessageProgressDialog(mContext);
        HttpUtils.get().url(coreManager.getConfig().MANUAL_PAY_ADD_WITHDRAW_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<ScanRecharge>(ScanRecharge.class) {

                    @Override
                    public void onResponse(ObjectResult<ScanRecharge> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, getString(R.string.addsuccess));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });

    }
}
