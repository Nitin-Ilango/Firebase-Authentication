package com.nitin.firebasebasics

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.nitin.firebasebasics.databinding.PhoneLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: PhoneLoginBinding

    private var mVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PhoneLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                resendToken = token
            }
        }

        binding.btnOtp.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if( phoneNumber.isNotEmpty())
                startPhoneNumberVerification(phoneNumber)
        }
        binding.btnResendOtp.setOnClickListener{
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if( phoneNumber.isNotEmpty())
                resendVerificationCode(phoneNumber, resendToken)
        }
        binding.btnOtpEnter.setOnClickListener{
            val code = binding.etOtp.text.toString().trim()
            if( code.isNotEmpty())
                verifyPhoneNumberWithCode(mVerificationId,code)
        }
        binding.tvGoToLogin.setOnClickListener {
            gotoLoginActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null) {
            gotoBioDataActivity()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG,"start verification")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
}

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        Log.d(TAG,"resend verification code")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        Log.d(TAG,"verification code")
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)

        // [END verify_with_code]
    }


    private fun signInWithPhoneAuthCredential( credential: PhoneAuthCredential) {
        CoroutineScope(Dispatchers.IO).launch {
            try{
                auth.signInWithCredential(credential).await()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@SignupActivity, "Login Successful", Toast.LENGTH_LONG).show()
                    gotoBioDataActivity()
                }
            } catch( e: Exception){
                withContext(Dispatchers.Main){
                    Log.d(TAG,"$e")
                    Toast.makeText(this@SignupActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }





    private fun gotoLoginActivity() {
        auth.signOut()
        finish()
    }

    //login success activity
    private fun gotoBioDataActivity() {
        startActivity( Intent( this, BioDataActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
    }


}







    //coroutine sign up
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            auth.createUserWithEmailAndPassword(newUserSignupEmail, newUserSignupPassword)
//                .await()
//            withContext(Dispatchers.Main) {
//                if (auth.currentUser != null) {
//                    Toast.makeText(this@SignupActivity, "Signup successful", Toast.LENGTH_SHORT)
//                        .show()
//                    gotoLoginActivity()
//                }
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(
//                    this@SignupActivity,
//                    "Signup failed : " + e.message,
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }