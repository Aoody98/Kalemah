package com.Motawer.kalemah.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Motawer.kalemah.MainActivity;
import com.Motawer.kalemah.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;



public class SignIn_Activity extends AppCompatActivity
{
    EditText emailEditText,passwordEditText;
    TextView signUp,forgetPassword;
    Button btn_login,fb, signInGoogle;
    String email,password,gUsername,gEmail;
    ProgressDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference reference =database.getReference("User");
    Uri image;
    int RC_SIGN_IN=100;
    CallbackManager mCallbackManager;
    LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in_);
        // init facebook sdk
        FacebookSdk.sdkInitialize(SignIn_Activity.this);
        initFacebook();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient =GoogleSignIn.getClient(this,gso);



        initViews();
        initButtons();
    }

    private void initFacebook()
    {
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel()
            {
            }
            @Override
            public void onError(FacebookException error)
            {
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token)
    {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String uid =task.getResult().getUser().getUid();
                            gUsername=task.getResult().getUser().getDisplayName();
                            gEmail=task.getResult().getUser().getEmail();
                            image =task.getResult().getUser().getPhotoUrl();

                            UserModel userModel=new UserModel(gUsername,gEmail);
                            reference.child(uid).setValue(userModel);
                            reference.child(uid).child("photo").setValue(String.valueOf(image));
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignIn_Activity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user)
    {
        if ( user != null)
        {
            Toast.makeText(this, "Facebook sign in successful..", Toast.LENGTH_SHORT).show();

            Intent intent =new Intent(SignIn_Activity.this,MainActivity.class);
            startActivity(intent);
        }else 
        {
            Toast.makeText(this, "please sign in to continue", Toast.LENGTH_SHORT).show();
        }

    }

    private void initButtons()
    {
        signInGoogle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

        signUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(SignIn_Activity.this,SignUp_Activity.class));

            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(SignIn_Activity.this,ForgetPassword_Activity.class));

            }
        });

        btn_login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                email=emailEditText.getText().toString().trim();
                password=passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email))
                {
                    Toast.makeText(getApplicationContext(), "Enter your email", Toast.LENGTH_SHORT).show();
                    emailEditText.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(getApplicationContext(), "Enter your password", Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                    return;
                }

                progressDialog = new ProgressDialog(SignIn_Activity.this);
                progressDialog.setMessage("Wait ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();

                firebaseAuth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful())
                                {
                                    progressDialog.dismiss();
                                    FirebaseUser user =firebaseAuth.getCurrentUser();
                                    Toast.makeText(SignIn_Activity.this, "Sign in successful..", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignIn_Activity.this,MainActivity.class));
                                    finish();
                                }else
                                {
                                    Toast.makeText(SignIn_Activity.this, "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();

                                }

                            }
                        });
            }
        });
    }

    private void initViews()
    {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUp = findViewById(R.id.signUp);
        forgetPassword = findViewById(R.id.forgetPassword);
        btn_login = findViewById(R.id.btn_login);
        firebaseAuth=FirebaseAuth.getInstance();
        signInGoogle =findViewById(R.id.sign_in_google);
        fb =  findViewById(R.id.fb);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                         //   FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (task.getResult().getAdditionalUserInfo().isNewUser())
                            {
                                String uid =task.getResult().getUser().getUid();
                                gUsername=task.getResult().getUser().getDisplayName();
                                gEmail=task.getResult().getUser().getEmail();
                                image =task.getResult().getUser().getPhotoUrl();

                                UserModel userModel=new UserModel(gUsername,gEmail);
                                reference.child(uid).setValue(userModel);
                                reference.child(uid).child("photo").setValue(String.valueOf(image));

                            }
                            Toast.makeText(SignIn_Activity.this, "Sign in with gmail successful..", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(SignIn_Activity.this,MainActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignIn_Activity.this, "Login Failed...", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignIn_Activity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickFacebookButton(View view)
    {
        if (view == fb) {
            loginButton.performClick();
        }
    }

}
