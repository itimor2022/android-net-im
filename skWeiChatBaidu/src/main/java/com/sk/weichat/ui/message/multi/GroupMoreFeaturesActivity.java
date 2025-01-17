package com.sk.weichat.ui.message.multi;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.bean.message.MucRoomMember;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.sortlist.BaseComparator;
import com.sk.weichat.sortlist.BaseSortModel;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.BannedDialog;
import com.sk.weichat.view.CircleImageView;
import com.sk.weichat.view.ClearEditText;
import com.sk.weichat.view.HorizontalListView;
import com.sk.weichat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Features: 展示群成员
 * Features  禁言 && 删除群成员
 * Features: 群主对群内成员备注
 * <p>
 * 因为管理员也可以进入该界面进行前三种操作，且管理员需要显示userName 群主显示cardName 所以需要区分下
 * // Todo 当群组人数过多时，排序需要很久，先干掉排序功能，考虑替换排序规则
 */
public class GroupMoreFeaturesActivity extends BaseActivity {
    private ClearEditText mEditText;
    private boolean isSearch;

    private PullToRefreshListView mListView;
    private GroupMoreFeaturesAdapter mAdapter;
    // private List<BaseSortModel<RoomMember>> mSortRoomMember;
    // private List<BaseSortModel<RoomMember>> mSearchSortRoomMember;
    private List<RoomMember> mSortRoomMember;
    private List<RoomMember> mSearchSortRoomMember;
    private List<RoomMember> mSelectedRoomMember;
    private BaseComparator<RoomMember> mBaseComparator;

    private TextView mTextDialog;
    private TextView mTextSelectAll;
    private RelativeLayout delLayout;
    private Button mBtnBatchdel;
    private HorizontalListView mHorizontalListView;
    private HorListViewAdapter mHorAdapter;

    private String mRoomId;
    private boolean isLoadByService;
    private boolean isBanned;
    private boolean isDelete;
    private boolean isSetRemark;

    private RoomMember mRoomMember;
    private Map<String, String> mRemarksMap = new HashMap<>();
    private List<MucRoomMember> roomMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_all_member);
        mRoomId = getIntent().getStringExtra("roomId");
        isLoadByService = getIntent().getBooleanExtra("isLoadByService", false);
        isBanned = getIntent().getBooleanExtra("isBanned", false);
        isDelete = getIntent().getBooleanExtra("isDelete", false);
        isSetRemark = getIntent().getBooleanExtra("isSetRemark", false);

        initActionBar();
        initData();
        initView();
        if (isLoadByService) {
            loadDataByService(false);
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.group_member);

    }

    private void initData() {
        AsyncUtils.doAsync(this, c -> {
            List<Friend> mFriendList = FriendDao.getInstance().getAllFriends(coreManager.getSelf().getUserId());
            for (int i = 0; i < mFriendList.size(); i++) {
                if (!TextUtils.isEmpty(mFriendList.get(i).getRemarkName())) {// 针对该好友进行了备注
                    mRemarksMap.put(mFriendList.get(i).getUserId(), mFriendList.get(i).getRemarkName());
                }
            }
            c.uiThread(r -> {
                mAdapter.notifyDataSetChanged();// 刷新页面
            });
        });

        mSortRoomMember = new ArrayList<>();
        mSearchSortRoomMember = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mSelectedRoomMember = new ArrayList<>();

        List<RoomMember> data = RoomMemberDao.getInstance().getRoomMember(mRoomId);
        mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(mRoomId, coreManager.getSelf().getUserId());

        mSortRoomMember.addAll(data);
    }

    private void initView() {
        mListView = findViewById(R.id.pull_refresh_list);
        if (!isLoadByService) {// 不支持刷新
            mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        }
        mAdapter = new GroupMoreFeaturesAdapter(mSortRoomMember);
        mListView.getRefreshableView().setAdapter(mAdapter);

        mEditText = findViewById(R.id.search_et);
        mEditText.setHint(getString(R.string.search));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearch = true;
                mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mSearchSortRoomMember.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mListView.setMode(PullToRefreshBase.Mode.BOTH);
                    mAdapter.setData(mSortRoomMember);
                    return;
                }
//                for (int i = 0; i < mSortRoomMember.size(); i++) {
///*
//                    if (getName(mSortRoomMember.get(i).getBean()).contains(str)) { // 符合搜索条件的好友
//                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
//                    }
//*/
//
//                    if (getName(mSortRoomMember.get(i)).contains(str)) { // 符合搜索条件的好友
//                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
//                    }
//                }
//                mAdapter.setData(mSearchSortRoomMember);
                searchMember(mRoomId, str);// 调接口搜索
            }
        });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(false);
            }
        });

        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BaseSortModel<RoomMember> baseSortModel;
                final RoomMember roomMember;
                if (isSearch) {
                    // baseSortModel = mSearchSortRoomMember..get((int) id);
                    roomMember = mSearchSortRoomMember.get((int) id);
                } else {
                    // baseSortModel = mSortRoomMember..get((int) id);
                    roomMember = mSortRoomMember.get((int) id);
                }

                if (isDelete) {// 踢人
                    if (!checkDeleteMember(roomMember, true)) {
                        return;
                    }
                    boolean isSelected = findRoomMemberInList(mSelectedRoomMember, roomMember.getUserId()) != null;
                    if (isSelected) {
                        removeSelectMember(roomMember, true);
                    } else {
                        addSelectMember(roomMember, true);
                    }
                    mAdapter.notifyDataSetInvalidated();
                    mBtnBatchdel.setText(getString(R.string.add_chat_ok_btn, mSelectedRoomMember.size() + ""));
                } else if (isBanned) {// 禁言
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_banned_self);
                        return;
                    }

                    if (roomMember.getRole() == 1) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_ban_owner));
                        return;
                    }

                    if (roomMember.getRole() == 2) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_ban_manager));
                        return;
                    }

                    showBannedDialog(roomMember.getUserId());
                } else if (isSetRemark) {// 备注
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_remark_self);
                        return;
                    }
                    setRemarkName(roomMember.getUserId(), getName(roomMember));
                } else {
                    BasicInfoActivity.start(mContext, roomMember.getUserId(), BasicInfoActivity.FROM_ADD_TYPE_GROUP);
                }
            }
        });
        mTextSelectAll = (TextView)findViewById((R.id.tv_title_right));
        delLayout = (RelativeLayout)findViewById(R.id.delete_layout);
        if (isDelete) {
            mTextSelectAll.setVisibility(View.VISIBLE);
            mTextSelectAll.setText(R.string.select_all);
            mTextSelectAll.setOnClickListener(v -> {
                toggleSelectAll();
            });
            delLayout.setVisibility(View.VISIBLE);
            mBtnBatchdel = (Button)findViewById(R.id.ok_btn);
            mBtnBatchdel.setText(getString(R.string.add_chat_ok_btn, mSelectedRoomMember.size() + ""));
            mHorizontalListView = (HorizontalListView)findViewById(R.id.horizontal_list_view);
            mHorAdapter = new HorListViewAdapter();
            mHorizontalListView.setAdapter(mHorAdapter);
        } else {
            mTextSelectAll.setText("");
            mTextSelectAll.setVisibility(View.INVISIBLE);
            delLayout.setVisibility(View.GONE);
        }
        if (mBtnBatchdel != null) {
            mBtnBatchdel.setOnClickListener(v -> {
                deleteMember();
            });
        }
    }

    private  void deleteMember() {
        if (mSelectedRoomMember.size() <= 0) {
            Toast.makeText(GroupMoreFeaturesActivity.this, R.string.tip_select_at_lease_one_member, Toast.LENGTH_SHORT).show();
            return;
        }

        SelectionFrame mSF = new SelectionFrame(GroupMoreFeaturesActivity.this);
        StringBuilder sb = new StringBuilder("");
        Iterator<RoomMember> it = mSelectedRoomMember.iterator();
        while (it.hasNext()) {
            sb.append(getName(it.next()) + ",");
        }
        String userIdListDisplayStr = sb.toString();
        if (userIdListDisplayStr.endsWith(",")) {
            userIdListDisplayStr = userIdListDisplayStr.substring(0, userIdListDisplayStr.length() -1);
        }
        if (userIdListDisplayStr.length() > 100) {
            // 大于100个member名称，默认显示不下，用...代替
            userIdListDisplayStr += "...";
        }

        mSF.setSomething(null, getString(R.string.sure_remove_member_for_group, userIdListDisplayStr),
                new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        StringBuilder userIdsBuilder = new StringBuilder("");
                        Iterator<RoomMember> it = mSelectedRoomMember.iterator();
                        while (it.hasNext()) {
                            userIdsBuilder.append(it.next().getUserId() + ",");
                        }
                        String userIdListStr = userIdsBuilder.toString();
                        if (userIdListStr.endsWith(",")) {
                            userIdListStr = userIdListStr.substring(0, userIdListStr.length() -1);
                        }
                        userIdListStr = ""+ userIdListStr + "";
                        Map<String, String> params = new HashMap<>();
                        params.put("access_token", coreManager.getSelfStatus().accessToken);
                        params.put("roomId", mRoomId);
                        params.put("userIds", userIdListStr);
                        final  String evtIds = userIdListStr;
                        DialogHelper.showDefaulteMessageProgressDialog(GroupMoreFeaturesActivity.this);
                        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_BATCH_DEL)
                                .params(params)
                                .build()
                                .execute(new BaseCallback<Void>(Void.class) {

                                    @Override
                                    public void onResponse(ObjectResult<Void> result) {
                                        DialogHelper.dismissProgressDialog();
                                        if (Result.checkSuccess(mContext, result)) {
                                            Toast.makeText(mContext, R.string.remove_success, Toast.LENGTH_SHORT).show();
                                            removeListData(mSortRoomMember, mSelectedRoomMember);
                                            mSelectedRoomMember.clear();
                                            mTextSelectAll.setText(R.string.select_all);

                                            mBtnBatchdel.setText(getString(R.string.add_chat_ok_btn, "0"));
                                            mEditText.setText("");

                                            mAdapter.notifyDataSetInvalidated();
                                            mHorAdapter.notifyDataSetInvalidated();
                                            EventGroupStatus evt = new EventGroupStatus(10001, -1);
                                            evt.setExtraData(evtIds);
                                            EventBus.getDefault().post(evt);
                                        }
                                    }

                                    @Override
                                    public void onError(Call call, Exception e) {
                                        DialogHelper.dismissProgressDialog();
                                        ToastUtil.showErrorNet(mContext);
                                    }
                                });
                    }
                });
        mSF.show();
    }

    // 删除成员成功后，将selected从 target中移除
    private  void removeListData(List<RoomMember> target, List<RoomMember> selected) {
        if (target == null || selected == null) return;
        Iterator<RoomMember> it = target.iterator();
        List<RoomMember> result = new ArrayList<>();
        while (it.hasNext()) {
            RoomMember targetMember = it.next();
            boolean isExistInDeleteList = findRoomMemberInList(selected, targetMember.getUserId()) != null;
            if (!isExistInDeleteList) {
                result.add(targetMember);
            }else {
                // 同步删除本地数据
                RoomMemberDao.getInstance().deleteRoomMember(mRoomId, targetMember.getUserId());
            }
        }
        target.clear();
        target.addAll(result);
    }
    private boolean checkDeleteMember(RoomMember roomMember, boolean warnning) {
        if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
            if (warnning) {
                ToastUtil.showToast(mContext, R.string.can_not_remove_self);
            }
            return false;
        }
        if (roomMember.getRole() == 1) {
            if (warnning) {
                ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_owner));
            }
            return false;
        }

        if (roomMember.getRole() == 2 && mRoomMember != null && mRoomMember.getRole() != 1) {
            if (warnning) {
                ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_manager));
            }
            return false;
        }
        return true;
    }
    private void addSelectMember(RoomMember member, boolean now) {
        boolean isExist = findRoomMemberInList(mSelectedRoomMember, member.getUserId()) != null;
        if (isExist) return;
        mSelectedRoomMember.add(member);
        if (now) {
            mHorAdapter.notifyDataSetInvalidated();
        }
    }

    private  void removeSelectMember(RoomMember member, boolean now) {
        RoomMember m = findRoomMemberInList(mSelectedRoomMember, member.getUserId());
        if (m != null) {
            mSelectedRoomMember.remove(m);
            if (now) {
                mHorAdapter.notifyDataSetInvalidated();
            }
        }
    }

    private  RoomMember findRoomMemberInList(List<RoomMember> list, String userId) {
        if (list == null) return null;
        Iterator<RoomMember> it = list.iterator();
        while (it.hasNext()) {
            RoomMember m = it.next();
            if (m.getUserId().equals(userId)) {
                return m;
            }
        }
        return null;
    }

    private void toggleSelectAll() {
        boolean isSelectAllTxt = mTextSelectAll.getText().toString().equals(getString(R.string.select_all));
        if (isSelectAllTxt) {
            // 全选
            List<RoomMember> listData = mAdapter.getData();
            Iterator<RoomMember> it = listData.iterator();
            while (it.hasNext()) {
                RoomMember m = it.next();
                boolean isPass = checkDeleteMember(m, false);
                if (isPass) {
                    addSelectMember(m, false);
                }
            }
            if (mSelectedRoomMember.size() > 0) {
                mTextSelectAll.setText(R.string.select_all_none);
            }
        } else {
            // 全不选
            mSelectedRoomMember.clear();
            mTextSelectAll.setText(R.string.select_all);
        }
        mBtnBatchdel.setText(getString(R.string.add_chat_ok_btn, mSelectedRoomMember.size() + ""));
        mAdapter.notifyDataSetInvalidated();
        mHorAdapter.notifyDataSetInvalidated();
    }

    private void searchMember(String roomId, String keyword) {
        roomMembers = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("keyword", keyword);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_SEARCH)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoomMember>(MucRoomMember.class) {

                    @Override
                    public void onResponse(ArrayResult<MucRoomMember> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            roomMembers = result.getData();
                            if (roomMembers.size() > 0) {
                                for (int i = 0; i < roomMembers.size(); i++) {
                                    RoomMember roomMember = new RoomMember();
                                    roomMember.setRoomId(mRoomId);
                                    roomMember.setUserId(roomMembers.get(i).getUserId());
                                    roomMember.setUserName(roomMembers.get(i).getNickName());
                                    if (TextUtils.isEmpty(roomMembers.get(i).getRemarkName())) {
                                        roomMember.setCardName(roomMembers.get(i).getNickName());
                                    } else {
                                        roomMember.setCardName(roomMembers.get(i).getRemarkName());
                                    }
                                    roomMember.setRole(roomMembers.get(i).getRole());
                                    roomMember.setCreateTime(roomMembers.get(i).getCreateTime());
                                    mSearchSortRoomMember.add(roomMember);
                                }
                            }
                            mAdapter.setData(mSearchSortRoomMember);
                        } else {
                            mAdapter.setData(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        mAdapter.setData(new ArrayList<>());
                    }
                });
    }

    private void loadDataByService(boolean reset) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        if (reset) {
            params.put("joinTime", String.valueOf(0));
        } else {
            long lastRoamingTime = PreferenceUtils.getLong(MyApplication.getContext(), Constants.MUC_MEMBER_LAST_JOIN_TIME + coreManager.getSelf().getUserId() + mRoomId, 0);
            params.put("joinTime", String.valueOf(lastRoamingTime));
        }
        params.put("pageSize", Constants.MUC_MEMBER_PAGE_SIZE);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoomMember>(MucRoomMember.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoomMember> result) {
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }

                        HashMap<String, String> toRepeatHashMap = new HashMap<>();
                        for (RoomMember member : mSortRoomMember) {
                            toRepeatHashMap.put(member.getUserId(), member.getUserId());
                        }

                        if (Result.checkSuccess(mContext, result)) {
                            List<MucRoomMember> mucRoomMemberList = result.getData();
                            if (mucRoomMemberList.size() == Integer.valueOf(Constants.MUC_MEMBER_PAGE_SIZE)) {
                                mListView.setMode(PullToRefreshBase.Mode.BOTH);
                            } else {
                                mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            }
                            if (mucRoomMemberList.size() > 0) {
                                List<RoomMember> roomMemberList = new ArrayList<>();
                                for (int i = 0; i < mucRoomMemberList.size(); i++) {
                                    if (!reset &&
                                            toRepeatHashMap.containsKey(mucRoomMemberList.get(i).getUserId())) {
                                        continue;
                                    }
                                    RoomMember roomMember = new RoomMember();
                                    roomMember.setRoomId(mRoomId);
                                    roomMember.setUserId(mucRoomMemberList.get(i).getUserId());
                                    roomMember.setUserName(mucRoomMemberList.get(i).getNickName());
                                    if (TextUtils.isEmpty(mucRoomMemberList.get(i).getRemarkName())) {
                                        roomMember.setCardName(mucRoomMemberList.get(i).getNickName());
                                    } else {
                                        roomMember.setCardName(mucRoomMemberList.get(i).getRemarkName());
                                    }
                                    roomMember.setRole(mucRoomMemberList.get(i).getRole());
                                    roomMember.setCreateTime(mucRoomMemberList.get(i).getCreateTime());
                                    roomMemberList.add(roomMember);
                                }

                                if (reset) {
                                    RoomMemberDao.getInstance().deleteRoomMemberTable(mRoomId);
                                }
                                AsyncUtils.doAsync(this, mucChatActivityAsyncContext -> {
                                    for (int i = 0; i < roomMemberList.size(); i++) {// 在异步任务内存储
                                        RoomMemberDao.getInstance().saveSingleRoomMember(mRoomId, roomMemberList.get(i));
                                    }
                                });

                                RoomInfoActivity.saveMucLastRoamingTime(coreManager.getSelf().getUserId(), mRoomId, mucRoomMemberList.get(mucRoomMemberList.size() - 1).getCreateTime(), reset);

                                // 刷新本地数据
                                if (reset) {
                                    mSortRoomMember.clear();
                                    mSortRoomMember.addAll(roomMemberList);
                                    mAdapter.notifyDataSetInvalidated();
                                } else {
                                    mSortRoomMember.addAll(roomMemberList);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    private String getName(RoomMember member) {
        if (mRoomMember != null && mRoomMember.getRole() == 1) {
            if (!TextUtils.equals(member.getUserName(), member.getCardName())) {// 当userName与cardName不一致时，我们认为群主有设置群内备注
                return member.getCardName();
            } else {
                if (mRemarksMap.containsKey(member.getUserId())) {
                    return mRemarksMap.get(member.getUserId());
                } else {
                    return member.getUserName();
                }
            }
        } else {
            if (mRemarksMap.containsKey(member.getUserId())) {
                return mRemarksMap.get(member.getUserId());
            } else {
                return member.getUserName();
            }
        }
    }


    private void showBannedDialog(final String userId) {
        final int daySeconds = 24 * 60 * 60;
        BannedDialog bannedDialog = new BannedDialog(mContext, new BannedDialog.OnBannedDialogClickListener() {

            @Override
            public void tv1Click() {
                bannedVoice(userId, 0);
            }

            @Override
            public void tv2Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() / 1000 + daySeconds / 48);
            }

            @Override
            public void tv3Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() / 1000 + daySeconds / 24);
            }

            @Override
            public void tv4Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() / 1000 + daySeconds);
            }

            @Override
            public void tv5Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() / 1000 + daySeconds * 3);
            }

            @Override
            public void tv6Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() / 1000 + daySeconds * 7);
            }

            @Override
            public void tv7Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() / 1000 + daySeconds * 15);
            }
        });
        bannedDialog.show();
    }

    /**
     * 删除群成员
     */
    private void deleteMember(final RoomMember baseSortModel, final String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("userId", userId);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(mContext, R.string.remove_success, Toast.LENGTH_SHORT).show();
                            mSortRoomMember.remove(baseSortModel);
                            mEditText.setText("");

                            RoomMemberDao.getInstance().deleteRoomMember(mRoomId, userId);
                            EventBus.getDefault().post(new EventGroupStatus(10001, Integer.valueOf(userId)));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 禁言
     */
    private void bannedVoice(String userId, final long time) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("userId", userId);
        params.put("talkTime", String.valueOf(time));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (time == 0) {
                                ToastUtil.showToast(mContext, R.string.canle_banned_succ);
                            } else {
                                ToastUtil.showToast(mContext, R.string.banned_succ);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 对群内成员备注
     */
    private void setRemarkName(final String userId, final String name) {
        DialogHelper.showLimitSingleInputDialog(this, getString(R.string.change_remark), name, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newName = ((EditText) v).getText().toString().trim();
                if (TextUtils.isEmpty(newName) || newName.equals(name)) {
                    return;
                }

                Map<String, String> params = new HashMap<>();
                params.put("access_token", coreManager.getSelfStatus().accessToken);
                params.put("roomId", mRoomId);
                params.put("userId", userId);
                params.put("remarkName", newName);
                DialogHelper.showDefaulteMessageProgressDialog(GroupMoreFeaturesActivity.this);

                HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {

                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                if (Result.checkSuccess(mContext, result)) {
                                    ToastUtil.showToast(mContext, R.string.modify_succ);
                                    RoomMemberDao.getInstance().updateRoomMemberCardName(mRoomId, userId, newName);

                                    for (int i = 0; i < mSortRoomMember.size(); i++) {
/*
                                                if (mSortRoomMember.get(i).getBean().getUserId().equals(userId)) {
                                                    mSortRoomMember.get(i).getBean().setCardName(newName);
                                                }
*/
                                        if (mSortRoomMember.get(i).getUserId().equals(userId)) {
                                            mSortRoomMember.get(i).setCardName(newName);
                                        }
                                    }
                                    if (!TextUtils.isEmpty(mEditText.getText().toString())) {// 清空mEditText
                                        mEditText.setText("");
                                    } else {
                                        mAdapter.setData(mSortRoomMember);
                                    }
                                    // 更新群组信息页面
                                    EventBus.getDefault().post(new EventGroupStatus(10003, 0));
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                ToastUtil.showErrorNet(mContext);
                            }
                        });
            }
        });
    }

    class GroupMoreFeaturesAdapter extends BaseAdapter {
        List<RoomMember> mSortRoomMember;

        GroupMoreFeaturesAdapter(List<RoomMember> sortRoomMember) {
            this.mSortRoomMember = new ArrayList<>();
            this.mSortRoomMember = sortRoomMember;
        }

        public void setData(List<RoomMember> sortRoomMember) {
            this.mSortRoomMember = sortRoomMember;
            notifyDataSetChanged();
        }

        public List<RoomMember> getData() {
            return  this.mSortRoomMember;
        }

        @Override
        public int getCount() {
            if (mSortRoomMember == null) return 0;
            return mSortRoomMember.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortRoomMember.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_room_all_member, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView roleS = ViewHolder.get(convertView, R.id.roles);
            CheckBox checkSelect = ViewHolder.get(convertView, R.id.check_box_delete_staff);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);

/*
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSortRoomMember.get(position).getFirstLetter());
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
            }
*/
            catagoryTitleTv.setVisibility(View.GONE);

            RoomMember member = mSortRoomMember.get(position);
            if (member != null) {
                AvatarHelper.getInstance().displayAvatar(getName(member), member.getUserId(), avatarImg, true);
                switch (member.getRole()) {
                    case RoomMember.ROLE_OWNER:
                        roleS.setText(getString(R.string.group_owner));
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role1)));
                        break;
                    case RoomMember.ROLE_MANAGER:
                        roleS.setText(getString(R.string.group_manager));
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role2)));
                        break;
                    case RoomMember.ROLE_MEMBER:
                        roleS.setText(getString(R.string.group_role_normal));
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role3)));
                        break;
                    case RoomMember.ROLE_INVISIBLE:
                        roleS.setText(R.string.role_invisible);
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role4)));
                        break;
                    case RoomMember.ROLE_GUARDIAN:
                        roleS.setText(R.string.role_guardian);
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role5)));
                        break;
                    default:
                        Reporter.unreachable();
                        roleS.setVisibility(View.GONE);
                        break;
                }

                userNameTv.setText(getName(member));
                if (!isDelete) {
                    checkSelect.setVisibility(View.INVISIBLE);
                }else {
                    boolean isSelected = false;
                    // 数据源不同，但存在可能勾选的情况
                    RoomMember rm = findRoomMemberInList(mSelectedRoomMember, member.getUserId());
                    if (rm != null) {
                        isSelected = true;
                    }
                    checkSelect.setVisibility(View.VISIBLE);
                    if (isSelected) {
                        checkSelect.setChecked(true);
                        ColorStateList tabColor = SkinUtils.getSkin(GroupMoreFeaturesActivity.this).getTabColorState();
                        Drawable drawable = getResources().getDrawable(R.drawable.sel_check_wx2);
                        drawable = DrawableCompat.wrap(drawable);
                        DrawableCompat.setTintList(drawable, tabColor);
                        checkSelect.setButtonDrawable(drawable);
                    } else {
                        checkSelect.setChecked(false);
                        checkSelect.setButtonDrawable(getResources().getDrawable(R.drawable.sel_nor_wx2));
                    }
                }
            }
            return convertView;
        }

/*
        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortRoomMember.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortRoomMember.get(position).getFirstLetter().charAt(0);
        }
*/
    }

    class HorListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSelectedRoomMember.size();
        }

        @Override
        public Object getItem(int position) {
            return mSelectedRoomMember.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new CircleImageView(mContext);
                int size = DisplayUtil.dip2px(mContext, 37);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            ImageView imageView = (ImageView) convertView;
            RoomMember member  = (RoomMember)getItem(position);
            AvatarHelper.getInstance().displayAvatar(member.getUserId(), imageView, true);
            return convertView;
        }
    }
}
