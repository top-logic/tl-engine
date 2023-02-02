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

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.layoutRenderer.LayoutControlAdapterRenderer;

/**
 * The class {@link LayoutControlAdapter} id an adapter to use an ordinary {@link Control} in the
 * {@link LayoutControl} hierarchy.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutControlAdapter extends AbstractLayoutControl<LayoutControlAdapter> {

	private final HTMLFragment view;

	private String _customCssClass;

	/**
	 * @param aView
	 *        the view which can be used to render the content of this {@link Control}. must not be
	 *        <code>null</code>
	 * @see AbstractLayoutControl#AbstractLayoutControl(Map)
	 */
	protected LayoutControlAdapter(HTMLFragment aView, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);

		assert aView != null: "The given view must not be null";
		this.view = aView;
	}

	/**
	 * @see LayoutControlAdapter#LayoutControlAdapter(HTMLFragment, Map)
	 */
	public LayoutControlAdapter(HTMLFragment aView) {
		this(aView, Collections.<String, ControlCommand> emptyMap());
	}
	
	@Override
	public Object getModel() {
		return null;
	}

	/**
	 * A custom CSS class to allow modifying e.g. the background of this {@link LayoutControl}.
	 */
	public String getCssClass() {
		return _customCssClass;
	}

	/**
	 * @see #getCssClass()
	 */
	public void setCssClass(String cssClass) {
		_customCssClass = cssClass;
	}

	/**
	 * An {@link LayoutControlAdapter} does not have children.
	 * 
	 * @see CompositeControl#getChildren()
	 */
	@Override
	public List<? extends LayoutControl> getChildren() {
		return Collections.emptyList();
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append(_customCssClass);
	}

	/**
	 * Returns a view which can be used to render the content of this
	 * {@link Control}. Never <code>null</code>.
	 */
	public final HTMLFragment getWrappedView() {
		return this.view;
	}

	@Override
	protected ControlRenderer<? super LayoutControlAdapter> createDefaultRenderer() {
		return LayoutControlAdapterRenderer.INSTANCE;
	}

	@Override
	public LayoutControlAdapter self() {
		return this;
	}

	/**
	 * Wraps the given {@link Control} into a {@link LayoutControl}.
	 * 
	 * <p>
	 * If the given {@link Control} is already a {@link LayoutControl} then nothing is changed.
	 * </p>
	 */
	public static LayoutControl wrap(Control control) {
		if (control == null || control instanceof LayoutControl) {
			return (LayoutControl) control;
		} else {
			return new LayoutControlAdapter(control);
		}
	}

}
