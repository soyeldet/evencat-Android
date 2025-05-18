package com.example.evencat_android.activities

import SocketClient
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.Message
import com.example.evencat_android.R

class ChatActivity : AppCompatActivity() {
    private lateinit var socketClient: SocketClient
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        enableEdgeToEdge()

        val userId = MainActivity.UserSession.id
        val chatId = intent.getIntExtra("chat_id", 0)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMessages)
        val messages = mutableListOf<Message>()

        val adapter = ChatAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val editText = findViewById<EditText>(R.id.editMessage)
        val sendButton = findViewById<ImageButton>(R.id.buttonSend)
        val backButton = findViewById<ImageButton>(R.id.back_image_button)

        backButton.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        socketClient = SocketClient(userId!!, chatId) { msg, from ->
            runOnUiThread {
                messages.add(Message(msg, from == userId))
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }

        socketClient.connect()

        sendButton.setOnClickListener {
            val content = editText.text.toString()
            if (content.isNotBlank()) {
                socketClient.sendMessage(content)
                editText.text.clear()
            }
        }
    }
}