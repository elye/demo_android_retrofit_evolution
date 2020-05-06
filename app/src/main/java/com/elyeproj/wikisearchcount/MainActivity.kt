package com.elyeproj.wikisearchcount

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.btn_search_defer
import kotlinx.android.synthetic.main.activity_main.btn_search_enqueue
import kotlinx.android.synthetic.main.activity_main.btn_search_execute
import kotlinx.android.synthetic.main.activity_main.btn_search_rx
import kotlinx.android.synthetic.main.activity_main.btn_search_suspend
import kotlinx.android.synthetic.main.activity_main.edit_search
import kotlinx.android.synthetic.main.activity_main.txt_search_result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null

    private val dispatcherIoScope = CoroutineScope(Dispatchers.IO)

    private var call: Call<Model.Result>? = null

    private var myAsyncTasks: MyAsyncTask? = null

    private val wikiApiServe by lazy {
        WikiApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_search_execute.setOnClickListener {
            if (edit_search.text.toString().isNotEmpty()) {
                beginSearchExecute(edit_search.text.toString())
            }
        }

        btn_search_enqueue.setOnClickListener {
            if (edit_search.text.toString().isNotEmpty()) {
                beginSearchEnqueue(edit_search.text.toString())
            }
        }

        btn_search_rx.setOnClickListener {
            if (edit_search.text.toString().isNotEmpty()) {
                beginSearchRx(edit_search.text.toString())
            }
        }

        btn_search_defer.setOnClickListener {
            if (edit_search.text.toString().isNotEmpty()) {
                beginSearchDefer(edit_search.text.toString())
            }
        }

        btn_search_suspend.setOnClickListener {
            if (edit_search.text.toString().isNotEmpty()) {
                beginSearchSuspend(edit_search.text.toString())
            }
        }
    }

    class MyAsyncTask(
        private val doInBackground: () -> MyResult?,
        private val onPostExecuteSuccess: (result: Model.Result) -> Unit,
        private val onPostExecuteFailure: (result: String?) -> Unit
    ) : AsyncTask<String?, Void?, MyAsyncTask.MyResult>() {

        data class MyResult(val result: Model.Result? = null, val error: String? = null)

        override fun doInBackground(vararg params: String?): MyResult? {
            return doInBackground.invoke()
        }

        override fun onPostExecute(result: MyResult) {
            result.result?.let {
                onPostExecuteSuccess.invoke(it)
            } ?: onPostExecuteFailure(result.error)
        }
    }

    private fun beginSearchExecute(searchString: String) {
        myAsyncTasks = MyAsyncTask({
            call = wikiApiServe.hitCountCheckCall(
                "query", "json", "search", searchString)
            call?.let {
                try {
                    val response = it.execute()
                    if (response.isSuccessful) {
                        response.body()?.let { result ->
                            MyAsyncTask.MyResult(result = result)
                        }
                    } else {
                        MyAsyncTask.MyResult(error = "${response.code()}:${response.message()}")
                    }
                } catch (e: Exception) {
                    MyAsyncTask.MyResult(error = e.message)
                }
            } },
            { result -> try { displayResult(result) } catch (e:java.lang.Exception) { toastError(e.message)} },
            { error -> toastError(error) })

        myAsyncTasks?.execute()
    }

    private fun beginSearchEnqueue(searchString: String) {
        call = wikiApiServe.hitCountCheckCall("query", "json", "search", searchString)
        call?.enqueue(
            object : Callback<Model.Result> {
                override fun onFailure(call: Call<Model.Result>, t: Throwable) { toastError(t.message) }

                override fun onResponse(call: Call<Model.Result>, response: Response<Model.Result>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            try {
                                displayResult(it)
                            } catch (e: Exception) {
                                toastError(e.message)
                            }
                        } ?: toastError()
                    } else {
                        toastError("${response.code()}:${response.message()}")
                    }
                }
            })
    }

    private fun beginSearchSuspend(searchString: String) {
        dispatcherIoScope.launch {
            try {
                val result = wikiApiServe.hitCountCheckSuspend("query", "json", "search", searchString)
                withContext(Dispatchers.Main) {
                    displayResult(result)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    toastError(exception.message)
                }
            }
        }
    }

    private fun beginSearchDefer(searchString: String) {
        val deferFetch = wikiApiServe.hitCountCheckAsync("query", "json", "search", searchString)
        dispatcherIoScope.launch {
            try {
                val result = deferFetch.await()
                withContext(Dispatchers.Main) {
                    displayResult(result)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    toastError(exception.message)
                }
            }
        }
    }

    private fun beginSearchRx(searchString: String) {
        disposable = wikiApiServe.hitCountCheckRx("query", "json", "search", searchString)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> try { displayResult(result) } catch (e: Exception) { toastError(e.message) } },
                { error -> toastError(error.message) }
            )
    }

    private fun displayResult(result: Model.Result) {
        txt_search_result.text = "${result.query.searchinfo.totalhits} result found"
        hideKeyboard()
    }

    private fun toastError(error: String? = null) {
        Toast.makeText(this, error ?: "Unknown Error", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
        dispatcherIoScope.cancel()
        call?.cancel()
        myAsyncTasks?.cancel(true)
    }
}

fun Activity.hideKeyboard() {
    currentFocus?.let {
        val inputManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}
