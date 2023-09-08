/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;

/**
 * Formatter for externalizing and reconstructing a wrapper.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WrapperIDFormatter extends Format {

    /** Singleton attribute for accessing this instance. */
    public static final Format INSTANCE = new WrapperIDFormatter();

    /** Separator for the different aspects. */
    private static final char SEPARATOR = '#';

    @Override
	public StringBuffer format(Object anObject, StringBuffer aBuffer, FieldPosition aPos) {
		{
            Wrapper         theWrapper = (Wrapper) anObject;
            KnowledgeObject theKO      = theWrapper.tHandle();
            String          theName    = IdentifierUtil.toExternalForm(theKO.getObjectName());
            String          theType    = theKO.tTable().getName();
			long theBranch = theKO.getBranchContext();
			long theRev = theKO.getHistoryContext();

            aBuffer.append(theName).append(SEPARATOR).append(theType).append(SEPARATOR).append(theBranch);

			if (Revision.CURRENT_REV != theRev) {
                aBuffer.append(SEPARATOR).append(theRev);
            }

            return aBuffer;
        }
    }

    @Override
	public Object parseObject(String aSource, ParsePosition aPos) {
        try {
            int      theNPos   = aSource.indexOf(SEPARATOR);
            int      theTPos   = aSource.indexOf(SEPARATOR, theNPos + 1);
            int      theBPos   = aSource.indexOf(SEPARATOR, theTPos + 1);
			TLID theName = IdentifierUtil.fromExternalForm(aSource.substring(0, theNPos));
            String   theType   = aSource.substring(theNPos + 1, theTPos);
			long theBranch = theBPos == -1 ? Long.parseLong(aSource.substring(theTPos + 1))
				: Long.parseLong(aSource.substring(theTPos + 1, theBPos));
			Revision revision =
				theBPos == -1 ? HistoryUtils.getRevision(Revision.CURRENT_REV) : HistoryUtils.getRevision(Long
					.parseLong(aSource.substring(theBPos + 1)));

            aPos.setIndex(aSource.length());

			return WrapperFactory.getWrapper(HistoryUtils.getBranch(theBranch),
                                                           revision, 
                                                           theName, 
                                                           theType);
        }
        catch (Exception ex) {
            Logger.error("Failed to get object from external representation " + aSource, ex, this);
        }

        return null;
    }
}
