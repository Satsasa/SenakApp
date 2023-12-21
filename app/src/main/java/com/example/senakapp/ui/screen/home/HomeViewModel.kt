package com.example.senakapp.ui.screen.home

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senakapp.data.repository.HomeRepository
import com.example.senakapp.data.retrofit.BiodataService
import com.example.senakapp.data.retrofit.HomeService
import com.example.senakapp.model.FoodRecommendationsResponse
import com.example.senakapp.model.biodata.VerifyChildResponse
import com.example.senakapp.utils.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val sharedPreferences: SharedPreferences,private val biodataService: BiodataService, private val homeService: HomeService, private val homeRepository: HomeRepository): ViewModel(){

companion object
{
    const val PAGE_SIZE = 7
}

    fun getToken(): String? {
        return sharedPreferences.getString("homeToken", null)
    }

    fun saveTokenAsync(token: String) {
        sharedPreferences.edit().putString("homeToken", token).apply()
    }

    private val _foodRecommendations = MutableStateFlow<ApiResponse<FoodRecommendationsResponse>>(ApiResponse.Empty)
    val foodRecommendations: StateFlow<ApiResponse<FoodRecommendationsResponse>> get() = _foodRecommendations

    private val _verifyChildResult =
        MutableStateFlow<ApiResponse<VerifyChildResponse>>(ApiResponse.Empty)
    val verifyChildResult: StateFlow<ApiResponse<VerifyChildResponse>> get() = _verifyChildResult



    fun getFoodRecommendations(userId: String, pagination: Int) {
        viewModelScope.launch {
            _foodRecommendations.value = ApiResponse.Loading
            try {
                val result = homeRepository.getFoodRecommendations(userId, pagination ?: PAGE_SIZE)
                _foodRecommendations.value = result
            } catch (e: Exception) {
                _foodRecommendations.value = ApiResponse.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun verifyChild(userId: String) {
        viewModelScope.launch {
            try {
                _verifyChildResult.value = ApiResponse.Loading

                // Panggil fungsi getBioChild dari BiodataService
                val response = biodataService.getBioChild(userId)
                val name = response.body()?.data?.name

                // Periksa apakah respons berhasil atau tidak
                if (response.isSuccessful) {
                    // Respons berhasil, simpan respons ke StateFlow
                    _verifyChildResult.value = ApiResponse.Success(response.body()!!)
                    Log.d("BiodataViewModel", "verifyChild: Success")
                    // Lakukan tindakan sukses jika diperlukan
                } else {
                    // Respons tidak berhasil, simpan pesan kesalahan ke StateFlow
                    _verifyChildResult.value =
                        ApiResponse.Error("Gagal verifikasi anak: ${response.message()}")
                }
            } catch (e: Exception) {
                // Tangani kesalahan yang mungkin terjadi selama proses
                // Simpan pesan kesalahan ke StateFlow
                _verifyChildResult.value =
                    ApiResponse.Error("Terjadi kesalahan verifikasi anak: ${e.message}")
            }
        }
    }



    fun getIdUser(): String? {
        return sharedPreferences.getString("idUser", null)
    }




}