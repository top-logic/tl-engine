/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Set;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Abstract immutable implementation of {@link DynamicAttributedObject} which allows to read to
 * dynamic attribute values but not to change them.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class ImmutableKnowledgeObjectBase extends AbstractImmutableKnowledgeObject implements
		DynamicAttributedObject {

	private FlexData _globalDynamicValues;

	public ImmutableKnowledgeObjectBase(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(kb, type);
	}

	@Override
	public Object getAttributeValue(String attributeName, long revision) throws NoSuchAttributeException {
		if (isStaticAttribute(attributeName)) {
			try {
				return super.getAttributeValue(attributeName, revision);
			} catch (NoSuchAttributeException ex) {
				throw new UnreachableAssertion("Attribute '" + attributeName + "' is a KO attribute", ex);
			}
		} else {
			return getDynamicValue(attributeName);
		}
	}

	@Override
	public boolean needsToBeLoaded(long revision) {
		// value does not depend on revision, because this item is immutable
		return getGlobalDynamicValues() == null;
	}

	@Override
	public synchronized void initDynamicValues(long dataRevision, FlexData values) {
		// value does not depend on revision, because this item is immutable
		if (_globalDynamicValues == null) {
			_globalDynamicValues = values;
		}
	}

	private synchronized FlexData getGlobalDynamicValues() {
		return _globalDynamicValues;
	}

	private synchronized FlexData lookupPersistentState() {
		FlexData cachedGlobalState = _globalDynamicValues;
		if (cachedGlobalState != null) {
			return cachedGlobalState;
		}
		_globalDynamicValues = getFlexDataManager().load(getKnowledgeBase(), tId(), false);
		return _globalDynamicValues;
	}

	private Object getDynamicValue(String dynamicAttributeName) {
		FlexData attributeData = this.lookupPersistentState();
		if (!attributeData.hasAttribute(dynamicAttributeName)) {
			return null;
		}
		try {
			return attributeData.getAttributeValue(dynamicAttributeName);
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion("Data has attribute '" + dynamicAttributeName + "'", ex);
		}
	}

	@Override
	public Set<String> getAllAttributeNames() {
		Set<String> staticAttributes = super.getAllAttributeNames();
		FlexData dataObjectForRead = lookupPersistentState();
		staticAttributes.addAll(dataObjectForRead.getAttributes());
		return staticAttributes;
	}


}

