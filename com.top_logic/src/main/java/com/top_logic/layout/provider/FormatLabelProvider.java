/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.text.Format;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} that delegates to a {@link Format}.
 * 
 * <p>
 * Note: Since {@link Format} is not guaranteed to be thread-safe, a
 * {@link FormatLabelProvider} must be used in at least thread-local fashion.
 * Otherwise, the creator is responsible to use it only with thread-safe
 * {@link Format} implementation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormatLabelProvider implements LabelProvider {

    private Format format;

	/**
	 * Creates a {@link FormatLabelProvider}.
	 * 
	 * @param format
	 *        The {@link Format} to delegate {@link #getLabel(Object)} calls to.
	 *        
	 * @see Format#format(Object)
	 */
    public FormatLabelProvider(Format format) {
        this.format = format;
    }

    @Override
	public String getLabel(Object anObject) {
        if (anObject == null) {
            return null;
        }
        else {
			return this.format.format(anObject);
        }
    }
}