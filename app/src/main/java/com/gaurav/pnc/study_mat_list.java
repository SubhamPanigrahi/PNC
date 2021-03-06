package com.gaurav.pnc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.gaurav.pnc.Models.StudyOrAssign;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageTask;

public class study_mat_list extends AppCompatActivity {
    private String CourseName, subject, chapter;
    private DatabaseReference rootref;
    private DatabaseReference studymatListref;
    private String chapterSl;
    private ProgressDialog loadingBar;

    private FirebaseRecyclerAdapter adapter;
    private RecyclerView studymatList;
    private String title, url;
    private String filename1, fileurl;
    private StorageTask uploadTask, uploadfiletask;
    private Uri fileUri;
    private String checker = "", myfileurl = "", filename = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_mat_list);

        CourseName = getIntent().getStringExtra("cource");
        subject = getIntent().getStringExtra("sujectName");
        chapter = getIntent().getStringExtra("Chapter");
        chapterSl = getIntent().getStringExtra("code");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getSupportActionBar().setTitle("Study Materials");
        studymatList = findViewById(R.id.studymatList);

        studymatList.setLayoutManager(new LinearLayoutManager(this));

        rootref = FirebaseDatabase.getInstance().getReference();
        studymatListref = rootref.child("Cources").child(CourseName).child(subject).child("Chapters").child(chapterSl).child("study_mat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadVideos();
    }

    @Override
    protected void onStart() {
        super.onStart();
        studymatListref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    studymatList.setVisibility(View.VISIBLE);
                    TextView no = findViewById(R.id.no_study);
                    no.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadVideos() {
        loadingBar = new ProgressDialog(this);
        loadingBar.setCancelable(false);
        loadingBar.setTitle("Loading...");
        loadingBar.setMessage("Please Wait");
        loadingBar.show();
        Query query = studymatListref;

        FirebaseRecyclerOptions<StudyOrAssign> options =
                new FirebaseRecyclerOptions.Builder<StudyOrAssign>()
                        .setQuery(query, new SnapshotParser<StudyOrAssign>() {
                            @NonNull
                            @Override
                            public StudyOrAssign parseSnapshot(@NonNull DataSnapshot snapshot) {
                                loadingBar.dismiss();
                                String name = " ";
                                String url = " ";
                                int key = 0;
                                if (snapshot.hasChild("name") && snapshot.hasChild("url")) {
                                    name = snapshot.child("name").getValue().toString();
                                    url = snapshot.child("url").getValue().toString();
                                    key = Integer.parseInt(snapshot.getKey());
                                }
                                return new StudyOrAssign(name, url, key);
                            }

                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<StudyOrAssign, StudyViewHolder>(options) {
            @NonNull
            @Override
            public StudyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View viewHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_study_assign, parent, false);
                return new StudyViewHolder(viewHolder);
            }

            @Override
            protected void onBindViewHolder(@NonNull StudyViewHolder myVideoViewHolder, int i, @NonNull final StudyOrAssign study) {
                loadingBar.dismiss();
                filename1 = study.getSlno() + ": " + study.getName();
                fileurl = study.getUrl();
                myVideoViewHolder.name.setText(filename1);
                myVideoViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), Pdf.class);
                        i.putExtra("filename", study.getName());
                        i.putExtra("fileurl", study.getUrl());
                        startActivity(i);
                    }
                });
            }
        };
        loadingBar.dismiss();
        studymatList.setAdapter(adapter);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    class StudyViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public StudyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.study_assign_name);
        }
    }
}