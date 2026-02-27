/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.listener.ListenerRegistry;
import com.top_logic.basic.listener.ListenerRegistryFactory;

/**
 * Simple mutable implementation of {@link BinaryDataValue}.
 *
 * <p>
 * Use this for cases where no form field is involved, e.g. to share audio data between a recorder,
 * a player, and a download control.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SimpleBinaryDataValue implements BinaryDataValue {

	private final ListenerRegistry<BinaryData> _listeners =
		ListenerRegistryFactory.getInstance().createSimple();

	private BinaryData _data;

	/**
	 * Creates a new {@link SimpleBinaryDataValue} with no data.
	 */
	public SimpleBinaryDataValue() {
		this(null);
	}

	/**
	 * Creates a new {@link SimpleBinaryDataValue}.
	 *
	 * @param data
	 *        The initial data, or {@code null}.
	 */
	public SimpleBinaryDataValue(BinaryData data) {
		_data = data;
	}

	@Override
	public BinaryData getData() {
		return _data;
	}

	@Override
	public void setData(BinaryData data) {
		if (_data == data) {
			return;
		}
		_data = data;
		_listeners.notify(data);
	}

	@Override
	public void addListener(Listener<? super BinaryData> listener) {
		_listeners.register(listener);
	}

	@Override
	public void removeListener(Listener<? super BinaryData> listener) {
		_listeners.unregister(listener);
	}

}
