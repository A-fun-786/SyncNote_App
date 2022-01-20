package com.example.signupnote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView profile;
//    private ImageView signOut;
    private EditText title;
    private EditText description;
    private Button saveButton;
    private Spinner spinner;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String url;
    private String uId;
    private String uName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        url = intent.getStringExtra("profileUrl");
        uId = intent.getStringExtra("uId");
        uName = intent.getStringExtra("username");
        title = findViewById(R.id.Title);
        description = findViewById(R.id.Description);
        saveButton = findViewById(R.id.saveButton);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        addProfilePic(url);
//        signOut = findViewById(R.id.signOut);
//        signOut.setOnClickListener((view)->{
//            signOut();
//        });

        saveButton.setOnClickListener((view)->{
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().
                    getReference().child(uId).push();
            Map<String, Object> map = new HashMap<>();
            map.put("id", databaseReference.getKey());
            map.put("title", title.getText().toString());
            map.put("desc", description.getText().toString());

            databaseReference.setValue(map);

        });

        fetch();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(uId);
        query.orderByChild("id");

        FirebaseRecyclerOptions<Note> options =
                new FirebaseRecyclerOptions.Builder<Note>()
                        .setQuery(query, new SnapshotParser<Note>() {
                            @NonNull
                            @Override
                            public Note parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Note(snapshot.child("id").getValue().toString(),
                                        snapshot.child("title").getValue().toString(),
                                        snapshot.child("desc").getValue().toString());
                            }
                        })
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Note, ViewHolder>(options) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().
                        inflate(R.layout.note_item,parent,false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position,
                                            @NonNull Note note) {
                View view = holder.itemView;
                TextView title = view.findViewById(R.id.textView);
                TextView description = view.findViewById(R.id.textView2);
                FloatingActionButton deleteButton = view.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener((v)->{
                    deleteNote(note);
                });
                title.setText(note.getTitle());
                description.setText(note.getDescription());
            }
        };
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
        System.out.println("Adapter setup succesful");
    }

    private void deleteNote(Note note) {
        FirebaseDatabase.getInstance().
                getReference().child(uId).child(note.getId()).removeValue();
    }

    //Adding profile picture to mainActivity
    private void addProfilePic(String url) {
        profile = findViewById(R.id.profile);
        Glide.with(MainActivity.this).load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) { return false; }
            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                           DataSource dataSource, boolean isFirstResource) { return false; }
        }).circleCrop().into(profile);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInClient googleSignInClient =
                GoogleSignIn.getClient(this,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build());
        googleSignInClient.signOut();
        Intent signInIntent = new Intent(this,Signin.class);
        startActivity(signInIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                Toast.makeText(this,"Logging out",Toast.LENGTH_LONG).show();
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}