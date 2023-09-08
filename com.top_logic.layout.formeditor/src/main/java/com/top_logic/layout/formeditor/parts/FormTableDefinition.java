/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import java.util.List;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.layout.formeditor.definition.TextDefinition;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.table.command.TableCommandConfig;
import com.top_logic.model.form.definition.AttributeDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.definition.VisibilityConfig;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Display of a dynamic table of associated objects.
 * 
 * @see #getRows()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("formTable")
@DisplayOrder({
	FormTableDefinition.LABEL,
	FormTableDefinition.TYPE,
	FormTableDefinition.ROWS,
	FormTableDefinition.COLUMNS,
	FormTableDefinition.COMMANDS,
})
public interface FormTableDefinition
		extends FormElement<FormTableTemplateProvider>, TextDefinition, TypeTemplateParameters {

	/** Configuration name for the value of the {@link #getRows()}. */
	String ROWS = "rows";

	/** Configuration name for the value of the {@link #getColumns()}. */
	String COLUMNS = "columns";

	/** Configuration name for the value of the {@link #getCommands()}. */
	String COMMANDS = "commands";

	/**
	 * Expression creating a list of objects edited in the displayed table as rows.
	 * 
	 * @see #getColumns()
	 */
	@Name(ROWS)
	@ItemDisplay(ItemDisplayType.VALUE)
	Expr getRows();

	/**
	 * The columns to display for the given {@link #getRows()}.
	 */
	@Name(COLUMNS)
	List<ColumnDisplay> getColumns();

	/**
	 * Name of the form field that is created for this table.
	 */
	@Hidden
	@Nullable
	String getFormFieldName();

	/**
	 * Setter for {@link #getFormFieldName()}.
	 */
	void setFormFieldName(String name);

	/**
	 * Additional command models to display in the configured table.
	 */
	@Name(COMMANDS)
	List<TableCommandConfig> getCommands();

	/**
	 * {@link ConfigPart} representing a column in a {@link FormTableDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	interface ColumnDisplay extends ConfigPart {

		/** @see #getTable() */
		String TABLE = "table";

		/**
		 * The {@link FormTableDefinition} this {@link ColumnDisplay} is a part of.
		 */
		@Hidden
		@Name(TABLE)
		@Container
		FormTableDefinition getTable();

		/**
		 * Visits this {@link ColumnDisplay} with the given {@link ColumnDisplayVisitor}.
		 * 
		 * @param <R>
		 *        Return type of the {@link ColumnDisplayVisitor}.
		 * @param <A>
		 *        Argument type of the {@link ColumnDisplayVisitor}.
		 * @param vistor
		 *        Callback visitor.
		 * @param arg
		 *        Argument for the given visitor.
		 * 
		 * @return The return value of the given visitor.
		 */
		<R, A> R visit(ColumnDisplayVisitor<R, A> vistor, A arg);

	}

	/**
	 * Visitor interface for {@link ColumnDisplay}.
	 * 
	 * @param <R>
	 *        Return type of the {@link ColumnDisplayVisitor}.
	 * @param <A>
	 *        Argument type of the {@link ColumnDisplayVisitor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ColumnDisplayVisitor<R, A> {

		/**
		 * Visit case for {@link AttributeColumn}.
		 */
		R visitAttributeColumn(AttributeColumn col, A arg);

	}

	/**
	 * {@link ColumnDisplay} representing an attribute.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		AttributeDefinition.NAME_ATTRIBUTE,
		VisibilityConfig.VISIBILITY,
	})
	@TagName(AttributeColumn.TAG_NAME)
	interface AttributeColumn extends ColumnDisplay, AttributeDefinition, VisibilityConfig {

		/** Tag name of {@link AttributeColumn}. */
		String TAG_NAME = "column";

		@DerivedRef({ TABLE, FormTableDefinition.TYPE })
		@Override
		TLModelPartRef getOwner();

	}

}