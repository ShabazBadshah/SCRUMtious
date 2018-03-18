package ca.mvp.scrumtious.scrumtious.presenter_impl;

import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import ca.mvp.scrumtious.scrumtious.R;
import ca.mvp.scrumtious.scrumtious.interfaces.presenter_int.TaskBoardPresenterInt;
import ca.mvp.scrumtious.scrumtious.view_impl.TaskBoardFragment;

public class TaskBoardPresenter implements TaskBoardPresenterInt {

    private TaskBoardFragment taskBoardView;
    private String pid, usid;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private Query mQuery;

    public TaskBoardPresenter(TaskBoardFragment taskBoardView, String pid, String usid){
        this.taskBoardView = taskBoardView;
        this.pid = pid;
        this.usid = usid;
    }

    @Override
    public FirebaseRecyclerAdapter<ca.mvp.scrumtious.scrumtious.model.Task, TaskBoardFragment.TaskBoardViewHolder> setupTaskBoardAdapter(String type) {
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        mQuery = mRef.child("projects").child(pid).child("user_stories").child(usid).child("tasks").orderByChild("status")
                .equalTo(type);

        FirebaseRecyclerAdapter<ca.mvp.scrumtious.scrumtious.model.Task, TaskBoardFragment.TaskBoardViewHolder> taskBoardListAdapter
                = new FirebaseRecyclerAdapter<ca.mvp.scrumtious.scrumtious.model.Task, TaskBoardFragment.TaskBoardViewHolder>(
                ca.mvp.scrumtious.scrumtious.model.Task.class,
                R.layout.task_row,
                TaskBoardFragment.TaskBoardViewHolder.class,
                mQuery
        ) {


            @Override
            protected void populateViewHolder(TaskBoardFragment.TaskBoardViewHolder viewHolder, ca.mvp.scrumtious.scrumtious.model.Task model, int position) {
                final String tid = getRef(position).getKey().toString();
                final TaskBoardFragment.TaskBoardViewHolder mViewHolder = viewHolder;
                String assignedTo = "Nobody";

                String status = model.getStatus().toString();

                // Assigned to someone
                if (!model.getAssignedTo().equals("null")){
                    assignedTo = model.getAssignedTo().toString();
                }

                viewHolder.setDetails(model.getTaskDesc(), assignedTo);

                ImageButton deleteTaskBtn = viewHolder.getTaskDelete();
                ImageButton switchTaskBtn = viewHolder.getTaskSwitch();

                if (status.equals("not_started")){
                    viewHolder.setCardRed();
                }
                else if (status.equals("in_progress")){
                    viewHolder.setCardYellow();
                }
                else{
                    viewHolder.setCardGreen();
                }

                switchTaskBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        taskBoardView.onClickSwitchTask(view,tid);
                    }
                });
                // User wants to delete the task
                deleteTaskBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        taskBoardView.onClickDeleteTask(tid);
                    }
                });

                // The following checks if the current user is the project owner
                mDatabase = FirebaseDatabase.getInstance();
                mRef = mDatabase.getReference();
                mRef.child("projects").child(pid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String projectOwnerUid = dataSnapshot.child("projectOwnerUid").getValue().toString();

                            // Current user is not group owner, don't allow them to view delete button
                            if(!projectOwnerUid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                mViewHolder.setDeleteInvisible();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
            @Override
            public void onDataChanged() {
                taskBoardView.setEmptyStateView();
            }


        };
        return taskBoardListAdapter;
    }

    // Project owner wants to delete a task
    @Override
    public void deleteTask(final String tid) {
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference().child("projects").child(pid).child("user_stories").child(usid).child("numTasks");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    long oldNumTasks = (long) dataSnapshot.getValue();

                    mDatabase = FirebaseDatabase.getInstance();
                    mRef = mDatabase.getReference().child("projects").child(pid).child("user_stories").child(usid);

                    Map deleteTaskMap = new HashMap();
                    deleteTaskMap.put("/numTasks", oldNumTasks+1);
                    deleteTaskMap.put("/tasks/" + tid, null);

                    mRef.updateChildren(deleteTaskMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                            // Successfully deleted task
                            if (task.isSuccessful()){
                                taskBoardView.showMessage("Successfully deleted task.", false);
                            }
                            // Failed to delete task
                            else{
                                taskBoardView.showMessage("An error occurred, failed to delete task.", false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void changeStatus(String tid, final String newStatus) {
        mRef = FirebaseDatabase.getInstance().getReference().child("projects").child(pid).child("user_stories")
                .child(usid).child("tasks")
                .child(tid);
        Map changeStatusMap = new HashMap();
        changeStatusMap.put("/status", newStatus);

        mRef.updateChildren(changeStatusMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                // Successful task change
                if (task.isSuccessful()){
                    if (newStatus.equals("not_started")){
                        taskBoardView.showMessage("Marked the task as \"Not Started\".", false);
                    }
                    else if (newStatus.equals("in_progress")){
                        taskBoardView.showMessage("Marked the task as \"In Progress\".", false);
                    }
                    else{
                        taskBoardView.showMessage("Marked the task as \"Completed\".", false);
                    }
                }
                // Failed to change the status
                else{
                    if (newStatus.equals("not_started")){
                        taskBoardView.showMessage("An error occurred, failed to mark the task as \"Not Started\".", false);
                    }
                    else if (newStatus.equals("in_progress")){
                        taskBoardView.showMessage("An error occurred, failed to mark the task as \"In Progress\".", false);
                    }
                    else{
                        taskBoardView.showMessage("An error occurred, failed to mark the task as \"Completed\".", false);
                    }
                }
            }
        });
    }
}
