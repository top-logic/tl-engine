/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.SelectionTableModel;

/**
 * A {@link SelectField} that has an additional
 * {@link #getOptionAccessor() accessor} property for displaying its options in
 * a table view (see {@link TableControl}).
 * 
 * <p>
 * When a table view is created based on an instance of this class, an
 * {@link ObjectTableModel} is constructed that uses the option objects as row
 * objects and that accesses its column values with this
 * {@link SelectionTableField}'s {@link #getOptionAccessor()} property.
 * </p>
 * 
 * @see SelectionTableModel for a concrete {@link ObjectTableModel}
 *      implementation that provides a bidirectional link between the currently
 *      selected options of a {@link SelectionTableField} and a
 *      {@link TableModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectionTableField extends SelectField {

	private Accessor optionAccessor;
	
	/**
	 * Creates a new {@link SelectionTableField}.
	 * 
	 * @param columnAccessor
	 *        see {@link #getOptionAccessor()}
	 * 
	 * @see SelectField#SelectField(String, com.top_logic.layout.form.model.utility.OptionModel,
	 *      boolean, boolean, boolean, Constraint)
	 */
	protected SelectionTableField(String name, List options, Accessor columnAccessor, boolean multiple,
			boolean mandatory, boolean immutable, Constraint constraint) {
		super(name, new DefaultListOptionModel(options), multiple, mandatory, immutable, constraint);
		init(columnAccessor);
	}

	private void init(Accessor anOptionAccessor) {
		this.optionAccessor = anOptionAccessor;
	}

	/**
	 * An {@link Accessor} that provides reflective access to the option objects
	 * of this field.
	 * 
	 * <p>
	 * The returned {@link Accessor} can be used to display the option objects
	 * of this field in a table view.
	 * </p>
	 */
	public Accessor getOptionAccessor() {
		return optionAccessor;
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitSelectionTableField(this, arg);
	}
}
