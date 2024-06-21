/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link ListRenderer} that produces a HTML list using the three CSS classes
 * {@link #SELECTED_CSS_CLASS}, {@link #FOCUS_CSS_CLASS}, and
 * {@link #FIXED_CSS_CLASS} for its items.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultListRenderer extends AbstractListRenderer {
	
	private static final String MULTISELECTION_DATA_ATTRIBUTE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "multiselection";

	/**
	 * CSS class marking a selected list item.
	 */
	public static final String SELECTED_CSS_CLASS = "selected";
	
	/**
	 * CSS class that marks the lead selection index in a list.
	 */
	public static final String FOCUS_CSS_CLASS = "focus";
	
	/**
	 * CSS class that marks a list item that cannot be selected.
	 */
	public static final String FIXED_CSS_CLASS = "fixed";

	/** Static instance of {@link DefaultListRenderer} */
	public static final ListRenderer INSTANCE = new DefaultListRenderer();

	private List<ListItemCssAdorner> _listItemAdorners = new ArrayList<>();

	private FixedOptionMarker _fixedOptionMarker;

	private boolean _allowConfiguration;

	/**
	 * {@link DefaultListRenderer}, that is not configurable (e.g. adornable by
	 * {@link ListItemCssAdorner}.
	 */
	protected DefaultListRenderer() {
		this(false);
	}

	/**
	 * Create a {@link DefaultListRenderer}, that is configurable (e.g. add
	 * {@link ListItemCssAdorner}s), or not.
	 */
	public DefaultListRenderer(boolean allowConfiguration) {
		_allowConfiguration = allowConfiguration;
		_listItemAdorners.add(DefaultCssAdorner.INSTANCE);
		_fixedOptionMarker = DefaultFixedMarker.INSTANCE;
	}

	/**
	 * Adds {@link ListItemCssAdorner}s to this renderer.
	 */
	public void addListItemAdorner(ListItemCssAdorner adorner) {
		if (_allowConfiguration) {
			_listItemAdorners.add(adorner);
		} else {
			throw new IllegalStateException(
				"Unmodifiable list renderers cannot receive additional list item adorners!");
		}
	}
	
	/**
	 * Setter of {@link FixedOptionMarker} of list items.
	 */
	public void setFixedOptionMarker(FixedOptionMarker fixedOptionMarker) {
		if (_allowConfiguration) {
			_fixedOptionMarker = fixedOptionMarker;
		} else {
			throw new IllegalStateException(
				"Unmodifiable list renderers cannot receive different fixed option markers!");
		}
	}

	@Override
	protected String getControlTag(ListControl control) {
		return DIV;
	}
	
	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, ListControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);
		ListControl listControl = control;
		if (!listControl.isSingleSelection()) {
			out.writeAttribute(MULTISELECTION_DATA_ATTRIBUTE, String.valueOf(true));
		}
		out.writeAttribute(TL_TYPE_ATTR, "services.form.ListControl");
		writeOnMouseDown(out, control);
		writeOnKeyDown(out, control);
		/* When selection via onClick */
//		out.writeAttribute(ONCLICK_ATTR, "return services.form.ListControl.ctrlsListSelected(arguments[0], '" + control.getID() + "');");
		DoubleClickAction dblClickAction = listControl.getDblClickAction();
		if (dblClickAction != null) {
			writeOnDblClick(out, control, dblClickAction);
		}
		listControl.writeDNDInfo(context, out);
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, ListControl control) throws IOException {
		super.writeControlContents(context, out, control);

		HTMLUtil.writeScriptAfterRendering(out, "services.form.ListControl.scrollToFocus('" + control.getID() + "');");
	}

	private void writeOnMouseDown(TagWriter out, Control control) throws IOException {
		out.beginAttribute(ONMOUSEDOWN_ATTR);
		out.append("return services.form.ListControl.mouseDownSelect(arguments[0], '");
		out.append(control.getID());
		out.append("');");
		out.endAttribute();
	}

	private void writeOnKeyDown(TagWriter out, Control control) throws IOException {
		/* Select the anchor if return is pressed and the element is focused. */
		out.beginAttribute(ONKEYDOWN_ATTR);
		out.append("return services.form.ListControl.clickSelect(arguments[0], '");
		out.append(control.getID());
		out.append("');");
		out.endAttribute();
	}

	private void writeOnDblClick(TagWriter out, Control control, DoubleClickAction dblClickAction) throws IOException {
		out.beginAttribute(ONDBLCLICK_ATTR);
		out.append("return services.form.ListControl.dblClick(arguments[0], '");
		out.append(control.getID());
		out.append("', ");
		out.append(String.valueOf(dblClickAction.isWaitPaneRequested()));
		out.append(");");
		out.endAttribute();
	}

	@Override
	protected String getContentTag() {
		return UL;
	}
	
	@Override
	protected void renderItem(DisplayContext context, TagWriter out, 
		ListControl listControl, Object listItem, 
		String itemId, boolean isSelectable, boolean isSelected, boolean isFocused
	) throws IOException {
		out.beginBeginTag(LI);
		out.writeAttribute(ID_ATTR, itemId);

		writeCssClasses(out, listItem, isSelectable, isSelected, isFocused);
		writeFixedOptionMarker(out, listItem, isSelectable, isSelected, isFocused);
		
		if (!isSelectable) {
			/*
			 * Here we write the mousedown-attribute attribute, since we need
			 * that the event does not bubble to the control element to prevent
			 * drag&drop for fixed elements.
			 */
			out.writeAttribute(ONMOUSEDOWN_ATTR, "return services.form.ListControl.handleFixed(arguments[0], this)");
			
			/*
			 * prevent selection of fixed elements so the event must not bubble
			 * up to the ancestor node which react on clicks and dblclicks.
			 */
			out.writeAttribute(ONCLICK_ATTR, "return services.form.ListControl.handleFixed(arguments[0], this)");
			out.writeAttribute(ONDBLCLICK_ATTR, "return services.form.ListControl.handleFixed(arguments[0], this)");
		}
		out.endBeginTag();
		{
			// additional for #4175
			if (isSelectable) {
				out.beginBeginTag(HTMLConstants.ANCHOR);
				out.writeAttribute(HTMLConstants.HREF_ATTR, HTMLConstants.HREF_EMPTY_LINK);
				/* Cancel on click event to prevent document from scrolling to the the top (this is
				 * done because of the "#" href). */
				out.writeAttribute(ONCLICK_ATTR, "return BAL.cancelEvent(BAL.getEvent(event));");
				out.endBeginTag();
			} else {
				out.beginTag(SPAN);
			}
			{
				int currentDepth = out.getDepth();
				try {
					listControl.getItemContentRenderer().write(context, out, listItem);
				} catch (Throwable itemError) {
					try {
						out.endAll(currentDepth);

						produceLabelRenderingError(context, out, listControl, itemError);
					} catch (Throwable inner) {
						// In the rare case of catastrophe better throw the original.
						throw itemError;
					}
				}
			}
			if (isSelectable) {
				out.endTag(ANCHOR);
			} else {
				out.endTag(SPAN);
			}
		}
		out.endTag(LI);
	}

	private void produceLabelRenderingError(DisplayContext context, TagWriter out, ListControl listControl,
			Throwable itemError) throws IOException {
		RuntimeException labelRenderingError = ExceptionUtil.createException(
			"Error occured during rendering of item of list '" + getListFieldName(listControl) + "'.",
			Collections.singletonList(itemError));
		listControl.produceErrorOutput(context, out, labelRenderingError);
	}

	private String getListFieldName(ListControl view) {
		ListField listField = view.getListField();
		return listField != null ? listField.getName() : "listField";
	}

	private void writeCssClasses(TagWriter out, Object listItem, boolean isSelectable, boolean isSelected,
			boolean isFocused)
			throws IOException {
		out.beginCssClasses();
		adornItemCss(out, listItem, isSelectable, isSelected, isFocused);
		out.endCssClasses();
	}

	private void adornItemCss(TagWriter out, Object listItem, boolean isSelectable, boolean isSelected,
			boolean isFocused) throws IOException {
		for (ListItemCssAdorner listItemCssAdorner : _listItemAdorners) {
			listItemCssAdorner.addClasses(out, listItem, isSelected, isSelectable, isFocused);
		}
	}

	private void writeFixedOptionMarker(TagWriter out, Object listItem, boolean isSelectable, boolean isSelected,
			boolean isFocused) throws IOException {
		_fixedOptionMarker.markFixed(out, listItem, isSelected, isSelectable, isFocused);
	}
}
