/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.container.ConfigPartUtilInternal;

/**
 * The {@link ConfigurationChange} to be sent to the {@link ConfigurationListener}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class ChangeEvent implements Change, ConfigurationChange {

	private final ConfigurationListener _receiver;

	private Kind _kind;

	private final AbstractConfigItem _item;

	private final PropertyDescriptor _property;

	private Object _oldValue;

	private Object _newValue;

	private int _index;

	/**
	 * Creates a {@link ChangeEvent}.
	 * 
	 * @param item
	 *        The {@link AbstractConfigItem} that is changing.
	 * @param property
	 *        The {@link PropertyDescriptor} of the property that is changing. See {@link Change}
	 *        for why the {@link PropertyDescriptor} has to be passed twice: First in this
	 *        constructor and than in the methods of {@link Change}.
	 * @param receiver
	 *        The {@link ConfigurationListener} that should receive this {@link ChangeEvent}.
	 */
	public ChangeEvent(AbstractConfigItem item, PropertyDescriptor property, ConfigurationListener receiver) {
		_item = item;
		_property = property;
		_receiver = receiver;
	}

	@Override
	public Kind getKind() {
		return _kind;
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	@Override
	public ConfigurationItem getModel() {
		return _item.getInterface();
	}

	@Override
	public Object getOldValue() {
		return _oldValue;
	}

	@Override
	public Object getNewValue() {
		return _newValue;
	}

	@Override
	public int getIndex() {
		return _index;
	}

	@Override
	public void update(PropertyDescriptor property, Object oldValue, Object newValue) {
		assert property == _property;

		_kind = Kind.SET;
		_oldValue = oldValue;
		_newValue = newValue;

		_receiver.onChange(this);
	}

	@Override
	public void add(PropertyDescriptor property, int index, Object element) {
		assert property == _property;

		_kind = Kind.ADD;
		_index = index;
		_newValue = element;

		_item.markSet(property);
		ConfigPartUtilInternal.initContainer(element, getModel());

		_receiver.onChange(this);
	}

	@Override
	public void remove(PropertyDescriptor property, int index, Object element) {
		assert property == _property;

		_kind = Kind.REMOVE;
		_index = index;
		_oldValue = element;

		_item.markSet(property);
		ConfigPartUtilInternal.clearContainer(element);

		_receiver.onChange(this);
	}

}
