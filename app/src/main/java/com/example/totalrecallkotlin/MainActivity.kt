package com.example.totalrecallkotlin

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.totalrecallkotlin.dialogs.AcceptDialogFragment
import com.example.totalrecallkotlin.services.MyService
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main


class MainActivity : AppCompatActivity() {
    var counter : Int = 0
    var job : Job? = null

    lateinit var thread: MyThread
    var handler = object : Handler(Looper.getMainLooper())
    {
        override fun handleMessage(msg: Message){
            when (msg.what){
                0 -> showSnackbar("Integer:" + msg.obj)
                1 -> showSnackbar("String: " + msg.obj)
            }
        }
    }

    var autoHandler = AutoHandler(Looper.getMainLooper()) {
        s: String -> showSnackbar(s)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*thread = MyThread(autoHandler)
        thread.start()*/

        sendCancel.setOnClickListener() {
            showSnackbar("Coroutine was canceled")
            job?.cancel()
            myProgress.visibility = View.GONE
            sendCancel.isEnabled = false
        }

        var data : String? = intent.getStringExtra("STR_DATA")
        if (data != null)
            showSnackbar(data)
    }

    fun makeNotification(){

    }

    fun takeTime(v: View){
        var callback = object: TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
                showToast("" + p1 + ":" + p2)
            }

        }
        var dialog = TimePickerDialog(this,
            callback, 0, 0, true)
        dialog.show()
    }

    fun openSecondActivity(view: View){
        var intent = Intent(this, NavActivity::class.java)

        /*val options =
            ActivityOptions.makeCustomAnimation(this, R.anim.activity_scale, R.anim.activity_scale)*/
        startActivity(intent)
    }

    fun openBrowser(v: View){
        var uri = Uri.parse("https://www.google.com/")
        var intent = Intent(Intent.ACTION_VIEW, uri)

        // в манифест надо добавить теги <queries>
        if (intent.resolveActivity(packageManager) != null)
            startActivity(intent)
        else
            showToast("Error!")
    }

    suspend fun notifyAfterAutoSend(){
        withContext(Main){
            setProgressBar(false)
            sendCancel.isEnabled = false
            showToast("Done!")
        }
    }

    fun setProgressBar(state: Boolean){
        myProgress.visibility = if (state) View.VISIBLE else View.GONE
    }

    fun sendAutoAction(view: View){
        setProgressBar(true)
        sendCancel.isEnabled = true

        job = CoroutineScope(IO).launch()
        {
            for (i in 0..10){
                showSnackbar("Counter is $counter")
                delay(2000)
                counter++
            }

            notifyAfterAutoSend()
        }
    }

    fun startService(v: View){
        startService(Intent(this, MyService::class.java))
    }

    fun sendAction(view: View){
        val acceptDialog = AcceptDialogFragment()
        acceptDialog.show(supportFragmentManager, "acceptDialog")
    }

    fun showSnackbar(text: String){
        Snackbar.make(notif, text, Snackbar.LENGTH_SHORT)
                .show()
    }

    private suspend fun showSnackbar(text: String, buttonText: String, listener: () -> Unit){
        withContext(Main) {
            Snackbar.make(notif, text, Snackbar.LENGTH_SHORT)
                    .setAction(buttonText) { listener.invoke() }
                    .show()
        }
    }

    private fun showToast(text: String){
        Toast
                .makeText(this, text, Toast.LENGTH_SHORT)
                .show();
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("counter", counter)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        counter = savedInstanceState.getInt("counter")
    }
}