/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;

/**
 * The class {@link SimpleCard} is an immutable {@link Card} which constantly
 * returns the same things.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleCard implements Card {

	private final String name;
	private final Object info;
	private final Object content;

	/**
	 * Actually calls {@link SimpleCard#SimpleCard(Object, String, Object)}
	 */
	public SimpleCard(String info, Object content) {
		this(info, info, content);
	}

	/**
	 * Creates a card with the given info, name, and content.
	 * 
	 * @param info
	 *        will be returned by {@link #getCardInfo()}
	 * @param name
	 *        will be returned by {@link #getName()}
	 * @param content
	 *        will be returned by {@link #getContent()}
	 */
	public SimpleCard(Object info, String name, Object content) {
		this.info = info;
		this.name = name;
		this.content = content;
	}

	@Override
	public Object getCardInfo() {
		return info;
	}

	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Writes the {@link #getName() name} to the given writer.
	 * 
	 * @see Card#writeCardInfo(DisplayContext, Appendable)
	 */
	@Override
	public void writeCardInfo(DisplayContext context, Appendable out) throws IOException {
		out.append(getName());
	}

}
