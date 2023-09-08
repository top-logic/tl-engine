/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.journal.JournalEntry;
import com.top_logic.knowledge.journal.JournalEntryImpl;
import com.top_logic.knowledge.journal.JournalLine;
import com.top_logic.knowledge.journal.JournalManager;
import com.top_logic.knowledge.journal.Journallable;
import com.top_logic.knowledge.journal.MessageJournalAttributeEntryImpl;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.Resources;

/**
 * Helper to journal Document events
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class DocumentJournalSupport {

    /**
     * Only static methods
     */
    private DocumentJournalSupport() {
        super();
    }
    
    /**
     * Journal a Document event
     * 
     * @param aDoc          the Document. If it is <code>null</code> nothing will be journaled
     * @param aJournallable the Journallable. If it is <code>null</code> the method tries to resolve it
     *                      Has to be a Wrapper.
     * @param aType         the event type. Used for I18N.
     * @param aVersion      the Version. If it is empty or <code>null</code> the current version will be used
     */
    public static void journalDocumentEvent(Document aDoc, Object aJournallable, String aType, String aVersion) {
        if (aDoc == null) {
            return;
        }

        try {
            if (StringServices.isEmpty(aVersion)) {
                aVersion = aDoc.getLatestVersionNumber();
            }
            if(!StringServices.isEmpty(aVersion)){
				aVersion = Resources.getInstance().getString(I18NConstants.DOCUMENT_VERSION) + " " + aVersion;
            }
            
            if (aJournallable == null) {
                ContainerWrapper theContainer = WebFolder.getContainer(aDoc);

                if (theContainer instanceof AbstractBoundWrapper) {
                    aJournallable = ((AbstractBoundWrapper) theContainer).getSecurityParent();
                }
            }
            
            if (aJournallable instanceof Wrapper) {
                String    theDocName   = aDoc.getName();
                String    theMOName    = ((Wrapper)aJournallable).tHandle().tTable().getName();
                if (aJournallable instanceof Journallable && JournalManager.getInstance().isToJournal(theMOName)) {
                    JournalEntry theEntry  = new JournalEntryImpl(KBUtils.getWrappedObjectName(((Wrapper)aJournallable)), theMOName, 1);
                    String       theDocMsg = Resources.getInstance().getString(I18NConstants.DOCUMENT_EVENT_TYPES.key(aType)) + " " + theDocName + " " + aVersion;
                    theEntry.addAttribute(new MessageJournalAttributeEntryImpl(theDocMsg, theMOName, "read", theDocMsg));
                    
                    JournalLine  theLine   = new JournalLine(PersonManager.getManager().getCurrentPerson().getName(), 1);
                    theLine.add(theEntry);
                    
                    JournalManager.getInstance().journal(theLine);
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to journal document download event", e, DocumentJournalSupport.class);
        }

    }

    
}
