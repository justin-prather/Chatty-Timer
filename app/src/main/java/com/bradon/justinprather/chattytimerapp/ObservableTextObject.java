package com.bradon.justinprather.chattytimerapp;

import java.util.Observable;

/**
 * Created by justinprather on 2015-02-05.
 */
public class ObservableTextObject extends Observable {
    private int hours;
    private int minutes;
    private int seconds;
    private String comment;

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
        setChanged();
        notifyObservers( "time" );
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
        setChanged();
        notifyObservers( "time" );
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
        setChanged();
        notifyObservers( "time" );
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        setChanged();
        notifyObservers( "comment" );
    }
}
