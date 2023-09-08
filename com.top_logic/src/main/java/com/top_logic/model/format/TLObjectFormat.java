/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLObject;

/**
 * A {@link ConfigurationValueProvider} for {@link TLObject}s, that uses their {@link ObjectKey}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLObjectFormat extends AbstractConfigurationValueProvider<TLObject> {

	/** The {@link TLObjectFormat} instance. */
	public static final TLObjectFormat INSTANCE = new TLObjectFormat();

	private TLObjectFormat() {
		super(TLObject.class);
	}

	@Override
	protected TLObject getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		KnowledgeBase knowledgeBase = PersistencyLayer.getKnowledgeBase();
		ObjectKey key;
		try {
			key = ObjectKey.fromStringObjectKey(knowledgeBase.getMORepository(), propertyValue.toString());
		} catch (IllegalArgumentException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_CAN_NOT_PARSE_REFERENCE__REFERENCE.fill(propertyValue),
				propertyName, propertyValue, ex);
		} catch (UnknownTypeException ex) {
			throw new ConfigurationException(
				I18NConstants.ERROR_CAN_NOT_RESOLVE_STATIC_TYPE__REFERENCE.fill(propertyValue), propertyName,
				propertyValue, ex);
		}
		KnowledgeItem item = knowledgeBase.resolveObjectKey(key);
		if (item == null) {
			throw new ConfigurationException(
				I18NConstants.ERROR_CAN_NOT_RESOLVE_REFERENCE__REFERENCE.fill(propertyValue), propertyName,
				propertyValue);
		}
		return item.getWrapper();
	}

	@Override
	protected String getSpecificationNonNull(TLObject tlObject) {
		return tlObject.tHandle().tId().asString();
	}

}
