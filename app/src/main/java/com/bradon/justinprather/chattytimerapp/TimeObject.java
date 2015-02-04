package com.bradon.justinprather.chattytimerapp;

/**
 * Created by justinprather on 2015-01-05.
 */
public class TimeObject {
    private String timeComment;
    private long timeMillis;

    public TimeObject(String timeComment, int timeMillis ) {
        this.timeComment = timeComment;
        this.timeMillis = timeMillis;
    }

    public TimeObject(TimeObject timeObject) {
        this.timeComment = timeObject.getTimeComment();
        this.timeMillis = timeObject.getTimeMillis();
    }


    public String getTimeComment() {
        return timeComment;
    }

    public void setTimeComment(String timeComment) {
        this.timeComment = timeComment;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

}
