/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Two-way binding between a {@link ViewChannel} and the {@link FieldModel} of an input control.
 *
 * <p>
 * What the user enters becomes the value of the channel, and a value the channel receives from
 * elsewhere appears in the input. This is how a value belonging to the view rather than to a model
 * object is edited - the term a table filters by, the text a search runs on - with no form and no
 * object to hold it.
 * </p>
 *
 * <p>
 * A value chosen from options is represented as a list by the control editing it, whatever the
 * channel holds: a single-valued selection is unwrapped on its way to the channel and wrapped again
 * on its way back, so the channel holds the value itself and not a list of one.
 * </p>
 */
public class ChannelFieldBinding {

	private final ViewChannel _channel;

	private final AbstractFieldModel _field;

	private final boolean _selection;

	private final boolean _multiple;

	private boolean _transferring;

	private final ChannelListener _channelListener = (sender, oldValue, newValue) -> toField(newValue);

	private final FieldModelListener _fieldListener = new FieldModelListener() {
		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			toChannel(newValue);
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			// The value alone is shared with the channel; who may change it is decided by the
			// element owning the input.
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			// Only a value that was accepted reaches the field, and therefore the channel.
		}
	};

	private ChannelFieldBinding(ViewChannel channel, AbstractFieldModel field, boolean selection,
			boolean multiple) {
		_channel = channel;
		_field = field;
		_selection = selection;
		_multiple = multiple;
	}

	/**
	 * Binds the given channel to the given field model in both directions.
	 *
	 * <p>
	 * The field takes over the current value of the channel before it starts to follow it, so the
	 * input shows what the channel holds from the moment it appears.
	 * </p>
	 *
	 * @param channel
	 *        The channel holding the edited value.
	 * @param field
	 *        The field model of the control editing it.
	 * @param selection
	 *        Whether the value is chosen from options, which the control represents as a list.
	 * @param multiple
	 *        Whether the channel holds a collection of values rather than a single one.
	 * @return The binding, to be {@link #dispose() disposed} when the control goes away.
	 */
	public static ChannelFieldBinding bind(ViewChannel channel, AbstractFieldModel field, boolean selection,
			boolean multiple) {
		ChannelFieldBinding result = new ChannelFieldBinding(channel, field, selection, multiple);
		result.start();
		return result;
	}

	private void start() {
		Object initial = fieldValue(_channel.get());
		_field.setValue(initial);
		// What the channel holds when the input appears is the value the input has by default, so
		// an input that was never touched does not report itself as changed.
		_field.setDefaultValue(initial);
		_field.addListener(_fieldListener);
		_channel.addListener(_channelListener);
	}

	/**
	 * Detaches this binding from the channel and the field.
	 */
	public void dispose() {
		_channel.removeListener(_channelListener);
		_field.removeListener(_fieldListener);
	}

	/**
	 * Hands a value the channel received to the field.
	 */
	private void toField(Object channelValue) {
		if (_transferring) {
			return;
		}
		_transferring = true;
		try {
			_field.setValue(fieldValue(channelValue));
		} finally {
			_transferring = false;
		}
	}

	/**
	 * Hands a value the user entered to the channel.
	 */
	private void toChannel(Object fieldValue) {
		if (_transferring) {
			return;
		}
		_transferring = true;
		try {
			_channel.set(channelValue(fieldValue));
		} finally {
			_transferring = false;
		}
	}

	/**
	 * The given channel value in the representation the control expects.
	 */
	private Object fieldValue(Object channelValue) {
		if (!_selection) {
			return channelValue;
		}
		if (channelValue == null) {
			return Collections.emptyList();
		}
		if (channelValue instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return Collections.singletonList(channelValue);
	}

	/**
	 * The given field value in the representation the channel carries.
	 */
	private Object channelValue(Object fieldValue) {
		if (!_selection) {
			return fieldValue;
		}
		List<?> selection;
		if (fieldValue instanceof List<?> list) {
			selection = list;
		} else if (fieldValue == null) {
			selection = Collections.emptyList();
		} else {
			selection = Collections.singletonList(fieldValue);
		}
		if (_multiple) {
			return new ArrayList<>(selection);
		}
		return selection.isEmpty() ? null : selection.get(0);
	}

}
