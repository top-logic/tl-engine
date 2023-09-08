/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * Annotation to configure a {@link PDFRenderer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("pdf-renderer")
public interface PDFRendererAnnotation extends TLTypeAnnotation, TLAttributeAnnotation {

	/**
	 * {@link PDFRenderer} to use.
	 */
	@Mandatory
	PolymorphicConfiguration<PDFRenderer> getImpl();

}

