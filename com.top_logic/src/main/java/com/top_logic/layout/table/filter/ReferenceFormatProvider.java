/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.Format;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link FormatProvider}, that creates a {@link Format} out of a given format reference (e.g.
 * 'percent').
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ReferenceFormatProvider implements FormatProvider {

	private String _formatReference;
	private String _referenceSource;

	/**
	 * Create a new {@link ReferenceFormatProvider}.
	 */
	public ReferenceFormatProvider(String formatReference) {
		_formatReference = formatReference;
		_referenceSource = null;
	}
	
	/**
	 * @param referenceSource
	 *        from where this reference was taken (e.g. xml location).
	 */
	public void setReferenceSource(String referenceSource) {
		_referenceSource = referenceSource;
	}

	@Override
	public Format getFormat(ColumnConfiguration column) {
		Format format = HTMLFormatter.getInstance().getFormat(_formatReference);
		if (format != null) {
			return format;
		} else {
			handleGlobalFormatNotFound();
		}
		return null;
	}

	private void handleGlobalFormatNotFound() {
		StringBuilder error = new StringBuilder();
		error.append("No global format found for given reference '");
		error.append(_formatReference);
		error.append("' configured for table filter");
		if (!StringServices.isEmpty(_referenceSource)) {
			error.append(" in '");
			error.append(_referenceSource);
			error.append("'");
		}
		error.append(".");
		Logger.warn(error.toString(), ReferenceFormatProvider.class);
	}

}
