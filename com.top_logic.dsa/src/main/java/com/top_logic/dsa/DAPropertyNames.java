/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa;

/**
 * Names of the constants of the Attributes returned by
 * {@link com.top_logic.dsa.DataAccessProxy#getProperties()}.
 *
 * @author  Klaus Halfmann
 */
public interface DAPropertyNames {

    /** Type: Long, the length of the underlying data
     *  {@link com.top_logic.dsa.DataAccessProxy#getEntry()},
     *   if available. This value should not be &lt; 0
     */
    public static String SIZE           = "size";

    /** Type: Long, Milliseconds (as used by Java) when
     *      underlying data was last changed 
     *   if available. This value should not be &lt; 0
     */
    public static String LAST_MODIFIED   = "lastModified";

    /** Type: Boolean, writing of data will faile when false
     */
    public static String IS_READABLE     = "isReadable";

    /** Type: Boolean, writing of data will faile when false
     */
    public static String IS_WRITEABLE    = "isWriteable";

    /** Type: Boolean, same as 
     * {@link com.top_logic.dsa.DataAccessProxy#isEntry() }
     */
    public static String IS_ENTRY        = "isEntry";

    /** Type: Boolean, same as 
     * {@link com.top_logic.dsa.DataAccessProxy#isContainer }
     */
    public static String IS_CONTAINER    = "isContainer";

    /** Type: Boolean, same as 
     * {@link com.top_logic.dsa.DataAccessProxy#exists }
     */
    public static String EXISTS             = "exists";

    /** Type: String, same as 
     * {@link com.top_logic.dsa.DataAccessProxy#getName }
     */
    public static String NAME               = "name";

    /** Type: Integer, Indicates number of versions for this file.
     */
    public static String NUM_VERSIONS      = "numVersions";

    /** Type: Boolean, Indicates that revision is locked by a user.
     */
    public static String LOCKED             = "locked";

    /** Type: Boolean, Indicates that revision is locked by a user.
     */
    public static String DELETED            = "deleted";

    /** Type: String, User that last changed the data
     */
    public static String AUTHOR            = "author";

    /** Type: String, Person that currently locks the entry.
     *          only expected when isLocked is true.
     */
    public static String LOCKER            = "locker";

}
