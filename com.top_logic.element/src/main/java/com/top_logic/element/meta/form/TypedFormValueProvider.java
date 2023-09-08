/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ValueProvider} that looks up values from {@link FormField}s in a {@link FormGroup} for
 * {@link TLStructuredTypePart#getName() meta attribute names} that are identified the
 * {@link TLStructuredTypePart} (identifier by their corresponding GUI-ID as defined by
 * {@link MetaAttributeGUIHelper#getAttributeID(TLStructuredTypePart, com.top_logic.model.TLObject)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypedFormValueProvider implements ValueProvider {

	private final FormGroup group;
	private final TLClass type;

	/**
	 * Creates a new {@link TypedFormValueProvider} that uses attributes of the
	 * given {@link TLClass} to translate access to the given
	 * {@link FormGroup}.
	 * 
	 * @param type
	 *        The type for which access keys are translated.
	 * @param group
	 *        The group in which field values are looked up.
	 */
	public TypedFormValueProvider(TLClass type, FormGroup group) {
		this.type = type;
		this.group = group;
	}

	@Override
	public Object getValue(String key) {
		try {
			TLStructuredTypePart metaAttribute = MetaElementUtil.getMetaAttribute(type, key);
			FormField field = group.getField(MetaAttributeGUIHelper.getAttributeIDCreate(metaAttribute, null));
			if (! field.hasValue()) {
				return null;
			}
			Object value = field.getValue();

			// TODO BHU: Quirks to work around incompatible values of single
			// select fields and meta attributes with single reference type.
			switch (AttributeOperations.getMetaAttributeType(metaAttribute)) {
				case LegacyTypeCodes.TYPE_SINGLE_REFERENCE:
			case LegacyTypeCodes.TYPE_SINGLEWRAPPER:
			case LegacyTypeCodes.TYPE_WRAPPER:
				if (field instanceof SelectField) {
					value = CollectionUtil.getSingleValueFromCollection((Collection<?>) value);
				}
			}
			
			return value;
		} catch (NoSuchAttributeException e) {
			return null;
		}
	}

	@Override
	public void setValue(String name, Object value) {
		throw new UnsupportedOperationException();
	}

}
