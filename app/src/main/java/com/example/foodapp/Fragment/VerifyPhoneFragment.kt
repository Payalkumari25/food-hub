package com.example.foodapp.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.foodapp.R
import com.example.foodapp.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_verify_phone.*
import java.util.concurrent.TimeUnit


class VerifyPhoneFragment : Fragment() {

    private var verificationId : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutPhone.visibility = View.VISIBLE
        layoutVerification.visibility = View.GONE

        btn_send_verification.setOnClickListener {

            val phone = et_phone.text.toString().trim()
            if (phone.isEmpty() || phone.length != 10) {
                et_phone.error = "Enter a valid phone"
                et_phone.requestFocus()
                return@setOnClickListener
            }

            val phoneNumber = '+' + ccp.selectedCountryCode + phone

            PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    phoneAuthCallbacks
                )

            layoutPhone.visibility = View.GONE
            layoutVerification.visibility = View.VISIBLE
        }

        btn_verify.setOnClickListener {
            val code = et_code.text.toString().trim()
            if (code.isEmpty()) {
                et_code.error = "Code required"
                et_code.requestFocus()
                return@setOnClickListener
            }

            verificationId?.let {
                val credential = PhoneAuthProvider.getCredential(it, code)
                addPhoneNumber(credential)
            }
        }
    }

    private val phoneAuthCallbacks = object :
        PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            phoneAuthCredential?.let {
                addPhoneNumber(it)
            }
        }
        override fun onVerificationFailed(exception: FirebaseException) {
            context?.toast(exception?.message!!)
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            this@VerifyPhoneFragment.verificationId = verificationId

        }
    }

    private fun addPhoneNumber(phoneAuthCredential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
            .currentUser?.updatePhoneNumber(phoneAuthCredential)
            ?.addOnCompleteListener {task ->
                if (task.isSuccessful){
                    context?.toast("Phone Added")
                    val action = VerifyPhoneFragmentDirections.actionPhoneVerified()
                    Navigation.findNavController(btn_verify).navigate(action)
                }else{
                    context?.toast(task.exception?.message!!)
                }
            }
    }
}




