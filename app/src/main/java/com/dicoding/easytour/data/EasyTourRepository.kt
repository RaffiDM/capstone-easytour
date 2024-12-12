package com.dicoding.easytour.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.easytour.data.pref.UserModel
import com.dicoding.easytour.data.pref.UserPreference
import com.dicoding.easytour.entity.HomeEntity
import com.dicoding.easytour.entity.UserEntity
import com.dicoding.easytour.request.FilterRequest
import com.dicoding.easytour.response.CategoriesResponse
import com.dicoding.easytour.response.ErrorResponse
import com.dicoding.easytour.response.LoginResponse
import com.dicoding.easytour.response.RegisterResponse
import com.dicoding.easytour.retrofit.ApiService
import com.dicoding.easytour.retrofit.CategoryRequest
import com.dicoding.easytour.retrofit.IdRequest
import com.dicoding.easytour.retrofit.LoginRequest
import com.dicoding.easytour.retrofit.PredictRequest
import com.dicoding.easytour.retrofit.RegisterRequest
import com.dicoding.easytour.room.PlaceDao
import com.dicoding.easytour.room.UserDao
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class EasyTourRepository(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    val placeDao: PlaceDao,
    val userDao: UserDao
) {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): ResultState<RegisterResponse> {
        try {
            val registerRequest = RegisterRequest(name, email, password)
            val response = apiService.register(registerRequest) // API call
            if (response.isSuccessful.not()) {
//                Log.d("kjdfsjhdjk", "register1: ${response.errorBody()?.string()}")
//
//                val errorString = response.errorBody()?.string()
//                Log.d("kjdfsjhdjk", "register2: ${errorString}")

                val gson = Gson()
                val errorResponse = gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)

                Log.d("kjdfsjhdjk", "register: ${errorResponse.message}")
                Log.d("kjdfsjhdjk", "register: ${errorResponse}")
                return ResultState.Error(errorResponse.message ?: "Registration failed")
            } else {
                // Simpan data pengguna ke database lokal
                val dataSuccess = response.body()
                dataSuccess?.let {
                    val userEntity = UserEntity(
                        id = it.userID.toString(),
                        username = name,
                        email = email
                    )
                    saveSession(UserModel(email = email, userId = it.userID.toString(), isLogin = true, token = "", username = name ))
                    userDao.insertUser(userEntity)
                    Log.d("hai", "Response: $response" )
                    return ResultState.Success(it)
                }
                return ResultState.Error("An error occurred during registration")

            }
        } catch (e: Exception) {
            Log.d("kjdfsjhdjk", "register error: ${e}")
            return ResultState.Error(e.localizedMessage ?: "An error occurred during registration")
        }
    }

    suspend fun login(email: String, password: String): ResultState<LoginResponse> {
        try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest) // API call

            if (response.isSuccessful.not()) {
                val gson = Gson()
                val errorResponse = gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)

                Log.d("login_debug", "login error: ${errorResponse.message}")
                Log.d("login_debug", "errorResponse: ${errorResponse}")
                return ResultState.Error(errorResponse.message ?: "Login failed")
            } else {
                val dataSuccess = response.body()
                dataSuccess?.let {
                    val userEntity = UserEntity(
                        id = it.userID.toString(),
                        username = it.username ?: "",
                        email = email
                    )

                    userDao.insertUser(userEntity)
                    Log.d("login_debug", "Response: $response")
                    return ResultState.Success(it)
                }

                return ResultState.Error("An error occurred during login")
            }
        } catch (e: Exception) {
            Log.d("login_debug", "login error: ${e}")
            return ResultState.Error(e.localizedMessage ?: "An error occurred during login")
        }
    }




    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }
    suspend fun getCategories(userId: String?): CategoriesResponse {
        try {
            val categoryRequest = CategoryRequest(
                userID = userId.toString(),
                TamanHiburan = false,
                Budaya = false,
                Bahari = false,
                CagarAlam = false,
                PusatPerbelanjaan = false,
                TempatIbadah = false
            )
            val response = apiService.getCategories(categoryRequest)
            Log.d("EasyTourRepository", "Categories fetched: $response")

            return response
        } catch (e: Exception) {
            Log.e("EasyTourRepository", "Error fetching categories: ${e.localizedMessage}")

            throw e
        }
    }


    suspend fun sendCategoryPreferences(categoryRequest: CategoryRequest): CategoriesResponse {
        try {
            val response = apiService.getCategories(categoryRequest)
            Log.d("EasyTourRepository", "Categories sent: $response")
            return response
        } catch (e: Exception) {
            Log.e("EasyTourRepository", "Error sending categories: ${e.localizedMessage}")
            throw e
        }
    }

    fun getHome(id: String): LiveData<ResultState<List<HomeEntity>>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.getHome(IdRequest(id))
            if (response.code == 200 && response.data != null) {
                val homeEntities = response.data.mapNotNull { dataItem ->
                    dataItem?.let {
                        val isFavorited = placeDao.isFavorited(it.name ?: "")
                        Log.d("repo", "getHome called : {$isFavorited}")
                        Log.d("repo", "getHome placedao: ${placeDao.isFavorited(it.name.orEmpty())}, name: ${it.name}")
                        HomeEntity(
                            iD = it.iD ?: "",  // Ganti ke String
                            name = it.name ?: "Unknown",
                            description = it.description ?: "No description",
                            city = it.city ?: "Unknown city",
                            imageUrl = it.imageUrl,
                            price = it.price ?: "Unknown price",
                            category = it.category ?: "Unknown category",
                            categoryMatch = it.categoryMatch ?: false,
                            explanation = it.explanation ?: "No explanation",
                            rating = it.rating ?: 0.0f,
                            isFavorite = isFavorited
                        )
                    }
                }
                val result = placeDao.insert(homeEntities)
                Log.d("repo2", "getHome called: $result")// Simpan ke Room
                emit(ResultState.Success(homeEntities)) // Emit data berhasil
            } else {
                emit(ResultState.Error("Failed to fetch home data: ${response.code}"))
            }
        } catch (e: Exception) {
            emit(ResultState.Error("Error fetching home data: ${e.localizedMessage}"))
        }
    }

//    suspend fun getHomeFromDatabase(): LiveData<ResultState<List<HomeEntity>>> = liveData {
//        emit(ResultState.Loading)
//        try {
//            val cachedData = placeDao.getFavoritedPlaces()
//            emit(ResultState.Success(cachedData))
//        } catch (e: Exception) {
//            emit(ResultState.Error("Error fetching home data from database: ${e.localizedMessage}"))
//        }
//    }

    suspend fun searchAndSaveResults(query: String): List<HomeEntity> {
        val response = apiService.predict(PredictRequest(place_name = query))

        if (response.code != 200 || response.data?.recommendations.isNullOrEmpty()) {
            throw Exception("Failed to fetch recommendations.")
        }

        // Process the data into HomeEntity objects
        val recommendations = response.data?.recommendations?.mapNotNull { item ->
            item?.let {
                HomeEntity(
                    iD = it.id?.toString() ?: "Unknown ID",
                    name = it.name ?: "Unknown Name",
                    description = it.description ?: "No Description",
                    city = it.city ?: "Unknown City",
                    imageUrl = it.imageUrl ?: "",
                    price = it.price ?: "Unknown Price",
                    category = it.category ?: "Unknown Category",
                    categoryMatch = false,
                    explanation = it.explanation ?: "No Explanation",
                    rating = it.rating?.toString()?.toFloatOrNull() ?: 0.0f,
                    isFavorite = 0
                )
            }
        } ?: emptyList()

        // Save the results to the Room database
        val result= placeDao.insert(recommendations)
        Log.d("repo2", "searchandsave called $result")
        // Return the list of HomeEntity
        return recommendations
    }

    suspend fun filter(filterRequest: FilterRequest): List<HomeEntity> {
        val response = apiService.filter(filterRequest)
        if (response.code != 200 || response.data.isNullOrEmpty()) {
            throw Exception("Failed to fetch recommendations.")
        }
        val recommendations = response.data.map { item ->
            item.let {
                HomeEntity(
                    iD = it.id.toString(),
                    name = it.name,
                    description = it.description,
                    city = it.city,
                    imageUrl = it.imageUrl,
                    price = it.price,
                    category = it.category,
                    categoryMatch = false,
                    explanation = it.explanation,
                    rating = it.rating.toString().toFloatOrNull() ?: 0.0f,
                    isFavorite = 0
                )
            }
        } ?: emptyList()

        return recommendations.also{
            placeDao.insert(it)
        }
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }
    suspend fun saveFavorite(homeEntity: HomeEntity) {
        //Log.d("EasyTourRepository", "Saving: ${homeEntity.name}, isFavorite: ${homeEntity.isFavorite}")
        placeDao.updateFavorite(homeEntity)
    }
    fun getFavoritePlaces(): LiveData<List<HomeEntity>> {
        return placeDao.getFavoritedPlaces()
    }

    suspend fun updateFavoriteStatus(name: String, isFavorite: Int) {
        Log.d("repo2", "updatefavoritestatus: {$isFavorite}")
        Log.d("repo2", "updatefavoritestatus: {name: $name}")
        val flash = placeDao.getFlashbyName(name)
        placeDao.updateFavoriteStatus(name, isFavorite).also{
            Log.d("repo2", "update result: $it")
            Log.d("repo2", "flash: $flash")
        }
    }
    suspend fun isFavorited(name: String): Boolean { // Ubah parameter ke 'iD' jika konsisten
        return placeDao.isFavorited(name) == 1
    }

    suspend fun removeFavorite(homeEntity: HomeEntity) {
        placeDao.delete(homeEntity)
    }

    companion object {
        @Volatile
        private var instance: EasyTourRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            placeDao: PlaceDao,
            userDao: UserDao
        ): EasyTourRepository =
            instance ?: synchronized(this) {
                instance ?: EasyTourRepository(userPreference, apiService, placeDao, userDao)
            }.also { instance = it }
    }
}
