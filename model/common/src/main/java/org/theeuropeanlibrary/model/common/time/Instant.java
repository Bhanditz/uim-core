/* Instant.java - created on 22 de Mar de 2011, Copyright (c) 2011 The European Library, all rights reserved */
package org.theeuropeanlibrary.model.common.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.theeuropeanlibrary.model.common.FieldId;

/**
 * Defines a particular point in time, allowing different levels of detail (granularity)
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 22 de Mar de 2011
 */
public class Instant extends Temporal {
    /**
     * The instant itself. Only need to be exact up the level specified in granularity. The
     * remaining data in the Date should be ignored.
     */
    @FieldId(1)
    private Date               time;

    /**
     * States the levels of detail to which the Instant is defined in field "time"
     */
    @FieldId(2)
    private InstantGranularity granularity;

    /**
     * States if this instant is uncertain Ex: when in MARC data we find "1992?"
     */
    @FieldId(3)
    private boolean            uncertain;

    /**
     * Creates a new instance of this class.
     */
    public Instant() {
        this(null, InstantGranularity.UNKNOWN, true);
    }

    /**
     * Creates a new instance of this class with a certain date
     * 
     * @param time
     *            The instant itself. Only need to be exact up the level specified in granularity.
     *            The remaining data in the Date should be ignored.
     * @param granularity
     *            States the levels of detail to which the Instant is defined in field "time"
     */
    public Instant(Date time, InstantGranularity granularity) {
        this(time, granularity, false);
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param time
     *            The instant itself. Only need to be exact up the level specified in granularity.
     *            The remaining data in the Date should be ignored.
     * @param granularity
     *            States the levels of detail to which the Instant is defined in field "time"
     * @param uncertain
     *            States if this instant is uncertain
     */
    public Instant(Date time, InstantGranularity granularity, boolean uncertain) {
        super();
        this.time = time;
        this.granularity = granularity;
        this.uncertain = uncertain;
    }

    /**
     * Creates a new instance of this class from a single year.
     * 
     * @param year
     */
    public Instant(int year) {
        granularity = InstantGranularity.YEAR;
        uncertain = false;
        Calendar cal = Calendar.getInstance();
        normalizeTime(cal, granularity);
        cal.set(Calendar.YEAR, year);
        time = cal.getTime();
    }

    /**
     * Creates a new instance of this class from a single year.
     * 
     * @param year
     * @param granularity
     */
    public Instant(int year, InstantGranularity granularity) {
        this.granularity = granularity;
        uncertain = false;
        Calendar cal = Calendar.getInstance();
        normalizeTime(cal, granularity);
        cal.set(Calendar.YEAR, year);
        time = cal.getTime();
    }

    /**
     * Creates a new instance of this class .
     * 
     * @param year
     * @param month
     */
    public Instant(int year, int month) {
        granularity = InstantGranularity.MONTH;
        uncertain = false;
        Calendar cal = Calendar.getInstance();
        normalizeTime(cal, granularity);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, month - 1);
        time = cal.getTime();
    }

    /**
     * Creates a new instance of this class .
     * 
     * @param year
     * @param month
     * @param day
     */
    public Instant(int year, int month, int day) {
        granularity = InstantGranularity.DAY;
        uncertain = false;
        Calendar cal = Calendar.getInstance();
        normalizeTime(cal, granularity);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        time = cal.getTime();
    }

    /**
     * Creates a new instance of this class .
     * 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     */
    public Instant(int year, int month, int day, int hour, int minute) {
        granularity = InstantGranularity.MINUTE;
        uncertain = false;
        Calendar cal = Calendar.getInstance();
        normalizeTime(cal, granularity);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        time = cal.getTime();
    }

    /**
     * Creates a new instance of this class .
     * 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param miliseconds
     */
    public Instant(int year, int month, int day, int hour, int minute, int miliseconds) {
        granularity = InstantGranularity.MILLISECOND;
        uncertain = false;
        Calendar cal = Calendar.getInstance();
        normalizeTime(cal, granularity);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.MILLISECOND, miliseconds);
        time = cal.getTime();
    }

    /**
     * @return The instant itself. Only need to be exact up the level specified in granularity. The
     *         remaining data in the Date should be ignored.
     */
    public Date getTime() {
        return time;
    }

    /**
     * @return States the levels of detail to which the Instant is defined in field "time"
     */
    public InstantGranularity getGranularity() {
        return granularity;
    }

    /**
     * @return States if this instant is uncertain
     */
    public boolean isUncertain() {
        return uncertain;
    }

    /**
     * @param time
     *            The instant itself. Only need to be exact up the level specified in granularity.
     *            The remaining data in the Date should be ignored.
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @param granularity
     *            States the levels of detail to which the Instant is defined in field "time"
     */
    public void setGranularity(InstantGranularity granularity) {
        this.granularity = granularity;
        if (time != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            normalizeTime(cal, granularity);
            time = cal.getTime();
        }
    }

    /**
     * @param uncertain
     *            States if this instant is uncertain
     */
    public void setUncertain(boolean uncertain) {
        this.uncertain = uncertain;
    }

    @Override
    public String getDisplay() {
        return getDisplay(granularity);
    }

    /**
     * @param granularity
     * @return display for given granularity
     */
    public String getDisplay(InstantGranularity granularity) {
        InstantGranularity localGranularity = granularity;
        String ret = null;
        if (time != null) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(time);

                if (localGranularity.isMoreDetailedThan(this.granularity))
                    localGranularity = this.granularity;

                switch (localGranularity) {
                case MILLENNIUM: {
                    int year = cal.get(Calendar.YEAR);
                    if (year >= 1000)
                        ret = String.valueOf(year).charAt(0) + "---";
                    else
                        ret = "0---";
                    break;
                }
                case CENTURY: {
                    int year = cal.get(Calendar.YEAR);
                    if (year >= 1000)
                        ret = String.valueOf(year).substring(0, 2) + "--";
                    else if (year >= 100)
                        ret = "0" + String.valueOf(year).substring(0, 1) + "--";
                    else
                        ret = "00--";
                    break;
                }
                case DECADE: {
                    int year = cal.get(Calendar.YEAR);
                    if (year >= 1000)
                        ret = String.valueOf(year).substring(0, 3) + "-";
                    else if (year >= 100)
                        ret = "0" + String.valueOf(year).substring(0, 2) + "-";
                    else if (year >= 10)
                        ret = "00" + String.valueOf(year).substring(0, 1) + "-";
                    else
                        ret = "000-";
                    break;
                }
                case YEAR: {
                    ret = new SimpleDateFormat("yyyy").format(time);
                    break;
                }
                case MONTH: {
                    ret = new SimpleDateFormat("yyyy-MM").format(time);
                    break;
                }
                case DAY: {
                    ret = new SimpleDateFormat("yyyy-MM-dd").format(time);
                    break;
                }
                case MINUTE: {
                    ret = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
                    break;
                }
                case SECOND: {
                    ret = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
                    break;
                }
                case MILLISECOND: {
                    ret = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSS").format(time);
                    break;
                }
                case UNKNOWN: {
                    return "?";
                }
                }
            } catch (Exception e) {
                // FIXME: what to do here?
                e.printStackTrace();
                return "?";
            }

            if (uncertain) ret += "?";
            return ret;
        } else {
            return "?";
        }
    }

    /**
     * Normalizes the time value to the start millisecond of its period. This is essential in order
     * to have working hashCode() and equals()
     */
    private static void normalizeTime(Calendar cal, InstantGranularity granularity) {
        switch (granularity) {
        case MILLENNIUM: {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case CENTURY: {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case DECADE: {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case YEAR: {
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case MONTH: {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case DAY: {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case MINUTE: {
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case SECOND: {
            cal.set(Calendar.MILLISECOND, 0);
            break;
        }
        case MILLISECOND:
        case UNKNOWN: {
            break;
        }
        }
    }

    /**
     * Gets a particular part of the date
     * 
     * @param calendarField
     *            java.util.Calendar field of the date
     * @return date field
     */
    public int getDateField(int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        return calendar.get(calendarField);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((granularity == null) ? 0 : granularity.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + (uncertain ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Instant other = (Instant)obj;
        if (granularity != other.granularity) return false;
        if (time == null) {
            if (other.time != null) return false;
        } else if (!time.equals(other.time)) return false;
        if (uncertain != other.uncertain) return false;
        return true;
    }
}
