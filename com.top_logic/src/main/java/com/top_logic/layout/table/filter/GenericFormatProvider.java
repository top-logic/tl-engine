/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.Format;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link FormatProvider} that inspects the format annotations on the column's
 * {@link ColumnInfo#TYPES}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericFormatProvider implements FormatProvider {

	/**
	 * Singleton {@link GenericFormatProvider} instance.
	 */
	public static final GenericFormatProvider INSTANCE = new GenericFormatProvider();

	private GenericFormatProvider() {
		// Singleton constructor.
	}

	@Override
	public Format getFormat(ColumnConfiguration column) {
		TLTypeContext contentType = column.get(ColumnInfo.TYPES);
		if (contentType != null) {
			return getFormat(contentType);
		}
		return null;
	}

	private Format getFormat(TLTypeContext typeContext) throws UnreachableAssertion {
		try {
			return getFormat((TLPrimitive) typeContext.getType(), typeContext);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	private Format getFormat(TLPrimitive primitiveType, AnnotationLookup modelPart)
			throws ConfigurationException, UnreachableAssertion {
		switch (primitiveType.getKind()) {
			case INT:
				return DisplayAnnotations.getLongFormat(modelPart);
			case FLOAT:
				return DisplayAnnotations.getFloatFormat(modelPart);
			case DATE:
				return DisplayAnnotations.getDateFormat(modelPart);
			default:
				throw new UnreachableAssertion("No formatted type: " + primitiveType);
		}
	}
}
