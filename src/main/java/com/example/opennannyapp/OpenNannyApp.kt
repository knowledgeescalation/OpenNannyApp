package com.example.opennannyapp

import android.app.Application
import com.example.opennannyapp.api.ApiService
import com.example.opennannyapp.api.NetworkModule

class OpenNannyApp : Application() {

    lateinit var networkModule: NetworkModule
    lateinit var serviceApi: ApiService
    lateinit var api_ip: String

    override fun onCreate() {
        super.onCreate()

        api_ip = getString(R.string.api_ip)
        val api_user = getString(R.string.api_user)
        val api_pass = getString(R.string.api_pass)

        networkModule = NetworkModule(api_ip=api_ip, api_user=api_user, api_pass=api_pass)
        serviceApi = networkModule.serviceAPI


    }

}