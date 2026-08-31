/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigPendingEntries.PendingEntry;
import com.top_logic.layout.configedit.ConfigValidation.Violation;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * The control that puts a {@link ConfigFormModel}'s edit mode on screen: a {@link ConfigEditorControl}
 * over {@link ConfigFormModel#edited()}, framed by Edit, Apply, and Cancel.
 *
 * <p>
 * In view mode the editor writes straight through to the original item, and a single "Edit"
 * button {@link ConfigFormModel#startEditing() starts} a working copy. In edit mode the editor is
 * rebuilt over that copy instead, framed by "Apply" and "Cancel": Cancel
 * {@link ConfigFormModel#cancelEditing() drops} the copy, Apply runs {@link ConfigValidation} and
 * either {@link ConfigFormModel#apply() carries the copy over} or, if a violation was found,
 * leaves edit mode open and puts every violation on the field that caused it - via
 * {@link ConfigValidation#report(List, ConfigFieldIndex)} against the {@link ConfigFieldIndex}
 * the current editor filled while it was built. Every mode change rebuilds the editor over
 * {@link ConfigFormModel#edited()} - the editor itself never learns which of the two, original or
 * copy, it was handed.
 * </p>
 *
 * <p>
 * A violation naming a property the editor renders as no field of its own has nowhere to go -
 * {@link ConfigValidation#report(List, ConfigFieldIndex)} answers {@code false} for it - and is
 * shown on a line of its own between the editor and the buttons instead, so a refused Apply is
 * never a button that appears to do nothing.
 * </p>
 *
 * <p>
 * Passing {@code withEditMode = false} turns all of that off: the control is then a thin wrapper
 * around the editor over the item itself, with no buttons at all - today's write-through
 * behaviour, kept available for a caller (e.g. the view designer) that must not gain an edit mode
 * of its own just because it now goes through this class.
 * </p>
 */
public class ConfigFormControl extends ReactFormLayoutControl {

	private final ReactContext _context;

	private final ConfigFormModel _model;

	private final boolean _withEditMode;

	private final ConfigFieldIndex _index = new ConfigFieldIndex();

	/**
	 * The {@link ConfigFormModel} listener that rebuilds this control on every mode change,
	 * registered in the constructor and removed in {@link #onCleanup()}. Kept in a field, not
	 * created afresh at each of those two call sites, so {@link ConfigFormModel#removeListener(Runnable)}
	 * actually finds the very instance {@link ConfigFormModel#addListener(Runnable)} was given.
	 */
	private final Runnable _onModeChange = this::rebuild;

	/**
	 * Creates a {@link ConfigFormControl} with a full edit mode.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 */
	public ConfigFormControl(ReactContext context, ConfigurationItem config) {
		this(context, config, true);
	}

	/**
	 * Creates a {@link ConfigFormControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param withEditMode
	 *        Whether to render Edit/Apply/Cancel around the editor. {@code false} renders the
	 *        editor alone, writing straight through to {@code config} - today's behaviour.
	 */
	public ConfigFormControl(ReactContext context, ConfigurationItem config, boolean withEditMode) {
		super(context);
		_context = context;
		_model = new ConfigFormModel(config);
		_withEditMode = withEditMode;
		_model.addListener(_onModeChange);
		rebuild();
	}

	/**
	 * Rebuilds the editor and its surrounding buttons over {@link ConfigFormModel#edited()}.
	 *
	 * <p>
	 * Registered as the {@link ConfigFormModel}'s listener, so every mode change - Edit, Apply,
	 * Cancel - runs this without the button handlers having to call it themselves.
	 * {@link #apply()} deliberately does not run this on a refusal: a rebuild would discard the
	 * very field models the violations were just put on, leaving the user no way to see what was
	 * wrong.
	 * </p>
	 *
	 * <p>
	 * Disposes the old children directly with {@link ReactControl#cleanupTree()} rather than going
	 * through {@link com.top_logic.layout.react.control.ReactCompositeControl#replaceChildren(List)}:
	 * that method deliberately leaves disposal to the caller, for a container that must defer it -
	 * e.g. past an in-progress observer notification. Nothing here is deferred - the old editor and
	 * buttons are discarded on the spot, not cached for later reuse - so immediate
	 * {@code cleanupTree()} is the right call, the same way {@link ConfigListEditorControl}'s own
	 * rebuild already does it in this package.
	 * </p>
	 *
	 * <p>
	 * The rebuilt {@link ConfigEditorControl} is editable exactly while either
	 * {@link #_withEditMode} is off (the thin-wrapper, write-through case - it was always editable
	 * and stays so) or the model {@link ConfigFormModel#isEditMode() is in edit mode}. In every
	 * other case - {@link #_withEditMode} on, model in view mode - it is built read-only: every
	 * field non-editable and no collection action rendered, so a form with a mode never accepts a
	 * change outside of one.
	 * </p>
	 */
	private void rebuild() {
		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}
		getChildren().clear();
		_index.clear();

		boolean editable = !_withEditMode || _model.isEditMode();
		addChild(new ConfigEditorControl(_context, _model.edited(), Collections.emptySet(), false, _index, editable));

		if (_withEditMode) {
			if (_model.isEditMode()) {
				addChild(applyButton());
				addChild(cancelButton());
			} else {
				addChild(editButton());
			}
		}
	}

	/**
	 * A refused Apply, carrying the given reason.
	 *
	 * <p>
	 * The reason is set as the result's <em>title</em> - the snackbar's headline - and the details
	 * name what has to change, one line each. Without a title The React
	 * servlet builds the snackbar's headline from {@link HandlerResult#getErrorTitle()} and falls
	 * back to a generic "the command failed" when there is none - which is all the user would read,
	 * since the details it lists come from {@link HandlerResult#getErrorMessage()} and the
	 * exception's causes, not from the error list that {@link HandlerResult#error(ResKey)} fills.
	 * The reason stays in that list as well, so the result still counts as failed.
	 * </p>
	 */
	private static HandlerResult refusal(ResKey reason, List<ResKey> details) {
		HandlerResult result = new HandlerResult();
		result.setErrorTitle(reason);
		result.addErrorMessage(reason);
		for (ResKey detail : details) {
			result.addErrorMessage(detail);
		}
		return result;
	}

	/** A refused Apply whose reason is all there is to say. */
	private static HandlerResult refusal(ResKey reason) {
		return refusal(reason, List.of());
	}

	/**
	 * Refuses while some collection below still holds an entry nobody confirmed, and says so at that
	 * entry as well as at the form.
	 *
	 * <p>
	 * A pending entry is not in the configuration, so {@link ConfigValidation} cannot see it, and
	 * applying would rebuild the form over the original and drop it. The user would watch what they
	 * typed disappear with no explanation - the same silent discard a rejected input once was.
	 * </p>
	 *
	 * <p>
	 * The message goes on the entry's own key field, where the eye already is, rather than only at
	 * the form: with several collections open there would otherwise be nothing saying which entry is
	 * meant.
	 * </p>
	 *
	 * @return Whether an entry was found, and Apply must therefore not proceed.
	 */
	private boolean refuseUnconfirmedEntries() {
		List<PendingEntry> pending = _index.pending();
		if (pending.isEmpty()) {
			return false;
		}
		for (PendingEntry entry : pending) {
			entry.setKeyFieldError(I18NConstants.ERROR_CONFIRM_OR_DISCARD_ENTRY);
		}
		return true;
	}

	/**
	 * Checks {@link ConfigFormModel#edited()} against {@link ConfigValidation} and either
	 * {@link ConfigFormModel#apply() carries it over} - which leaves edit mode via the model's
	 * listener - or puts every violation found on the field that caused it, leaving edit mode
	 * untouched and this control unrebuilt.
	 *
	 * <p>
	 * Takes back every violation the previous refusal placed before checking anything, via
	 * {@link ConfigFieldIndex#clearModelErrors()}: a violation still holding is placed again a few
	 * lines below, one the user has meanwhile fixed is gone. Without that, a violation could
	 * outlive its own cause - a cross-item constraint is fixed by editing <em>one</em> of the two
	 * fields it flagged, leaving the other one showing an error nothing will ever clear.
	 * </p>
	 *
	 * <p>
	 * Refuses before checking while a field still rejects what was typed into it:
	 * {@link ConfigValidation#check(ConfigurationItem)} inspects the configuration, and a field
	 * that rejected its raw input never wrote to the configuration in the first place - it keeps
	 * the rejected text on screen while the item still holds the last accepted value. Applying
	 * would therefore look clean, leave edit mode, and throw the typed text away without ever
	 * saying so. The classic declarative form refuses the same way, through
	 * {@code FormContext#checkAll()}. That refusal says so at form level too - a refused Apply must
	 * never look like a button that does nothing, whatever the reason for the refusal.
	 * </p>
	 *
	 * <p>
	 * A violation that {@link ConfigValidation#report(List, ConfigFieldIndex)} could not place -
	 * a property the editor renders as no field of its own, e.g. a
	 * {@link com.top_logic.basic.config.annotation.Hidden @Hidden} one or one named by a
	 * constraint that reached into another item - is shown at form level instead. Without that,
	 * Apply would refuse with nothing whatsoever on screen to explain it, and the only way out of
	 * the form would be Cancel, discarding the user's work.
	 * </p>
	 */
	private HandlerResult apply() {
		_index.clearModelErrors();

		// Before the unreadable-input check, not after: an unconfirmed entry whose key is already
		// spoken for carries exactly such an input error, and "an entry could not be read" would
		// then be said about a key that reads perfectly well and is merely taken.
		if (refuseUnconfirmedEntries()) {
			return refusal(I18NConstants.ERROR_ENTRY_NOT_CONFIRMED);
		}
		if (_index.hasInputError()) {
			return refusal(I18NConstants.ERROR_INPUT_NOT_READABLE);
		}
		List<Violation> violations = ConfigValidation.check(_model.edited());
		if (!violations.isEmpty()) {
			ConfigValidation.report(violations, _index);
			// Every violation is listed, not only those that found no field: the fields are spread
			// over a form taller than the screen, and the list is what says how many there are and
			// what they are without hunting for them.
			return refusal(I18NConstants.ERROR_CANNOT_APPLY,
				violations.stream().map(Violation::message).toList());
		}
		_model.apply();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The button offered in view mode, starting edit mode.
	 */
	private ReactButtonControl editButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.EDIT), ctx -> {
			_model.startEditing();
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	/**
	 * The button offered in edit mode that {@link #apply()}s the working copy.
	 */
	private ReactButtonControl applyButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.APPLY), ctx -> apply());
	}

	/**
	 * The button offered in edit mode that {@link ConfigFormModel#cancelEditing() drops} the
	 * working copy.
	 */
	private ReactButtonControl cancelButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.CANCEL), ctx -> {
			_model.cancelEditing();
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	@Override
	protected void onCleanup() {
		_model.removeListener(_onModeChange);
	}

}
