package com.example.my__notes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class notesactivity extends AppCompatActivity {

    FloatingActionButton mcreatenotesfab;

    private FirebaseAuth firebaseAuth;
    RecyclerView mrecyclerview;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder>noteAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notesactivity);

        mcreatenotesfab = findViewById(R.id.createnotefab);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getSupportActionBar().setTitle("All Notes");
        mcreatenotesfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(notesactivity.this, createnote.class));

            }
        });

        Query query=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("title",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<firebasemodel>  allusernotes=new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();
        noteAdapter=new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusernotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {
//                   ImageView popupbutton=noteViewHolder.itemView.findViewById(R.id.menupopbutton);
                    int colorcode=getrandomColor();
                ImageView popupbutton=noteViewHolder.itemView.findViewById(R.id.menupopbutton);
                noteViewHolder.mnote.setBackgroundColor(noteViewHolder.itemView.getResources().getColor(colorcode,null));
                    noteViewHolder.notetitle.setText(firebasemodel.getTitle());
                    noteViewHolder.notecontent.setText(firebasemodel.getContent());
                    String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();
                    noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(view.getContext(),notedetails.class);
                            intent.putExtra("title",firebasemodel.getTitle());
                            intent.putExtra("content",firebasemodel.getContent());
                            intent.putExtra("noteId",docId);
                            view.getContext().startActivity(intent);
                        }
                    });

                    popupbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenu popupMenu=new PopupMenu(view.getContext(),view);
                            popupMenu.setGravity(Gravity.END);
                            popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {

                                    Intent intent=new Intent(view.getContext(),editnoteactivity.class);
                                    intent.putExtra("title",firebasemodel.getTitle());
                                    intent.putExtra("content",firebasemodel.getContent());
                                    intent.putExtra("noteId",docId);
                                    view.getContext().startActivity(intent);
                                    return false;
                                }
                            });
                            popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(notesactivity.this);
                                    builder.setTitle("Delete");
                                    builder.setMessage("Are you sure you want to delete?");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            DocumentReference documentReference=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(docId);
                                            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(view.getContext(),"This note is deleted",Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(view.getContext(),"Failed To Delete",Toast.LENGTH_SHORT).show();
                                                }

                                            });
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    builder.create().show();
                                    return false;
                                }
                            });
                            popupMenu.show();
                        }
                    });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };
        mrecyclerview=findViewById(R.id.recyclerview);
        mrecyclerview.setHasFixedSize(true);
        staggeredGridLayoutManager=new WrapStaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mrecyclerview.setLayoutManager(staggeredGridLayoutManager);
        mrecyclerview.setAdapter(noteAdapter);

    }
    public class NoteViewHolder extends RecyclerView.ViewHolder{
        private TextView notetitle;
        private TextView notecontent;
        LinearLayout mnote;
        ImageView popup;
        public  NoteViewHolder(@NonNull View itemView){
            super(itemView);
            notetitle=itemView.findViewById(R.id.notetitle);
            notecontent=itemView.findViewById(R.id.notecontent);
            mnote=itemView.findViewById(R.id.note);
            popup=itemView.findViewById(R.id.menupopbutton);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logout:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(notesactivity.this,MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteAdapter!=null)
        {
            noteAdapter.stopListening();
        }
    }
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
   private int getrandomColor(){
       List<Integer>colorcode=new ArrayList<>();
       colorcode.add(R.color.gray);
       colorcode.add(R.color.pink);
       colorcode.add(R.color.lightgreen);
       colorcode.add(R.color.skyblue);
       colorcode.add(R.color.color1);
       colorcode.add(R.color.color2);colorcode.add(R.color.color3);colorcode.add(R.color.color4);colorcode.add(R.color.color5);
       colorcode.add(R.color.green);
       Random random=new Random();
       int number=random.nextInt(colorcode.size());
       return colorcode.get(number);
   }


}
class WrapStaggeredGridLayoutManager extends StaggeredGridLayoutManager{


    public WrapStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public WrapStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        }catch (IndexOutOfBoundsException e){
            Log.e("TAG","error");
        }
    }
}