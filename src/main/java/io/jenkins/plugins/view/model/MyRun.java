package io.jenkins.plugins.view.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.view.util.Matrix.Arrow;

@ExportedBean(defaultVisibility = 100)
public class MyRun { 
    
    private String displayName;

    private String parent;

    private String url;

    private Status status;

    private Arrow arrow;

    private long duration;

    private long startTimeMillis;

    @Exported
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Exported
    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public MyRun(String displayName, String parent, String url, Status status, Arrow arrow, long duration, long startTimeMillis) {
        this.displayName = displayName;
        this.parent = parent;        
        this.url = url;
        this.status = status;
        this.arrow = arrow;
        this.duration = duration;
        this.startTimeMillis = startTimeMillis;
    }
    @Exported
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    @Exported
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Exported
    public Arrow getArrow() {
        return arrow;
    }

    public void setArrow(Arrow arrow) {
        this.arrow = arrow;
    }

    @Exported
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Exported
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public enum Status {
        /**
         * Hudson Result
         */
        SUCCESS, UNSTABLE, FAILURE, NOT_BUILT, ABORT, BUILDING, PENDING, MANUAL
    }
}