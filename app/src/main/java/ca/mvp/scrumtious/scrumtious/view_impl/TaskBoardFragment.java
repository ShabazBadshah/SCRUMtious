package ca.mvp.scrumtious.scrumtious.view_impl;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import ca.mvp.scrumtious.scrumtious.R;
import ca.mvp.scrumtious.scrumtious.interfaces.presenter_int.TaskBoardPresenterInt;
import ca.mvp.scrumtious.scrumtious.interfaces.view_int.TaskBoardViewInt;
import ca.mvp.scrumtious.scrumtious.model.Task;
import ca.mvp.scrumtious.scrumtious.presenter_impl.TaskBoardPresenter;
import ca.mvp.scrumtious.scrumtious.utils.SnackbarHelper;

public class TaskBoardFragment extends Fragment implements TaskBoardViewInt {

    private String pid, usid;
    private String type;

    private TaskBoardPresenterInt taskBoardPresenter;
    private RecyclerView taskBoardList;
    private LinearLayout emptyStateView;
    private FirebaseRecyclerAdapter<Task, TaskBoardViewHolder> taskBoardAdapter;
    private ProgressDialog deletingTaskDialog;

    private AssignToFragment assignToFragment;


    public TaskBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pid = getArguments().getString("projectId");
        this. usid = getArguments().getString("userStoryId");
        type = getArguments().getString("type");
        this.taskBoardPresenter = new TaskBoardPresenter(this,pid,usid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_board, container, false);
        taskBoardList = view.findViewById(R.id.taskBoardFragmentRecyclerView);
        emptyStateView = view.findViewById(R.id.taskBoardFragmentNoTasksEmptyStateView);
        setupRecyclerView();
        return view;
    }

    @Override
    public void onPause() {

        // Pop up has to be dismissed
        if (assignToFragment != null){
            assignToFragment.dismiss();
        }

        super.onPause();
    }

    public void setupRecyclerView(){
        taskBoardList.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Sets up the specified type of adapter
        taskBoardAdapter = taskBoardPresenter.setupTaskBoardAdapter(type);
        taskBoardList.setAdapter(taskBoardAdapter);
    }
    @Override
    public void showMessage(String message, boolean showAsToast) {

        // Pop up has to be dismissed
        if (assignToFragment != null){
            assignToFragment.dismiss();
        }

        if (deletingTaskDialog!=null && deletingTaskDialog.isShowing()){
            deletingTaskDialog.dismiss();
        }

        // Show message in toast so it persists across activity transitions
        if (showAsToast){
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }

        else {
            // Call the utils class method to handle making the snackbar
            SnackbarHelper.showSnackbar(getActivity(), message);
        }
    }

    // If there are no tasks to show in this specific tab, or there are
    @Override
    public void setEmptyStateView() {
        if(taskBoardAdapter.getItemCount() == 0){
            emptyStateView.setVisibility(View.VISIBLE);
            taskBoardList.setVisibility(View.GONE);
        }
        else{
            emptyStateView.setVisibility(View.GONE);
            taskBoardList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClickDeleteTask(final String tid){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deletingTaskDialog = new ProgressDialog(getContext());
                        deletingTaskDialog.setTitle("Delete Task");
                        deletingTaskDialog.setCancelable(false);
                        deletingTaskDialog.setMessage("Attempting to delete task...");
                        deletingTaskDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        deletingTaskDialog.show();

                        taskBoardPresenter.deleteTask(tid);
                        }

                    }
                )
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remain in this screen
                    }
                })
                .create().show();
    }
    public void onClickSwitchTask(View view,final String tid){
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.task_status_view, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.task_not_started:
                        taskBoardPresenter.changeStatus(tid,"not_started");
                        return true;


                    case R.id.task_in_progress:
                        taskBoardPresenter.changeStatus(tid,"in_progress");
                        return true;

                    case R.id.task_completed:
                        taskBoardPresenter.changeStatus(tid,"completed");
                        return true;
                }

                return false;
            }
        });

        popup.show();
    }

    // Open up the dialog fragment to choose which user to assign this task to
    @Override
    public void onLongClickTask(String tid){

        assignToFragment = AssignToFragment.newInstance(pid,usid,tid);
        assignToFragment. setTaskBoardView(this);
        assignToFragment.show(getFragmentManager(), "AssignToFragment");
    }

    public static class TaskBoardViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView descriptionView, assignedToView;
        ImageButton taskSwitch;
        ImageButton taskDelete;
        CardView card;

        public TaskBoardViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;

            descriptionView = (TextView) mView.findViewById(R.id.taskRowTaskDescTextView);
            assignedToView = (TextView) mView.findViewById(R.id.taskRowAssignedToTextView);
            taskDelete = (ImageButton) mView.findViewById(R.id.taskRowDeleteTaskImageButton);
            taskSwitch = (ImageButton) mView.findViewById(R.id.taskRowSwitchTaskStatesImageButton);
            card = (CardView) mView.findViewById(R.id.taskRowCardView);
        }
        public void setDetails(String description, String assignedTo){

            descriptionView.setText(description);
            if (assignedTo.equals("")){
                assignedToView.setText("Not currently assigned to a member");
            }
            else {
                assignedToView.setText("Assigned to " + assignedTo);
            }
        }
        public ImageButton getTaskSwitch(){
            return this.taskSwitch;
        }

        public ImageButton getTaskDelete(){
            return this.taskDelete;
        }

        // Only owner should be able to delete user stories
        public void setDeleteInvisible(){
            this.taskDelete.setVisibility(View.GONE);
        }

        public void setCardRed(){
            card.setCardBackgroundColor(Color.parseColor("#F44336"));
        }

        public void setCardYellow(){
            card.setCardBackgroundColor(Color.parseColor("#F5BA0A"));
        }

        public void setCardGreen(){
            card.setCardBackgroundColor(Color.parseColor("#8BC34A"));

        }

    }
}
