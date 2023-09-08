/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.layoutRenderer.WrappingControlRenderer;

/**
 * The class {@link WrappingControl} is an {@link AbstractLayoutControl} which contains exactly one
 * child.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class WrappingControl<I extends WrappingControl<?>> extends AbstractLayoutControl<I> {

	/** the wrapped {@link LayoutControl} */
	private AbstractLayoutControl<?> wrappedControl;

	/**
	 * Creates a plain {@link WrappingControl} with empty content.
	 */
	public WrappingControl() {
		this(Collections.emptyMap());
	}

	/**
	 * Constructor to call from sub-classes {@link WrappingControl}.
	 *
	 * @param commandsByName
	 *        See {@link AbstractControlBase#AbstractControlBase(Map)}.
	 */
	protected WrappingControl(Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
	}

	@Override
	public Object getModel() {
		return null;
	}

	/**
	 * Returns an unmodifiable list containing exactly the wrapped control if there is one or
	 * {@link Collections#EMPTY_LIST} if no wrapped control was set.
	 * 
	 * @see com.top_logic.layout.CompositeControl#getChildren()
	 */
	@Override
	public final List<? extends LayoutControl> getChildren() {
		return CollectionUtilShared.singletonOrEmptyList(wrappedControl);
	}

	/**
	 * This method sets the {@link LayoutControl} which is wrapped by this {@link WrappingControl}
	 * 
	 * @param aLayoutControl
	 *        the wrapped control
	 * @return The former child control.
	 */
	public LayoutControl setChildControl(LayoutControl aLayoutControl) {
		AbstractLayoutControl<?> old = wrappedControl;
		if (old != null) {
			old.detach();
			old.resetParent();
		}
		wrappedControl = (AbstractLayoutControl<?>) aLayoutControl;
		if (wrappedControl != null) {
			wrappedControl.initParent(this);
		}

		return old;
	}

	/**
	 * This method returns the wrapped control.
	 */
	@TemplateVariable("child")
	public final LayoutControl getChildControl() {
		return wrappedControl;
	}

	/**
	 * Service method to write the {@link #getChildControl() child} of this
	 * control. If actually calls
	 * {@link Control#write(DisplayContext, TagWriter)} on it.
	 * 
	 * @param context
	 *        the context in which rendering occurs
	 * @param out
	 *        the {@link TagWriter} to write content to.
	 * 
	 * @throws IOException
	 *         iff the child control throws some during writing.
	 * @throws NullPointerException
	 *         if there is no child control.
	 */
	public void writeChildControl(DisplayContext context, TagWriter out) throws IOException {
		getChildControl().write(context, out);
	}

	@Override
	protected ControlRenderer<? super I> createDefaultRenderer() {
		return WrappingControlRenderer.INSTANCE;
	}

}
