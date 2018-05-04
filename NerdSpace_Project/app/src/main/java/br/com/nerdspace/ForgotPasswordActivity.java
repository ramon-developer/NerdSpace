package br.com.nerdspace;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText passwordEmail;
    private Button resetPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //backButton
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        passwordEmail = (EditText)findViewById(R.id.editTextPasswordEmail);
        resetPassword = (Button)findViewById(R.id.buttonPasswordEmail);
        mAuth = FirebaseAuth.getInstance();

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = passwordEmail.getText().toString().trim();

                if (userEmail.equals("")) {
                    Toast.makeText(ForgotPasswordActivity.this, "Por favor, insira o seu email", Toast.LENGTH_SHORT).show();
                } else {

                    progressDialog.setMessage("Verificando email...");
                    progressDialog.show();

                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            progressDialog.dismiss();

                            if(task.isSuccessful()){
                                Toast.makeText(ForgotPasswordActivity.this, "Email para resetar a senha enviado!", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Erro ao enviar email para resetar a senha!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            //ends the activity
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
