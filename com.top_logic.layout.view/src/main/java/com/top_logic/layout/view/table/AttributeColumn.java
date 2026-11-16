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
import com.top_logic.layout.view.channel.ChannelCondition;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The {@code <column>} of a table: one column showing a model attribute of its rows.
 *
 * <p>
 * The attribute says everything about the column - what its cells hold, how they are displayed,
 * sorted, filtered and searched, how wide the column is and what its header says - so declaring the
 * attribute is all such a column needs. Where the rows of the table are edited, an edited cell
 * writes its value to that attribute.
 * </p>
 *
 * <p>
 * The column follows the model access rights of the user: a cell whose value the user may not read
 * on its row is empty, and a column whose attribute the user may read on no row at all is not
 * offered. Since the rows the user may read an attribute on change with the data - the user's first
 * object of a type grants the role reading it - such a column exists as soon as the user may read
 * the attribute on some row, from the next refresh of the rows of the table on. Where the rows are
 * edited, a cell is edited as a form field for its attribute on that row is: only where the model
 * lets the attribute be edited and the user may write it.
 * </p>
 *
 * @implNote A column of an attribute the user may read on no row is
 *           {@link ChannelCondition#withholds() withheld} until the table asks its existence again
 *           after a refresh, see {@link ColumnExistence#onRefresh()}.
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

	private AttributeColumn(String attribute, boolean hiddenByDefault) {
		super(hiddenByDefault);
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
		return new AttributeColumn(part.getName(), false);
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
		return new AttributeColumn(part.getName(), true);
	}

	@Override
	public List<String> declaredNames() {
		return List.of(_attribute);
	}

	@Override
	public List<ColumnSetup> resolve(ColumnResolution scope) {
		String attribute = _attribute;
		TLStructuredTypePart part = scope.part(attribute);
		ColumnSetup setup = setup(attribute, derivedLabel(part), ColumnType.of(part),
			row -> ColumnProviderService.attributeValue(row, attribute), new AttributeCellEditing(attribute),
			scope);
		if (part != null && !mayRead(scope, attribute)) {
			// The user may read the attribute on no row now - the column exists once that changes.
			return List.of(setup.existingIf(new ReadableAttribute(scope, attribute)));
		}
		return List.of(setup);
	}

	/**
	 * Whether the current user may read the given attribute on any of the row types holding one of
	 * that name.
	 *
	 * <p>
	 * Each type is checked with the attribute it holds itself: an attribute of the same name on
	 * another type is another attribute, with access rights of its own.
	 * </p>
	 */
	private static boolean mayRead(ColumnResolution scope, String attribute) {
		for (TLStructuredType type : scope.rowTypes()) {
			TLStructuredTypePart part = type.getPart(attribute);
			if (part != null && ModelAccessPolicy
				.onAttributeOfType(SimpleBoundCommandGroup.READ, type, part).isExecutable()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The condition that the current user may read an attribute on some row of a table.
	 *
	 * <p>
	 * Whether the user may read the attribute on any row depends on the roles the user holds on the
	 * objects of the application, not on a channel: it is asked again whenever the rows of the table
	 * were refreshed. Once it holds, it holds for the rest of the table's life, so that a column
	 * shown is not checked again on every refresh.
	 * </p>
	 */
	private static final class ReadableAttribute implements ChannelCondition {

		private final ColumnResolution _scope;

		private final String _attribute;

		private boolean _readable;

		ReadableAttribute(ColumnResolution scope, String attribute) {
			_scope = scope;
			_attribute = attribute;
		}

		@Override
		public boolean holds() {
			if (!_readable) {
				_readable = mayRead(_scope, _attribute);
			}
			return _readable;
		}

		@Override
		public boolean withholds() {
			return !holds();
		}

		@Override
		public List<ViewChannel> channels() {
			return List.of();
		}
	}

	/**
	 * The label the column derives: the attribute's, and the attribute name for a row type that
	 * holds no such attribute - or could not be resolved at all.
	 */
	private ResKey derivedLabel(TLStructuredTypePart part) {
		return part != null ? TLModelNamingConvention.resourceKey(part) : ResKey.text(_attribute);
	}

}
