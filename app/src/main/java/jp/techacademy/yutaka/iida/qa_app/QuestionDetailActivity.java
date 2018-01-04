package jp.techacademy.yutaka.iida.qa_app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private FloatingActionButton mFab2;
    private boolean mIsFavorite = false;

    private DatabaseReference mAnswerRef;
    private Realm mRealm;

    private ChildEventListener mEventListner = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap)dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()){
                if(answerUid.equals(answer.getAnswerUid())){
                    return;
                }
            }
            String body = (String)map.get("body");
            String name = (String)map.get("name");
            String uid = (String)map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        Bundle extras = getIntent().getExtras();
        mQuestion = (Question)extras.get("question");
        mRealm = Realm.getDefaultInstance();

        setTitle(mQuestion.getTitle());

        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        mFab2 = (FloatingActionButton)findViewById(R.id.fab2);
        mFab2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // お気に入りの更新
                updateFavorite();
                return;
            }
        });

        showFavbutton();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListner);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mRealm.close();
    }

    private void updateFavorite(){
        if(mIsFavorite == false){ // 追加
            Favorite favorite = new Favorite();
            RealmResults<Favorite> favoriteRealmResults = mRealm.where(Favorite.class).findAll();
            int identifier;
            if (favoriteRealmResults.max("id") != null) {
                identifier = favoriteRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            favorite.setId(identifier);
            favorite.setQuestionid(mQuestion.getQuestionUid());
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(favorite);
            mRealm.commitTransaction();
        }
        else{ // 削除
            RealmResults<Favorite> results = mRealm.where(Favorite.class).equalTo(Const.QuestionID, mQuestion.getQuestionUid()).findAll();
            mRealm.beginTransaction();
            results.deleteAllFromRealm();
            mRealm.commitTransaction();
        }
        showFavbutton();
    }

    @Override
    protected void onResume(){
        super.onResume();
        showFavbutton();
    }

    private void showFavbutton(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // ログインしてないので表示しない
        if(user == null) {
            mFab2.setVisibility(View.INVISIBLE);
        }
        else{
            mFab2.setVisibility(View.VISIBLE);
        }
        // お気に入りの状況に合わせてぼたん表示
        RealmResults<Favorite> results = mRealm.where(Favorite.class).equalTo(Const.QuestionID, mQuestion.getQuestionUid()).findAll();
        if(results.size() == 0) {
            mFab2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fav_off, null));
            mIsFavorite = false;
        }
        else{
            mFab2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fav, null));
            mIsFavorite = true;
        }

    }
}
