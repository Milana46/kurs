package com.example.kursnewsapp.ui.fragments

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kursnewsapp.models.Article
import com.example.kursnewsapp.models.NewsResponse
import com.example.kursnewsapp.repository.NewsRepository
import com.example.kursnewsapp.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response
import java.util.Locale.IsoCountryCode

class NewsViewModel(app: Application, private val newsRepository: NewsRepository) : AndroidViewModel(app) {

    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponse: NewsResponse? = null

    var searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newsSearchQuery: String? = null
    var oldSearchQuery: String? = null


    init{
        getHeadlines("us")
    }


    fun getHeadlines(countryCode: String)=viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery:String)=viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }


    private fun handleHeadlineResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = resultResponse
                } else {
                    val oldArticles = headlinesResponse?.articles ?: mutableListOf()
                    val newArticles = resultResponse.articles
                    oldArticles.addAll(newArticles)
                    headlinesResponse?.articles = oldArticles // Обновляем список статей
                }
                return Resource.Success(headlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null || newsSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newsSearchQuery
                    searchNewsResponse = resultResponse
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles ?: mutableListOf()
                    val newArticles = resultResponse.articles
                    oldArticles.addAll(newArticles)
                    searchNewsResponse?.articles = oldArticles // Обновляем список статей
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
     //корутины
    fun addToFavorites(article: Article)=viewModelScope.launch {
        newsRepository.insertArticle(article)

    }
    fun deleteNews(article:Article)=viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
    fun getAll()=newsRepository.getAllArticle()


    fun internetConnection(context: Context):Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply{
            return getNetworkCapabilities(activeNetwork)?.run{
                when{
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                    else->false
                }
            }?: false
        }


    }

    private suspend fun headlinesInternet(countryCode:String){
        headlines.postValue(Resource.Loading())
        try{
            if(internetConnection(this.getApplication())){
                val response=newsRepository.getHeadlines(countryCode,headlinesPage)
                headlines.postValue(handleHeadlineResponse(response))
            }
            else{
                headlines.postValue(Resource.Error("No Internet connection"))
            }
        }
        catch(t:Throwable){
            when(t){
                is IOException->headlines.postValue(Resource.Error("Unable to connect"))
                else->headlines.postValue(Resource.Error("No signal"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery:String){
        newsSearchQuery=searchQuery
        searchNews.postValue(Resource.Loading())
        try{
            if(internetConnection(this.getApplication())){
                val response=newsRepository.searchForNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }
            else{
                searchNews.postValue(Resource.Error("No Internet connection"))
            }

        } catch(t:Throwable){
            when(t){
                is IOException->searchNews.postValue(Resource.Error("Unable to connect"))
                else->searchNews.postValue(Resource.Error("No signal"))
            }
        }


    }


}
