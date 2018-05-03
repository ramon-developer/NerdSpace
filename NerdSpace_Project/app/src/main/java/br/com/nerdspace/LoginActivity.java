package br.com.nerdspace;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;



public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;
    private LoginButton mFacebookLoginButton;
    SignInButton signInButtonGoogle;
    private CallbackManager mCallbackManager;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;

    //Google params
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSignIn = (Button) findViewById(R.id.buttomSignin);
        textViewSignup = (TextView) findViewById(R.id.textViewSignUp);
        signInButtonGoogle = (SignInButton)findViewById(R.id.buttonGoogleSignIn);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }

        progressDialog = new ProgressDialog(this);

        buttonSignIn.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);

        // Configura Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);


        signInButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }

        });

        initComponent();
        initFirebaseCallback();
        onClickFacebookLoginButton();

    } /*** fim onCreate method ***/



    /*************************************
     *            USER LOGIN             *
     *************************************/

    private void userLogin(){

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Por favor, digite seu email.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Por favor, digite a senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Validando...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()){

                            finish();
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

                        } else {

                            Toast.makeText(LoginActivity.this, "   Falha na Autenticação: \n" + "Email ou Senha incorretos!",
                                    Toast.LENGTH_LONG).show();

                        }

                    }

                });

    } /*************************************/



    /*************************************
     *            GOOGLE LOGIN           *
     *************************************/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Logou na conta Google com sucesso, autentica com o Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e){
                // Falha ao logar na conta Google, mostra um log de falha
                Log.w(TAG, "Falha ao logar com a conta Google.", e);
            }
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);//Facebook Login, verificar se é abaixo ou em cima.
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getIdToken(),null);

        progressDialog.setMessage("Validando...");
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            finish();
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "   Falha na Autenticação: \n" + "Email ou Senha incorretos!",
                                    Toast.LENGTH_LONG).show();

                        }

                        progressDialog.dismiss();

                    }

                });

    } /*************************************/




//    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, 101);
//    }

    /*FACEBOOK COMPONENT*/
    private void initComponent() {
        mFacebookLoginButton = (LoginButton) findViewById(R.id.buttonFacebookSignin);
        mFacebookLoginButton.setReadPermissions("email","public_profile");
    }

    private void initFirebaseCallback() {
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
    }

    private void firebaseAuthWithFacebook(AccessToken accessToken) {

        progressDialog.setMessage("Validando...");
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                  Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
                  startActivity(i);
//////                    FirebaseUser user = mAuth.getCurrentUser();
//                    finish();
//                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

                } else {

                    Toast.makeText(LoginActivity.this, "   Falha na Autenticação: \n" + "Erro ao logar com Facebook.",
                            Toast.LENGTH_LONG).show();

                }

                progressDialog.dismiss();

            }
        });
    }



    /*** ações ao clicar nos botões ***/
    @Override
    public void onClick(View view){

        if(view == buttonSignIn){

            userLogin();

        }

        if(view == textViewSignup){

            finish();
            startActivity(new Intent(this, RegisterActivity.class));

        }

    }


    private void onClickFacebookLoginButton() {

        mFacebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                alert("Operação cancelada!");
            }

            @Override
            public void onError(FacebookException error) {
                alert("Erro ao tentar logar com o Facebook!");
            }
        });

    }


    /*** Alert ***/
    private void alert(String s) {

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

    }

} /*** fim activity ***/
