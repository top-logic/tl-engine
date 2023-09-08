/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Factory;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.model.AbstractButtonField;

/**
 * {@link Factory} class to create a {@link GenericPropertyListener} which reacts on
 * {@link FormMember#DISABLED_PROPERTY} and {@link FormMember#IMMUTABLE_PROPERTY} events on a
 * {@link FormMember}. The resulting listener is attached to the observed {@link FormMember} and
 * updates its {@link CommandModel#getNotExecutableReasonKey()} property. Bubbling events are
 * ignored.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NotExecutableListener {

	public interface Listener {
		// pure sum interface

		void addAsListener(FormMember member);

		void removeAsListener(FormMember member);
	}

	/**
	 * Creates a {@link GenericPropertyListener} which updates the
	 * {@link CommandModel#getNotExecutableReasonKey()} of one model with the given key.
	 * 
	 * @since 5.7.4
	 * 
	 * @param reasonKey
	 *        The I18N key to set as {@link CommandModel#getNotExecutableReasonKey()}.
	 * @param command
	 *        The model for update. must not be <code>null</code>
	 */
	public static Listener createNotExecutableListener(ResKey reasonKey, AbstractButtonField command) {
		return new OneModelNotExecutableListener(reasonKey, command);
	}

	/**
	 * Convenience variant of {@link #createNotExecutableReasonKey(ResKey, AbstractButtonField...)}.
	 */
	public static Listener createNotExecutableReasonKey(ResKey reasonKey, Collection<AbstractButtonField> commands) {
		return createNotExecutableReasonKey(reasonKey, commands.toArray(new AbstractButtonField[0]));
	}

	/**
	 * Creates a {@link GenericPropertyListener} which updates the
	 * {@link CommandModel#getNotExecutableReasonKey()} of many models with the given key.
	 * 
	 * @since 5.7.4
	 * 
	 * @param reasonKey
	 *        The I18N key to set as {@link CommandModel#getNotExecutableReasonKey()}.
	 * @param commands
	 *        The models for update. must not be or contain <code>null</code>
	 */
	public static Listener createNotExecutableReasonKey(ResKey reasonKey, AbstractButtonField... commands) {
		if (commands.length == 1) {
			return createNotExecutableListener(reasonKey, commands[0]);
		}
		return new ManyModelNotExecutableListener(reasonKey, commands);
	}

	private abstract static class AbstractNotExecutableListener implements Listener, DisabledPropertyListener,
			ImmutablePropertyListener {

		protected AbstractNotExecutableListener() {
			// nothing to do in general
		}

		FormMember _owner;

		@Override
		public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			if (_owner != sender) {
				// Only care for events occurred on the FormField this listener is attached to.
				return Bubble.BUBBLE;
			}
			setModelVisibility(!(Boolean) newValue);
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			if (_owner != sender) {
				// Only care for events occurred on the FormField this listener is attached to.
				return Bubble.BUBBLE;
			}
			if (newValue.booleanValue()) {
				updateModels();
			}
			return Bubble.BUBBLE;
		}

		@Override
		public void addAsListener(FormMember member) {
			if (_owner != null) {
				throw new IllegalStateException("Already attached to " + _owner);
			}
			_owner = member;
			member.addListener(FormMember.DISABLED_PROPERTY, this);
			member.addListener(FormMember.IMMUTABLE_PROPERTY, this);

		}

		@Override
		public void removeAsListener(FormMember member) {
			member.removeListener(FormMember.IMMUTABLE_PROPERTY, this);
			member.removeListener(FormMember.DISABLED_PROPERTY, this);
			_owner = null;
		}

		protected abstract void updateModels();

		protected abstract void setModelVisibility(boolean visible);

		protected abstract ResKey getReasonKey();

		protected final void setReasonKey(AbstractButtonField commandModel) {
			commandModel.setNotExecutableReasonKey(getReasonKey());
		}

	}

	private static class OneModelNotExecutableListener extends AbstractNotExecutableListener {

		private final AbstractButtonField _command;

		private final ResKey _reasonKey;

		OneModelNotExecutableListener(ResKey reasonKey, AbstractButtonField command) {
			_reasonKey = reasonKey;
			_command = command;
		}

		@Override
		protected void updateModels() {
			setReasonKey(_command);
		}

		@Override
		protected void setModelVisibility(boolean visible) {
			_command.setVisible(visible);
		}

		@Override
		protected ResKey getReasonKey() {
			return _reasonKey;
		}
	}

	private static class ManyModelNotExecutableListener extends AbstractNotExecutableListener {

		private final AbstractButtonField[] _commands;

		private final ResKey _reasonKey;

		ManyModelNotExecutableListener(ResKey reasonKey, AbstractButtonField... commands) {
			_reasonKey = reasonKey;
			_commands = commands;
		}

		@Override
		protected void updateModels() {
			for (AbstractButtonField commandModel : _commands) {
				setReasonKey(commandModel);
			}
		}

		@Override
		protected void setModelVisibility(boolean visible) {
			for (AbstractButtonField commandModel : _commands) {
				commandModel.setVisible(visible);
			}
		}

		@Override
		protected ResKey getReasonKey() {
			return _reasonKey;
		}
	}

}
