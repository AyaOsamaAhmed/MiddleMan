package com.aya.middleman.BroadCast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.aya.middleman.R

class MyBroadCastReceiver : BroadcastReceiver() {

    var  receive_data : String = ""


    override fun onReceive(context: Context?, intent: Intent?) {


         if ( intent!!.action.equals("com.aya.emitter")) {
            val receiveData: String = intent.getStringExtra("data").toString()
            Toast.makeText(context,"Middle"+ receiveData, Toast.LENGTH_SHORT).show()

             receive_data = receiveData
             val alertDialogBuilder = AlertDialog.Builder(context!!)
             alertDialogBuilder.setTitle(R.string.received_result)
             alertDialogBuilder.setMessage(receiveData)
             alertDialogBuilder.setPositiveButton(R.string.o_k){dialogInterface, i ->
                dialogInterface.dismiss()
             }
             alertDialogBuilder.show()
        }

    }

}