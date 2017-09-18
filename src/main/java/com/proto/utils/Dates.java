package com.proto.utils;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Date related convenience functions.
 * <p>
 *
 * 10/22/00: Standardized all functions to use java.util.Dates, now only sqlDate
 * returns a java.sql.Date. Also factored out setting h,m,s into setTime().
 * Converted comments to javadoc style.
 * <p>
 *
 * @author wfrancis
 */
public class Dates {

    /** thread-local calendar instance */
    private static final ThreadLocal<Calendar> calendar = new ThreadLocal<Calendar>() {
        protected Calendar initialValue() { return Calendar.getInstance(); }
    };
    
    private static final Date SOME_EARLY_DATE = getDayAt(new Date(1000*60*60*24*5), 23, 59);
    private static SimpleDateFormat df8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'Z");

    /**
     * Shareable instance of the default timezone, which is surprisingly expensive to create
     */
    public static final TimeZone DEFAULT_TZ = TimeZone.getDefault();


    private Dates() {
    }

    /**
     * @return the TimeZone with the specified zoneID, e.g. America/New_York.
     * @throws IllegalArgumentException if the ID is unknown
     */
    public static TimeZone getTimeZone(String zoneID) throws IllegalArgumentException {
        TimeZone zone = TimeZone.getTimeZone(zoneID);
        if (!zoneID.equals(zone.getID())) {
            throw new IllegalArgumentException("Unknown timezone: " + zoneID);
        }

        return zone;
    }

    /**
     * @return the number of ms between midnight on the date and the date at the system's timezone.
     *
     * NOTE: If you want a Date relative to the Epoch that represents the time component of date, use:
     * getDateRelativeToEpoch(getMillisFromMidnight(date));
     *
     * WARNING: don't be tempted to just instantiate a new Date... new Date expects the ms from the Epoch in GMT
     * not in local time.
     */
    public static long getMillisFromMidnight(Date date){
        long midnight = getDayAt(date, 0, 0).getTime();
        long time = date.getTime();

        return time - midnight;
    }

    /**
     * Dubious method that returns a Date that has only Time-of-day information.
     *
     * We currently use it to create template Slices, Durations and Trajectories.
     *
     * TODO: we should create a type specifically for that purpose instead of using
     * this more confusing scheme.
     *
     * @param msSinceMidnight the number of ms in the current timezone since midnight.
     * For example: if we want a Date to represent 1:00 am we would use 60 * 60 * 1000
     *
     * @return a Date that represents the time of day intended by msSinceMidnight but
     * of the first day of the epoch: January 1, 1970
     *
     * @throws IndexOutOfBoundsException if msSinceMidnight is less than 0 or more than 86399999
     */
    public static Date getDateRelativeToEpoch(long msSinceMidnight){
        Calendar c = calendar.get();
        c.clear();
        c.set(Calendar.MILLISECOND, (int) msSinceMidnight); // since we limit the size of msSinceMidnight, this cast is safe.
        return c.getTime();
    }

    /**
     * Returns a Date that has the same timezone and day as the ones passed in but with the number of milliseconds
     * set to msSinceMidnight relative to midnight of the date passed in.
     * 
     * @throws IndexOutOfBoundsException if msSinceMidnight is less than 0 or more than 86399999
     */
    public static Date getDateRelativeToMidnight(Date date, TimeZone timeZone, long msSinceMidnight){
        Calendar c = calendar.get();        
        try {
            c.setTimeZone(timeZone);
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, (int) msSinceMidnight); // since we limit the size of msSinceMidnight, this cast is safe.
            return c.getTime();            
        }
        finally {
            // reset the timezone
            c.setTimeZone(TimeZone.getDefault());
        }
    }

    public static Date getTodayAt(int hh, int mm){
        return getDayAt(new Date(), hh, mm);
    }

    public static Date getDayAt(Date day, int hh, int mm){
        return getDayAt(day, hh, mm, 0, 0);
    }


    /**
     * Returns a new date with the given year, month, and day using a default
     * Calendar instance to create it (i.e. with the system timezone).
     *
     * WARNING: month is Java Calendar style, i.e. 0=January! Use {@link Calendar#JANUARY}
     */
    public static Date getDayAtMidnight (int year, int month, int day){
        return getDate(year, month, day, 0, 0, 0, 0);
    }

    public static Date getDate(int year, int month, int day, int hh, int mm) {
        return getDate(year, month, day, hh, mm, 0, 0);
    }

    /**
     * Date Arithmetic function.  Adds the specified (signed) amount of time
     * in Milliseconds, based on the calendar's rules.
     * Equivalent to: Calendar.add( Calendar.MILLISECOND, millisToAdd ).
     *
     * NOTE: If millis is negative... this method subtracts the time.
     *
     * NOTE: This method can't add or subtract more than about 24.8 days,
     * because millis is an int, not a long.
     */
    public static Date addMilliseconds( Date original, int millis ){
        return addTime(original, 0, 0, 0, millis);
    }


    /**
     * @param day the date information.
     * @param timeOfDay the time information.
     * @return a Date that represents the merger between the Date information of day and the Time information of timeOfDay
     */
    public static Date getDayAndTime(Date day, Date timeOfDay) {
        Calendar cal = calendar.get();
        cal.setTime(timeOfDay);
        int hh = cal.get(Calendar.HOUR_OF_DAY);
        int mm = cal.get(Calendar.MINUTE);
        int ss = cal.get(Calendar.SECOND);
        int ms = cal.get(Calendar.MILLISECOND);
        return getDayAt(day, hh, mm, ss, ms);
    }

    /**
     * @param pattern a pattern that describes the fields in the string, eg. HH:mm for hours:minutes.
     * @param date the string representation of the date
     * @return a Date reprsenting the interpretation of the pattern on the date parameter.
     *
     * @throws IllegalArgumentException if there is a problem parsing
     */
    public static Date parseDate(String pattern, String date) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date returnValue = format.parse(date);
            Date endOfFirstDay = SOME_EARLY_DATE;
            if (returnValue.before(endOfFirstDay)) {
                return getDayAndTime(new Date(), returnValue);
            } else {
                return returnValue;
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    //************************ Helper Methods

    public static Date getDate(int year, int month, int day, int hh, int min, int ss, int mmm) {
        assert year > 31; //This is not strict... I just want to catch swaping of DD and YY mistakes
        assert month >= 0 && month < 12;
        assert day > 0 && day <= 31; //This is not strict... I just want to catch swapping of DD and YY mistakes
        assert hh >= 0 && hh < 24;
        assert min >= 0 && min < 60;
        assert ss >= 0 && ss < 60;
        assert mmm >= 0 && mmm < 1000;

        Calendar cal = calendar.get();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(year, month, day, hh, min, ss);
        cal.set(Calendar.MILLISECOND, mmm);
        return cal.getTime();
    }

    /**
     * Returns true if two times fall on the same calendar day in the same timezone
     */
    public static boolean onSameDay(Date time1, Date time2, TimeZone tz) {
        Calendar cal1 = getCalendarAtMidnight(time1, tz);
        Calendar cal2 = getCalendarAtMidnight(time2, tz);
        return cal1.equals(cal2);

    }

    //************************ Old Dates methods

    /** Returns java.sql.Date of the argument date. */
    public static java.sql.Date sqlDate(Date d) {
        return (d == null) ? null : new java.sql.Date(d.getTime());
    }

    /**
     * Date Arithmetic function. Adds the specified (signed) amount of time in Days, based
     * on the calendar's rules.
     * Equivalent to: Calendar.add(Calendar.DATE, daysToAdd).
     *
     * NOTE: if days is negative... this method subtracts the time.
     */
    public static Date addDays(Date d, int days) {
        Calendar c = calendar.get();
        c.setTime(d);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    /**
     * This method adds (or subtracts, for negative days) the specified number of days from
     * a date <i>String</i> in YYYYMMDD format, and returns its result in the same format.
     * Returns null if the input date was not correctly formatted.
     */
    public static String addDays(String date, int days) {
        Date realDate = parseDate("yyyyMMdd", date);
        if (realDate == null)
            return null;

        return date2YYYYMMDD(addDays(realDate, days));
    }

    /**
     * Date Arithmetic function. Adds the specified (signed) amount of time in Years, based
     * on the calendar's rules.
     * Equivalent to: Calendar.add(Calendar.YEAR, daysToAdd).
     *
     * NOTE: if yearss is negative... this method subtracts the time.
     */
    public static Date addYears(Date d, int years) {
        Calendar c = calendar.get();
        c.setTime(d);
        c.add(Calendar.YEAR, years);
        return c.getTime();
    }

    /**
     * Returns true if the calendar represents a saturday
     * or sunday.
     */
    public static boolean isWeekend(Calendar c) {
        int dow = c.get(Calendar.DAY_OF_WEEK);
        if (dow == Calendar.SUNDAY || dow == Calendar.SATURDAY)
            return true;

        return false;
    }

    /**
     * Returns the start of the last weekend (i.e. midnight of the most recent
     * Saturday). Returns midnight the same day if called with a saturday
     * argument.
     */
    public static Date getStartOfLastWeekend(Date date) {
        return getStartOfNextWeekend(addDays(date, -7));
    }

    /**
     * Returns the start of the next weekend (i.e. midnight of the next
     * saturday). Returns midnight of the saturday a week hence if called with a
     * saturday argument date.
     */
    public static Date getStartOfNextWeekend(Date date) {
        Calendar c = calendar.get();
        c.setTime(date);
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            c.add(Calendar.DATE, 7);
            return getDayAtMidnight(c.getTime());
        } else {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            return getDayAtMidnight(c.getTime());
        }
    }

    /**
     * Returns the day of week of the given date. Values returned are
     * Calendar.SUNDAY, Calendar.SATURDAY, etc.
     */
    public static int getDayOfWeek(Date date) {
        Calendar c = calendar.get();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    /** Returns the argument date with time (h,m,s,ms) set as indicated. */
    public static Date getDayAt(Date d, int h, int m, int s, int ms) {
        Calendar c = calendar.get();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.SECOND, s);
        c.set(Calendar.MILLISECOND, ms);
        return c.getTime();
    }

    /** Returns the argument date with the secs and millis truncated to 0. */
    public static Date truncateSeconds(Date d) {
        Calendar c = calendar.get();
        c.setTime(d);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /** Returns the argument date with the millis truncated to 0. */
    public static Date truncateMillis(Date d) {
        Calendar c = calendar.get();
        c.setTime(d);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Returns a new date with the given year, month, day, hour, minute and
     * second using a default Calendar instance to create it (i.e. with the
     * system timezone).
     *
     * WARNING: month is Java Calendar style, i.e. 0=January!
     */
    public static Date getDate(int year, int month, int date, int hour, int min, int sec) {
        return getDate(year, month, date, hour, min, sec, 0);
    }

    /**
     * Date Arithmetic function.  Adds the specified (signed) amount of time
     * in hrs, mins, secs, and milliseconds, based on the calendar's rules.
     *
     * NOTE: If any param is negative... this method subtracts the time.
     */
    public static Date addTime(Date d, int hrs, int mins, int secs, int millis) {
        Calendar c = calendar.get();
        c.setTime(d);
        c.add(Calendar.SECOND, secs);
        c.add(Calendar.MINUTE, mins);
        c.add(Calendar.HOUR_OF_DAY, hrs);
        c.add(Calendar.MILLISECOND, millis );
        return c.getTime();
    }

    /**
     * This method convert a Date object to Calendar Object.
     * @param date The date we want to convert
     * @return The Calendar representation for the date.
     */
    public static Calendar convertDateToCalendar(Date date){
        return (new DateTime(date)).toGregorianCalendar();
    }
    /**
     * Date Arithmetic function.  Adds the specified (signed) amount of time
     * in mins, secs, based on the calendar's rules.
     *
     * NOTE: If any param is negative... this method subtracts the time.
     */
    public static Date addTime(Date d, int mins, int secs) {
        return addTime(d, 0, mins, secs, 0);
    }

    /**
     * Date Arithmetic function. Adds the specified (signed) amount of time in Seconds, based
     * on the calendar's rules.
     * Equivalent to: Calendar.add(Calendar.SECOND, secondsToAdd).
     *
     * NOTE: if secs is negative... this method subtracts the time.
     */
    public static Date addSeconds(Date d, int secs) {
        return addTime(d, 0, 0, secs, 0);
    }

    /** Returns the number of days between two dates. Argument dates
     * are first truncated to midnight, and the number of 24 hour periods
     * between the two is returned. <p>
     *
     * Implementation note: since the number of days as calculated
     * (ms between the dates / ms per 24 hour period) doesn't always divide
     * evenly due to daylight savings and leap seconds, we round the result:
     * these deviations should never be more than about an hour.
     */
    public static int getDaysBetween(Date a, Date b) {
        // (truncate both dates to midnight)
        Date d1 = getDayAtMidnight(a);
        Date d2 = getDayAtMidnight(b);

        // ms between dates assuming a before b
        long duration = d2.getTime() - d1.getTime();
        // long nDays = duration / (24L * 60L * 60L * 1000L);
        double nDays = duration / (double)(24L * 60L * 60L * 1000L);
        return (int) Math.round(nDays);
    }

    /** Returns the number of hours between two dates. The number of 1 hour periods
     * between the two is returned. <p>
     *
     * Implementation note: we truncate the fractional hours, so a difference of 1.75 hours
     * will return 1.
     */
    public static int getHoursBetween(Date a, Date b) {
        // ms between dates assuming a before b
        long duration = b.getTime() - a.getTime();
        // long nHours = duration / (60L * 60L * 1000L);
        double nHours = duration / (double)(60L * 60L * 1000L);
        return (int) nHours;
    }

    /**
     * Returns a date with the same y/m/d, but with time truncated to midnight
     * that morning.
     */
    public static Date getDayAtMidnight(Date d) {
        return getDayAt(d, 0, 0, 0, 0);
    }

    /**
     * Returns a date with the same y/m/d, but with time truncated to midnight
     * that morning.
     */
    public static Date getDayAtMidnight(long millis) {
        return getDayAt(new Date(millis), 0, 0, 0, 0);
    }

    /**
     * Returns the current date with time truncated to midnight.
     */
    public static Date getMidnight() {
        return getDayAt(new Date(), 0, 0, 0, 0);
    }

    /**
     * @return the earlier of the two dates
     */
    public static Date getEarlier(Date a, Date b) {
        return a.before(b) ? a : b;
    }

    /**
     * @return the latter of the two dates
     */
    public static Date getLatter(Date a, Date b) {
        return a.before(b) ? b : a;
    }

    //---------------------------------------------------------------------------

    public static Calendar getCalendarAtMidnight(Date date, TimeZone timeZone) {
        Calendar c = new GregorianCalendar(timeZone);
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }

    public static Calendar getCalendarAtMidnight(int year, int month, int day, TimeZone timeZone) {
        Calendar c = new GregorianCalendar(timeZone);
        c.set(year, month, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }

    //---------------------------------------------------------------------------

    /*
     * Parse an ISO8601 date or return null on failure.
     */
    public static Date getDate8601(String s) {
        Date d;
        try {
            d = df8601.parse(s);
        } catch (Exception e) {
            d = null;
        }
        return d;
    }

    /**
     * This method can be used to transform a calendar object to a string in the YYYYMMDD format. Note
     * that January is transfored to 1, i.e., Jan 1st, 2007 will be translated to 20070101.
     * @param cal The calendar object.
     * @return A string with the date in YYYYMMDD format. null if an error occured.
     */
    public static String calendar2YYYYMMDD(Calendar cal){
        if ( cal == null )
            return null;
        String date = "" + cal.get(Calendar.YEAR );
        if ( ( cal.get(Calendar.MONTH ) + 1 ) < 10 ){
            date += "0";
        }
        date += ( cal.get(Calendar.MONTH ) + 1 );
        if ( cal.get(Calendar.DAY_OF_MONTH ) < 10 ){
            date += "0";
        }
        date += cal.get(Calendar.DAY_OF_MONTH );
        return date;
    }

    /**
     * Converts a Date object into a YYYYMMDD formatted string.
     * @see #calendar2YYYYMMDD(Calendar)
     */
    public static String date2YYYYMMDD(Date date){
        Calendar cal = calendar.get();

        cal.setTime(date);

        return calendar2YYYYMMDD(cal);
    }

    /**
     * Converts a Date object into a YYYY-MM-DD formatted string.
     */
    public static String date2YYYY_MM_DD(Date date) {
        Calendar cal = calendar.get();
        cal.setTime(date);

        String res = "" + cal.get(Calendar.YEAR) + "-";

        if ((cal.get(Calendar.MONTH) + 1) < 10) {res += "0";}
        res += (cal.get(Calendar.MONTH) + 1) + "-";

        if (cal.get(Calendar.DAY_OF_MONTH) < 10 ) {res += "0";}
        res += cal.get(Calendar.DAY_OF_MONTH);

        return res;
    }

    /**
     * This method can be used to retrieve the last requested day of the week that
     * occurred before the current date.<br/>
     * For example, it can return the last Friday before Aug 26, 2007.<br/>
     * Note that if the date given IS the requested day of the week,
     * it will return the same day from the <b>previous week</b>.
     *
     * @param date The target date to search backwards from
     * @param dayOfWeek The target day of the week you want to reach
     * @return The target date
     */
    public static Date getLastDayFrom(Date date, int dayOfWeek) {
        int daysBetween = ((7 + dayOfWeek - Dates.getDayOfWeek(date)) % 7) - 7;

        // make sure that we never pick the current day, even if it is the day of the week we want
        if (daysBetween == 0)
            daysBetween = 7;

        return Dates.addDays(date, daysBetween);
    }

    private static final SortedMap<Long, String> PERIOD_STRINGS = new TreeMap<Long, String>() {{
        put(1000L, "second");
        put(60*1000L, "minute");
        put(60*60*1000L, "hour");
        put(24*60*60*1000L, "day");
        put(7*24*60*60*1000L, "week");
        put(30*24*60*60*1000L, "month");
        put(365*24*60*60*1000L, "year");
    }};

    /**
     * Return human-friendly representation of the time between two dates.
     * @param delta Time interval in milliseconds
     * @return Representation like "2 days" or "31 minutes"
     */
    public static String getFriendlyInterval(long delta) {

        long div = 1;
        String desc = "millisecond";
        for (Long period : PERIOD_STRINGS.keySet()) {
            if (period < delta) {
                div = period;
                desc = PERIOD_STRINGS.get(period);
            }
        }

        long count = delta / div;
        return count + " " + desc + (count == 1 ? "" : "s");
    }

    /**
     * Return the latest date between the two provided.
     */
    public static Date max(Date d1, Date d2) {
        return d1.after(d2) ? d1 : d2;
    }

    /**
     * Return the earliest date between the two provided.
     */
    public static Date min(Date d1, Date d2) {
        return d1.before(d2) ? d1 : d2;
    }

    public static Date fromYYYYMMDD(String date) {
        return Dates.parseDate("yyyyMMdd", date);
    }

    private static DateFormat DURATION_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * Parses a duration in the format HH:mm:ss.  Returns milliseconds.
     * For example: 01:30:00 becomes 1.5*L_MS_PER_HOUR.
     * @param dur Duration to parse
     * @return Time in milliseconds
     */
    public static int parseDuration(String dur) {
        try {
            return (int)(DURATION_FORMAT.parse(dur).getTime() - DURATION_FORMAT.parse("00:00:00").getTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static DateTime min(DateTime d1, DateTime d2) {
        if (d2.isBefore(d1)) {
            return d2;
        }

        return d1;
    }

    public static DateTime max(DateTime d1, DateTime d2) {
        if (d2.isAfter(d1)) {
            return d2;
        }

        return d1;
    }
}
