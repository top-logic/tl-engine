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
import com.top_logic.layout.configedit.ConfigValidation.Violation;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
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

	/**
	 * The CSS classes of {@link #_formError}.
	 *
	 * <p>
	 * {@code tlFormField__error} is the very class the React form field's own error area carries,
	 * so a form-level refusal is styled like the field-level errors it accompanies.
	 * {@code tlFormLayout__formError} is what puts it on a row of its own: a form lays its children
	 * out in columns sized for fields, so without it the message is auto-placed beside whatever
	 * stands in that row - and beside an editor taller than the viewport, it is stretched to that
	 * height and its text pinned to the top of it, out of sight of the button that produced it.
	 * </p>
	 */
	private static final String FORM_ERROR_CSS_CLASS = "tlFormField__error tlFormLayout__formError";

	/**
	 * The ARIA role of {@link #_formError}: the same {@code alert} the React form field and panel
	 * put on their own error areas, so a refusal that appears without the reader having moved
	 * anywhere is announced rather than sitting silently on screen.
	 */
	private static final String ALERT_ROLE = "alert";

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
	 * The line carrying a refusal that no field could carry, or {@code null} outside edit mode.
	 *
	 * <p>
	 * A {@link ReactTextControl} rather than a dialog: a refused Apply leaves the user in the form
	 * they were already editing, and a modal interruption to say so would have to be dismissed
	 * before the very fields it talks about could be reached. It is created empty with every
	 * rebuild into edit mode and filled only by {@link #apply()}, so entering, leaving, and
	 * re-entering edit mode never carries an earlier attempt's message over.
	 * </p>
	 *
	 * <p>
	 * Styled with the {@code tlFormField__error} class the React form field's own error area
	 * already uses, so a form-level refusal reads as the same kind of message as the field-level
	 * ones it accompanies rather than as unmarked text.
	 * </p>
	 */
	private ReactTextControl _formError;

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
				_formError = new ReactTextControl(_context, "", FORM_ERROR_CSS_CLASS);
				_formError.setRole(ALERT_ROLE);
				addChild(_formError);
				addChild(applyButton());
				addChild(cancelButton());
			} else {
				_formError = null;
				addChild(editButton());
			}
		}
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
	private void apply() {
		_index.clearModelErrors();
		setFormError(null);

		if (_index.hasInputError()) {
			setFormError(I18NConstants.ERROR_INPUT_NOT_READABLE);
			return;
		}
		List<Violation> violations = ConfigValidation.check(_model.edited());
		if (!violations.isEmpty()) {
			boolean complete = ConfigValidation.report(violations, _index);
			setFormError(complete ? null : I18NConstants.ERROR_CANNOT_APPLY);
			return;
		}
		_model.apply();
	}

	/**
	 * Shows the given message at form level, or clears the line if {@code null} is given.
	 */
	private void setFormError(ResKey message) {
		if (_formError == null) {
			return;
		}
		_formError.setText(message == null ? "" : Resources.getInstance().getString(message));
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
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.APPLY), ctx -> {
			apply();
			return HandlerResult.DEFAULT_RESULT;
		});
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
