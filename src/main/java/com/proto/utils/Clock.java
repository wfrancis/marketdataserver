package com.proto.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link IClock} implementation for simulation purposes.
 *
 * @author wfrancis
 */
public class Clock implements IClock {

    private final boolean isRealTime;
    private final boolean isTimeOffset;

    private AtomicReference<Date> simulatedDate;
    private AtomicBoolean offSetReferenceSet;

    private long simulatedMidnightTime;
    private long simOffsetReferenceTime;
    private long currentMidnightTime;

    public Clock() {
        this(true, false, new Date());
    }

    public Clock(boolean realTime, Date simulatedDate) {
        this(realTime, false, simulatedDate);
    }

    public Clock(boolean realTime, boolean isTimeOffset, Date simulatedDate) {
        if(simulatedDate == null) {
            throw new IllegalArgumentException("simulated date must not be null");
        }

        this.isRealTime = realTime;
        this.isTimeOffset = isTimeOffset;
        this.simulatedDate = new AtomicReference<Date>(simulatedDate);
        this.simulatedMidnightTime = Dates.getDayAtMidnight(simulatedDate).getTime();
        this.currentMidnightTime = Dates.getDayAtMidnight(System.currentTimeMillis()).getTime();
        this.offSetReferenceSet = new AtomicBoolean(false);
    }

    public Date currentTime() {

        if(isRealTime) {
            return new Date(simulatedMidnightTime + getTimeRealTimeOffsetFromMidnight());
        } else {
            return simulatedDate.get();
        }
    }

    private long getTimeRealTimeOffsetFromMidnight() {
        return System.currentTimeMillis() - currentMidnightTime;
    }

    private long getTimeWithOffsetFromMidnight() {
        return System.currentTimeMillis() - simOffsetReferenceTime;
    }

    public long currentTimeMillis() {
        if(isRealTime) {
            if(isTimeOffset) {
                //set initial reference time
                setReferenceTime();
                return simulatedDate.get().getTime() + getTimeWithOffsetFromMidnight();
            }
            else {
                return simulatedMidnightTime + getTimeRealTimeOffsetFromMidnight();    
            }           
        }
        else {
            return simulatedDate.get().getTime();
        }
    }

    private void setReferenceTime() {
        if(!offSetReferenceSet.get()) {
            simOffsetReferenceTime = System.currentTimeMillis();
            offSetReferenceSet.set(true);
        }
    }

    public long currentTimeNanos() {
        return TimeUnit.NANOSECONDS.convert(currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Increments the current time by one millisecond
     */
    public void tick() {
        tick(1L, TimeUnit.MILLISECONDS);
    }

    /**
     * increments the current time by the given amount and units
     *
     * @param duration how much to increment the clock by
     * @param unit     the units of the duration
     */
    public void tick(long duration, TimeUnit unit) {
        if(!isRealTime) {
            simulatedDate.set(new Date(simulatedDate.get().getTime() + TimeUnit.MILLISECONDS.convert(duration, unit)));
        }
    }
}
