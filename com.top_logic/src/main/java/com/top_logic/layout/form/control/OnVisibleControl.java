/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Collections;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * A {@link OnVisibleControl} is a control whose visibility is exactly that of a given
 * {@link FormMember}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OnVisibleControl extends AbstractCompositeControl<OnVisibleControl> {

	/**
	 * Listener attached to the given {@link #_member}
	 * 
	 * @see VisibilityListenerImpl
	 */
	private final VisibilityListener visibilityListener = new VisibilityListenerImpl();

	/**
	 * the {@link FormMember} which has the same visibility as this control
	 */
	final FormMember _member;

	/**
	 * Creates a new {@link OnVisibleControl} which reacts on the visibility of the given member,
	 * i.e. {@link FormMember#isVisible()} and {@link OnVisibleControl#isVisible()} have the same
	 * value if this control is attached.
	 * 
	 * @param member
	 *        the {@link FormMember} with the same visibility. must not be <code>null</code>
	 */
	public OnVisibleControl(FormMember member) {
		super(Collections.<String, ControlCommand> emptyMap());
		_member = member;
		if (_member == null) {
			throw new IllegalArgumentException("Given " + FormMember.class.getName() + " must not be null");
		}
	}

	@Override
	public FormMember getModel() {
		return _member;
	}

	@Override
	public boolean isVisible() {
		return _member.isVisible();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		_member.addListener(FormMember.VISIBLE_PROPERTY, visibilityListener);
	}

	@Override
	protected void internalDetach() {
		_member.removeListener(FormMember.VISIBLE_PROPERTY, visibilityListener);
		super.internalDetach();
	}

	@Override
	public OnVisibleControl self() {
		return this;
	}

	/**
	 * Listener which forces the enclosing control to redraw if the visibility of the model, this
	 * listener is attached to, changes.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private final class VisibilityListenerImpl implements VisibilityListener {

		VisibilityListenerImpl() {
			// default constructor
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			if (sender != OnVisibleControl.this._member) {
				/* Don't care about change of visibility of member different from the observed one.
				 * In case the observed member is a FormGroup, also visibility changes of inner
				 * FormFields are propagated. */
				return Bubble.BUBBLE;
			}
			OnVisibleControl.this.requestRepaint();
			return Bubble.BUBBLE;
		}

	}
}

