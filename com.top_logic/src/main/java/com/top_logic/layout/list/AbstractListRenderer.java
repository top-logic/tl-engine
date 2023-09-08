/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.io.IOException;

import javax.swing.ListSelectionModel;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Convenient base class for {@link ListRenderer} implementations.
 * 
 * <p>
 * All required information to render a single list item is extracted from the currently rendered
 * {@link ListControl} and passed to the method
 * {@link #renderItem(DisplayContext, TagWriter, ListControl, Object, String, boolean, boolean, boolean)},
 * which must be implemented.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractListRenderer extends DefaultControlRenderer<ListControl> implements ListRenderer {

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, ListControl control) throws IOException {
		ListControl listControl = control;

		out.beginBeginTag(getContentTag());
		out.writeAttribute(ID_ATTR, listControl.getContentID());
		writeContentTagAttributes(context, out, listControl);
		out.endBeginTag();
		{
			renderItems(context, out, listControl, 0, listControl.getListModel().getSize());
		}
		out.endTag(getContentTag());
	}

	/**
	 * This method writes additional attributes for the container tag of the list items. The
	 * {@link HTMLConstants#ID_ATTR ID attribute} must not be written.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to
	 * @param context
	 *        the {@link DisplayContext} to get information about the context this rendering occurs.
	 * @param listControl
	 *        the list control which is currently written by this renderer.
	 * @see #writeControlTagAttributes(DisplayContext, TagWriter, ListControl)
	 */
	protected void writeContentTagAttributes(DisplayContext context, TagWriter out, ListControl listControl) {
		// no content tag attributes here
	}

	/**
	 * Returns the tag for the container of the list items.
	 */
	protected abstract String getContentTag();


	@Override
	public void renderItems(DisplayContext context, TagWriter out, ListControl view, final int startIndex, final int stopIndex) throws IOException {
		for (int n = startIndex; n < stopIndex; n++) {
			renderItem(context, out, n, view);
		}
	}

	/**
	 * Renders the item with the given index.
	 * 
	 * @param index
	 *        Index of the item to render.
	 * @param view
	 *        {@link ListControl} which is currently rendered.
	 */
	protected void renderItem(DisplayContext context, TagWriter out, int index, ListControl view) throws IOException {
		assert view.isAttached();

		String itemID = view.getItemID(index);
		Object listElement = view.getListModel().getElementAt(index);
		boolean isSelectable = view.canSelect(index);
		ListSelectionModel selectionModel = view.getSelectionModel();
		boolean isSelected = selectionModel.isSelectedIndex(index);
		int leadSelectionIndex = selectionModel.getLeadSelectionIndex();
		boolean isFocused = leadSelectionIndex == index;
		if (index == 0) {
			// if currently no selection is given focus the element with index 0
			isFocused = isFocused || leadSelectionIndex == -1;
		}

		renderItem(context, out, view, listElement, itemID, isSelectable, isSelected, isFocused);
	}

	/**
	 * Renders the given item based on the given information extracted from the
	 * currently rendered {@link ListControl}.
	 * 
	 * <p>
	 * After the item structure is written, the control flow has to be forwarded
	 * to the given item renderer by passing the current list item.
	 * </p>
	 */
	protected abstract void renderItem(DisplayContext context, TagWriter out, 
			ListControl listControl, Object listItem,
			String itemId, boolean isSelectable, boolean isSelected, boolean isFocused
		) throws IOException;

}
