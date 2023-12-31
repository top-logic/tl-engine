/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.model.impl;

/**
 * Basic interface for {@link #PAGE_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface PageBase extends com.top_logic.element.structured.StructuredElement {

	/**
	 * Name of type <code>Page</code>
	 */
	String PAGE_TYPE = "Page";

	/**
	 * Part <code>content</code> of <code>Page</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model.i18n:I18NHtml</code> in configuration.
	 * </p>
	 */
	String CONTENT_ATTR = "content";

	/**
	 * Part <code>importSource</code> of <code>Page</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:String</code> in configuration.
	 * </p>
	 */
	String IMPORT_SOURCE_ATTR = "importSource";

	/**
	 * Part <code>name</code> of <code>Page</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:String</code> in configuration.
	 * </p>
	 */
	String NAME_ATTR = "name";

	/**
	 * Part <code>position</code> of <code>Page</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:Integer</code> in configuration.
	 * </p>
	 */
	String POSITION_ATTR = "position";

	/**
	 * Part <code>title</code> of <code>Page</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model.i18n:I18NString</code> in configuration.
	 * </p>
	 */
	String TITLE_ATTR = "title";

	/**
	 * Part <code>uuid</code> of <code>Page</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:String</code> in configuration.
	 * </p>
	 */
	String UUID_ATTR = "uuid";

	/**
	 * Getter for part {@link #CHILDREN_ATTR}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	default java.util.List<? extends com.top_logic.doc.model.Page> getChildren() {
		return (java.util.List<? extends com.top_logic.doc.model.Page>) tValueByName(CHILDREN_ATTR);
	}

	/**
	 * Getter for part {@link #CONTENT_ATTR}.
	 */
	default com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText getContent() {
		return (com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText) tValueByName(CONTENT_ATTR);
	}

	/**
	 * Setter for part {@link #CONTENT_ATTR}.
	 */
	default void setContent(com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText newValue) {
		tUpdateByName(CONTENT_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #IMPORT_SOURCE_ATTR}.
	 */
	default String getImportSource() {
		return (String) tValueByName(IMPORT_SOURCE_ATTR);
	}

	/**
	 * Setter for part {@link #IMPORT_SOURCE_ATTR}.
	 */
	default void setImportSource(String newValue) {
		tUpdateByName(IMPORT_SOURCE_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #PARENT_ATTR}.
	 */
	@Override
	default com.top_logic.doc.model.Page getParent() {
		return (com.top_logic.doc.model.Page) tValueByName(PARENT_ATTR);
	}

	/**
	 * Getter for part {@link #POSITION_ATTR}.
	 */
	default Integer getPosition() {
		return (Integer) tValueByName(POSITION_ATTR);
	}

	/**
	 * Setter for part {@link #POSITION_ATTR}.
	 */
	default void setPosition(Integer newValue) {
		tUpdateByName(POSITION_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #TITLE_ATTR}.
	 */
	default com.top_logic.basic.util.ResKey getTitle() {
		return (com.top_logic.basic.util.ResKey) tValueByName(TITLE_ATTR);
	}

	/**
	 * Setter for part {@link #TITLE_ATTR}.
	 */
	default void setTitle(com.top_logic.basic.util.ResKey newValue) {
		tUpdateByName(TITLE_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #UUID_ATTR}.
	 */
	default String getUuid() {
		return (String) tValueByName(UUID_ATTR);
	}

	/**
	 * Setter for part {@link #UUID_ATTR}.
	 */
	default void setUuid(String newValue) {
		tUpdateByName(UUID_ATTR, newValue);
	}

}
