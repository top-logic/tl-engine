/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
 * The sequence is expanded from the wizard's sources, and expanded again whenever one of the
 * channels a source names takes a new value: a step appended to a list channel becomes a step of the
 * wizard in the same breath. The expansion runs inside the channel notification, so an action chain
 * that writes such a channel and then moves on already sees the step it created. What the user is
 * looking at survives the re-expansion where its key is still in the sequence - the content stays as
 * it is and only its position is corrected; otherwise the display falls back to where the step
 * channel value points, which for a key that has gone is the first step.
 * </p>
 *
 * <p>
 * The client addresses a step by a string, because a step key is any object. The control assigns
 * each key such an identifier once and keeps both directions of the mapping, so that the identifier
 * a step is published under is the one a click on it comes back with. An identifier stays assigned
 * once given, so a step that comes back after being away comes back under the name it had.
 * </p>
 *
 * <p>
 * The React component {@code TLWizard} renders the indicator and the active step. It is told which
 * way the display moved ({@link #DIRECTION}), so the step entering and the step leaving can be
 * animated in that direction, and how long a step stays before the wizard moves on by itself
 * ({@link #AUTO_ADVANCE}), which the component turns into a timer reporting back through
 * {@link #ADVANCE_STEP_COMMAND} - for a step entered going forward, since a step the user came back
 * to is one they want to look at.
 * </p>
 */
public class ReactWizardControl extends ReactControl implements ChildRevealer {

	private static final String REACT_MODULE = "TLWizard";

	/** State key for the published steps, one entry per step of the wizard. */
	public static final String STEPS = "steps";

	/** Step entry key for the client-side identifier of the step. */
	public static final String STEP_KEY = "key";

	/** Step entry key for the name of the step, resolved for the session being served. */
	private static final String STEP_LABEL = "label";

	/** Step entry key for the encoded icon of the step. */
	private static final String STEP_ICON = "icon";

	/** State key for the position of the step displayed. */
	public static final String ACTIVE_INDEX = "activeIndex";

	/** State key for the content of the step displayed. */
	private static final String ACTIVE_CHILD = "activeChild";

	/** State key for whether the indicator counts the steps. */
	private static final String COUNTER = "counter";

	/** State key for the bar showing how far through the wizard the step displayed is. */
	private static final String PROGRESS = "progress";

	/** State key for whether the indicator lists the steps by name. */
	private static final String STEP_LIST = "stepList";

	/**
	 * State key for the way the step displayed was reached, {@link #FORWARD} or {@link #BACKWARD}.
	 */
	public static final String DIRECTION = "direction";

	/** {@link #DIRECTION} of a move towards the end of the sequence. */
	public static final String FORWARD = "forward";

	/** {@link #DIRECTION} of a move towards its beginning. */
	public static final String BACKWARD = "backward";

	/**
	 * State key for how long the step displayed stays before the wizard moves on by itself,
	 * {@code null} for a step the user leaves - which a step entered going back always is.
	 */
	public static final String AUTO_ADVANCE = "autoAdvance";

	/** Identifier prefix of a step whose key is no string and is therefore numbered. */
	private static final String GENERATED_ID_PREFIX = "step";

	/** The {@link ReactCommandHandler} that displays a step the user picked from the indicator. */
	public static final String GOTO_STEP_COMMAND = "gotoStep";

	/** The {@link ReactCommandHandler} that moves on when a step's own time is up. */
	public static final String ADVANCE_STEP_COMMAND = "advanceStep";

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

	/** The key of the step whose content is displayed, {@code null} while none is. */
	private Object _activeKey;

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
		expandSteps();
		_scope = new WizardScope(stepChannel, this::steps);

		_stepListener = (sender, oldValue, newValue) -> displayCurrentStep();
		_stepChannel.addListener(_stepListener);
		addCleanupAction(() -> _stepChannel.removeListener(_stepListener));

		ChannelListener sequenceListener = (sender, oldValue, newValue) -> refreshSteps();
		for (ViewChannel channel : sequenceChannels()) {
			channel.addListener(sequenceListener);
			addCleanupAction(() -> channel.removeListener(sequenceListener));
		}

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
		putState(DIRECTION, FORWARD);
		commitUpdate(tx);

		displayCurrentStep();
	}

	/**
	 * The steps of the wizard, in the order it walks them, as the sources currently answer them.
	 */
	public List<WizardStep> steps() {
		return _steps;
	}

	/**
	 * Asks every source for its steps and makes their concatenation the sequence of this wizard.
	 *
	 * <p>
	 * The sequence keeps its identity, so whoever holds the {@link WizardScope} reads the steps as
	 * they are now rather than as they were when the scope was handed out.
	 * </p>
	 */
	private void expandSteps() {
		_steps.clear();
		for (WizardStepSource source : _element.getSources()) {
			_steps.addAll(source.steps(_context));
		}
	}

	/**
	 * The channels the sources decide their contribution by, each of them once however many sources
	 * name it.
	 */
	private Set<ViewChannel> sequenceChannels() {
		Set<ViewChannel> result = new LinkedHashSet<>();
		for (WizardStepSource source : _element.getSources()) {
			result.addAll(source.observedChannels(_context));
		}
		// The step channel is followed on its own account, and a listener registered twice would be
		// removed once.
		result.remove(_stepChannel);
		return result;
	}

	/**
	 * Expands the sequence anew and brings the display in line with it.
	 */
	private void refreshSteps() {
		if (_disposed) {
			return;
		}
		expandSteps();

		Object tx = beginUpdate();
		putState(STEPS, publishedSteps());
		displayCurrentStep();
		commitUpdate(tx);
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
		Object key = index < 0 ? null : _steps.get(index).key();
		// The content belongs to a step, not to a position: a step that keeps its key keeps its
		// content, however far the steps before it have moved it along.
		boolean sameStep = _content != null && Objects.equals(key, _activeKey);
		if (sameStep && index == _activeIndex) {
			// Nothing about the step displayed has changed; its content is bound to its own channels
			// and updates itself.
			return;
		}
		int previousIndex = _activeIndex;
		_activeIndex = index;
		_activeKey = key;

		ReactControl content = sameStep ? _content
			: index < 0 ? ContentControls.combine(_context, List.of()) : buildContent(_steps.get(index));
		ReactControl previous = _content;
		_content = content;

		Object tx = beginUpdate();
		putState(ACTIVE_INDEX, Integer.valueOf(index));
		putState(ACTIVE_CHILD, content);
		if (!sameStep) {
			// A step that keeps its key was not moved to, it was carried along by the steps before
			// it: the display keeps the direction it last moved in, and a step already counting down
			// keeps counting rather than starting over.
			boolean forward = previousIndex <= index;
			putState(DIRECTION, forward ? FORWARD : BACKWARD);
			// The time of a step runs while the flow leads through it. Coming back to it is the user
			// going somewhere, so the step waits for them instead of sending them where they left.
			putState(AUTO_ADVANCE, forward && index >= 0 ? _steps.get(index).autoAdvanceMillis() : null);
		}
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
	 * Handles a step whose own time is up.
	 *
	 * <p>
	 * The move happens only while the step the timer was started for is still the one displayed: the
	 * user may have moved on themselves in the meantime, and a timer that outlived its step must not
	 * carry the display past what they chose.
	 * </p>
	 */
	@ReactCommandHandler(value = ADVANCE_STEP_COMMAND, technical = true)
	void handleAdvanceStep(AdvanceStepArguments args) {
		if (_activeKey == null || !args.getStepId().equals(idFor(_activeKey))) {
			return;
		}
		_scope.next();
	}

	/**
	 * Addresses the content of the step displayed by that step's identifier (e.g.
	 * {@code step[payment]}), so that content addresses encode the step they belong to.
	 */
	@Override
	public String scriptingChildSlot(ReactControl child) {
		if (child == _content && _activeKey != null) {
			return ScriptingControl.slotSegment(STEP_SLOT, idFor(_activeKey));
		}
		return null;
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

}
