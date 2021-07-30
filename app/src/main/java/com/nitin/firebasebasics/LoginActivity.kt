package com.nitin.firebasebasics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nitin.firebasebasics.databinding.LoginActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: LoginActivityBinding
    private val requestCodeSignIn = 100
//    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //common for email and google sign in
        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener{
            loginUser()
        }
        binding.tvGoToSignup.setOnClickListener{
            gotoSignupActivity()
        }
        binding.googleSignInButton.setOnClickListener{

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()
            val signInClient = GoogleSignIn.getClient(this, gso)
            signInClient.signInIntent.also{
                startActivityForResult(it, requestCodeSignIn)
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null) {
            gotoBioDataActivity()
        }
    }

    private fun googleAuthForFirebase(idToken: String?){
        val credentials = GoogleAuthProvider.getCredential(idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                auth.signInWithCredential(credentials).await()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_LONG).show()
                    gotoBioDataActivity()
                }
            } catch( e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try{
            if(requestCode == requestCodeSignIn) {
                googleAuthForFirebase(GoogleSignIn.getSignedInAccountFromIntent(data).result.idToken)
            }
        } catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.d("Login", e.printStackTrace().toString())
        }

    }


    //email sign in
    private fun loginUser() {

        val userEmail = binding.etEmail.text.toString()
        val userPassword = binding.etPassword.text.toString()

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        //coroutine login
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .await()
                withContext(Dispatchers.Main) {
                    if (auth.currentUser != null) {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT)
                            .show()
                        gotoBioDataActivity()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity,"Login failed : " + e.message, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    //login success activity
    private fun gotoBioDataActivity() {
        startActivity( Intent( this, BioDataActivity::class.java))
        finish()
    }

    //create account activity
    private fun gotoSignupActivity() {
        startActivity( Intent( this, SignupActivity::class.java))
    }

}