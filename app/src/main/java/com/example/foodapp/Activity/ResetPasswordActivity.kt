package com.example.foodapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.example.foodapp.R
import com.example.foodapp.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.activity_login.progressbar as progressbar1

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        btn_reset_password.setOnClickListener{
            val email = txt_email.text.toString().trim()

            if(email.isEmpty()){
                et_login_email.error = "Email Required"
                et_login_email.requestFocus()
                return@setOnClickListener
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                et_login_email.error = " Valid Email Required"
                et_login_email.requestFocus()
                return@setOnClickListener
            }

            progressbar.visibility = View.VISIBLE

            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnCompleteListener {task ->
                    progressbar.visibility = View.GONE
                    if(task.isSuccessful){
                        this.toast("Check your email")
                    }else{
                        this.toast(task.exception?.message!!)
                    }

                }
        }
    }

}