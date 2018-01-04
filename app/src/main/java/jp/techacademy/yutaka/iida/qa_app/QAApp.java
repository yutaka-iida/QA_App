package jp.techacademy.yutaka.iida.qa_app;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by iiday on 2018/01/04.
 */

public class QAApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
