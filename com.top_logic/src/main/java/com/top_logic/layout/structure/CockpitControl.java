/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.layoutRenderer.CockpitRenderer;
import com.top_logic.util.Utils;

/**
 * The class {@link CockpitControl} contains a {@link LayoutControl} as content which is normally
 * displayed.
 * 
 * <p>
 * Any of the child controls of a {@link CockpitControl} can be maximized to the full size of the
 * {@link CockpitControl}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CockpitControl extends WrappingControl<CockpitControl> {

	/**
	 * Observable property that stores the currently maximized content.
	 * 
	 * @see #getMaximizedControl()
	 */
	public static EventType<MaximizedControlListener, CockpitControl, LayoutControl> MAXIMIZED_CONTENT =
		new EventType<>("maximizedContent") {
			@Override
			public Bubble dispatch(MaximizedControlListener listener, CockpitControl sender, LayoutControl oldValue,
					LayoutControl newValue) {
				listener.notifyMaximizedControlChanged(sender, oldValue, newValue);
				return Bubble.BUBBLE;
			}
		};

	/**
	 * The control which is currently displayed maximized, or <code>null</code> if no content is
	 * currently maximized.
	 */
	private LayoutControl _maximizedControl;

	/**
	 * The {@link LayoutData} of the currently displayed maximized {@link LayoutControl} before it
	 * was maximized.
	 */
	private LayoutData _normalConstraint;

	/**
	 * @see AbstractLayoutControl#AbstractLayoutControl(Map)
	 */
	protected CockpitControl(Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
	}

	/**
	 * Creates a new {@link CockpitControl}.
	 */
	public CockpitControl() {
		this(Collections.<String, ControlCommand> emptyMap());
	}

	/**
	 * The {@link LayoutControl} which is currently maximized or <code>null</code>, if no
	 * {@link LayoutControl} is currently maximized.
	 * 
	 * <p>
	 * This property is observable through the {@link #MAXIMIZED_CONTENT} property key.
	 * </p>
	 * 
	 * @see #addListener(EventType, PropertyListener)
	 */
	public LayoutControl getMaximizedControl() {
		return _maximizedControl;
	}

	/**
	 * Maximizes the given content control.
	 * 
	 * @param content
	 *        The content to maximize. If another content is currently maximized, it is put back to
	 *        normalized state before.
	 * @return Whether the the maximized state of the given control has changed.
	 * 
	 * @see #normalize(LayoutControl)
	 */
	public boolean maximize(LayoutControl content) {
		LayoutControl maximizedBefore = _maximizedControl;
		if (Utils.equals(content, maximizedBefore)) {
			return false;
		}
		restoreOldControl();
		_maximizedControl = content;
		_normalConstraint = content.getConstraint();
		content.setConstraint(getChildControl().getConstraint());
		notifyListeners(MAXIMIZED_CONTENT, this, maximizedBefore, content);
		requestRepaint();
		return true;
	}

	/**
	 * Normalize the state of the given control, if it is currently maximized.
	 * 
	 * <p>
	 * If the given control is not currently the {@link #getMaximizedControl()}, the call is
	 * ignored.
	 * </p>
	 * 
	 * @param control
	 *        The content to restore its state to normal.
	 * @return Whether the maximized state of the given control has changed.
	 * 
	 * @see #maximize(LayoutControl)
	 */
	public boolean normalize(LayoutControl control) {
		LayoutControl maximizedBefore = _maximizedControl;
		if (maximizedBefore != control) {
			return false;
		}

		restoreOldControl();
		_maximizedControl = null;
		_normalConstraint = null;
		notifyListeners(MAXIMIZED_CONTENT, this, maximizedBefore, null);
		requestRepaint();
		return true;
	}

	/**
	 * Restores the currently maximized control to its old constraint.
	 */
	private void restoreOldControl() {
		LayoutControl theMaximized = getMaximizedControl();
		if (theMaximized != null) {
			theMaximized.setConstraint(_normalConstraint);
		}
	}

	/**
	 * Searches the next {@link CockpitControl} on the path form the given {@link LayoutControl} to
	 * the root of this control tree.
	 * 
	 * @param control
	 *        The {@link LayoutControl} to search the next {@link CockpitControl} for, must not be
	 *        <code>null</code>.
	 * @return The next {@link CockpitControl} on the way to the root, or <code>null</code> if no
	 *         ancestor {@link CockpitControl} is found.
	 */
	public static CockpitControl getCockpitFor(LayoutControl control) {
		if (control == null) {
			throw new IllegalArgumentException("Argument must not be 'null'.");
		}
		LayoutControl parent = control.getParent();
		while (parent != null && !(parent instanceof CockpitControl)) {
			parent = parent.getParent();
		}
		return (CockpitControl) parent;
	}

	@Override
	protected ControlRenderer<? super CockpitControl> createDefaultRenderer() {
		return CockpitRenderer.INSTANCE;
	}

	@Override
	public CockpitControl self() {
		return this;
	}
}
