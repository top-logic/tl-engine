/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.basic.SessionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ReferenceManager {

	private static final Property<ReferenceManager> REFERENCE =
		TypedAnnotatable.property(ReferenceManager.class, "referenceManager");

    private final BidiHashMap map;

	private int nextId;

    /** 
     * Creates a {@link ReferenceManager}.
     */
    protected ReferenceManager() {
        this.map      = new BidiHashMap();
		this.nextId = SecureRandomService.getInstance().getRandom().nextInt(10000);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " [map: " + this.map.size()
                + ", idSource: " + this.nextId
                + ']';
    }

    /** 
     * Return a unique reference for the given source.
     * 
     * @param    aSource    The source to get a reference for, must not be <code>null</code>.
     * @return   The requested unique reference, never <code>null</code>.
     */
	public synchronized String getReference(String aSource) {
        if (StringServices.isEmpty(aSource)) {
			throw new IllegalArgumentException("Internal reference must not be empty.");
        }
        else {
            String theKey = (String) this.map.getKey(aSource);

            if (theKey == null) {
				theKey = Integer.toString(nextId++);
                
                this.map.put(theKey, aSource); 
            }

            return theKey;
        }
    }
    
    /**
	 * Return the source for the given reference.
	 * 
	 * @param aReference
	 *        The reference to be resolved, must not be <code>null</code>.
	 * @return The requested source, never <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If the given reference is empty or undefined.
	 */
    public String getSource(String aReference) throws IllegalArgumentException {
        if (StringServices.isEmpty(aReference)) {
			throw new IllegalArgumentException("External reference must not be empty.");
        }
        else {
            String result = (String) this.map.get(aReference);
            if (result == null) {
                throw new IllegalArgumentException("Undefined reference '" + aReference + "'.");
            }
            return result;
        }
    }

    /**
	 * Return the instance for the current session.
	 * 
	 * @return The requested manager, never <code>null</code>.
	 * @throws IllegalStateException
	 *         If there is no valid session.
	 */
    public static ReferenceManager getSessionInstance() throws IllegalStateException {
		SessionContext session = ThreadContextManager.getSession();
		if (session == null) {
			throw new IllegalStateException("No session.");
		}
		
		ReferenceManager referenceManager = session.get(ReferenceManager.REFERENCE);
		if (referenceManager == null) {
			// Ensure that only one thread installs the manager.
			synchronized (session) {
				referenceManager = session.get(ReferenceManager.REFERENCE);
				if (referenceManager == null) {
					referenceManager = new ReferenceManager();
					session.set(ReferenceManager.REFERENCE, referenceManager);
				}
			}
        }

		return referenceManager;
    }
}
