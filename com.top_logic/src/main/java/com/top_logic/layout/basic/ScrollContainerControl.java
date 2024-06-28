/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * A {@link ScrollContainerControl} is a control which serves as scroll container for its content.
 * 
 * <p>
 * This controls writes an absolute positioned {@link HTMLConstants#DIV} which has the size of its
 * parent container. This control can scroll its viewport, such that a given element of its content
 * is visible.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ScrollContainerControl extends AbstractControlBase {

	/** Value of the {@link HTMLConstants#STYLE_ATTR} of the element written by this control. */
	private static final String STYLE_ATTRIBUTE_VALUE;
	static {
		StringBuilder b = new StringBuilder();
		/* Do not set height and width 100%, because this also includes padding which is not wanted. */
		b.append("top:0;");
		b.append("bottom:0;");
		b.append("left:0;");
		b.append("right:0;");
		/* must set position attribute to be able to */
		b.append("position:absolute;");
		STYLE_ATTRIBUTE_VALUE = b.toString();
	}

	/** @see #getContent() */
	private HTMLFragment _content = Fragments.empty();

	/** @see #scrollToRange(ScrollRange) */
	private ScrollRange _scrollRange;

	/**
	 * Creates a new {@link ScrollContainerControl}.
	 */
	public ScrollContainerControl() {
		super();
	}

	@Override
	public Object getModel() {
		return null;
	}

	/* Show scroll bar if content does not fit. */
	@Override
	protected String getTypeCssClass() {
		return FormConstants.OVERFLOW_AUTO_CLASS;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.writeAttribute(STYLE_ATTR, STYLE_ATTRIBUTE_VALUE);
		out.endBeginTag();
		getContent().write(context, out);
		out.endTag(DIV);
		if (hasScrollRange()) {
			String scrollId = _scrollRange.getMainElementId();
			if (scrollId != null) {
				HTMLUtil.beginScriptAfterRendering(out);
				appendScrollScript(out, _scrollRange.getMinElementId(), scrollId, _scrollRange.getMaxElementId());
				HTMLUtil.endScriptAfterRendering(out);
			}
			resetScrollRange();
		}
	}

	/**
	 * Calls the client side script function to execute scrolling.
	 */
	void appendScrollScript(Appendable out, String minId, String mainId, String maxId) throws IOException {
		out.append("services.form.ScrollContainerControl.scrollTo('");
		out.append(getID());
		out.append("','");
		out.append(mainId);
		out.append("'");
		if (minId == null) {
			out.append(",null");
		} else {
			out.append(",'");
			out.append(minId);
			out.append("'");
		}
		if (maxId == null) {
			out.append(",null");
		} else {
			out.append(",'");
			out.append(maxId);
			out.append("'");
		}
		out.append(");");
	}

	/**
	 * Returns the inner {@link HTMLFragment}, i.e. the actual content visible on the GUI. Each
	 * element written by that {@link HTMLFragment} can be made displayed by setting the appropriate
	 * {@link #scrollToRange(ScrollRange) scroll range}.
	 */
	public final HTMLFragment getContent() {
		return _content;
	}

	/**
	 * Sets the actual content of this control.
	 */
	public final void setContent(HTMLFragment content) {
		if (!_content.equals(content)) {
			_content = content;
			requestRepaint();
		}
	}

	/**
	 * Scrolls the viewport once such that the elements, defined by the given {@link ScrollRange}
	 * are visible.
	 * 
	 * <p>
	 * This method must be called for each "scroll job".
	 * </p>
	 * 
	 * @param scrollRange
	 *        The range defining the elements to be visible.
	 */
	public void scrollToRange(ScrollRange scrollRange) {
		_scrollRange = scrollRange;
	}

	/**
	 * Resets the scroll range. If method is called, a formerly requested "scroll" is cancelled.
	 */
	public void resetScrollRange() {
		_scrollRange = null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		return hasScrollRange();
	}

	private boolean hasScrollRange() {
		return _scrollRange != null;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		String mainId = _scrollRange.getMainElementId();
		if (mainId != null) {
			String minId = _scrollRange.getMinElementId();
			String maxId = _scrollRange.getMaxElementId();
			actions.add(new JSSnipplet(new ScrollScriptDisplayValue(this, minId, mainId, maxId)));
		}
		resetScrollRange();
	}

	private static class ScrollScriptDisplayValue extends AbstractDisplayValue {

		private final String _mainId;

		private final String _minId;

		private final String _maxId;

		private final ScrollContainerControl _control;

		public ScrollScriptDisplayValue(ScrollContainerControl control, String minElementId, String mainElementId,
				String maxElementId) {
			_control = control;
			_minId = minElementId;
			_mainId = mainElementId;
			_maxId = maxElementId;
		}

		@Override
		public void append(DisplayContext context, Appendable out) throws IOException {
			_control.appendScrollScript(out, _minId, _mainId, _maxId);
		}

	}

	/**
	 * Range defining the range of the content which should be displayed.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ScrollRange {

		/**
		 * Defines the id of an element "before" the element defined by {@link #getMainElementId()}.
		 * If it is possible the element with the given id is made visible.
		 * 
		 * <p>
		 * The value is just relevant if {@link #getMainElementId()} is not <code>null</code>.
		 * </p>
		 * 
		 * @return The id of the "minimum" element to made visible. <code>null</code> if there is no
		 *         such element. In this case the element returned by {@link #getMainElementId()} is
		 *         the minimal displayed element.
		 * 
		 * @see #getMainElementId()
		 */
		String getMinElementId();

		/**
		 * Defines the id of the element which is in the focus of the scroll range. The element
		 * defined by that id will be made visible.
		 * 
		 * @return The client side id of an element rendered by the inner content of the
		 *         {@link ScrollContainerControl}. If <code>null</code> no scrolling is executed.
		 */
		String getMainElementId();

		/**
		 * Defines the id of an element "after" the element defined by {@link #getMainElementId()}.
		 * If it is possible the element with the given id is made visible.
		 * 
		 * <p>
		 * The value is just relevant if {@link #getMainElementId()} is not <code>null</code>.
		 * </p>
		 * 
		 * @return The id of the "maximum" element to made visible. <code>null</code> if there is no
		 *         such element. In this case the element returned by {@link #getMainElementId()} is
		 *         the maximal displayed element.
		 * 
		 * @see #getMainElementId()
		 */
		String getMaxElementId();
	}

}
