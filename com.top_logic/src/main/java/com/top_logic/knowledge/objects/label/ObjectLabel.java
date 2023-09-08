/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.label;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Label assignment to a persistent object.
 * 
 * @see #createLabel(String, KnowledgeObject)
 * @see #getLabeledObject(String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectLabel {

	private static final String KO_TYPE = "ObjectLabel";

	private static final String LABEL_ATTR = "label";

	private static final String TARGET_ATTR = "target";

	/**
	 * Add a label to the given object.
	 * 
	 * @param label
	 *        The assigned label (must be unique).
	 * @param object
	 *        The object to label.
	 */
	public static final void createLabel(String label, KnowledgeObject object) {
		{
			KnowledgeObject tag = PersistencyLayer.getKnowledgeBase().createKnowledgeObject(KO_TYPE);
			tag.setAttributeValue(LABEL_ATTR, label);
			tag.setAttributeValue(TARGET_ATTR, object);
		}
	}

	/**
	 * Lookup an object with a certain label.
	 * 
	 * @param label
	 *        The label to resolve.
	 * @return The labeled object, or <code>null</code>, if no object caries the given label.
	 */
	public static final KnowledgeObject getLabeledObject(String label) {
		DataObject tag = PersistencyLayer.getKnowledgeBase().getObjectByAttribute(KO_TYPE, LABEL_ATTR, label);
		if (tag == null) {
			return null;
		}
		try {
			final KnowledgeObject result = (KnowledgeObject) tag.getAttributeValue(TARGET_ATTR);
			return result;
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
	}
}
