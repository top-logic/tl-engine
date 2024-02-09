/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.basic.contextmenu.config.ContextMenuCommandsProvider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link Control} providing a context menu from collections of {@link CommandHandler}s specific for
 * a given model.
 * 
 * @see ContextMenuControl
 * @see #writeContents(DisplayContext, TagWriter)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ContextCommandsControl<T> extends ConstantControl<T> implements ContextMenuOwner {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(ContextMenuOpener.INSTANCE);

	private final ContextMenuCommandsProvider _contextMenu;

	private final LabelProvider _titleProvider;

	private LayoutComponent _component;

	/**
	 * Creates a {@link ContextCommandsControl}.
	 * 
	 * @param contextMenu
	 *        The {@link ContextMenuCommandsProvider} to create the context menu for the given
	 *        model.
	 */
	public ContextCommandsControl(ContextMenuCommandsProvider contextMenu, LabelProvider titleProvider, T model) {
		super(model, COMMANDS);
		_titleProvider = titleProvider;
		_contextMenu = contextMenu;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		_component = MainLayout.getComponent(context);

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
		T model = getModel();
		List<CommandHandler> commands = _contextMenu.getContextCommands(model);
		Map<String, Object> arguments = ContextMenuUtil.createArguments(model);
		Stream<CommandModel> buttonsStream = ContextMenuUtil.toButtonsStream(_component, arguments, commands);
		Menu result = ContextMenuUtil.toContextMenu(buttonsStream);
		String title = _titleProvider.getLabel(model);
		if (title != null) {
			result.setTitle(Fragments.text(title));
		}
		return result;
	}
}
