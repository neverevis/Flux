package com.socialapp.flux.tool

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocalizacaoHelper(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) {

    interface Callback {
        fun onLocalizacaoRecebida(latitude: Double, longitude: Double)
        fun onErro(mensagem: String)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun obterLocalizacaoAtual(callback: Callback) {
        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    callback.onLocalizacaoRecebida(lat, lon)
                } else {
                    callback.onErro("Localização indisponível")
                }
            }
            .addOnFailureListener {
                callback.onErro("Erro ao acessar sensor GPS")
            }
    }
}
