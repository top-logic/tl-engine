/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.AbstractConstantControlBase;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;

/**
 * {@link Control} that manages a list of child {@link HTMLFragment}s.
 * 
 * <p>
 * The appearance is controlled by a {@link DefaultControlRenderer} (see {@link #getRenderer()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractCompositeControl<I extends AbstractCompositeControl<?>>
		extends AbstractConstantControlBase implements CompositeControl {

	/**
	 * @see #getChildren()
	 */
	private List<HTMLFragment> lazyChildren;

	private List<HTMLFragment> unmodifiableView;
	
	/**
	 * The renderer which writes this control. It is an {@link DefaultControlRenderer} to ensure
	 * that the ID is written to the client.
	 * 
	 * @see #getRenderer()
	 */
	private ControlRenderer<? super I> renderer = DefaultSimpleCompositeControlRenderer.SPAN_INSTANCE;

	/**
	 * Constructor creates a new {@link AbstractCompositeControl}.
	 * 
	 * @see AbstractConstantControlBase#AbstractConstantControlBase(Map)
	 */
	public AbstractCompositeControl(Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
	}
	
	/**
	 * @see #getRenderer()
	 */
	public void setRenderer(ControlRenderer<? super I> newRenderer) {
		if (newRenderer == null) {
			throw new IllegalArgumentException("New Renderer must not be null");
		}
		boolean changed = newRenderer != this.renderer;
		if (changed) {
			this.renderer = newRenderer;
			requestRepaint();
		}
	}
	
	/**
	 * The renderer that displays this composite control.
	 */
	public Renderer<? super I> getRenderer() {
		return renderer;
	}

	/**
	 * Sets the children by deleting the previous children and adding all new children to the
	 * children list.
	 * 
	 * @param newChildren
	 *        List containing the new children of this control.
	 */
	protected void setChildren(List<? extends HTMLFragment> newChildren) {
		if (newChildren != null && newChildren.size() > 0) {
			initChildren();
			removeChildren();
			this.lazyChildren.addAll(newChildren);
		} else {
			if (this.lazyChildren != null) {
				this.lazyChildren.clear();
			}
		}
		requestRepaint();
	}


	/**
	 * Initializes the list of children.
	 */
	private void initChildren() {
		if (this.lazyChildren == null) {
			this.lazyChildren = new ArrayList<>();
			this.unmodifiableView = Collections.unmodifiableList(this.lazyChildren);
		}
	}
	
	/**
	 * Adds the given view to this composite and calls {@link AbstractControlBase#requestRepaint()}.
	 */
	public final void addChild(HTMLFragment child) {
		assert child != null : "Child must not be null.";
		initChildren();
		this.lazyChildren.add(child);
		requestRepaint();
	}
	
    /**
     * Adds the given view to this composite at the given index and
     * calls {@link AbstractControlBase#requestRepaint()}.
     */
	public final void addChild(int index, HTMLFragment child) {
		assert child != null : "Child must not be null.";
        initChildren();
		this.lazyChildren.add(index, child);
        requestRepaint();
    }
	
	/**
	 * Remove the given child.
	 * 
	 * @return Whether the given child was contained in the list of children.
	 */
	protected boolean removeChild(HTMLFragment child) {
		if (lazyChildren == null) {
			return false;
		}
		
		boolean result = lazyChildren.remove(child);
		if (result) {
			detachChild(child);
			requestRepaint();
		}
		return result;
	}

	/**
	 * Removes all children.
	 * 
	 */
	public void removeChildren() {
		if (lazyChildren != null) {
			while (lazyChildren.size() > 0) {
				HTMLFragment child = lazyChildren.remove(0);
				detachChild(child);
			}
			requestRepaint();
		}
	}
	
	/**
	 * The (unmodifiable) list of children {@link HTMLFragment}s that this {@link Control} is
	 * composed of.
	 */
	@Override
	public final List<? extends HTMLFragment> getChildren() {
		if (lazyChildren == null) {
			return Collections.emptyList();
		} else {
			return unmodifiableView;
		}
	}
	
	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();
		
		detachChildren();
	}

	/**
	 * Detaches all children, but does not remove them.
	 */
	protected void detachChildren() {
		if (this.lazyChildren != null) {
			for (int n = 0, cnt = lazyChildren.size(); n < cnt; n++) {
				HTMLFragment child = lazyChildren.get(n);
				detachChild(child);
			}
		}
	}
	
	/**
	 * Checks whether the given child can be detached and detaches it.
	 */
	private void detachChild(HTMLFragment child) {
		if (child instanceof Control) {
			((Control) child).detach();
		}
	}

	@Override
	protected final void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		renderer.write(context, out, self());
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		renderer.appendControlCSSClasses(out, self());
	}

	/**
	 * This instance as implementation type.
	 */
	@Override
	public abstract I self();

}
