package com.nitin.firebasebasics

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nitin.firebasebasics.databinding.BiodataActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class BioDataActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: BiodataActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BiodataActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val uid = FirebaseAuth.getInstance().uid.toString()
        val userDocument: DocumentReference = Firebase.firestore.collection("users").document(uid)

        fillUserData( userDocument)

        binding.btnLogout.setOnClickListener{
            logoutUser()
        }
        binding.btnSubmit.setOnClickListener{
            submitDetails( userDocument)
        }

    }

    private fun fillUserData(userDocument: DocumentReference) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userDocument.get().await().toObject(User::class.java)
                withContext(Dispatchers.Main) {
                    binding.etUsername.setText(user?.uname, TextView.BufferType.EDITABLE)
                    binding.etNationality.setText(user?.nationality, TextView.BufferType.EDITABLE)
                    binding.etCourse.setText(user?.course, TextView.BufferType.EDITABLE)
                }
            } catch (e:Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@BioDataActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun submitDetails(userDocument: DocumentReference) {

        val username = binding.etUsername.text.toString()
        val nationality = binding.etNationality.text.toString()
        val course = binding.etCourse.text.toString()
        val user = User(username, nationality, course)

        CoroutineScope(Dispatchers.IO).launch {
            try{
                userDocument.set(user).await()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@BioDataActivity, "data inserted", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@BioDataActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun logoutUser() {
        //logout user from firebase
        Firebase.auth.signOut()

        //logout user from google sign in cache
        //allows user to choose account next time without choosing the previous choice by default
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()
        GoogleSignIn.getClient(this, gso).signOut()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}

//data class converts data into object to store in firebase
data class User(
    val uname: String = "",
    val nationality: String = "",
    val course: String = ""
)