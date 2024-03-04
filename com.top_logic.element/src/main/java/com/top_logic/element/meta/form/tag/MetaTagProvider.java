/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * The {@link MetaTagProvider} creates tags to show {@link TLStructuredTypePart}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MetaTagProvider {

	/**
	 * This method creates an input tag to show the value of a
	 * {@link TLStructuredTypePart} of an {@link Wrapper}.
	 * 
	 * @param aMetaAttributeName
	 *            the name of the {@link TLStructuredTypePart} to get an input tag for.
	 * @param anAttributed
	 *            the holder of the value for the {@link TLStructuredTypePart}.
	 * @param aParentTag
	 *            the parent of the tag to build
	 * @return may return <code>null</code>.
	 */
	public Tag createInputTag(String aMetaAttributeName, Wrapper anAttributed, Tag aParentTag);

	/**
	 * This method creates a label tag for the value of a {@link TLStructuredTypePart}
	 * of an {@link Wrapper}.
	 * 
	 * @param aMetaAttributeName
	 *            the name of the {@link TLStructuredTypePart} to get a label tag for.
	 * @param anAttributed
	 *            the holder of the value for the {@link TLStructuredTypePart}.
	 * @param aParentTag
	 *            the parent of the tag to build
	 * @return may return <code>null</code>.
	 */
	public Tag createLabelTag(String aMetaAttributeName, Wrapper anAttributed, Tag aParentTag);

	/**
	 * This method creates an error tag for a {@link TLStructuredTypePart} of an
	 * {@link Wrapper}.
	 * 
	 * @param aMetaAttributeName
	 *            the name of the {@link TLStructuredTypePart} to get an error tag for.
	 * @param anAttributed
	 *            the holder of the value for the {@link TLStructuredTypePart}.
	 * @param aParentTag
	 *            the parent of the tag to build
	 * @return may return <code>null</code>.
	 */
	public Tag createErrorTag(String aMetaAttributeName, Wrapper anAttributed, Tag aParentTag);

}
