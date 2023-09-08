/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collection;

import com.top_logic.basic.col.InlineList;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link AbstractComponentChannel} that can be linked to some input channel(s).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractInputChannel extends AbstractComponentChannel {

	private Object _sources = InlineList.newInlineList();

	/**
	 * Creates a {@link AbstractInputChannel}.
	 */
	public AbstractInputChannel(LayoutComponent component, String name) {
		super(component, name);
	}

	@Override
	public void link(ComponentChannel source) {
		_sources = InlineList.add(ComponentChannel.class, _sources, source);
		source.internalLinkBack(this);
	}

	@Override
	public void unlink(ComponentChannel source) {
		_sources = InlineList.remove(_sources, source);
		source.internalUnlinkFrom(this);
	}

	@Override
	public final Collection<ComponentChannel> sources() {
		return InlineList.toList(ComponentChannel.class, _sources);
	}

}
