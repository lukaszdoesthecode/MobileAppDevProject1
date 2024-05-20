package com.example.tryingmybest

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.madness.connections.queries.auth.SuspendedQueriesAuth
import com.example.tryingmybest.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.toxicbakery.bcrypt.Bcrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

/**
 * LogIn activity gives user access to the application. It verifies the user
 * with Firebase Authentication.
 */
class LogIn : AppCompatActivity() {
    private lateinit var binding:  ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var email: EditText? = null
    private var password: EditText? = null

    /**
     * Creates the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signUp : TextView = findViewById(R.id.SignUpTV)

        signUp.setOnClickListener{goToSignUp()}

        binding.button.setOnClickListener{
            email = binding.emailEV
            password = binding.passwordEV

            if (validate())register()

        }
    }

    /**
     * Validates user input.
     */
    private fun validate(): Boolean {

        if (email?.text.toString().isEmpty() || password?.text.toString().isEmpty()) {
            Toast.makeText(this, "please fill out all your information", Toast.LENGTH_SHORT).show()
            return false
        }
        return true}


    /**
     * Registers the user with Firebase Authentication.
     */
    private fun register(){
        firebaseAuth.signInWithEmailAndPassword(email?.text.toString(), password?.text.toString()).addOnCompleteListener {
            var userId = 0
            lifecycleScope.launch {
                userId = SuspendedQueriesAuth.getAuth(email?.text.toString(), password?.text.toString())
            }

            if(it.isSuccessful){
                lifecycleScope.launch {
                    var role = ""

                    if (userId != -1) {
                        lifecycleScope.launch {
                            role = SuspendedQueriesAuth.getRole(email?.text.toString())
                            Toast.makeText(this@LogIn, "role: $role", Toast.LENGTH_SHORT).show()
                            lifecycleScope.launch {
                                if (role.uppercase() == "ADMIN") {
                                    goToAdmin()
                                    Toast.makeText(
                                        this@LogIn,
                                        "log in succeeded as admin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LogIn,
                                        "log in succeeded as user",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    goToMain()
                                }
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(this, "log in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Changes activity to the Sign Up activity.
     */
    private fun goToSignUp() {
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Changes activity to the Home activity.
     */
    private fun goToMain() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Changes activity to the Admin activity.
     */
    private fun goToAdmin() {
        val intent = Intent(this, Vaccines::class.java)
        startActivity(intent)
        finish()
    }

}

