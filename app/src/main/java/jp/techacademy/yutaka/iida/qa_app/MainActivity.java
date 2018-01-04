package jp.techacademy.yutaka.iida.qa_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int mGenre = 0;

    private List<Favorite> mFavorites;
    private Realm mRealm;


    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionListAdapter mAdapter;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String questionUid = dataSnapshot.getKey();

            // お気に入り
            if(mGenre == Const.FavoriteItem){
                // お気に入り以外はふるいにかける
                RealmResults<Favorite> results = mRealm.where(Favorite.class).equalTo(Const.QuestionID, questionUid).findAll();
                if(results.size() == 0) {
                    return;
                }
            }
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if(imageString != null){
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            }
            else{
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap)map.get("answers");
            if(answerMap != null){
                for(Object key: answerMap.keySet()){
                    HashMap temp = (HashMap)answerMap.get(key);
                    String answerBody = (String)temp.get("body");
                    String answerName = (String)temp.get("name");
                    String answerUid = (String)temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String)key);
                    answerArrayList.add(answer);
                }
            }
            if(mGenre == Const.FavoriteItem){
                String genr = (String)map.get(Const.GenrID);
                Question question = new Question(title, body, name, uid, questionUid, Integer.parseInt(genr), bytes, answerArrayList);
                mQuestionArrayList.add(question);
            }
            else{
                Question question = new Question(title, body, name, uid, questionUid, mGenre, bytes, answerArrayList);
                mQuestionArrayList.add(question);
            }
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
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGenre == 0){
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if(mGenre == Const.FavoriteItem){
                    Snackbar.make(view, "お気に入りでは使用できません", Snackbar.LENGTH_LONG).show();
                    return;
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("趣味");
                    mGenre = 1;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("生活");
                    mGenre = 2;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("健康");
                    mGenre = 3;
                } else if (id == R.id.nav_compter) {
                    mToolbar.setTitle("コンピューター");
                    mGenre = 4;
                } else if (id == R.id.nav_favorite) {
                    mToolbar.setTitle("お気に入り");
                    mGenre = Const.FavoriteItem;
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                refreshList();

                return true;
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new QuestionListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
        // お気に入りの表示非表示設定
        setFavoriteMenuItem();
        // レルムの設定
        mRealm = Realm.getDefaultInstance();
    }

    // リストビューの再表示
    private void refreshList(){

        if(mGenre == 0){
            return;
        }
        mQuestionArrayList.clear();
        mAdapter.setmQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        if(mGenreRef != null){
            mGenreRef.removeEventListener(mEventListener);
        }
        if(mGenre == Const.FavoriteItem){
            // お気に入りの時は全ジャンルを読み込み対象とする
            for(int i=0; i < Const.FavoriteItem-1; i++){
                mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i+1));
                mGenreRef.addChildEventListener(mEventListener);
            }
        }
        else{
            mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
            mGenreRef.addChildEventListener(mEventListener);
        }
        return;
    }

    @Override
    protected void onResume(){
        super.onResume();
        // お気に入りの表示非表示設定
        setFavoriteMenuItem();
        // リストの更新
        refreshList();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mRealm.close();
    }

    // お気に入りの表示設定
    private void setFavoriteMenuItem(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem favoMenuItem = menu.findItem(R.id.nav_favorite);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            favoMenuItem.setVisible(false);
        }
        else{
            favoMenuItem.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
