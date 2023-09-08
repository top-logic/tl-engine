/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * {@link AttributeValueBinding} for a {@link TLClassifier} attribute.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClassifierAttributeValueBinding extends AbstractPrimitiveAttributeValueBinding<Object> {

	/**
	 * Singleton {@link ClassifierAttributeValueBinding} instance.
	 */
	public static final ClassifierAttributeValueBinding INSTANCE = new ClassifierAttributeValueBinding();

	private ClassifierAttributeValueBinding() {
		// Singleton constructor.
	}

	@Override
	protected Object parseValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute, String rawValue) {
		TLType type = attribute.getType();
		TLEnumeration enumeration = (TLEnumeration) type;
		if (attribute.isMultiple()) {
			ArrayList<TLClassifier> result = new ArrayList<>();
			for (String name : rawValue.split("\\s,\\s")) {
				TLClassifier classifier = enumeration.getClassifier(name);
				if (classifier == null) {
					log.error(in.getLocation(),
						I18NConstants.NO_SUCH_CLASSIFIER__NAME_ATTR_ENUM.fill(name, attribute, enumeration));
				} else {
					result.add(classifier);
				}
			}
			return result;
		} else {
			TLClassifier classifier = enumeration.getClassifier(rawValue);
			if (classifier == null) {
				log.error(in.getLocation(),
					I18NConstants.NO_SUCH_CLASSIFIER__NAME_ATTR_ENUM.fill(rawValue, attribute, enumeration));
				return enumeration.getClassifiers().get(0);
			}
			return classifier;
		}
	}

	@Override
	protected String serializeValue(TLStructuredTypePart attribute, Object value) {
		if (value instanceof Collection<?>) {
			return ((Collection<?>) value).stream().map(entry -> ((TLClassifier) entry).getName())
				.collect(Collectors.joining(","));
		} else {
			return ((TLClassifier) value).getName();
		}
	}


}
