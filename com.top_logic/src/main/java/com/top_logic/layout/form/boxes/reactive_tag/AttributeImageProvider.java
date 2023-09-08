/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * An {@link ImageProvider} to get a {@link ThemeImage} for a given {@link TLClassPart}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class AttributeImageProvider implements ImageProvider {

	private TLTypePart _attribute;

	/**
	 * Sets the {@link TLClassPart} to get the {@link ThemeImage} for.
	 */
	public AttributeImageProvider(TLTypePart attribute) {
		_attribute = attribute;
	}

	@Override
	public ThemeImage getImage(Object obj, Flavor flavor) {
		TLTypePart attribute = _attribute;

		if (attribute.getType() instanceof TLPrimitive) {
			Kind kind = ((TLPrimitive) attribute.getType()).getKind();

			switch (kind) {
				case BINARY:
					return Icons.FORM_EDITOR__BINARY;
				case BOOLEAN:
				case TRISTATE:
					return Icons.FORM_EDITOR__BOOLEAN;
				case CUSTOM:
					return Icons.FORM_EDITOR__CUSTOM;
				case DATE:
					return Icons.FORM_EDITOR__DATE;
				case FLOAT:
					return Icons.FORM_EDITOR__FLOAT;
				case INT:
					return Icons.FORM_EDITOR__INT;
				case STRING:
					if (isMultiline()) {
						return Icons.FORM_EDITOR__STRING_MULTILINE;
					} else {
						return Icons.FORM_EDITOR__STRING;
					}
				default:
					return Icons.FORM_EDITOR__NO_ICON;
			}
		} else {
			return Icons.FORM_EDITOR__REFERENCE;
		}
	}

	private boolean isMultiline() {
		MultiLine annotation = TLAnnotations.getAnnotation(_attribute, MultiLine.class);
		if (annotation == null) {
			return false;
		}
		return annotation.getValue();
	}
}