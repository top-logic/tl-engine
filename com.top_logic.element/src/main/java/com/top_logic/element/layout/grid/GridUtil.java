/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.element.layout.grid.GridComponent.*;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.top_logic.element.layout.grid.GridBuilder.GridHandler;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.model.TLObject;

/**
 * Utilities for working with the {@link GridComponent}.
 * <p>
 * <em>IMPORTANT:</em>Use the existing methods in the {@link GridComponent} whenever possible. These
 * methods are for code that has to work indirectly with a {@link GridComponent}, but does not have
 * access to its instance. The advantage of the methods on the GridComponent is, that they don't
 * have to guess whether a tree or a table is displayed. And they work even if a new
 * {@link GridHandler} is invented.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GridUtil {

	/**
	 * Converts from the grid internal row objects to the corresponding {@link FormGroup}s.
	 * <p>
	 * Use {@link GridComponent#getFormGroupsFromInternalRows(Collection)} whenever the instance of the
	 * {@link GridComponent} is accessible.
	 * </p>
	 * 
	 * @param rows
	 *        Grid internal row objects: Either {@link FormGroup}s or {@link GridTreeTableNode}s. Is
	 *        not allowed to be or contain null.
	 * @return A new, mutable and resizable {@link LinkedHashSet}. Never null. Never contains null.
	 */
	public static LinkedHashSet<FormGroup> getFormGroupsFromInternalRows(Collection<?> rows) {
		LinkedHashSet<FormGroup> result = new LinkedHashSet<>();
		for (Object row : rows) {
			result.add(getFormGroupFromInternalRow(row));
		}
		return result;
	}

	/**
	 * Converts from the grid internal row objects to the corresponding {@link FormGroup}s.
	 * <p>
	 * Use {@link GridComponent#getFormGroupFromInternalRow(Object)} whenever the instance of the
	 * {@link GridComponent} is accessible.
	 * </p>
	 * 
	 * @param row
	 *        A grid internal row object: Either a {@link FormGroup} or {@link GridTreeTableNode}.
	 *        Is allowed to be null.
	 * @return Null, if the parameter is null.
	 */
	public static FormGroup getFormGroupFromInternalRow(Object row) {
		if (row instanceof GridTreeTableNode) {
			return (FormGroup) ((GridTreeTableNode) row).getBusinessObject();
		}
		return (FormGroup) row;
	}

	/**
	 * Converts from the grid internal row objects to the corresponding business object.
	 * <p>
	 * Use {@link GridComponent#getBusinessObjectFromInternalRow(Object)} whenever the instance of
	 * the {@link GridComponent} is accessible.
	 * </p>
	 * 
	 * @param row
	 *        A grid internal row object: Either a {@link FormGroup} or {@link GridTreeTableNode}.
	 *        Is allowed to be null.
	 * @return The business object, usually a {@link TLObject}. Null, if the parameter is null or
	 *         null is the business object.
	 */
	public static Object getBusinessObjectFromInternalRow(Object row) {
		return getRowObject(getFormGroupFromInternalRow(row));
	}

}
