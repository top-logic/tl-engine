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
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingControl;
import com.top_logic.layout.react.control.common.ReactProgressControl;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RouteManager.Navigation;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelNotificationScope;
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
 * {@link #ADVANCE_STEP_COMMAND}.
 * </p>
 *
 * <p>
 * A step with a time of its own is a {@link WizardStep#passThrough() pass-through step}: the flow
 * leads through it, and the user never returns to it. Its time runs on every entry, whichever way
 * the display moved. The indicator offers no jump to it ({@link #STEP_TRANSIT}), and it is no
 * address: while it is displayed, the control holds a {@link RouteManager#beginNavigation()
 * navigation} open, so that the address bar keeps naming the step the flow came from, and the step
 * the flow arrives at afterwards becomes the next history entry. An address that names a
 * pass-through step nevertheless - a bookmark, a typed URL - displays the step a
 * {@link WizardScope#back() move back} from it reaches, and the address bar is corrected to that
 * step; a pass-through step that no step to return to precedes simply runs.
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

	/**
	 * Step entry key for whether the step is a {@link WizardStep#passThrough() pass-through step},
	 * which the indicator offers no jump to.
	 */
	public static final String STEP_TRANSIT = "transit";

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
	 * {@code null} for a step the user leaves.
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

	/** The routing of the display this wizard is part of, {@code null} for none. */
	private final RouteManager _routes;

	/**
	 * The navigation held open while a {@link WizardStep#passThrough() pass-through step} is
	 * displayed, {@code null} while none is.
	 */
	private Navigation _passage;

	/** The element this control displays, the container a step is addressed through. */
	private final WizardElement _element;

	/** The position of this wizard in the display, which every step extends by its own key. */
	private final RevealPath _here;

	private final ViewChannel _stepChannel;

	private final WizardScope _scope;

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
		_routes = context.getRouteManager();
		_element = element;
		_here = RevealPath.of(context);
		_stepChannel = stepChannel;

		_steps = new ArrayList<>();
		expandSteps();
		_scope = new WizardScope(stepChannel, this::steps, _routes);

		Registration stepRegistration = _stepChannel.addListener((sender, oldValue, newValue) -> displayCurrentStep());
		addCleanupAction(stepRegistration::dispose);

		ChannelListener sequenceListener = (sender, oldValue, newValue) -> refreshSteps();
		for (ViewChannel channel : sequenceChannels()) {
			Registration sequenceRegistration = channel.addListener(sequenceListener);
			addCleanupAction(sequenceRegistration::dispose);
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
		showCurrentStep();
		updatePassage();
	}

	/**
	 * Brings the content, the position and the time published in line with the step the step
	 * channel names.
	 */
	private void showCurrentStep() {
		int index = _scope.currentIndex();
		if (redirectAddress(index)) {
			return;
		}
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
			// The flow only leads through a step with a time of its own, whichever way it entered it.
			putState(AUTO_ADVANCE, index >= 0 ? _steps.get(index).autoAdvanceMillis() : null);
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
	 * Displays the step a move back reaches in place of a pass-through step an address names.
	 *
	 * <p>
	 * A pass-through step is no address, so a URL naming it - a bookmark, a typed URL - leads to the
	 * step the user would return to from it. Where no such step precedes it, the step runs, because
	 * there is nothing else the address could mean.
	 * </p>
	 *
	 * <p>
	 * The step channel is written once the notification in progress has unwound: this runs inside a
	 * listener of the very channel, and a value written from there would reach the listeners after
	 * this one before the value they are being told about. Until then, the display stays as it is.
	 * The address bar follows when the adoption finishes, which reports the address of the display it
	 * arrived at.
	 * </p>
	 *
	 * @param index
	 *        The position of the step the step channel names.
	 * @return Whether the step is displayed in place of the one at the given position.
	 */
	private boolean redirectAddress(int index) {
		if (index < 0 || _routes == null || !_routes.isAdopting()) {
			return false;
		}
		WizardStep addressed = _steps.get(index);
		if (!addressed.passThrough()) {
			return false;
		}
		int target = _scope.backIndex(index);
		if (target < 0) {
			return false;
		}
		Object addressedKey = addressed.key();
		Object targetKey = _steps.get(target).key();
		ChannelNotificationScope.current().afterNotification(() -> {
			// Another write in the meantime decided where the wizard is.
			if (!_disposed && Objects.equals(_stepChannel.get(), addressedKey)) {
				_stepChannel.set(targetKey);
			}
		});
		return true;
	}

	/**
	 * Holds a navigation open while a pass-through step is displayed, and ends it otherwise.
	 *
	 * <p>
	 * While the navigation is open, the address bar is not told about the display, and keeps naming
	 * the step the flow came from. Ending it reports the address of the step the flow arrived at as
	 * the next history entry - or nothing, where the flow returned to the step the address bar names.
	 * A wizard that leaves the display, or is disposed, while it shows a pass-through step ends the
	 * navigation as well: an open navigation keeps every later change of the display from the
	 * address bar.
	 * </p>
	 *
	 * <p>
	 * The navigation is opened from inside the notification of the step channel, after a binding of
	 * that channel to the address may have heard of the move already. That the binding reports
	 * nothing of it is owed to the {@link WizardScope}, whose every move is a navigation of its own.
	 * </p>
	 */
	private void updatePassage() {
		boolean passing = !_disposed && isAttached() && _activeIndex >= 0 && _activeIndex < _steps.size()
			&& _steps.get(_activeIndex).passThrough();
		if (passing) {
			if (_passage == null && _routes != null) {
				_passage = _routes.beginNavigation();
			}
		} else if (_passage != null) {
			Navigation passage = _passage;
			_passage = null;
			passage.end();
		}
	}

	/**
	 * Whether the step with the given key is a pass-through step of this wizard.
	 */
	private boolean isPassThrough(Object key) {
		for (WizardStep step : _steps) {
			if (Objects.equals(step.key(), key)) {
				return step.passThrough();
			}
		}
		return false;
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
	 * The steps as the client sees them: identifier, name in the language of the session, icon, and
	 * whether the step is a pass-through step.
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
			entry.put(STEP_TRANSIT, Boolean.valueOf(step.passThrough()));
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
	 *
	 * <p>
	 * A pass-through step is no step to jump to, and the indicator offers none; a request naming
	 * one leaves the wizard where it is.
	 * </p>
	 */
	@ReactCommandHandler(GOTO_STEP_COMMAND)
	void handleGotoStep(GotoStepArguments args) {
		String stepId = args.getStepId();
		if (isPassThrough(_keyById.get(stepId))) {
			return;
		}
		revealChild(stepId);
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
	protected void onAttach() {
		super.onAttach();
		updatePassage();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		updatePassage();
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		updatePassage();
		super.onCleanup();
	}

}
