package br.com.nerdspace;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonSignup;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignin;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }
        //esse é um teste que funcionou, mas deve ser revisado a utilizacao
        else {
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //initializing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin);
        buttonSignup = (Button) findViewById(R.id.buttomRegister);

        progressDialog = new ProgressDialog(this);

        //attaching listener to button
        buttonSignup.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);
    }

    private void registerUser() {
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

        progressDialog.setMessage("Registrando usuário...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task){
                       if(task.isSuccessful()){
                           finish();
                           startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                       }else {
                           Toast.makeText(MainActivity.this, "Não foi possível registrar, tente novamente.", Toast.LENGTH_SHORT).show();
                       }
                       progressDialog.dismiss();
                   }
                });

    }


    @Override
    public void onClick(View view){

        if (view == buttonSignup){

            registerUser();

        }

        if (view == textViewSignin){

            startActivity(new Intent(this, LoginActivity.class));

        }

    }

}

