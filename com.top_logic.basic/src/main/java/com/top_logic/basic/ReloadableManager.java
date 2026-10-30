/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;

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
     * @return   true, if reloading succeeds.
     */
    public boolean reload (String aName) {
        this.alreadyLoaded = false;

        return (this._reload (aName));
    }

    /**
     * Send the reload message to all known Reloadables.
     *
     * @param    anArray    The name of the object to be reloaded.
     * @return   The list of objects not been reloaded (who returned false, failed or are unknown).
     *
     * @see #reloadKnownReporting(Collection)
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
	 * Sends the reload message to the known {@link Reloadable}s with the given names and reports
	 * why each failing one could not be reloaded.
	 *
	 * <p>
	 * A failing {@link Reloadable} does not keep the others from being reloaded. A failure is
	 * written to the log.
	 * </p>
	 *
	 * @param names
	 *        The names of the {@link Reloadable}s to reload, in the order to reload them.
	 * @return For each name whose {@link Reloadable} could not be reloaded, the
	 *         {@link ReloadFailure} telling why, in the order of the given names. A name no
	 *         {@link Reloadable} is registered under counts as failed. Empty, if all were reloaded.
	 */
	public Map<String, ReloadFailure> reloadKnownReporting(Collection<String> names) {
		this.alreadyLoaded = false;

		return _reloadKnownReporting(names);
	}

	/**
	 * Implementation of {@link #reloadKnownReporting(Collection)}.
	 */
	private Map<String, ReloadFailure> _reloadKnownReporting(Collection<String> names) {
		Map<String, ReloadFailure> failures = new LinkedHashMap<>();
		for (String name : names) {
			ReloadFailure failure = tryReload(name, (Reloadable) this.getMap().get(name));
			if (failure != null) {
				failures.put(name, failure);
			}
		}
		return failures;
	}

    /**
	 * Send the reload message to all known {@link Reloadable}s.
	 *
	 * @return The list of objects not been reloaded (who returned false).
	 */
    protected String [] _reloadKnown () {
		return _reloadKnown(getKnown());
    }

    /**
     * Send the reload message to all known Reloadables.
     *
     * @param    anArray    The name of the object to be reloaded.
     * @return   The list of objects not been reloaded (who returned false).
     */
    protected String [] _reloadKnown (String [] anArray) {
		return _reloadKnownReporting(Arrays.asList(anArray)).keySet().toArray(new String[0]);
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
     * @return   true, if reloading succeeds.
     */
    protected boolean _reload (String aName) {
		return _reloadKnownReporting(Collections.singletonList(aName)).isEmpty();
    }

    /**
     * Send the reload message to the given Reloadable.
     *
     * If there is no such Reloadable, false will be returned.
     *
     * @param    anObject    The object to be reloaded.
     * @return   true, if reloading succeeds.
     */
    protected boolean _reload (Reloadable anObject) {
		return tryReload(anObject, anObject) == null;
    }

	/**
	 * Sends the reload message to the given {@link Reloadable} and logs a failure.
	 *
	 * @param name
	 *        How the {@link Reloadable} is named in the log.
	 * @param reloadable
	 *        The {@link Reloadable} to reload, <code>null</code> if none is registered under the
	 *        given name.
	 * @return Why the {@link Reloadable} could not be reloaded, <code>null</code> if it was
	 *         reloaded.
	 */
	private ReloadFailure tryReload(Object name, Reloadable reloadable) {
		if (reloadable == null) {
			return new ReloadFailure(I18NConstants.ERROR_RELOADABLE_UNKNOWN__NAME.fill(name), null);
		}
		try {
			this.checkXMLProperties(reloadable);

			if (reloadable.reload()) {
				return null;
			}
			Logger.error("Reload of '" + name + "' returned false.", ReloadableManager.class);
			return new ReloadFailure(I18NConstants.ERROR_RELOAD_REFUSED, null);
		} catch (Exception ex) {
			Logger.error("Unable to reload '" + name + "'.", ex, ReloadableManager.class);
			return new ReloadFailure(reason(ex), ex);
		}
	}

	private static ResKey reason(Exception ex) {
		if (ex instanceof I18NFailure failure) {
			return failure.getErrorKey();
		}
		String message = ex.getMessage();
		return I18NConstants.ERROR_RELOAD_FAILED__MESSAGE
			.fill(StringServices.isEmpty(message) ? ex.getClass().getName() : message);
	}

	/**
	 * Why a {@link Reloadable} could not be reloaded.
	 *
	 * @param reason
	 *        What the administrator reads as reason of the failure.
	 * @param cause
	 *        The exception the reload failed with, <code>null</code> if the {@link Reloadable}
	 *        refused the reload or none is registered under the given name.
	 *
	 * @see ReloadableManager#reloadKnownReporting(Collection)
	 */
	public record ReloadFailure(ResKey reason, Throwable cause) {
		// Pure value.
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
