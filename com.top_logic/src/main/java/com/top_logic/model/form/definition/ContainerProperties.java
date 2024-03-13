/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.form.implementation.FormEditorTemplateProvider;

/**
 * Definition of visual properties of form container contents.
 * 
 * @see ContainerDefinition
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ContainerProperties<T extends FormEditorTemplateProvider> extends FormElement<T> {

	/** Configuration name for the value of the {@link #getColumns()}. */
	String COLUMNS = "columns";

	/** Configuration name for the value of the {@link #getLabelPlacement()}. */
	String LABEL_PLACEMENT = "labelPlacement";

	/**
	 * The maximum default number of columns in this container.
	 * 
	 * <p>
	 * This number can be overwritten at other container elements locally.
	 * </p>
	 * 
	 * @return Number of columns inside of this container.
	 */
	@Name(COLUMNS)
	Columns getColumns();

	/**
	 * @see #getColumns()
	 */
	void setColumns(Columns columns);

	/**
	 * Placement strategy of the labels of the form container contents.
	 * 
	 * @return Where the labels of the elements inside this container element are rendered.
	 */
	@Name(LABEL_PLACEMENT)
	LabelPlacement getLabelPlacement();

	/**
	 * @see #getLabelPlacement()
	 */
	void setLabelPlacement(LabelPlacement labelPlacement);

}
