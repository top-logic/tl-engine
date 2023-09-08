/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
/**
 * {@link AttributeValueBinding} for {@link TLModelPart} references using their qualified names.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelPartReferenceValueBinding extends AbstractPrimitiveAttributeValueBinding<TLModelPart> {

	/**
	 * Singleton {@link ModelPartReferenceValueBinding} instance.
	 */
	public static final ModelPartReferenceValueBinding INSTANCE = new ModelPartReferenceValueBinding();

	private ModelPartReferenceValueBinding() {
		// Singleton constructor.
	}

	@Override
	protected TLModelPart parseValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute, String rawValue) {
		try {
			return (TLModelPart) TLModelUtil.resolveQualifiedName(attribute.getModel(), rawValue);
		} catch (TopLogicException ex) {
			log.error(in.getLocation(),
				I18NConstants.CANNOT_RESOLVE_MODEL_PART__REF__ATTR_DETAILS.fill(rawValue, attribute, ex.getErrorKey()));
			return null;
		}
	}

	@Override
	protected String serializeValue(TLStructuredTypePart attribute, TLModelPart value) {
		return TLModelUtil.qualifiedName(value);
	}

}
