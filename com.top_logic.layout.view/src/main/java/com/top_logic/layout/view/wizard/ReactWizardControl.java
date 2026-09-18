/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingControl;
import com.top_logic.layout.react.control.common.ReactProgressControl;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;
import com.top_logic.util.Resources;

/**
 * Server-side control of {@link WizardElement}.
 *
 * <p>
 * Follows the step channel and displays the content of the step its value names, together with the
 * indicator the wizard is configured with. Leaving a step disposes its content rather than keeping
 * it, so that what a step contributes to its surroundings - a form's Save button, say - goes with
 * it; coming back builds the step anew.
 * </p>
 *
 * <p>
 * The client addresses a step by a string, because a step key is any object. The control assigns
 * each key such an identifier once and keeps both directions of the mapping, so that the identifier
 * a step is published under is the one a click on it comes back with.
 * </p>
 *
 * <p>
 * The React component {@code TLWizard} renders the indicator and the active step.
 * </p>
 */
public class ReactWizardControl extends ReactControl implements ChildRevealer {

	private static final String REACT_MODULE = "TLWizard";

	/** State key for the published steps, one entry per step of the wizard. */
	private static final String STEPS = "steps";

	/** Step entry key for the client-side identifier of the step. */
	private static final String STEP_KEY = "key";

	/** Step entry key for the name of the step, resolved for the session being served. */
	private static final String STEP_LABEL = "label";

	/** Step entry key for the encoded icon of the step. */
	private static final String STEP_ICON = "icon";

	/** State key for the position of the step displayed. */
	private static final String ACTIVE_INDEX = "activeIndex";

	/** State key for the content of the step displayed. */
	private static final String ACTIVE_CHILD = "activeChild";

	/** State key for whether the indicator counts the steps. */
	private static final String COUNTER = "counter";

	/** State key for the bar showing how far through the wizard the step displayed is. */
	private static final String PROGRESS = "progress";

	/** State key for whether the indicator lists the steps by name. */
	private static final String STEP_LIST = "stepList";

	/** Identifier prefix of a step whose key is no string and is therefore numbered. */
	private static final String GENERATED_ID_PREFIX = "step";

	/** The {@link ReactCommandHandler} that displays a step the user picked from the indicator. */
	public static final String GOTO_STEP_COMMAND = "gotoStep";

	/** Addresses the content of a step by the step's identifier. */
	private static final String STEP_SLOT = "step";

	private final ViewContext _context;

	/** The element this control displays, the container a step is addressed through. */
	private final WizardElement _element;

	/** The position of this wizard in the display, which every step extends by its own key. */
	private final RevealPath _here;

	private final ViewChannel _stepChannel;

	private final WizardScope _scope;

	private final ChannelListener _stepListener;

	private final List<WizardStep> _steps;

	/** The identifier each step key is published under. */
	private final Map<Object, String> _idByKey = new LinkedHashMap<>();

	/** The step key each published identifier names. */
	private final Map<String, Object> _keyById = new HashMap<>();

	private final ReactProgressControl _progressControl;

	private ReactControl _content;

	private int _activeIndex = -1;

	private boolean _disposed;

	/**
	 * Creates a new {@link ReactWizardControl}.
	 *
	 * @param context
	 *        The {@link ViewContext} in which the {@code <wizard>} is embedded, and in which each
	 *        step's content is built.
	 * @param element
	 *        The element this control displays, the container a step is addressed through.
	 * @param stepChannel
	 *        The channel holding the key of the step displayed.
	 */
	public ReactWizardControl(ViewContext context, WizardElement element, ViewChannel stepChannel) {
		super(context, null, REACT_MODULE);
		_context = context;
		_element = element;
		_here = RevealPath.of(context);
		_stepChannel = stepChannel;

		_steps = new ArrayList<>();
		for (WizardStepSource source : element.getSources()) {
			_steps.addAll(source.steps(context));
		}
		_scope = new WizardScope(stepChannel, this::steps);

		_stepListener = (sender, oldValue, newValue) -> displayCurrentStep();
		_stepChannel.addListener(_stepListener);
		addCleanupAction(() -> _stepChannel.removeListener(_stepListener));

		RevealRegistry registry = context.getRevealRegistry();
		if (registry != null) {
			addCleanupAction(registry.registerContainer(element, _here, this));
		}

		_progressControl = element.hasProgress() ? new ReactProgressControl(getReactContext(), 0d, null) : null;

		Object tx = beginUpdate();
		putState(STEPS, publishedSteps());
		putState(COUNTER, Boolean.valueOf(element.hasCounter()));
		putState(STEP_LIST, Boolean.valueOf(element.hasStepList()));
		putState(PROGRESS, _progressControl);
		commitUpdate(tx);

		displayCurrentStep();
	}

	/**
	 * The steps of the wizard, in the order it walks them.
	 */
	public List<WizardStep> steps() {
		return _steps;
	}

	/**
	 * The {@link WizardScope} every step's content sees.
	 */
	public WizardScope getScope() {
		return _scope;
	}

	/**
	 * Displays the step the step channel names, building its content and disposing the content of
	 * the step left behind.
	 */
	private void displayCurrentStep() {
		if (_disposed) {
			return;
		}
		int index = _scope.currentIndex();
		if (index == _activeIndex && _content != null) {
			// The step displayed is unchanged; its content is bound to its own channels and updates
			// itself.
			return;
		}
		_activeIndex = index;

		ReactControl content =
			index < 0 ? ContentControls.combine(_context, List.of()) : buildContent(_steps.get(index));
		ReactControl previous = _content;
		_content = content;

		Object tx = beginUpdate();
		putState(ACTIVE_INDEX, Integer.valueOf(index));
		putState(ACTIVE_CHILD, content);
		if (_progressControl != null) {
			_progressControl.setFraction(fraction(index));
		}
		commitUpdate(tx);

		if (isAttached()) {
			content.attach();
		}
		if (previous != null && previous != content) {
			ContentControls.retire(previous);
		}
	}

	/**
	 * How far through the wizard the step at the given position is, the step itself counted as
	 * reached.
	 */
	private double fraction(int index) {
		int total = _steps.size();
		if (total == 0 || index < 0) {
			return 0d;
		}
		return (index + 1) / (double) total;
	}

	/**
	 * Creates the content of the given step, in a child context that isolates it and hands it the
	 * {@link WizardScope}.
	 */
	private ReactControl buildContent(WizardStep step) {
		String id = idFor(step.key());
		ViewContext stepContext = _context.childContext("wizard")
			.withChildSlotPath(id)
			.withScope(WizardScope.class, _scope)
			.withScope(RevealPath.class, _here.append(_element, id));
		return step.content().apply(stepContext);
	}

	/**
	 * The steps as the client sees them: identifier, name in the language of the session, icon.
	 */
	private List<Map<String, Object>> publishedSteps() {
		List<Map<String, Object>> result = new ArrayList<>(_steps.size());
		for (WizardStep step : _steps) {
			String id = idFor(step.key());
			Map<String, Object> entry = new HashMap<>();
			entry.put(STEP_KEY, id);
			entry.put(STEP_LABEL, label(step, id));
			if (step.icon() != null) {
				entry.put(STEP_ICON, step.icon());
			}
			result.add(entry);
		}
		return result;
	}

	/**
	 * The name of the given step, falling back to its identifier while it has no name of its own, so
	 * that an unnamed step is still something the indicator can point at.
	 */
	private static String label(WizardStep step, String id) {
		ResKey key = step.label();
		String label = key == null ? null : Resources.getInstance().getString(key, null);
		return StringServices.isEmpty(label) ? id : label;
	}

	/**
	 * The identifier the given step key is published under, assigned on first use and kept for as
	 * long as this control lives.
	 *
	 * <p>
	 * A key that is a string is published as itself, so that the identifier reads like what the view
	 * writes; any other key is numbered. A collision is resolved by numbering as well, so that no
	 * two keys ever share an identifier.
	 * </p>
	 */
	private String idFor(Object key) {
		String assigned = _idByKey.get(key);
		if (assigned != null) {
			return assigned;
		}
		String base = key instanceof String text && !text.isEmpty() ? text : GENERATED_ID_PREFIX + _idByKey.size();
		String id = base;
		for (int suffix = 2; _keyById.containsKey(id); suffix++) {
			id = base + '_' + suffix;
		}
		_idByKey.put(key, id);
		_keyById.put(id, key);
		return id;
	}

	/**
	 * Displays the step the given identifier names.
	 *
	 * @param key
	 *        The identifier of a step, as this control published it.
	 */
	@Override
	public void revealChild(String key) {
		if (!_keyById.containsKey(key)) {
			throw new IllegalArgumentException("A wizard has no step '" + key + "'.");
		}
		_scope.goTo(_keyById.get(key));
	}

	/**
	 * Handles a jump to a step the user picked from the indicator.
	 */
	@ReactCommandHandler(GOTO_STEP_COMMAND)
	void handleGotoStep(GotoStepArguments args) {
		revealChild(args.getStepId());
	}

	/**
	 * Addresses the content of the step displayed by that step's identifier (e.g.
	 * {@code step[payment]}), so that content addresses encode the step they belong to.
	 */
	@Override
	public String scriptingChildSlot(ReactControl child) {
		if (child == _content && _activeIndex >= 0) {
			return ScriptingControl.slotSegment(STEP_SLOT, idFor(_steps.get(_activeIndex).key()));
		}
		return null;
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

}
