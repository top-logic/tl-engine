/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.security.WithAccessControl;

/**
 * Configuration of a labeled part of a container that holds content of its own, such as a tab of a
 * tab bar or a section of an accordion.
 *
 * @see ContentSection
 */
public interface ContentSectionConfig extends WithAccessControl {

	/** Configuration name for {@link #getId()}. */
	String ID = "id";

	/** Configuration name for {@link #getLabel()}. */
	String LABEL = "label";

	/** Configuration name for {@link #getChildren()}. */
	String CHILDREN = "children";

	/** Configuration name for {@link #getIcon()}. */
	String ICON = "icon";

	/**
	 * The identifier of this part, unique within its container.
	 */
	@Name(ID)
	String getId();

	/**
	 * The CSS icon class shown next to the label (e.g. {@code "css:fa-solid fa-tags"}), or empty
	 * for no icon.
	 */
	@Name(ICON)
	String getIcon();

	/**
	 * The label displayed for this part.
	 *
	 * <p>
	 * If no label is given, the {@link #getId() ID} is displayed.
	 * </p>
	 */
	@Name(LABEL)
	ResKey getLabel();

	/**
	 * The content elements of this part.
	 */
	@Name(CHILDREN)
	@DefaultContainer
	@TreeProperty
	@Options(fun = AllInAppImplementations.class)
	List<PolymorphicConfiguration<? extends UIElement>> getChildren();

}
