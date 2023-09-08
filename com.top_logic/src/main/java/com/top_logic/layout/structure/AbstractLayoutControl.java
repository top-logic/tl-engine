/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.util.Utils;

/**
 * The class {@link AbstractLayoutControl} is a base class for classes implementing
 * {@link LayoutControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractLayoutControl<I extends AbstractLayoutControl<?>> extends AbstractControlBase
		implements LayoutControl {
    
	/**
	 * Property name for observing changes to the {@link #getParent()} property.
	 * 
	 * @see #addListener(EventType, PropertyListener)
	 * @see ParentChangedListener
	 */
	public static final EventType<ParentChangedListener, LayoutControl, LayoutControl> PARENT_PROPERTY =
		new NoBubblingEventType<>("parent") {

			@Override
			protected void internalDispatch(ParentChangedListener listener, LayoutControl sender,
					LayoutControl oldValue, LayoutControl newValue) {
				listener.handleParentChanged(sender, oldValue, newValue);
			}

		};

    /**
     * The Renderer used to write the control
     */
	private ControlRenderer<? super I> renderer;

	/**
	 * the parent of this {@link LayoutControl}. may be <code>null</code> if no parent was set.
	 */
	private LayoutControl parent;

	/** the {@link LayoutData} of this {@link LayoutControl}. never <code>null</code>. */
	private LayoutData layoutConstraint;

	private boolean resizable;

	/**
	 * Default css class for all {@link LayoutControl}
	 */
	private static final String LAYOUT_CONTROL_CSS_CLASS = "layoutControl";

	/**
	 * Creates a new AbstractLayoutControl.
	 * @param commandsByName
	 *        map of {@link ControlCommand}s accessible by this control
	 */
	protected AbstractLayoutControl(Map<String, ControlCommand> commandsByName) {
        super(commandsByName);
		resizable = true;
		this.layoutConstraint = DefaultLayoutData.NO_SCROLL_CONSTRAINT;
    }

	void initParent(LayoutControl newParent) {
		LayoutControl oldParent = this.parent;
		
		assert newParent != null : "The parent must not be null.";
		assert oldParent == null : "Already part of another hierarchy.";
		this.parent = newParent;

		parentChanged(oldParent, newParent);
	}

	void resetParent() {
		LayoutControl oldParent = this.parent;
		LayoutControl newParent = null;
		
		this.parent = newParent;

		parentChanged(oldParent, newParent);
	}

	/**
	 * Notifies about linking/unlinking this control to/from a hierarchy.
	 * 
	 * @param oldParent
	 *        The parent before.
	 * @param newParent
	 *        The new parent control.
	 * 
	 * @see #getParent()
	 */
	private void parentChanged(LayoutControl oldParent, LayoutControl newParent) {
		if (!Utils.equals(newParent, oldParent)) {
			handleAnchestorChange(this, oldParent, newParent);
		}
	}

	/**
	 * Local hook for observing {@link #PARENT_PROPERTY} changes.
	 * 
	 * @param target
	 *        The {@link LayoutControl} whose parent has changed.
	 * @param oldParent
	 *        The {@link #getParent()} before.
	 * @param newParent
	 *        The new value of {@link #getParent()}.
	 */
	protected void handleAnchestorChange(LayoutControl target, LayoutControl oldParent, LayoutControl newParent) {
		notifyListeners(PARENT_PROPERTY, target, oldParent, newParent);
		
		for (LayoutControl child : getChildren()) {
			((AbstractLayoutControl<?>) child).handleAnchestorChange(target, oldParent, newParent);
		}
	}

	/**
     * This method sets the renderer.
     *
     * @param    renderer    The renderer to set.
     */
	public final void setRenderer(ControlRenderer<? super I> renderer) {
        this.renderer = renderer;
    }
    
    /**
     * This method returns the renderer.
     * 
     * @return    Returns the renderer.
     */
	public final ControlRenderer<? super I> getRenderer() {
		if (renderer == null) {
			renderer = createDefaultRenderer();
		}
		return this.renderer;
    }

	/**
	 * Creates a default renderer for this control instance.
	 */
	protected abstract ControlRenderer<? super I> createDefaultRenderer();

	/**
	 * @see com.top_logic.layout.structure.LayoutControl#getParent()
	 */
	@Override
	public final LayoutControl getParent() {
		return this.parent;
	}

	@Override
	public LayoutData getConstraint() {
		return this.layoutConstraint;
	}

	/**
	 * This method sets the {@link LayoutData} of this {@link LayoutControl}
	 */
	@Override
	public void setConstraint(LayoutData configuredConstraint) {
		if (layoutConstraint.equals(configuredConstraint)) {
			return;
		}
		this.layoutConstraint = configuredConstraint;
		requestRepaint();
	}

	@Override
	public void setSize(DisplayDimension width, DisplayDimension height) {
		layoutConstraint = layoutConstraint.resized(width, height);
	}

	@Override
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	@Override
	@TemplateVariable("isResizable")
	public boolean isResizable() {
		return resizable;
	}

	/**
	 * This method returns whether <code>aLayoutControl</code> is hereditarily a child of this
	 * {@link LayoutControl}.
	 */
	public boolean isDerivedChild(LayoutControl aLayoutControl) {
		LayoutControl itsParent = aLayoutControl.getParent();
		if (itsParent == null) {
			return false;
		} else {
			return itsParent == this || isDerivedChild(itsParent);
		}
	}

	/**
	 * returns the root {@link LayoutControl} of this control, i.e. the method climes up the
	 * {@link #getParent()} until a {@link LayoutControl} without parent is reached.
	 */
	public final LayoutControl getRoot() {
		LayoutControl root = this;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		return root;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}
	
	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// since there are no updates there is nothing to do here.
	}

	@Override
	public final boolean isVisible() {
		return true;
	}

	/**
	 * This method is a hook for subclasses to do something directly before rendering
	 * 
	 * @param context
	 *            the {@link DisplayContext} which is used for rendering.
	 */
	protected void beforeRendering(DisplayContext context) {
		// nothing to do in general
	}

	@Override
	protected final void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		beforeRendering(context);

		this.getRenderer().write(context, out, self());

		afterRendering(context);
	}

	/**
	 * This instance cast to the implementation type parameter.
	 */
	@Override
	public abstract I self();

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		getRenderer().appendControlCSSClasses(out, self());
		writeScrollingClass(out);
	}

	@Override
	protected String getTypeCssClass() {
		return LAYOUT_CONTROL_CSS_CLASS;
	}

	/**
	 * This method is a hook for subclasses to do something directly after rendering.
	 * 
	 * @param context
	 *            the {@link DisplayContext} which was used for rendering.
	 */
	protected void afterRendering(DisplayContext context) {
		// nothing to do in general
	}

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

	@Override
	protected void writeErrorFragment(DisplayContext context, TagWriter out, String errorReason) {
		// No error fragments shall be generated, because the client-side layouting algorithm cannot
		// handle it.
	}

	/**
	 * Whether there are no child layout controls.
	 */
	public boolean isLeafControl() {
		return getChildren().isEmpty();
	}

	/**
	 * Adds the overflow attribute to the given <code>style</code>.
	 * 
	 * @return The value for the <code>overflow</code> style property, or null, if no such property
	 *         should be set.
	 */
	public Scrolling getScrolling() {
		if (isLeafControl()) {
			return getConstraint().getScrollable();
		} else {
			return null;
		}
	}

	/**
	 * Writes the scrolling CSS class depending on {@link #getScrolling()}.
	 */
	public void writeScrollingClass(Appendable out) throws IOException {
		// Wrap overflow style into a CSS class to allow dynamic overrides using CSS rules.
		// This is especially relevant for switching the overflow style in minimized layouts.
		Scrolling scrolling = getScrolling();
		if (scrolling != null) {
			switch (scrolling) {
				case AUTO:
					out.append("lcAuto");
					break;
				case NO:
					out.append("lcHidden");
					break;
				case YES:
					out.append("lcScroll");
					break;
			}
		}
	}

}
