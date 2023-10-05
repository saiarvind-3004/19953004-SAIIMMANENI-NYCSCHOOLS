package com.github.billman64.nycschoolssatscores

import android.util.Log
import com.github.billman64.nycschoolssatscores.Model.School
import com.github.billman64.nycschoolssatscores.Model.SchoolsAPI
import com.github.billman64.nycschoolssatscores.Model.ScoresAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val TAG = this.javaClass.simpleName

    @Test
    fun schoolApiIsSuccessful(){
        val schoolAPI = Retrofit.Builder()
            .baseUrl("https://data.cityofnewyork.us/resource/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SchoolsAPI::class.java)

        GlobalScope.launch(Dispatchers.IO){
            try{
                val response = schoolAPI.getSchools().awaitResponse()
                assert(response.isSuccessful)
            } catch(e:Exception){
                fail("Network exception " + e)
            }
        }
    }

    @Test
    fun schoolApiFields(){
        val schoolAPI = Retrofit.Builder()
            .baseUrl("https://data.cityofnewyork.us/resource/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SchoolsAPI::class.java)

        GlobalScope.launch(Dispatchers.IO){
            try{
                val response = schoolAPI.getSchools().awaitResponse()

                val data = response.body()?.asJsonArray

                data.let {

                    assert(data?.first()!!.asJsonObject!!.has("dbn") &&
                            data.first()!!.asJsonObject!!.has("school_name"))
                }
                fail()
            } catch(e:Exception){
                fail("Network exception " + e)
            }
        }
    }

    @Test
    fun satApiFields(){
        val scoresAPI = Retrofit.Builder()
            .baseUrl("https://data.cityofnewyork.us/resource/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScoresAPI::class.java)

        GlobalScope.launch(Dispatchers.IO){
            try{
                val response = scoresAPI.getScores("").awaitResponse()

                val data = response.body()?.asJsonArray

                data.let {

                    assert(data?.first()!!.asJsonObject!!.has("dbn") &&
                            data.first()!!.asJsonObject!!.has("school_name") &&
                            data.first()!!.asJsonObject!!.has("num_of_sat_test_takers") &&
                            data.first()!!.asJsonObject!!.has("sat_critical_reading_avg_score") &&
                            data.first()!!.asJsonObject!!.has("sat_math_avg_score") &&
                            data.first()!!.asJsonObject!!.has("sat_writing_avg_score")
                    )
                }
                fail()
            } catch(e:Exception){
                fail("Network exception " + e)
            }
        }
    }

    @Test
    fun satApiIsSuccessful(){
        val scoresAPI = Retrofit.Builder()
            .baseUrl("https://data.cityofnewyork.us/resource/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScoresAPI::class.java)

        GlobalScope.launch(Dispatchers.IO){
            try{
                val response = scoresAPI.getScores("").awaitResponse()
                assert(response.isSuccessful)
            } catch(e:Exception){
                fail("Network exception " + e)
            }
        }
    }

    @Test
    fun schoolDataClassHasDbn(){
        val s = School("abc", "x")
        assert(!s.dbn.isNullOrBlank())
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
