package att2.com.wrc.aas;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
* AttendanceActivity, Created by Aurghya
* This activity is responsible for adding or editing attendance
* for a specific date. Can only be accessible to teachers.
* Later on, more security and teacher flags should be added.
**/
public class AttendanceActivity extends AppCompatActivity {
    //TODO: Refactor code. A lot of issues to be checked.

    DatabaseReference studentRef;
    FirebaseRecyclerOptions<Student> options;
    RecyclerView recyclerView;

    String classDate;
    String classId;
    String classPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        studentRef = FirebaseDatabase.getInstance().getReference("Students");

        classDate = getIntent().getStringExtra("date");
        classId = getIntent().getStringExtra("class");
        classPeriod = getIntent().getStringExtra("period");

        // Init query for getting the students
        Query query = studentRef;

        // Make options for the FirebaserRecyclerAdapter
        options = new FirebaseRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class).build();

        recyclerView = findViewById(R.id.student_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Student, StudentHolder> adapter = new FirebaseRecyclerAdapter<Student, StudentHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final StudentHolder holder, int position, @NonNull final Student model) {

                final String studentIdKey = String.valueOf(getRef(position).getKey());

                holder.setName(model.getName());
                holder.setSid(studentIdKey);
                holder.setCheck(studentIdKey, classDate, classId, classPeriod);
                Query reference = FirebaseDatabase.getInstance().getReference("Attendance");

                // Use only addValueEventListener() here,
                // otherwise updated count will not properly show.
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(studentIdKey)) {
                            if (dataSnapshot.child(studentIdKey).hasChild("count")) {
                                holder.setCountView(String.valueOf((long) dataSnapshot.child(studentIdKey)
                                        .child("count").getValue()));
                            } else {
                                holder.setCountView("0");
                            }
                        } else {
                            holder.setCountView("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AttendanceActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                holder.getCheck().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final boolean isChecked = holder.getCheck().isChecked();
                        holder.updateCheck(String.valueOf(studentIdKey), classDate, classId, classPeriod, isChecked);
                    }
                });

            }

            @Override
            public StudentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.student_view, parent, false);
                return new StudentHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        // Add onStop() stop listening for the adapter
        adapter.startListening();
    }
}
