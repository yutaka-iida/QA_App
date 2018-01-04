package jp.techacademy.yutaka.iida.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AnswerSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener{

    private EditText mAnswerEditText;
    private Question mQuestion;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_send);

        Bundle extras = getIntent().getExtras();
        mQuestion = (Question)extras.get("question");

        mAnswerEditText = (EditText)findViewById(R.id.answerEditText);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("投稿中...");

        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }
    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference){
        mProgress.dismiss();
        if(databaseError == null){
            finish();
        }
        else{
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }
    @Override
    public void onClick(View v){
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference answerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);

        Map<String, String> data = new HashMap<String, String>();

        data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(Const.NameKEY, "");
        data.put("name", name);

        String answer = mAnswerEditText.getText().toString();
        if(answer.length() == 0){
            Snackbar.make(v, "回答を入力して下さい", Snackbar.LENGTH_LONG).show();
            return;
        }
        data.put("body", answer);

        mProgress.show();
        answerRef.push().setValue(data, this);
    }

}
