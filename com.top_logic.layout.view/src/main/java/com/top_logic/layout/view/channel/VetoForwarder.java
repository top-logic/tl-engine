/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
import com.top_logic.layout.view.form.StateHandler;

/**
 * A {@link VetoListener} that answers for another channel, which is written when the observed
 * channel changes.
 *
 * <p>
 * A component that writes a target channel from a
 * {@link ViewChannel.ChannelListener ChannelListener} of a source channel registers a forwarder
 * from the source to the target with {@link #forward(ViewChannel, ViewChannel)}. The unsaved
 * changes blocking the write of the target are then reported when the source is asked, before the
 * source is written: the veto reaches the user as a single question about the whole chain, and the
 * {@link ChannelVetoException#getContinuation() continuation} of the exception retries the write of
 * the source, which writes the target again.
 * </p>
 *
 * <p>
 * Without a forwarder, the veto is raised from inside the notification of the source and unwinds
 * through it: the source already holds its new value, the listeners after the writing one are never
 * told, and the continuation retries only the write of the target.
 * </p>
 *
 * <p>
 * The forwarded question is the {@link ViewChannel#dirtyHandlers()} of the target, independent of
 * the value the target receives: the value the listener computes is not known before the source is
 * written. The forwarding must not form a cycle - a target that forwards back to its source asks
 * itself endlessly.
 * </p>
 */
public class VetoForwarder implements VetoListener {

	private final ViewChannel _target;

	/**
	 * Registers a {@link VetoForwarder} from the source to the target channel.
	 *
	 * @param source
	 *        The channel that is written first, and whose change causes the write of the target.
	 * @param target
	 *        The channel that is written in reaction, and whose unsaved changes are reported when
	 *        the source is asked.
	 * @return A {@link Runnable} removing the forwarder again, to be called when the component
	 *         writing the target is disposed.
	 */
	public static Runnable forward(ViewChannel source, ViewChannel target) {
		VetoForwarder forwarder = new VetoForwarder(target);
		source.addVetoListener(forwarder);
		return () -> source.removeVetoListener(forwarder);
	}

	/**
	 * Creates a {@link VetoForwarder}.
	 *
	 * @param target
	 *        The channel to ask instead of the channel this listener is registered on.
	 */
	private VetoForwarder(ViewChannel target) {
		_target = target;
	}

	@Override
	public List<StateHandler> checkVeto(ViewChannel sender, Object oldValue, Object newValue) {
		return _target.dirtyHandlers();
	}

	@Override
	public List<StateHandler> checkDirty(ViewChannel sender) {
		return _target.dirtyHandlers();
	}

	@Override
	public String toString() {
		return "VetoForwarder[" + _target + "]";
	}

}
