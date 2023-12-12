package com.stockholmiot.proxyguide.ui.forum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.stockholmiot.proxyguide.R;

public class ForumChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageView sendButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_chat);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editmsg);
        sendButton = findViewById(R.id.send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }
    private void sendMessage(){
        String message = editText.getText().toString().trim();
        if(!message.isEmpty()){
            Toast.makeText(this, "Message sent: "+message, Toast.LENGTH_SHORT).show();
            editText.setText("");
        }else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }
}