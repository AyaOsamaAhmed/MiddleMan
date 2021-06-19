package com.aya.middleman

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aya.middleman.BroadCast.MyBroadCastReceiver
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    val broadCastReceiver: MyBroadCastReceiver = MyBroadCastReceiver()

    //
    lateinit var serverSocket: ServerSocket
    public lateinit var clientSocket_main: Socket
    lateinit var serverThread: Thread
    lateinit var handler: Handler

    //
    lateinit var start_connection: Button
    lateinit var send_data: Button
    lateinit var data: TextView
    val SERVER_PORT: Int = 3004

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentFilter = IntentFilter("com.aya.emitter")
        registerReceiver(broadCastReceiver, intentFilter)

        handler = Handler()

        start_connection = findViewById(R.id.connection)
        send_data = findViewById(R.id.send_data)
        data = findViewById(R.id.data)

        data.text = broadCastReceiver.receive_data

        start_connection.setOnClickListener { view ->
            // startServer()
            serverSocket = ServerSocket(SERVER_PORT)
            serverThread = Thread(ServerThread(serverSocket, this, SERVER_PORT))
            serverThread.start()
            Toast.makeText(this, "Server Connected with Client", Toast.LENGTH_SHORT).show()

            start_connection.visibility = View.GONE
            send_data.visibility = View.VISIBLE
        }

        send_data.setOnClickListener { view ->
            Toast.makeText(this, "Middle" + broadCastReceiver.receive_data, Toast.LENGTH_SHORT)
                .show()
            sendData(broadCastReceiver.receive_data)
        }

    }

    fun sendData(data: String) {
        if (clientSocket_main != null) {
            Thread(Runnable {
                kotlin.run {

                    var printWriter: PrintWriter
                    try {
                        printWriter = PrintWriter(
                            BufferedWriter(OutputStreamWriter(clientSocket_main.getOutputStream())), true)
                        printWriter.println(data)
                    } catch (e: IOException) {
                    } } }).start()
        }
    }

    fun startServer() {
        val clientProcessingPool: ExecutorService = Executors.newFixedThreadPool(10)
        val serverTask = Runnable {
            try {

                println("Waiting for clients to connect...")
                while (true) {
                    clientSocket_main = serverSocket.accept()
                    clientProcessingPool.submit(ClientTask(clientSocket_main, this))
                }
            } catch (e: IOException) {
                System.err.println("Unable to process client request")
                e.printStackTrace()
            }
        }
        serverThread = Thread(serverTask)
        serverThread.start()
        Toast.makeText(this, "Server Connected with Client", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastReceiver)
        if (serverThread != null) {
            serverThread.interrupt()
            serverThread == null
        }
    }

    class ServerThread(serverSocket: ServerSocket, context: Context, SERVER_PORT: Int) : Runnable {

        var server_socket: ServerSocket = serverSocket
        val SERVER_PORT: Int = SERVER_PORT
        val context: Context = context

        override fun run() {
            var scoket: Socket

            if (server_socket != null) {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        scoket = server_socket.accept()
                        var clientTask: ClientTask = ClientTask(scoket, context)
                        Thread(clientTask).start()
                    } catch (e: IOException) {
                        Toast.makeText(
                            context,
                            "Error Communicating to client ....",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }

    }

    class ClientTask constructor(var clientSocket: Socket, val context: Context) : Runnable {

        var input: BufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

        override fun run() {
            println("Got a client !")
            // Do whatever required to process the client's request

            while (!Thread.currentThread().isInterrupted) {
                try {
                    var read = input.readLine()
                    if (read == null || read.equals("Disconnect")) {
                        Thread.interrupted()
                        read = "Client Disconnect"
                        break
                    }
                    Toast.makeText(context, read, Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }
}