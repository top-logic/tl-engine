/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItemUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.util.Utils;

/**
 * @author    <a href=mailto:tsa@top-logic.com>Theo Sattttteller</a>
 */
public class AssociationCreatedDeletedJournalEntryGenerator {

    public static Collection getEntries(Wrapper aWrapper, Map someChanged, Map someCreated, Map someRemoved) {
        Collection theEntries = new ArrayList();
		{
            KnowledgeObject theKO = aWrapper.tHandle();
            if (someCreated != null) {
                createMessageEntries(theKO, someCreated, theEntries, "created");
            }
            if (someRemoved != null) {
                createMessageEntries(theKO, someRemoved, theEntries, "removed");
            }
        }
        return theEntries;
        
    }

    private static void createMessageEntries(KnowledgeObject aKO, Map someObjects, Collection anEntries, String aCause) {
        Iterator theIt = someObjects.values().iterator();
        while (theIt.hasNext()) {
            Object theObject = theIt.next();
			if (KnowledgeItemUtil.instanceOfKnowledgeAssociation(theObject)) {
                KnowledgeAssociation theKA = (KnowledgeAssociation) theObject;
                try {
                    KnowledgeObject theSource = theKA.getSourceObject();
                    if (Utils.equals(theSource, aKO)) {
						anEntries.add(new MessageJournalAttributeEntryImpl("association." + theKA.tTable().getName(), theKA.tTable().getName(), aCause, IdentifierUtil.toExternalForm(theKA.getObjectName())));
                    }
                } catch (InvalidLinkException e) {
                    Logger.error("Encountered invalid association while journalling.", e, AssociationCreatedDeletedJournalEntryGenerator.class);
                }
            }
        }
    }
}
