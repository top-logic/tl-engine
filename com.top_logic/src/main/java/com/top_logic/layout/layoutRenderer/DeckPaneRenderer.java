/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.DeckPaneControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.util.Resources;

/**
 * The class {@link DeckPaneRenderer} is used to render the content of a {@link DeckPaneControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeckPaneRenderer extends LayoutControlRenderer<DeckPaneControl> {

	/**
	 * Singleton {@link DeckPaneRenderer} instance.
	 */
	public static final DeckPaneRenderer INSTANCE = new DeckPaneRenderer();

	private DeckPaneRenderer() {
		// Singleton constructor.
	}

	private static final String NO_SELECTION_CLASS = "noSelection";

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, DeckPaneControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		DeckPaneControl deckPaneControl = control;
		deckPaneControl.writeLayoutSizeAttribute(out);
		writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
	}

	@Override
	public void writeControlContents(DisplayContext context, TagWriter out, DeckPaneControl value) throws IOException {
		DeckPaneControl deckPaneControl = value;
		{
			LayoutControl activeChild = deckPaneControl.getActiveChild();
			if (activeChild != null) {
				activeChild.write(context, out);
			} else {
				writeNoSelection(context, out, deckPaneControl);
			}
		}
	}

	/**
	 * Write the content in case nothing is selected.
	 * <p>
	 * Hook for subclasses. Default behaviour: Nothing is written
	 * </p>
	 */
	protected void writeNoSelection(DisplayContext context, TagWriter out, DeckPaneControl deckPaneControl)
			throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, NO_SELECTION_CLASS);
		out.endBeginTag();
		String deniedMessage = Resources.getInstance().getString(com.top_logic.layout.renderers.I18NConstants.ERROR_NO_TABS);
		out.writeText(deniedMessage);
		out.endTag(DIV);
	}

}
