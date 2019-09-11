package com.example.wquist.goshna

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.wquist.goshna.Api.Message
import com.example.wquist.goshna.ApiResponse.MessageResponse
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

class MessageService : Service() {

    private val notificationChannelIDConnStatus = "com.example.wquist.goshna.connection"
    private val notificationChannelIDMessages = "com.example.wquist.goshna.messages"
    private val notificationForegroundID = 1
    private var notificationCounter = notificationForegroundID + 1
    private lateinit var notificationBuilderConnStatus: NotificationCompat.Builder
    private lateinit var notificationBuilderMessage: NotificationCompat.Builder

    private var streamTask = MessageStreamTask(this)
    var reportMessage: OnMessageReceivedListener? = null
    private var messageCache = mutableListOf<MessageResponse>()

    /**
     * Copy of the Bindable functions through a Binder object.
     * This Binder instance is needed for the onBind function.
     */
    private val binder: Binder = LocalBinder(this)

    /**
     * Makes functions available to Activities bound to this Service.
     */
    inner class LocalBinder internal constructor(context: MessageService) : Binder() {
        private val serviceContext: MessageService = context

        fun setMessageListener(messageListener: OnMessageReceivedListener) {
            reportMessage = messageListener
        }

        fun getCachedMessages() : Array<MessageResponse> {
            return messageCache.toTypedArray()
        }

        /**
         * Starts listening for messages form the server at the given URL.
         * @param url The Server's URL to connect to.
         */
        fun start(context: Context, url: URL) {
            if (streamTask.status == AsyncTask.Status.RUNNING) {
                // Already running
                Toast.makeText(context, "Connected - new announcements will appear when ready", Toast.LENGTH_SHORT).show() // FIXME localise and strings.xml
                return
            } else if(streamTask.status == AsyncTask.Status.FINISHED) {
                // Re-init streamTask
                Toast.makeText(context, "Reconnecting to server", Toast.LENGTH_SHORT).show() // FIXME localise and strings.xml
                Log.d("GoshnaService", "Restarting streamTask")
                streamTask = MessageStreamTask(serviceContext)
            }
            // Start streamTask on url
            streamTask.execute(url)
        }

        /**
         * Stops the receiving messages from the server and stops the service.
         * @param clearNotifications Whether to clearAll notifications or leave them for the user
         *  to manually dismiss
         */
        fun stop(clearNotifications: Boolean = true) {
            // Clear outstanding notifications
            if (clearNotifications) {
                with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
                    cancelAll()
                }
            }

            // Stop the service (and therefore the streamTask in onDestroy)
            stopSelf()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        // Create the NotificationChannel (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Prepare generic notification channel elements
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            // Prepare the connection status channel
            val connectionChannel = NotificationChannel(
                    notificationChannelIDConnStatus,
                    "Server connection status", // TODO localise
                    importance
            )
            connectionChannel.description = "Displays the connection status of the server." // TODO localise
            // Prepare Messages channel
            val msgChannel = NotificationChannel(
                    notificationChannelIDMessages,
                    "Messages received", // TODO localise
                    importance
            )
            msgChannel.description = "Announcements received from the airport for your Gate" // TODO localise
            // Register the channels with the system; you can't change the importance
            // or other notification behaviours after this
            with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
                createNotificationChannel(connectionChannel)
                createNotificationChannel(msgChannel)
            }
        }
        // Put Service in foreground state (user should be aware this service is running)
        // TODO set action to load app and put in foreground
        notificationBuilderConnStatus = NotificationCompat.Builder(applicationContext, notificationChannelIDConnStatus)
                .setContentTitle("Pending") // TODO localise
                .setContentText("Preparing to connect to the server") // TODO localise
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO improve icon
                .setGroup(notificationChannelIDConnStatus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create Action/Intent to stop the foreground service
            // TODO add button/text to cancel the connection/service
            // TODO this class doesn't yet support handling/receiving Intents (support them or find alternative)
//            val stopIntent = Intent(this, MessageService::class.java).apply {
//                action = ACTION_CANCEL_LISTEN_FOR_MESSAGES
//            }
//            val stopPendingIntent: PendingIntent =
//                    PendingIntent.getBroadcast(this, 0, stopIntent, 0)
//            val stopAction = Notification.Action.Builder(null, "Stop", stopPendingIntent)
//            notificationBuilder.addAction(stopAction.build())
        }
        startForeground(notificationForegroundID, notificationBuilderConnStatus.build())

        // Prepare the Message received
        notificationBuilderMessage = NotificationCompat.Builder(applicationContext, notificationChannelIDMessages)
                .setSmallIcon(android.R.drawable.ic_dialog_email) // TODO use a custom icon
                .setContentTitle("New announcement") // TODO localise
    }

    override fun onDestroy() {
        // Stop the streamTask
        streamTask.cancel(true)

        // Note: leave outstanding notifications (e.g. in the event the server disconnects)
        // so user can still see them. We don't know here if Service closure was directed by user.

        super.onDestroy()
    }

    fun notificationConnecting() {
        notificationBuilderConnStatus
                .setContentTitle("Connecting...") // TODO localise
                .setContentText("Connecting to server for airport gate announcements") // TODO localise
        with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
            notify(notificationForegroundID, notificationBuilderConnStatus.build())
        }
    }

    fun notificationConnected() {
        notificationBuilderConnStatus
                .setContentTitle("Connected") // TODO localise
                .setContentText("Awaiting airport gate announcements") // TODO localise
        with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
            notify(notificationForegroundID, notificationBuilderConnStatus.build())
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class MessageStreamTask internal constructor(context: MessageService) : AsyncTask<URL, MessageResponse, Void?>() {
        private val activityReference: MessageService = context
        private val connections = ArrayList<HttpURLConnection>()

        override fun doInBackground(vararg urls: URL): Void? {
            try {
                for (url in urls) {
                    notificationConnecting()
                    Log.d("GoshnaServerMessage", "Connecting")
                    val urlConnection = url.openConnection() as HttpURLConnection
                    connections.add(urlConnection)
                    notificationConnected()
                    Log.d("GoshnaServerMessage", "Opening Buffered Input Stream")
                    readStream(BufferedInputStream(urlConnection.inputStream))
                }
            } catch (e: IOException) {
                Log.e("GoshnaServerMessage", "Error connecting to server", e)
                e.printStackTrace()
            } finally {
                Log.d("GoshnaServerMessage", "Cleaning up url connections")
                // Clean up connections - readStream is blocking (while loop), so fine to close connections here
                for (conn in connections) {
                    conn.disconnect()
                }
            }
            return null
        }

        private fun readStream(inputStream: InputStream) {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(inputStream))
                var line: String? // reader.readLine() returns null at end of stream.
                publishProgress(MessageResponse()) // Empty -- denotes successful connection, but no messages
                line = reader.readLine()
                while (line != null && !this.isCancelled) {
                    if (line.startsWith("data:")) { // Only 'data:' part of SSE is currently used
                        // Example:
                        // data: {"messages":[{"body":"Test 4","flight_id":1,"id":4,"time":1548776086}]}
                        try {
                            val json = JSONObject(line.substring(line.indexOf(':') + 2).trim { it <= ' ' })
                            val jsonMessages = json.getJSONArray("messages")
                            val newMessages = ArrayList<Message>()
                            for (i in 0 until jsonMessages.length()) {
                                val message = jsonMessages.getJSONObject(i)
                                newMessages.add(Message(
                                        message.getInt("flight_id"),
                                        message.getInt("id"),
                                        message.getString("body"),
                                        message.getInt("time")))
                            }
                            Log.d("GoshnaServerMessage", "Received: $newMessages")
                            publishProgress(MessageResponse(newMessages))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    line = reader.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                Log.d("GoshnaServerMessage", "Cleaning up InputStream")
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onProgressUpdate(vararg values: MessageResponse) {
            // Cache messages in case Activity is not active and wants to
            // get all messages once active again
            messageCache.addAll(values)
            // Send to Activity through the Listener
            activityReference.reportMessage?.onMessageReceived(values)

            // Show a notification for each message
            with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
                for (msgResponse in values) {
                    for (msg in msgResponse.messages) {
                        notificationBuilderMessage.setContentTitle("New announcement - ${msg.getTime()}") // TODO internationalise
                        notificationBuilderMessage.setContentText(msg.body)
                        notify(++notificationCounter, notificationBuilderMessage.build())
                    }
                }
            }
        }

//        override fun onPostExecute(result: Void?) {
//            super.onPostExecute(result)
//            // TODO occurs when server connection closed (e.g. by the server)
//            // TODO update the UI and notifications accordingly
//        }
    }
}