package com.nitin.firebasebasics

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.nitin.firebasebasics.databinding.SignupActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: SignupActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        binding.btnSignup.setOnClickListener{
            signupNewUser()
        }
        binding.tvGoToLogin.setOnClickListener{
            gotoLoginActivity()
        }
    }

    private fun signupNewUser() {

        val newUserSignupEmail: String = binding.etEmail.text.toString()
        val newUserSignupPassword: String = binding.etPassword.text.toString()
        val newUserConfirmSignupPassword: String = binding.etConfirmPassword.text.toString()

        if (newUserSignupEmail.isEmpty() || newUserSignupPassword.isEmpty() || newUserConfirmSignupPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newUserConfirmSignupPassword != newUserSignupPassword) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
            return
        }


        //coroutine sign up
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.createUserWithEmailAndPassword(newUserSignupEmail, newUserSignupPassword)
                    .await()
                withContext(Dispatchers.Main) {
                    if (auth.currentUser != null) {
                        Toast.makeText(this@SignupActivity, "Signup successful", Toast.LENGTH_SHORT)
                            .show()
                        gotoLoginActivity()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SignupActivity,
                        "Signup failed : " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun gotoLoginActivity() {
        auth.signOut()
        finish()
    }

}