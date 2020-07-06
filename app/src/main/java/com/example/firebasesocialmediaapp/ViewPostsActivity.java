package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Person;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity {

    private ListView postsListView;
    private ArrayList<String> usernamesArrayList;
    private ArrayAdapter arrayAdapter;
    private FirebaseAuth firebaseAuth;

    private ImageView sendPostImageView;
    private TextView txtDescription;
    private ArrayList<DataSnapshot> dataSnapshots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        firebaseAuth=FirebaseAuth.getInstance();

        setTitle("Posts");

        postsListView=findViewById(R.id.postsListView);
        usernamesArrayList=new ArrayList<>();
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,usernamesArrayList);
        postsListView.setAdapter(arrayAdapter);

        sendPostImageView=findViewById(R.id.sendPostImageView);
        txtDescription=findViewById(R.id.txtDescription);
        dataSnapshots=new ArrayList<>();

        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DataSnapshot my_dataSnapshot=dataSnapshots.get(position);
                String downloadLink= (String) my_dataSnapshot.child("imageLink").getValue();

                Picasso.get().load(downloadLink).into(sendPostImageView);
              txtDescription.setText((String)my_dataSnapshot.child("des").getValue());
            }
        });

        postsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder;
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                    builder=new AlertDialog.Builder(ViewPostsActivity.this,android.R.style.Theme_Material_Dialog_Alert);
                }else{
                    builder=new AlertDialog.Builder(ViewPostsActivity.this);
                }
                builder.setTitle("Delete entry");
                builder.setMessage("are you sure you want to delete this entry?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseStorage.getInstance().getReference()
                                .child("my_images")
                                .child((String)dataSnapshots.get(position).child("imageIdentifier").getValue())
                                .delete();

                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users")
                                .child(firebaseAuth.getCurrentUser().getUid())
                                .child("received_posts")
                                .child(dataSnapshots.get(position).getKey()).removeValue();



                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
                builder.setCancelable(false);

                return false;
            }
        });

        FirebaseDatabase.getInstance().getReference().child("my_users")
        .child(firebaseAuth.getUid())
        .child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                dataSnapshots.add(snapshot);
                String fromWhomUsername = (String) snapshot.child("fromWhom").getValue();
                usernamesArrayList.add(fromWhomUsername);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    int i=0;
                    for(DataSnapshot snapshots : dataSnapshots ){
                         if(snapshots.getKey().equals(snapshot.getKey())){
                             dataSnapshots.remove(i);
                             usernamesArrayList.remove(i);

                         }
                        i++;
                    }
                    arrayAdapter.notifyDataSetChanged();
                      sendPostImageView.setImageResource(R.drawable.placeholder2);
                    txtDescription.setText("");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}