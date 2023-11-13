package com.example.labbd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
//import com.github.library.bubbleview.BubbleTextView;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    View main;
    private FloatingActionButton sendButton;
    private FirebaseListAdapter<Message> adapter;
    private static final int SIGN_IN_CODE = 1;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE)
        {
            if (resultCode == RESULT_OK){
                Snackbar.make(main, "Вы авторизованы ", Snackbar.LENGTH_SHORT).show();
                displayAllMessages();
            }
            else{
                Snackbar.make(main, "Вы не авторизованы ", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main = findViewById(R.id.main);
        sendButton = findViewById(R.id.btnSend);
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        else{
            Snackbar.make(main, "Вы авторизованы ", Snackbar.LENGTH_SHORT).show();
            displayAllMessages();
        }

        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText textField = findViewById(R.id.messageField);
                if (textField.getText().toString()=="")
                    return;
                FirebaseDatabase.getInstance().getReference().push().setValue(new Message(
                        FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        textField.getText().toString()
                ));
                textField.setText("");
            };
        });
    }

    protected void displayAllMessages(){
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        Query query = FirebaseDatabase.getInstance().getReference();
        FirebaseListOptions <Message> options =
                new FirebaseListOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .setLayout(R.layout.list_item)
                        .build();
        adapter = new FirebaseListAdapter<Message>(options)
        {
            @Override
            protected void populateView(@NonNull View v, @NonNull Message model, int position) {
                TextView mess_user, mess_time;
                BubbleTextView mess_txt;
                mess_user = v.findViewById(R.id.message_user);
                mess_time = v.findViewById(R.id.message_time);
                mess_txt = v.findViewById(R.id.message_text);
                mess_user.setText(model.getUserName());
                mess_time.setText(android.text.format.DateFormat.format("dd-mm-yyyy HH:mm::ss", model.getMessageTime()));
                mess_txt.setText(model.getTextMessage());
            }
        };

        listOfMessages.setAdapter(adapter);
        adapter.startListening();

    }



}