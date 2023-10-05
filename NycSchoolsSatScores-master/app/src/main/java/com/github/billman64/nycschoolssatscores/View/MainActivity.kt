package com.github.billman64.nycschoolssatscores.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.billman64.nycschoolssatscores.Model.School
import com.github.billman64.nycschoolssatscores.Model.SchoolAdapter
import com.github.billman64.nycschoolssatscores.Model.SchoolsAPI
import com.github.billman64.nycschoolssatscores.R
import com.github.billman64.nycschoolssatscores.databinding.ActivityMainBinding
//import kotlinx.android.synthetic.main.activity_main.* // synthetics deprecated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

/* NYC SAT Scores API call demo
**  @author: Bill Lugo
**   Copyrights:
**   Background photo by Devon Delrio is licensed under Creative Commons CC0 (Public Domain). Source: https://pixy.org/107734/
 */

class MainActivity : AppCompatActivity() {
    val TAG:String = "SAT data demo" + this.javaClass.simpleName
    private var schoolList = ArrayList<School>()
    private lateinit var binding:ActivityMainBinding   // will be used to replace findViewById's


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)        // replaces: setContentView(R.layout.activity_main)

        val mainLayout = binding.mainLayout     // replaces: val mainLayout = findViewById<View>(R.id.mainLayout)
        mainLayout.setBackgroundResource(R.mipmap.definition)

        // Log whether or not retrieving data from a prior activity instance.
        savedInstanceState?.let{Log.d(TAG, "onCreate() with a savedInstanceState")} ?: Log.d(TAG, "onCreate()")

        // set up recyclerView, which is used to hold list of tappable school names
            val rv:RecyclerView = binding.recyclerView      // replaces: val rv:RecyclerView = findViewById(R.id.recyclerView)


        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = SchoolAdapter(ArrayList())

        // button action
        val refreshButton: Button = binding.refreshButton        // replaces: val refreshButton: Button = findViewById<Button>(R.id.refreshButton)

        refreshButton.setOnClickListener{   //TODO: new feature - implement search for individual school (closest match and wildcards to filter or pre-filter the data)
            getSchoolData()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save data for orientation change (TODO: alternatively use a fragment to hold recyclerView)
        outState.putParcelableArrayList("list", schoolList)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val restoreList:ArrayList<School>? = savedInstanceState.getParcelableArrayList("list")

        // If there is data to restore from, then populate the recyclerView with it.
        restoreList?.let{
            if(restoreList.count()>0) {
                schoolList = restoreList
                val rv: RecyclerView = binding.recyclerView      // replaces: val rv: RecyclerView = findViewById(R.id.recyclerView)

                rv.layoutManager = LinearLayoutManager(this)
                rv.adapter = SchoolAdapter(restoreList)

                val refreshButton = binding.refreshButton     // replaces: val refreshButton = findViewById<View>(R.id.refreshButton)

                refreshButton.visibility = View.GONE
            }
        }
    }


    fun getSchoolData(){

        // Progress bar displays
        val progressBar = binding.progressBar     // replaces: val progressBar = findViewById<View>(R.id.progress_bar)

        progressBar.visibility = View.VISIBLE
        progressBar.isShown

        //TODO: Refactor to a ViewModel that's observed using a DataBinding or LiveData object

        // Retrofit builder
        val schoolApi = Retrofit.Builder()
            .baseUrl("https://data.cityofnewyork.us/resource/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SchoolsAPI::class.java)
        Log.d(TAG, "Retrofit for schools api created: $schoolApi.toString()")

        // Coroutine for API call. Dispatcher used to prevent leak from directly running GlobalScope.
        GlobalScope.launch(Dispatchers.IO){         //TODO: Best practice: Make scope referenceable for more testing coverage
            Log.d(TAG, "Coroutine - calling API for SAT scores.")
            val refreshButton = binding.refreshButton      // replaces: val refreshButton = findViewById<View>(R.id.refreshButton)

            try{
                val responseSchools = schoolApi.getSchools().awaitResponse()
                Log.d(TAG, " response received. code: ${responseSchools.code()} size: ${responseSchools.message()}")

                if(responseSchools.isSuccessful){

                    Log.d(TAG, " response is successful! code+msg: " + responseSchools.code() +" "+ responseSchools.message())
                    Log.d(TAG, " response is successful! body: " + responseSchools.body().toString().substring(0,100))
                    val data = responseSchools.body()!!.getAsJsonArray()
                    Log.d(TAG, " data response successful! Length of data: " + data.toString().substring(0,100))

                    // add each school object to list before updating recyclerView
                    for(i in 0 until data.size()){
                        val s = School(
                            data[i].asJsonObject.get("dbn").toString(),
                            data[i].asJsonObject.get("school_name").toString().replace("Â","") //replace("^\"|\"$","")
                        )

                        // Truncate quote marks at the start and end of school name
                        val temp:String = s.schoolName

                        if(temp.substring(0,1).equals("\"") and temp.substring(temp.length-1,temp.length).equals("\"")){
                            s.schoolName = temp.substring(1,temp.length-1)
                            if(s.schoolName.substring(1,2) == "Ac") Log.d(TAG, " !!! school name: ${s.schoolName}")
                        }

                        schoolList.add(s)
                    }
                    Log.d(TAG, " school list count: ${schoolList.count()}")

                    // Update views
                    withContext(Dispatchers.Main){
                        progressBar.visibility = View.GONE
                        Toast.makeText(applicationContext,"schools found: ${schoolList.count()}", Toast.LENGTH_SHORT).show()
                        // sort
                        schoolList.sortBy { it.schoolName }

                        val recyclerView = binding.recyclerView        // replaces: val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

                        recyclerView.adapter = SchoolAdapter(schoolList)

                        refreshButton.visibility = View.GONE
                    }

                } else {

                    // handling for connected but not successful
                    if(responseSchools.errorBody()?.contentLength()?:0 >= 70) {
                        Log.d(TAG," Not successful. ErrorBody(): ${responseSchools.errorBody().toString().substring(0, 70)}")
                    }
                    else {
                        Log.d(TAG, " Not successful. ErrorBody(): ${responseSchools.errorBody().toString()}")
                    }

                    // response to http status code 403 (forbidden)
                    if(responseSchools.code() == 403) {
                        Log.d(TAG, " Possibly a bad or missing api key!")
                    }

                    // update UI
                    withContext(Dispatchers.Main){
                        progressBar.visibility = View.GONE
                        refreshButton.visibility = View.VISIBLE
                    }
                }

            } catch(e:Exception){   // connection error handling
                if(e.toString().length>=50) {
                    Log.d(TAG, "Net error: ${e.toString().substring(0,50)}")
                } else {
                    Log.d(TAG, "Net error: $e")
                }
                Log.d(TAG, "message: ${e.message}")

                // update UI
                withContext(Dispatchers.Main){
                    progressBar.visibility = View.GONE
                    refreshButton.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, getString(R.string.connection_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
