package com.proto.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/** 
 * Implementation of the Clock that returns the same time on every call, until that time is manually changed.
 * 
 * The default date is Today at 9:00 am
 * 
 * @author wfrancis
 */
public class SettableClock implements IClock{
    
    private Date time = Dates.getDayAt(new Date(), 9,0);
    
    public void setTime(Date time){
        this.time = time;
    }
    
    public void setTime(long time) {
        setTime(new Date(time));
    }

    /** sets the time to the same day but new hour and minute */
    public void setTime(int hh, int mm){
        this.time = Dates.getDayAt(currentTime(), hh, mm);
    }
    
    public Date currentTime() {
        return time;
    }
    
    public long currentTimeMillis() {
        return time.getTime();
    }

    /** Advances current time by 1 second; use to simulate behavior of 
     * old "counter" clock for those unit tests that used it. 
     */
    public void addOneSecond() {
        time = new Date(time.getTime() + 1000);
    }

    public long currentTimeNanos() {
        return TimeUnit.NANOSECONDS.convert(currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
}