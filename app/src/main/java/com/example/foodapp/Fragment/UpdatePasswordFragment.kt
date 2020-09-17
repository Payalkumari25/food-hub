package com.example.foodapp.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.foodapp.R
import com.example.foodapp.toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.fragment_update_email.*
import kotlinx.android.synthetic.main.fragment_update_email.btn_authenticate
import kotlinx.android.synthetic.main.fragment_update_email.et_password
import kotlinx.android.synthetic.main.fragment_update_email.layoutPassword
import kotlinx.android.synthetic.main.fragment_update_email.progressbar
import kotlinx.android.synthetic.main.fragment_update_password.*
import kotlinx.android.synthetic.main.fragment_update_email.btn_update as btn_update1
//import kotlinx.android.synthetic.main.fragment_update_password.layoutUpdatePassword as layoutUpdatePassword1


class UpdatePasswordFragment : Fragment() {

    private  val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_password, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutPassword.visibility = View.VISIBLE
        layoutUpdatePassword.visibility = View.GONE

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
                                layoutUpdatePassword.visibility = View.VISIBLE
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
            val password = et_new_password.text.toString().trim()

            if(password.isEmpty() || password.length < 6){
                et_new_password.error ="Atleast 6 char password required"
                et_new_password.requestFocus()
                return@setOnClickListener
            }
            if(password != et_new_password_confirm.text.toString().trim()){
                et_new_password.error ="Password did not match"
                et_new_password_confirm.requestFocus()
                return@setOnClickListener
            }

            currentUser?.let { user ->
                progressbar.visibility = View.VISIBLE
                user.updatePassword(password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val action = UpdatePasswordFragmentDirections.actionPasswordUpdated()
                            Navigation.findNavController(it).navigate(action)
                            context?.toast("Password Updated")
                        }else{
                            context?.toast(task.exception?.message!!)
                        }

                    }

            }
        }
    }
}