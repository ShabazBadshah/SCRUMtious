package ca.mvp.scrumtious.scrumtious.model;

import java.util.Map;

public class Project {

    private String projectOwnerUid;
    private String projectTitle;
    private String projectOwnerEmail;
    private String projectDesc;
    private long creationTimeStamp;
    private long numMembers;

    private final int MAX_DESC_SIZE = 254;

    // empty constructor is needed for firebase
    public Project() {

    }

    public String getProjectOwnerUid() { return this.projectOwnerUid; }
    public String getProjectTitle() { return this.projectTitle; }
    public String getProjectDesc() { return this.projectDesc; }
    public String getProjectOwnerEmail() { return this.projectOwnerEmail; }
    public long getCreationTimeStamp() {return creationTimeStamp;}
    public long getNumMembers(){ return numMembers;}

}
