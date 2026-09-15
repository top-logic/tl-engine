/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * The {@code <column>} of a table: one column showing a model attribute of its rows.
 *
 * <p>
 * The attribute says everything about the column - what its cells hold, how they are displayed,
 * sorted, filtered and searched, how wide the column is and what its header says - so declaring the
 * attribute is all such a column needs. Where the rows of the table are edited, an edited cell
 * writes its value to that attribute.
 * </p>
 */
@InApp
public class AttributeColumn extends AbstractColumnDeclaration {

	/** The tag a column over a model attribute is declared with. */
	public static final String TAG_NAME = "column";

	/**
	 * Configuration of an {@link AttributeColumn}.
	 */
	@TagName(TAG_NAME)
	public interface Config extends AbstractColumnDeclaration.Config<AttributeColumn> {

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		@Override
		@ClassDefault(AttributeColumn.class)
		Class<? extends AttributeColumn> getImplementationClass();

		/**
		 * The name of the attribute to display, which is the name of the column.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

	}

	private final String _attribute;

	/**
	 * Creates an {@link AttributeColumn} from configuration.
	 */
	@CalledByReflection
	public AttributeColumn(InstantiationContext context, Config config) {
		super(context, config);
		_attribute = config.getAttribute();
	}

	private AttributeColumn(String attribute, boolean readonly, boolean hiddenByDefault) {
		super(readonly, hiddenByDefault);
		_attribute = attribute;
	}

	/**
	 * The plain column a table derives for an attribute none of its declarations covers.
	 *
	 * <p>
	 * Such a column is editable exactly as a form field for that attribute is: no declaration says
	 * otherwise, so the model decides.
	 * </p>
	 *
	 * @param part
	 *        The attribute the column shows.
	 */
	public static AttributeColumn derived(TLStructuredTypePart part) {
		return new AttributeColumn(part.getName(), !DisplayAnnotations.isEditable(part), false);
	}

	/**
	 * The plain column for an attribute that is only <em>offered</em>: displayed once the user
	 * selects it in the column selection, and not before.
	 *
	 * @param part
	 *        The attribute the column shows.
	 * @see #derived(TLStructuredTypePart) The column a table shows from the start.
	 */
	public static AttributeColumn offered(TLStructuredTypePart part) {
		return new AttributeColumn(part.getName(), !DisplayAnnotations.isEditable(part), true);
	}

	@Override
	public List<String> declaredNames() {
		return List.of(_attribute);
	}

	@Override
	public List<ColumnSetup> resolve(ColumnResolution scope) {
		String attribute = _attribute;
		TLStructuredTypePart part = scope.part(attribute);
		return List.of(setup(attribute, derivedLabel(part), ColumnType.of(part),
			row -> ColumnProviderService.attributeValue(row, attribute), new AttributeCellEditing(attribute),
			scope));
	}

	/**
	 * The label the column derives: the attribute's, and the attribute name for a row type that
	 * holds no such attribute - or could not be resolved at all.
	 */
	private ResKey derivedLabel(TLStructuredTypePart part) {
		return part != null ? TLModelNamingConvention.resourceKey(part) : ResKey.text(_attribute);
	}

}
