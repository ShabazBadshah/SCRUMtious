package ca.mvp.scrumtious.scrumtious.presenter_impl;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;

import ca.mvp.scrumtious.scrumtious.R;
import ca.mvp.scrumtious.scrumtious.interfaces.presenter_int.SprintListPresenterInt;
import ca.mvp.scrumtious.scrumtious.interfaces.view_int.SprintListViewInt;
import ca.mvp.scrumtious.scrumtious.model.Sprint;
import ca.mvp.scrumtious.scrumtious.view_impl.SprintListActivity;

public class SprintListPresenter implements SprintListPresenterInt {

    private SprintListViewInt sprintListView;
    private String pid;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    private Query mQuery;

    public SprintListPresenter (SprintListViewInt sprintListView, String pid){
        this.sprintListView = sprintListView;
        this.pid = pid;
    }

    @Override
    public FirebaseRecyclerAdapter<Sprint, SprintListActivity.SprintsViewHolder> setupSprintListAdapter(final RecyclerView sprintList, String sortBy) {
        mRef = FirebaseDatabase.getInstance().getReference();
        mQuery = mRef.child("projects").child(pid).child("sprints").orderByChild(sortBy);

        FirebaseRecyclerAdapter<Sprint, SprintListActivity.SprintsViewHolder> sprintListAdapter
                = new FirebaseRecyclerAdapter<Sprint, SprintListActivity.SprintsViewHolder>(
                Sprint.class,
                R.layout.sprint_row,
                SprintListActivity.SprintsViewHolder.class,
                mQuery
        ) {

            @Override
            protected void populateViewHolder(SprintListActivity.SprintsViewHolder viewHolder, Sprint model, int position) {
                final String sid = getRef(position).getKey();

                // Grab the dates
                long startDate = model.getSprintStartDate();
                long endDate = model.getSprintEndDate();
                final String dateFormatted = DateFormat.format("MM/dd/yyyy", startDate).toString()
                        + " to " +  DateFormat.format("MM/dd/yyyy", endDate).toString();

                long currentTime = System.currentTimeMillis();

                Timestamp startDateTimestamp = new Timestamp(startDate);
                Timestamp endDateTimestamp = new Timestamp(endDate);
                Timestamp currentTimestamp = new Timestamp(currentTime);

                // Currently inside of this sprint's time interval
                if (currentTimestamp.after(startDateTimestamp) && currentTimestamp.before(endDateTimestamp)){
                    viewHolder.setCurrentSprintViewVisible();
                }


                viewHolder.setDetails(model.getSprintName(), model.getSprintDesc(), dateFormatted);


                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sprintListView.goToSprintScreen(sid);
                    }
                });
            }
            @Override
            public void onDataChanged() {
                sprintListView.setView();
            }
        };
        return sprintListAdapter;
    }


    }
