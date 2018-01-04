package jp.techacademy.yutaka.iida.qa_app;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by iiday on 2018/01/03.
 */

public class Favorite extends RealmObject implements Serializable {
    private String questionid; // タイトル

    // id をプライマリーキーとして設定
    @PrimaryKey
    private int id;

    public String getQuestionid() {
        return questionid;
    }
    public void setQuestionid(String questionid) {
        this.questionid = questionid;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

}
