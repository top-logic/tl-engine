
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.util.Date;

import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.person.Person;


/**
 * Info about the login and logout time of a user.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class LoginInfo {

    /** The affected user. */
	private Person user;

    /** The login time of the user, may be null. */
    private Date login;

    /** The logout time of the user, may be null. */
    private Date logout;

    /**
     * Creates a new instance of this class.
     *
     * @param    aUser    The user describing this instance.
     * @param    aDate    The login time of this user (may be null).
     * @throws   IllegalArgumentException    If the given user is null.
     */
	public LoginInfo(Person aUser, Date aDate)
                                        throws IllegalArgumentException {
        if (aUser == null) {
            throw new IllegalArgumentException ("Given user is null");
        }

        this.user  = aUser;
        this.login = aDate;
    }

    /**
     * Returns the debug information of this instance.
     *
     * @return    The debug description of this instance.
     */
    @Override
	public String toString () {
        return (this.getClass ().getName () + " [" +
                "user: " + this.getUserName () +
                ", login: " + this.login +
                ", logout: " + this.logout +
                ']');
    }

    /**
     * Returns the user of this instance.
     *
     * @return    The user (cannot be null).
     */
	public Person getUser() {
        return (this.user);
    }

    /**
     * Returns the name of the user of this instance.
     *
     * @return    The name of the user (cannot be null).
     */
    public String getUserName () {
		return (this.user.getName());
    }

    /**
     * Returns the login time of the user of this instance.
     *
     * @return    The login time (can be null).
     */
    public Date getLogin () {
        return (this.login);
    }

    /**
     * Returns the login time of the user of this instance.
     *
     * @return    The login time (can be an empty string).
     */
    public String getFormattedLogin () {
        if (this.login != null) {
            return (StringServices.getDatetimeFormat().format (this.login));
        }
        else {
            return ("");
        }
    }

    /**
     * Returns the logout time of the user of this instance.
     *
     * @return    The logout time (can be null).
     */
    public Date getLogout () {
        return (this.logout);
    }

    /**
     * Returns the logout time of the user of this instance.
     *
     * @return    The logout time (can be an empty string).
     */
    public String getFormattedLogout () {
        if (this.logout != null) {
            return (StringServices.getDatetimeFormat().format (this.logout));
        }
        else {
            return ("");
        }
    }

    /**
     * Checks, whether the logout time is set or not.
     *
     * @return    true, if the logout time is null.
     */
    public boolean isOpen () {
        return (this.logout == null);
    }

    /**
     * Returns the duration of the user in the system.
     *
     * The duration will be calculated by subtracting the logout time from
     * the login time. If one of that times is not available, the current
     * time will be used.
     *
     * @return    The duration in milliseconds.
     */
    public long getDuration () {
		// seems to happen for some reason...
        if (this.login == null) {
        	return 0;
        }

		Date theDate;

        if (this.logout == null) {
            theDate = new Date ();
        }
        else {
            theDate = this.logout;
        }

        return (theDate.getTime () - this.login.getTime ());
    }

    public String getFormattedDuration () {
        long theTime = this.getDuration ();

        if (theTime == 0) {
            return ("");
        }
        else {
            if (theTime > 60000) {
                return (Long.toString ((theTime / 60000)) + " min");
            }
            else {
                return (Long.toString ((theTime / 1000)) + " sec");
            }
        }
    }

    /**
     * Set the logout time of this instance.
     *
     * This method will return true only once. The other times false will
     * be returned. The logout time will only be set, if the date is 
     * not null and the logout time is later than the login time. If 
     * login time is specified, this criteria will be ignored.
     *
     * @param    aDate    The logout time of the user.
     * @return   true, if a valid date has been given first time.
     */
    public boolean setLogout (Date aDate) {
        boolean theResult = this.isOpen () && (aDate != null);

        if (theResult) {
            if (this.login == null || 
                    aDate.getTime () > this.login.getTime ()) {
                this.logout = aDate;
            }
            else {
                theResult = false;
            }
        }

        return (theResult);
    }
}

