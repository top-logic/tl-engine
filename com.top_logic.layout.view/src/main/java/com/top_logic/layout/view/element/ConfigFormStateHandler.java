/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
import com.top_logic.layout.view.form.StateHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * The unsaved changes of a {@link ConfigFormControl}, as the view layer asks about them before they
 * are lost.
 *
 * <p>
 * Takes part in the protocol a {@code <form>} takes part in, in the same two places: as a
 * {@link VetoListener} of the channel the form's item comes from, it blocks the write of another
 * item while the form {@link ConfigFormControl#isDirty() holds unsaved changes}; and it reports
 * those changes to the {@link DirtyChannel} of the enclosing scope, e.g. a frame of a
 * {@code <tile-stack>}, so that leaving the scope asks about them. Answering the question with
 * "save" runs the form's save, including the action it was given for saving; "discard" drops the
 * working copy.
 * </p>
 */
public class ConfigFormStateHandler implements StateHandler {

	private final ConfigFormControl _form;

	private final ViewChannel _input;

	private final DirtyChannel _dirtyChannel;

	private final VetoListener _veto = new VetoListener() {
		@Override
		public List<StateHandler> checkVeto(ViewChannel sender, Object oldValue, Object newValue) {
			return checkDirty(sender);
		}

		@Override
		public List<StateHandler> checkDirty(ViewChannel sender) {
			return isDirty() ? List.of(ConfigFormStateHandler.this) : List.of();
		}
	};

	/**
	 * Creates a {@link ConfigFormStateHandler} and installs it for as long as the form exists.
	 *
	 * @param form
	 *        The form whose unsaved changes are reported.
	 * @param input
	 *        The channel the form's item comes from, whose writes are blocked while the form holds
	 *        unsaved changes. <code>null</code> for a form editing an item of its own.
	 * @param dirtyChannel
	 *        The dirty-tracked scope enclosing the form, <code>null</code> for none.
	 */
	public ConfigFormStateHandler(ConfigFormControl form, ViewChannel input, DirtyChannel dirtyChannel) {
		_form = form;
		_input = input;
		_dirtyChannel = dirtyChannel;

		if (_input != null) {
			_input.addVetoListener(_veto);
			_form.addCleanupAction(() -> _input.removeVetoListener(_veto));
		}
		if (_dirtyChannel != null) {
			Runnable observation = _form.observeDirty(this::publish);
			_form.addCleanupAction(() -> {
				observation.run();
				_dirtyChannel.removeHandler(this);
			});
			publish();
		}
	}

	private void publish() {
		_dirtyChannel.updateState(this, isDirty());
	}

	/**
	 * The form whose unsaved changes this handler reports.
	 */
	public ConfigFormControl getForm() {
		return _form;
	}

	@Override
	public boolean isDirty() {
		return _form.isDirty();
	}

	@Override
	public boolean hasErrors() {
		return _form.hasVisibleErrors();
	}

	/**
	 * Saves the form like its Save command does.
	 *
	 * @throws TopLogicException
	 *         If the form refuses to be saved, e.g. because a field shows an error. The form then
	 *         stays in edit mode with the problems on display.
	 */
	@Override
	public void executeSave() {
		HandlerResult result = _form.save();
		if (!result.isSuccess()) {
			throw new TopLogicException(result.getErrorTitle());
		}
	}

	@Override
	public void executeDiscard() {
		_form.discard();
	}

	@Override
	public String getDescription() {
		return new ConfigLabelProvider().getLabel(_form.getItem());
	}

}
