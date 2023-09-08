
/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * Wrapper for
 * {@link com.top_logic.knowledge.objects.KnowledgeObject KnowledgeObjects}
 * of type Clipboard.
 *
 * The clipboard is a user oriented object, which ...
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Clipboard {

	/**
	 * The legacy name of the no longer existing persistent type for {@link Clipboard} objects.
	 */
    public static final String OBJECT_NAME = "Clipboard";

	private static final String HAS_CLIP_OBJECT_ASSOCIATION = "hasClipObject";

    /** Attribute of "hasClipObject" Association, Time when object was inserted */
    public static final String CLIPDATE		    = "clipDate";

    /** Attribute of "hasClipObject" Association, see {@link #CLIPMODE_COPY}, {@link #CLIPMODE_CUT} constants */
    public static final String CLIPMODE		    = "clipMode";
    
    /** Value of clipMode Attribute: Object was inserted by Cut-operation  */
    public static final String CLIPMODE_CUT	    = "CUT";

    /** Value of clipMode Attribute: Object was inserted by Copy-operation  */
    public static final String CLIPMODE_COPY	= "COPY";

	private static final AssociationSetQuery<KnowledgeAssociation> CONTENT =
		AssociationQuery.createOutgoingQuery("clipboardContent", HAS_CLIP_OBJECT_ASSOCIATION);

	private static final Clipboard INSTANCE = new Clipboard();

	protected Set<KnowledgeAssociation> getContentLinks() {
		return AbstractWrapper.resolveLinks(user(), CONTENT);
	}

	public Collection<? extends TLObject> getContent() {
		return AbstractWrapper.resolveWrappers(user(), CONTENT);
	}

    /** Variant of offical add() method to set the mode, too.
     *
     * One object can be added to the board only once, so if this method has
     * been called twice with the same object, it returns false.
     *
     * @param    newObject   The new object for the WebFolder.
     * @param    aMode      one of the {@link #CLIPMODE_COPY}, {@link #CLIPMODE_CUT} constants
     * @return   true,      if appending the object to WebFolder succeeds.
     */
	public boolean add(TLObject newObject, String aMode) {
		if (!this.contains(newObject)) {
            return (this._add (newObject, aMode));
        }
        else {
            if (Logger.isDebugEnabled (this)) {
				Logger.debug("Object '" + newObject + "' already in container!", this);
            }
        }
        return false;
    }
    
	public boolean contains(TLObject content) {
		return getContent().contains(content);
	}

    /**
     * Create a new association between the board and the given object.
     *
     * The method will set the attribute "clipDate" of the association to 
     * the current time and the given clipmode
     *
     * @param   newObject    The new object for the clipboard.
     * @param   aMode       one of the {@link #CLIPMODE_COPY}, {@link #CLIPMODE_CUT} constants
     * @return  true, if appending the object to clipboard succeeds.
     */
	protected boolean _add(TLObject newObject, String aMode) {
    	if (newObject == null) {
    		return false;
    	}

		KnowledgeObject thisKO = userKO();
		KnowledgeBase kb = thisKO.getKnowledgeBase();
		KnowledgeItem newObjectKO = newObject.tHandle();

		{
			KnowledgeAssociation link = kb.createAssociation(thisKO, newObjectKO, HAS_CLIP_OBJECT_ASSOCIATION);
			link.setAttributeValue(CLIPDATE, new Date());
			if (aMode != null) {
				link.setAttributeValue(CLIPMODE, aMode);
			}
		}

        return true;
    }

    /**
     * Create a new association between the board and the given object.
     *
     * The method will set the attribute "clipDate" of the association to 
     * the current time and the mode to "COPY".
     *
     * @param    newChild    The new object for the clipboard.
     * @return   true, if appending the object to clipboard succeeds.
     */
	public boolean add(TLObject newChild) {
		if (this.contains(newChild)) {
			return false;
		}

		return (this._add(newChild));
	}

	protected boolean _add(TLObject newChild) {
        return _add(newChild, CLIPMODE_COPY);
    }

    /**
     * Removes the association between the board and the given object.
     *
     * @param    oldChild    The object to be removed from the clipboard.
     * @return   true, if removing the object from clipboard succeeds.
     */
	public boolean remove(TLObject oldChild) {
		Iterator<KnowledgeAssociation> theIt =
			userKO().getOutgoingAssociations(HAS_CLIP_OBJECT_ASSOCIATION, (KnowledgeObject) oldChild.tHandle());
		return KBUtils.deleteAllKI(theIt);
    }

	public void clear() {
		Set<KnowledgeAssociation> contentLinks = getContentLinks();
		switch (contentLinks.size()) {
			case 0:
				break;
			case 1:
				contentLinks.iterator().next().delete();
				break;
			default:
				contentLinks.iterator().next().getKnowledgeBase().deleteAll(contentLinks);
				break;
		}
    }

    /** Return mode of first object (one of {@link #CLIPMODE_COPY}, {@link #CLIPMODE_CUT}):
     * 
     * @return null when there is no object at ll
     */
    public String getFirstMode() {
		Iterator<KnowledgeAssociation> theIt = userKO().
								getOutgoingAssociations(HAS_CLIP_OBJECT_ASSOCIATION);
        String result = null;
        if (theIt.hasNext()) {
			KnowledgeAssociation theAss = theIt.next();
            try {
				result = (String) theAss.getAttributeValue(CLIPMODE);
			} catch(NoSuchAttributeException nsax) {
                Logger.error("getFirstMode()", nsax, this);
			}
        }
        return result;
    }

    /**
     * Checks, whether the given association has the requested {@link #CLIPMODE}.
     * 
     * @param    anAss    The association to be inspected.
     * @param    aType    The clip mode of the association, null means all modes
     * @return   true, if the given type is <code>null</code> or the mode
     *           matches with the given one or there is no such attribute
     *           in the given association.
     */
    private boolean isClipMode (KnowledgeAssociation anAss, String aType) {
        if (aType == null) {
            return (true);
        }
        
        try {
            return aType.equals(anAss.getAttributeValue(CLIPMODE));
        } 
        catch(NoSuchAttributeException ex) {
            Logger.info("No matching clip mode found, returning true!", 
                        ex, this);

            return (true);
        }
    }

	/**
	 * Return Map of KOs contained in clipboard indexed by identifier.
	 * 
	 * This function should be used by Trees or tables when {@link #contains(TLObject)} is to
	 * expensive.
	 * 
	 * @param aType
	 *        One of the {@link #CLIPMODE_COPY}, {@link #CLIPMODE_CUT} constants or
	 *        <code>null</code> indicating all Types.
	 * @return Map of KO indexed by identifier
	 */
   public Map<Object,KnowledgeObject> getContained(String aType) {
       Map<Object,KnowledgeObject> result = new HashMap<>();

		for (Iterator<KnowledgeAssociation> theIt =
			userKO().getOutgoingAssociations(HAS_CLIP_OBJECT_ASSOCIATION); theIt.hasNext();) {
			KnowledgeAssociation theAss = theIt.next();

           if (this.isClipMode(theAss, aType)) {
               try {
                   KnowledgeObject theObject = theAss.getDestinationObject();
					result.put(theObject.getObjectName(), theObject);
               } 
               catch(InvalidLinkException ex) {
                   Logger.error("Unable to get destination of " + theAss, 
                                ex, this);
               }
           }
       }

       return result;
   }

	private KnowledgeObject userKO() {
		return (KnowledgeObject) user().tHandle();
	}

	private TLObject user() {
		return TLContext.getContext().getCurrentPersonWrapper();
	}

	/**
	 * Returns the clipboard of the user working in this thread.
	 *
	 * If there is no user, this method returns null.
	 *
	 * @param aBase
	 *        The knoeldgebase used to lookup the clipboard,
	 * 
	 * @return The clipboard of the current user in the given kBase, (or null in case there is no
	 *         current user)
	 */
	public static Clipboard getInstance(KnowledgeBase aBase) {
		return getInstance();
    }
    
    /**
     * Returns the clipboard of the user working in this thread.
     *
     * If there is no user, this method returns null.
     *
     * @return    The clipboard of the current user in the given kBase
     */
	public static Clipboard getInstance() {
		return INSTANCE;
    }
    
}
