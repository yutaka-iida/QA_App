package jp.techacademy.yutaka.iida.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mNameEditText;
    ProgressDialog mProgress;

    FirebaseAuth mAuth;
    OnCompleteListener<AuthResult> mCreateAccountListner;
    OnCompleteListener<AuthResult> mLoginListener;
    DatabaseReference mDataBaseReference;

    boolean mIsCreateAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCreateAccountListner = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String email = mEmailEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();
                    login(email, password);
                }
                else{
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "アカウントの作成に失敗しました", Snackbar.LENGTH_LONG).show();
                    mProgress.dismiss();
                }
            }
        };

        mLoginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());

                    if(mIsCreateAccount){
                        String name = mNameEditText.getText().toString();
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", name);
                        userRef.setValue(data);
                        saveName(name);
                    }
                    else{
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map data = (Map) dataSnapshot.getValue();
                                saveName((String)data.get("name"));
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                    mProgress.dismiss();
                    finish();
                }
                else{
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();
                    mProgress.dismiss();
                }
            }
        };

        setTitle("ログイン");
        mEmailEditText = (EditText)findViewById(R.id.emailText);
        mPasswordEditText = (EditText)findViewById(R.id.passwordText);
        mNameEditText = (EditText)findViewById(R.id.nameText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        Button createButton = (Button)findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String name = mNameEditText.getText().toString();

                if(email.length() != 0 && password.length() >= 6 && name.length() != 0){
                    mIsCreateAccount = true;
                    createAccount(email, password);
                }
                else{
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Button loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if(email.length() != 0 && password.length() >= 6){
                    mIsCreateAccount = false;
                    login(email, password);
                }
                else{
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
    private void createAccount(String email, String password){
        mProgress.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListner);
    }
    private void login(String email, String password){
        mProgress.show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(mLoginListener);
    }
    private void saveName(String name){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.NameKEY, name);
        editor.commit();
    }

}
