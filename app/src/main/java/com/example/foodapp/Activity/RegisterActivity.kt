package com.example.foodapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.example.foodapp.R
import com.example.foodapp.login
import com.example.foodapp.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.progressbar as progressbar1

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        btn_Register.setOnClickListener {
            val userName = et_Register_Name.text.toString().trim()
            val email = et_Register_Email.text.toString().trim()
            val phoneNumber = et_register_mobile_number.text.toString().trim()
            val userAddress = et_Register_Address.text.toString().trim()
            val password = et_Register_Password.text.toString().trim()
            val confirmPassword = et_Register_confirm_password.text.toString().trim()

            if(email.isEmpty()){
                et_Register_Email.error = "Email Required"
                et_Register_Email.requestFocus()
                //stop the further execution
                return@setOnClickListener
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                et_Register_Email.error = " Valid Email Required"
                et_Register_Email.requestFocus()
                return@setOnClickListener
            }

            if(phoneNumber.isEmpty()){
                et_register_mobile_number.error = "Phone No. Required"
                et_register_mobile_number.requestFocus()
                return@setOnClickListener
            }

            if(!Patterns.PHONE.matcher(phoneNumber).matches()){
                et_register_mobile_number.error = " Valid Phone No. Required"
                et_register_mobile_number.requestFocus()
                return@setOnClickListener
            }

            if(password.isEmpty() || password.length < 6){
                et_Register_Password.error = " 6 char password required"
                et_Register_Password.requestFocus()
                return@setOnClickListener
            }

            if(confirmPassword.isEmpty() || confirmPassword != password ){
                et_Register_confirm_password.error = " Password must be same"
                et_Register_confirm_password.requestFocus()
                return@setOnClickListener
            }

            registerUser(email,password)

        }

        txt_login.setOnClickListener{
            startActivity(Intent(this@RegisterActivity,
                LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, password: String) {
        progressbar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task ->
                progressbar.visibility = View.GONE
                if(task.isSuccessful){
                    //Registration Success
                   login()
                }else{
                    task.exception?.message?.let {
                        toast(it)
                    }
                }

            }
    }

    override fun onStart(){
        super.onStart()
        mAuth.currentUser?.let {
            login()
        }
    }
}