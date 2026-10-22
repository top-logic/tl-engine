/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that limits the size of the files uploaded to a binary attribute.
 *
 * <p>
 * The limit applies to each file on its own: several files uploaded at once to a multi-valued
 * attribute each may have this size. The application-wide maximum upload size of the upload
 * configuration still caps it, so where both are set, the smaller one applies.
 * </p>
 */
@TagName(TLMaxUploadSize.TAG_NAME)
@TargetType(TLTypeKind.BINARY)
@InApp
@Label("Maximum upload size")
public interface TLMaxUploadSize extends TLAttributeAnnotation, TLTypeAnnotation {

	/** The shortcut XML tag for this {@link ConfigurationItem} type. */
	String TAG_NAME = "max-upload-size";

	/** Configuration name of {@link #getValue()}. */
	String VALUE = "value";

	/**
	 * The maximum size of one uploaded file, e.g. <code>5m</code> for 5 megabytes.
	 *
	 * <p>
	 * A value of <code>0</code> sets no limit for the attribute, only the application-wide one
	 * applies.
	 * </p>
	 */
	@Name(VALUE)
	@Mandatory
	@Format(MemorySizeFormat.class)
	long getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(long value);

}
