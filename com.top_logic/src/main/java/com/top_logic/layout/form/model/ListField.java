/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.list.ListControl;
import com.top_logic.layout.list.model.ListSelectionManager;

/**
 * An adaptor of a {@link ListModel}, {@link ListSelectionModel} pair to the
 * {@link FormField} interface.
 * 
 * <p>
 * A {@link ListField} is displayed and managed by a {@link ListControl}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListField extends ConstantField {

	private final ListModel listModel;
	private final ListSelectionModel selectionModel;
	
	private Renderer<Object> itemRenderer;

	/**
	 * Creates a new {@link ListField}.
	 * 
	 * @param name
	 *        see {@link #getName()}
	 * @param listModel
	 *        see {@link #getListModel()}
	 * @param selectionModel
	 *        see {@link #getSelectionModel()}
	 * 
	 * @see ConstantField#ConstantField(String, boolean)
	 */
	protected ListField(String name, ListModel listModel, ListSelectionModel selectionModel) {
		super(name, !IMMUTABLE);
		
		this.listModel = listModel;
		this.selectionModel = selectionModel;
		
		// Keep selection and list model in sync.
		this.listModel.addListDataListener(new ListSelectionManager(this.selectionModel));
	}

	public ListModel getListModel() {
		return listModel;
	}

	public ListSelectionModel getSelectionModel() {
		return selectionModel;
	}

	public Set getSelectionSet() {
		HashSet result = new HashSet();
		addSelection(result);
		return result;
	}

	public List getSelectionList() {
		ArrayList result = new ArrayList();
		addSelection(result);
		return result;
	}

	private void addSelection(Collection result) {
		for (int max = selectionModel.getMaxSelectionIndex(), n = selectionModel.getMinSelectionIndex(); n <= max; n++) {
			if (selectionModel.isSelectedIndex(n)) {
				result.add(listModel.getElementAt(n));
			}
		}
	}

	public Renderer<Object> getItemRenderer() {
		return itemRenderer;
	}

	public void setItemRenderer(Renderer<Object> itemRenderer) {
		this.itemRenderer = itemRenderer;
	}
	
	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitListField(this, arg);
	}
	
}
