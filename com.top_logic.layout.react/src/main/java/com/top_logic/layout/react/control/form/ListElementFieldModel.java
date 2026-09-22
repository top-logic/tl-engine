/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;

/**
 * One element of a collection-valued {@link FieldModel}, seen as a field of its own.
 *
 * <p>
 * A control that edits a single value is given such a model: it reads and writes the value at one
 * {@link #getIndex() index} of the collection, while everything around it - the label, the
 * mandatory state, the validation of the field as a whole - stays with the collection. An element
 * is never mandatory and always {@link #isNullable() nullable}: an empty element is a value the
 * user has not entered yet, not a field left blank.
 * </p>
 *
 * <p>
 * The error state is the element's own, so an input rejecting what was typed into it - an
 * unreadable number, a text that is no web address - reports the problem on the element it
 * happened in.
 * </p>
 *
 * <p>
 * Changes travel in one direction only. {@link #setValue(Object)} writes the element back into the
 * collection, which makes the collection-valued model fire; no element listens to that model, so a
 * collection holding many values costs no listener per element. The other direction is the owner's
 * work: whoever created the elements reconciles them with the collection through
 * {@link #syncValue(Object)}, which takes a value over without writing it back. See
 * {@link ReactValueListControl}.
 * </p>
 */
public class ListElementFieldModel extends AbstractFieldModel {

	private final FieldModel _listModel;

	private final int _index;

	/**
	 * Creates a {@link ListElementFieldModel}.
	 *
	 * @param listModel
	 *        The collection-valued field this is one element of.
	 * @param index
	 *        The position of this element within that collection, see {@link #getIndex()}.
	 * @param value
	 *        The value the element holds.
	 */
	public ListElementFieldModel(FieldModel listModel, int index, Object value) {
		super(value);
		_listModel = listModel;
		_index = index;
	}

	/**
	 * The collection-valued field this is one element of.
	 */
	public FieldModel getListModel() {
		return _listModel;
	}

	/**
	 * The position of this element within the {@link #getListModel() collection}, counted from
	 * {@code 0}.
	 */
	public int getIndex() {
		return _index;
	}

	/**
	 * Stores the value and writes the whole collection back to the
	 * {@link #getListModel() collection-valued field}.
	 */
	@Override
	public void setValue(Object value) {
		Object oldValue = getValue();
		if (Objects.equals(oldValue, value)) {
			return;
		}
		setValueInternal(value);
		writeBack(value);
		fireValueChanged(oldValue, value);
	}

	/**
	 * Takes over the value the {@link #getListModel() collection} holds for this element, without
	 * writing anything back.
	 *
	 * <p>
	 * The counterpart of {@link #setValue(Object)}, for the owner reconciling its elements with a
	 * changed collection. Fires the value change like any other, so the control editing this
	 * element follows it.
	 * </p>
	 *
	 * @param value
	 *        The value the collection holds at {@link #getIndex() this position}.
	 */
	void syncValue(Object value) {
		super.setValue(value);
	}

	/**
	 * Replaces this element in the collection and stores the result in the
	 * {@link #getListModel() collection-valued field}.
	 *
	 * <p>
	 * The collection is written back as a {@link List} in element order, whatever kind of
	 * collection it arrived as: the order is what the elements are addressed by, and an attribute
	 * storing an unordered collection takes a list just as well.
	 * </p>
	 */
	private void writeBack(Object value) {
		List<Object> values = elementsOf(_listModel);
		while (values.size() <= _index) {
			values.add(null);
		}
		values.set(_index, value);
		_listModel.setValue(values);
	}

	/**
	 * The values of the given collection-valued field, in element order.
	 *
	 * @param listModel
	 *        The field holding the collection.
	 * @return A mutable list of the values, empty where the field holds nothing. A value that is no
	 *         collection is the single element of that list.
	 */
	public static List<Object> elementsOf(FieldModel listModel) {
		Object value = listModel.getValue();
		if (value == null) {
			return new ArrayList<>();
		}
		if (value instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		List<Object> result = new ArrayList<>();
		result.add(value);
		return result;
	}

}
