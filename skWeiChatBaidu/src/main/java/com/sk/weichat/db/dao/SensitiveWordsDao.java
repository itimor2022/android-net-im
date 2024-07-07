package com.sk.weichat.db.dao;

import android.text.TextUtils;

import com.sk.weichat.bean.Contact;
import com.sk.weichat.bean.Label;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.MyZan;
import com.sk.weichat.bean.SensitiveWords;
import com.sk.weichat.db.SQLiteHelper;
import com.sk.weichat.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 聊天敏感词的Dao
 */
public class SensitiveWordsDao {
    private static SensitiveWordsDao instance = null;
    public Dao<SensitiveWords, String> dao;

    public static SensitiveWordsDao getInstance() {
        if (instance == null) {
            synchronized (SensitiveWordsDao.class) {
                if (instance == null) {
                    instance = new SensitiveWordsDao();
                }
            }
        }
        return instance;
    }

    private SensitiveWordsDao() {
        try {
            dao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    SensitiveWords.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    // 创建联系人
    public boolean createWord(SensitiveWords word) {
        try {
            List<SensitiveWords> list = getWordById(word.getId());
            if (list.size() > 0) {
                return false;
            }
            dao.create(word);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取ownerId下指定id的联系人
    public List<SensitiveWords> getWordById(String id) {
        try {
            PreparedQuery<SensitiveWords> preparedQuery = dao.queryBuilder().where()
                    .eq("id", id)
                    .prepare();

            return dao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 删除联系人
    public void deleteWord(String id) {
        try {
            DeleteBuilder<SensitiveWords, String> builder = dao.deleteBuilder();
            builder.where().eq("id", id);
            dao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取所有words
    public List<SensitiveWords> getAll() {
        try {
            PreparedQuery<SensitiveWords> preparedQuery = dao.queryBuilder().where()
                    .isNotNull("id")
                    .prepare();

            return dao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean refreshWords(List<SensitiveWords> list) {
        // 1.清空本地
        List<SensitiveWords> oldList = getAll();
        if (oldList != null) {
            for (SensitiveWords w : oldList) {
                deleteWord(w.getId());
            }
        }
        if (list == null) return false;
        // 2.将服务端数据存入本地
        for (SensitiveWords ww : list) {
            if (!TextUtils.isEmpty(ww.getWord())) {
                createWord(ww);
            }
        }
        return true;
    }
}