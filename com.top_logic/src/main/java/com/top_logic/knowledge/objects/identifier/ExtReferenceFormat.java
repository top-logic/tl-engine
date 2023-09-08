/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.identifier;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.ExtIDFormat;

/**
 * Format for {@link ExtReference}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExtReferenceFormat extends Format {

	private static final char TYPE_SEPARATOR = '#';

	private static final char BRANCH_SEPARATOR = '/';

	/** Singleton {@link ExtIDFormat} instance. */
	public static final ExtReferenceFormat INSTANCE = new ExtReferenceFormat();

	private ExtReferenceFormat() {
		// singleton instance
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof ExtReference)) {
			throw failInvalidType(obj);
		}
		ExtReference ref = (ExtReference) obj;
		toAppendTo.append(ref.getObjectType());
		toAppendTo.append(TYPE_SEPARATOR);
		toAppendTo.append(ref.getBranchId());
		toAppendTo.append(BRANCH_SEPARATOR);
		ExtIDFormat.INSTANCE.format(ref.getObjectName(), toAppendTo, pos);
		return toAppendTo;

	}

	private static RuntimeException failInvalidType(Object obj) {
		StringBuilder error = new StringBuilder();
		error.append("Only ");
		error.append(ExtReference.class.getName());
		error.append(" can be formatted. Given: ");
		error.append(obj);
		return new IllegalArgumentException(error.toString());
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		StringBuilder tmp = new StringBuilder();
		String type;
		int i = 0;
		while (true) {
			if (i == source.length()) {
				pos.setErrorIndex(i);
				return null;
			}
			char character = source.charAt(i);
			i++;
			if (character == TYPE_SEPARATOR) {
				type = tmp.toString();
				break;
			} else {
				tmp.append(character);
			}
		}
		tmp.setLength(0);
		long branch;
		while (true) {
			if (i == source.length()) {
				pos.setErrorIndex(i);
				return null;
			}
			char character = source.charAt(i);
			i++;
			if (character == BRANCH_SEPARATOR) {
				branch = Long.parseLong(tmp.toString());
				break;
			} else {
				if (Character.isDigit(character)) {
					tmp.append(character);
				} else {
					pos.setErrorIndex(i);
					return null;
				}
			}
		}
		pos.setIndex(i);
		ExtID extId = (ExtID) ExtIDFormat.INSTANCE.parseObject(source, pos);
		if (extId == null) {
			return null;
		}
		return new ExtReference(branch, type, extId);
	}

	/**
	 * Parses the given formatted {@link ExtReference}.
	 * 
	 * <p>
	 * Actually the same as {@link #parseObject(String)} but throwing an
	 * {@link IllegalArgumentException} instead of {@link ParseException} in case of error.
	 * </p>
	 * 
	 * @param extRef
	 *        A {@link #format(Object) formatted} {@link ExtReference}.
	 * 
	 * @see #parseObject(String)
	 */
	public ExtReference parseExtReference(String extRef) {
		ParsePosition pos = new ParsePosition(0);
		Object result = parseObject(extRef, pos);
		if (pos.getIndex() == 0) {
			throw new IllegalArgumentException("Format.parseObject(String) failed at position " + pos.getErrorIndex());
		}
		return (ExtReference) result;
	}

}

