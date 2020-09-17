package com.example.foodapp.Fragment

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.foodapp.R
import com.example.foodapp.toast
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_update_email.*
import kotlinx.android.synthetic.main.activity_login.progressbar as progressbar1


class UpdateEmailFragment : Fragment() {
     private val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutPassword.visibility = View.VISIBLE
        layoutUpdateEmail.visibility = View.GONE

        btn_authenticate.setOnClickListener{
            val password = et_password.text.toString().trim()
             if(password.isEmpty()){
                 et_password.error = "Password required"
                 et_password.requestFocus()
                 return@setOnClickListener
             }

            currentUser?.let {user ->
                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                progressbar.visibility = View.VISIBLE
                user.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        progressbar.visibility = View.GONE
                        when {
                            task.isSuccessful -> {
                                layoutPassword.visibility = View.GONE
                                layoutUpdateEmail.visibility = View.VISIBLE
                            }
                            task.exception is FirebaseAuthInvalidCredentialsException -> {
                                et_password.error ="Invalid Password"
                                et_password.requestFocus()
                            }
                            else -> {
                                context?.toast(task.exception?.message!!)
                            }
                        }
                    }
            }
        }
        btn_update.setOnClickListener{
            val email = et_email.text.toString().trim()

            if(email.isEmpty()){
                et_email.error = "Email Required"
                et_email.requestFocus()
                return@setOnClickListener
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                et_email.error = " Valid Email Required"
                et_email.requestFocus()
                return@setOnClickListener
            }

            progressbar.visibility = View.VISIBLE
            currentUser?.let { user ->
                user.updateEmail(email)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val action = UpdateEmailFragmentDirections.actionEmailUpdated()
                            Navigation.findNavController(view).navigate(action)
                        }else{
                            context?.toast(task.exception?.message!!)
                        }

                    }

            }
        }
    }
}