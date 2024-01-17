/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manager for Reloadable objects.
 *
 * This manager allows to access all kind of Reloadable objects. There
 * are two kinds of those objects, the one with a name and the one without
 * a name.
 * 
 * <em>Known</em> Relodables have a name.
 * <em>Unknown</em> Relodables have a null or empty name. 
 * Unknown Reloadbles do not apper at the ui.
 * 
 * @author   <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ReloadableManager {

    /** The only instance of this class. */
    private static ReloadableManager singleton;

    /** The map holding all known reloadable modules. */
    private Map modules;

    /** The map holding all unknown reloadable modules. */
    private List list;

    /** Flag for the reload of XMLProperties. */
    private boolean alreadyLoaded;

    /**
     * Private constructor for singleton pattern.
     */
    private ReloadableManager () {
        super ();
    }

    /**
     * Returns the moduole with the given name.
     *
     * If there is no module with the given name, this method returns null.
     *
     * @param    aName    The name of the requested module.
     * @return   The requested module.
     */
    public Reloadable get (String aName) {
        return ((Reloadable) this.getMap ().get (aName));
    }

    /**
     * Send the reload message to all managed Reloadables.
     *
     * @return    The list of objects not been reloaded (who returned false).
     */
    public String [] reload () {
        this.alreadyLoaded = false;

        this._reloadUnknown ();

        return (this._reloadKnown ());
    }

    /**
     * Send the reload message to all known Reloadables.
     *
     * @return    The list of objects not been reloaded (who returned false).
     */
    public String [] reloadKnown () {
        this.alreadyLoaded = false;

        return (this._reloadKnown ());
    }

    /**
     * Send the reload message to the Reloadable with the given name.
     *
     * If there is no such Reloadable, false will be returned.
     *
     * @param    aName    The name of the object to be reloaded.
     * @return   true, if reloading fails.
     */
    public boolean reload (String aName) {
        this.alreadyLoaded = false;

        return (this._reload (aName));
    }

    /**
     * Send the reload message to all known Reloadables.
     *
     * @param    anArray    The name of the object to be reloaded.
     * @return   The list of objects not been reloaded (who returned false).
     */
    public String [] reloadKnown (String [] anArray) {
        this.alreadyLoaded = false;

        return (this._reloadKnown (anArray));
    }

    /**
	 * Send the reload message to all unknown {@link Reloadable}s.
	 *
	 * @return <code>0</code>, if everthing is OK, less than <code>0</code> if the reloads failed.
	 */
    public int reloadUnknown () {
        this.alreadyLoaded = false;

        return (this._reloadUnknown ());
    }

    /**
     * Returns the description for the reloadable with the given name.
     *
     * If there is no such reloadable, an empty string will be returned.
     *
     * @param    aName    The name of the Reloadable.
     * @return   The description of the Reloadable.
     */
    public String getDescription (String aName) {
        Reloadable theModule = (Reloadable) this.getMap ().get (aName);

        if (theModule != null) {
            return (theModule.getDescription ());
        }
        
        return ("");
    }

    /**
     * Returns an sorted array of the names of the known modules.
     *
     * @return    The array of the modules.
     */
    public String [] getKnown () {
        int       thePos   = 0;
        Set       theSet   = this.getMap ().keySet ();
        String [] theArray = new String [theSet.size ()];

        for (Iterator theIt = theSet.iterator (); theIt.hasNext (); ) {
            theArray [thePos++] = theIt.next ().toString ();
        }

        Arrays.sort (theArray);

        return (theArray);
    }

    /**
     * Appends the given Reloadable to the manager.
     *
     * Depending on the name of the Reloadable, it'll be placed in the map
     * of known objects or in the list of unknown objects. A Reloadable is
     * known, if its getName() method returns a none empty string.
     *
     * @param    aReloadable    The Reloadable to be appended.
     */
    public void addReloadable (Reloadable aReloadable) {
        String theName = aReloadable.getName ();

        if (StringServices.isEmpty (theName)) {
            this.getList ().add (aReloadable);
        }
        else {
            this.getMap ().put (theName, aReloadable);
        }
    }

    /**
     * Removes the given Reloadable from the manager.
     *
     * @param    aReloadable    The Reloadable to be removed.
     */
    public Object removeReloadable (Reloadable aReloadable) {
        Object theResult = null;
        List   theList   = this.getList ();

        if (theList.contains (aReloadable)) {
            if (theList.remove (aReloadable)) {
                theResult = aReloadable;
            }
        }
        else {
            theResult = this.getMap ().remove (aReloadable.getName ());
        }

        return (theResult);
    }

    /**
	 * Report, whether the given {@link Reloadable} is already registered with
	 * this {@link ReloadableManager}.
	 * 
	 * @param aReloadable
	 *     The {@link Reloadable} to test.
	 * @return
	 *     Whether the given {@link Reloadable} is registered with this manager. 
	 */
    public boolean isReloadableRegistered(Reloadable aReloadable) {
		String theName = aReloadable.getName();

		if (StringServices.isEmpty(theName)) {
			return this.getList().contains(aReloadable);
		} else {
			Object registeredReloadable = this.getMap().get(theName);
			return aReloadable == registeredReloadable;
		}
	}

    /**
	 * Send the reload message to all known {@link Reloadable}s.
	 *
	 * @return The list of objects not been reloaded (who returned false).
	 */
    protected String [] _reloadKnown () {
        String [] theArray;
        String    theKey;
        Map       theMap  = this.getMap ();
        int       len     = theMap.size();
        List      theList = new ArrayList (len);
        Iterator  theIt   = theMap.keySet ().iterator ();

        while (theIt.hasNext ()) {
            theKey = theIt.next ().toString ();

            if (!this._reload (theKey)) {
                theList.add (theKey);
            }
        }

        theArray = new String [theList.size ()];

        return ((String []) theList.toArray (theArray));
    }

    /**
     * Send the reload message to all known Reloadables.
     *
     * @param    anArray    The name of the object to be reloaded.
     * @return   The list of objects not been reloaded (who returned false).
     */
    protected String [] _reloadKnown (String [] anArray) {
        String [] theArray;
        String    theKey;
        int       len     = anArray.length;
        List      theList = new ArrayList (len);
        int       thePos  = 0;

        while (thePos < len) {
            theKey = anArray [thePos++];

            if (!this._reload (theKey)) {
                theList.add (theKey);
            }
        }

        theArray = new String [theList.size ()];

        return ((String []) theList.toArray (theArray));
    }

    /**
	 * Implementation of {@link #reloadUnknown()}.
	 */
    protected int _reloadUnknown () {
        int theResult = 0;

        for (Iterator theIt = this.getList ().iterator (); theIt.hasNext (); ) {
            if (!this._reload ((Reloadable) theIt.next ())) {
                theResult--;
            }
        }

        return (theResult);
    }

    /**
     * Send the reload message to the Reloadable with the given name.
     *
     * If there is no such Reloadable, false will be returned.
     *
     * @param    aName    The name of the object to be reloaded.
     * @return   true, if reloading fails.
     */
    protected boolean _reload (String aName) {
        return (this._reload ((Reloadable) this.getMap ().get (aName)));
    }

    /**
     * Send the reload message to the given Reloadable.
     *
     * If there is no such Reloadable, false will be returned.
     *
     * @param    anObject    The object to be reloaded.
     * @return   true, if reloading fails.
     */
    protected boolean _reload (Reloadable anObject) {
        try {
            if (anObject != null) {
                this.checkXMLProperties (anObject);

                return (anObject.reload ());
            }
        }
        catch (Exception ex) {
            Logger.info ("Unable to reload '" + anObject + '\'', ex, this);
        }
        return false;
    }

    /**
     * Check, whether the given object has used the XMLProperties.
     *
     * If the object uses the XMLProperties, this have also be reloaded.
     *
     * @param    anObject    The object to be inspected.
     */
    protected void checkXMLProperties (Reloadable anObject) {
        if (!this.alreadyLoaded) {
            if (anObject.usesXMLProperties ()) {
                this.loadXMLProperties ();
            }
        }
    }

    /**
     * Reload the XMLProperties.
     *
     * This method calls the reset() method in the XMLProperties and set
     * the alreadLoaded flag to true, to avoid multiple reloading of the
     * properties in one reload phase.
     */
    protected void loadXMLProperties () {
        try {
            XMLProperties.getInstance ().reset ();
        } catch (IOException iox) {
            Logger.error("Failed to loadXMLProperties()" , iox, this);
        }

        this.alreadyLoaded = true;
    }

    /**
     * Return the map containing all known modules.
     *
     * A module is known, if it returns a none empty string when calling
     * the getName() method. If it has no name, it'll be found in the module
     * list, which contains unknown modules.
     *
     * @return    The map with all modules.
     */
    private Map getMap () {
        if (this.modules == null) {
            this.modules = new HashMap ();
        }
        return (this.modules);
    }

    /**
     * Returns the list of unknown modules.
     *
     * A module is unknown, if it returns an empty string or <code>null</code>
     * when calling the getName() method. If it has a name, it'll be found in
     * the module map, which contains known modules.
     *
     * @return    The list of unknown modules.
     */
    private List getList () {
        if (this.list == null) {
            this.list = new ArrayList ();
        }

        return (this.list);
    }

    /**
     * Returns the only instance of this class.
     *
     * @return    The only instance of this class.
     */
    public static ReloadableManager getInstance () {
        if (singleton == null) {
            singleton = new ReloadableManager ();
        }

        return (singleton);
    }
}
