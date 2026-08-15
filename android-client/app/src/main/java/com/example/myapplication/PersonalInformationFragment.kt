// PersonalInformationFragment.kt
package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.api.AuthService
import com.example.myapplication.model.User
import com.example.myapplication.model.response.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class PersonalInformationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // 这里加载PersonalInformationFragment的布局
        return inflater.inflate(R.layout.activity_personal_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)

        // Access the application fom the fragment's context
        val myApplication = requireContext().applicationContext as MyApplication
        val authService = myApplication.authService

        // Get the AuthService instance that has the AuthInterceptor applied
        val protectedAuthService =
            myApplication.createProtectedApiService<AuthService>(AuthService::class.java)
        val TAG = "PersonalInformationActivity"
        val logoutButton: Button = view.findViewById<Button>(R.id.btn_logout)

        // 2. Add a click listener to the button
        logoutButton.setOnClickListener {
            authService.logout().enqueue( object :  Callback<ApiResponse<Number>> {
                override fun onResponse(
                    call: Call<ApiResponse<Number>> ,
                    response: Response<ApiResponse<Number>>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Logout successful: ${response.body()}")
                        myApplication.tokenManager.clearTokens()
                        val intent = Intent(
                            this@PersonalInformationFragment.context,
                            LoginActivity::class.java
                        )
                        startActivity(intent)
                    } else {
                        var errorMessage = "Logout call: ${response.code()}"
                        response.errorBody()?.let {
                            try {
                                errorMessage += " - ${it.string()}"
                            } catch (e: IOException) {
                                // ignore
                            }
                        }
                        Log.e(TAG, "Logout failed: $errorMessage")
                        // If 401, AuthInterceptor should have handled refresh/re-login.
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Number>> , t: Throwable) {
                    Log.e(TAG, "Logout failure", t)
                }
            })
        }
        protectedAuthService.getUserProfile().enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                if (response.isSuccessful) {
                    var phoneNumber: String = response.body()?.data?.phoneNumber ?: ""
                    if (phoneNumber != ""){
                        view.findViewById<TextView>(R.id.infor_phone).text = maskPhoneNumber(phoneNumber)
                    }
                    Log.d(TAG, "PersonalInformationActivity call successful: ${response.body()}")
                } else {
                    var errorMessage = "PersonalInformationActivity call: ${response.code()}"
                    response.errorBody()?.let {
                        try {
                            errorMessage += " - ${it.string()}"
                        } catch (e: IOException) {
                            // ignore
                        }
                    }
                    Log.e(TAG, "PersonalInformationActivity call failed: $errorMessage")
                    // If 401, AuthInterceptor should have handled refresh/re-login.
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                Log.e(TAG, "PersonalInformationActivity call failure", t)
            }
        })
    }
    fun maskPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length < 7) { // A very short number, might not have 4 middle digits to mask
            return phoneNumber // Or handle as an error, depending on requirements
        }

        val prefix = phoneNumber.substring(0, 3) // First three digits
        val suffix = phoneNumber.substring(phoneNumber.length - 4) // Last four digits

        return "$prefix****$suffix"
    }
}