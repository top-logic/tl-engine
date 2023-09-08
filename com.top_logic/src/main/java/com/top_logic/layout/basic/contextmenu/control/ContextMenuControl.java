/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Control} providing a context menu for its contents.
 * 
 * @see ContextCommandsControl
 * @see #writeContents(DisplayContext, TagWriter)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ContextMenuControl<T> extends ConstantControl<T> implements ContextMenuOwner {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(ContextMenuOpener.INSTANCE);

	private final ContextMenuProvider _contextMenu;

	/**
	 * Creates a {@link ContextMenuControl}.
	 * 
	 * @param contextMenu
	 *        The {@link ContextMenuProvider} to create the context menu for the given model.
	 */
	public ContextMenuControl(ContextMenuProvider contextMenu, T model) {
		super(model, COMMANDS);
		_contextMenu = contextMenu;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		String tag = tagName();
		out.beginBeginTag(tag);
		writeControlAttributes(context, out);
		out.writeAttribute(TL_CONTEXT_MENU_ATTR, getID());
		out.endBeginTag();
		{
			writeContents(context, out);
		}
		out.endTag(tag);
	}

	/**
	 * Tag name to use for the control tag.
	 * 
	 * <p>
	 * Default is {@link HTMLConstants#SPAN}.
	 * </p>
	 */
	protected String tagName() {
		return SPAN;
	}

	/**
	 * Writes the visible contents.
	 */
	protected abstract void writeContents(DisplayContext context, TagWriter out) throws IOException;

	@Override
	public Menu createContextMenu(String contextInfo) {
		return _contextMenu.getContextMenu(getModel());
	}
}
