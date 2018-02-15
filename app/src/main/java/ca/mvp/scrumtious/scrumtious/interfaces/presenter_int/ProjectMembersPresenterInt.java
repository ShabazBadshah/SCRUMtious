package ca.mvp.scrumtious.scrumtious.interfaces.presenter_int;

import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import ca.mvp.scrumtious.scrumtious.model.User;
import ca.mvp.scrumtious.scrumtious.view_impl.ProjectMembersFragment;

public interface ProjectMembersPresenterInt {
    FirebaseRecyclerAdapter<User, ProjectMembersFragment.MembersViewHolder> setupMembersAdapter
            (RecyclerView memberList);

    void validatePassword(String password, final String uid);
    void checkBeforeInvite(String emailAddress);
    void checkIfOwner();

}