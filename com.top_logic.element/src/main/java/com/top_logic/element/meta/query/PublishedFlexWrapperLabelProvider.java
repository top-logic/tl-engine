/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.util.TLContext;

/**
 * A {@link LabelProvider} for {@link StoredQuery}s. Depending on the current
 * user and the owner of the {@link StoredQuery} additional information is added
 * to the provided label.
 *
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PublishedFlexWrapperLabelProvider implements LabelProvider {

	private String PUBLISHED_MARKER = "* ";

	public static final PublishedFlexWrapperLabelProvider INSTANCE = new PublishedFlexWrapperLabelProvider();

	private PublishedFlexWrapperLabelProvider() {
		super();
    }

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return "----";
		}

		String theLabel = MetaLabelProvider.INSTANCE.getLabel(object);

		if (object instanceof Wrapper) {
			{
				Wrapper theWrapper = (Wrapper) object;
			    Person      theUser    = TLContext.getContext().getCurrentPersonWrapper();
			    Person      theCreator = theWrapper.getCreator();
    			if (!WrapperHistoryUtils.getUnversionedIdentity(theUser).equals(WrapperHistoryUtils.getUnversionedIdentity(theCreator))) {
					if (TLContext.isAdmin() || Person.isAdmin(theUser)) {
    				    String theSuffix = "";
    				    if (theCreator.wasAlive()) {
							theSuffix = " (" + theCreator.getLastName() + ", " + theCreator.getFirstName() + ")";
    				    }
    				    else {
    				        theSuffix = " (" + theCreator.getName() + ")";
    				    }
    				    theLabel += theSuffix;
    				}
    				else {
    					theLabel = PUBLISHED_MARKER + theLabel;
    				}
    			}
			}
		}

		return theLabel;
	}

	public String getMarker() {
    	return (PUBLISHED_MARKER);
    }

	public void setMarker(String aMarker) {
    	PUBLISHED_MARKER = aMarker;
    }
}
