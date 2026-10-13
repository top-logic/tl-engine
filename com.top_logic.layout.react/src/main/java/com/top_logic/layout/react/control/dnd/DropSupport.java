/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * The part of the drop protocol every control accepting drops shares: the wire names, resolving
 * the dragged objects from the source control, answering drop probes, and recording a drop in
 * replay-stable form.
 *
 * <p>
 * A control accepting drops creates one {@link DropSupport} and keeps to itself only what its
 * drop targets are: which client-side key designates which target, and what a drop on it does.
 * A control whose items are places of a drop - rows of a table, nodes of a tree - hands its
 * {@link DropTarget} to {@link #setTarget(DropTarget)} and resolves the item and zone a drop names
 * into a {@link DropPlace}; applying, probing, recording and replaying a drop is then
 * {@link #drop(DropArguments, DropPlace.Resolver)}, {@link #probe(DropProbeArguments, DropPlace.Resolver)},
 * {@link #recordDrop(Map, DropPlace.Resolver)} and {@link #dropObjects(DropObjectsArguments, Predicate)}.
 * </p>
 */
public final class DropSupport {

	/** State key telling the client whether the control's items may be dragged. */
	public static final String DRAG_ENABLED = "dragEnabled";

	/**
	 * State key holding the {@link DragSourceControl#dragKind() kind} of a drag payload, absent for a
	 * drag without a kind.
	 */
	public static final String DRAG_KIND = "dragKind";

	/**
	 * State key telling the client whether a drop is accepted of {@link AcceptedKinds#any() any}
	 * drag, with any kind or none.
	 */
	public static final String DROP_ACCEPTS_ANY = "dropAcceptsAny";

	/**
	 * State key holding the {@link AcceptedKinds#kinds() kinds} a drop is accepted of, where
	 * {@link #DROP_ACCEPTS_ANY} is not set.
	 */
	public static final String DROP_ACCEPTS = "dropAccepts";

	/**
	 * State key holding the {@link DropMode#wireName() wire names} of the
	 * {@link DropTarget#dropModes() modes} of the operations a drop is accepted by, in the target's
	 * order.
	 *
	 * <p>
	 * The client splits an item into the {@link DropZone zones} these modes need.
	 * </p>
	 */
	public static final String DROP_MODES = "dropModes";

	/**
	 * State key holding the verdicts answered to the {@link #CMD_DROP_PROBE probes} of the running
	 * drag, by {@link DropProbeArguments#getProbe() probe identifier}.
	 *
	 * <p>
	 * Each entry holds {@link #VERDICT_ACCEPTED} and, for a refusal, {@link #VERDICT_REASON}; an
	 * acceptance may name the {@link #VERDICT_MARKER marker} to draw and the
	 * {@link #VERDICT_MARKER_KEY item} to draw it at. The
	 * verdicts of a drag accumulate, so a client receiving several answers at once misses none of
	 * them; a probe of the next drag discards them.
	 * </p>
	 */
	public static final String DROP_VERDICTS = "dropVerdicts";

	/** Entry of a {@link #DROP_VERDICTS} verdict telling whether the drop is accepted. */
	public static final String VERDICT_ACCEPTED = "accepted";

	/**
	 * Entry of a refusing {@link #DROP_VERDICTS} verdict holding the reason, in the user's language.
	 */
	public static final String VERDICT_REASON = "reason";

	/**
	 * Entry of an accepting {@link #DROP_VERDICTS} verdict holding the {@link DropMarker#wireName()
	 * wire name} of the marker the client draws for the location the drop is accepted at.
	 */
	public static final String VERDICT_MARKER = "marker";

	/**
	 * Entry of an accepting {@link #DROP_VERDICTS} verdict holding the client-side key of the item
	 * the {@link #VERDICT_MARKER marker} is drawn at, absent for a marker of the control as a whole.
	 */
	public static final String VERDICT_MARKER_KEY = "markerKey";

	/** The command applying a drop the client made, see {@link DropArguments}. */
	public static final String CMD_DROP = "drop";

	/**
	 * The command applying a drop of objects named by their business identity, the form a drop is
	 * recorded in, see {@link DropObjectsArguments}.
	 */
	public static final String CMD_DROP_OBJECTS = "dropObjects";

	/**
	 * The command the client sends while a drag hovers a target, asking whether a drop right there
	 * would be accepted; answered in {@link #DROP_VERDICTS}.
	 *
	 * @see DropProbeArguments
	 */
	public static final String CMD_DROP_PROBE = "dropProbe";

	/**
	 * The objects of a drop resolved from the source control the client named: either the source
	 * and its dragged objects, or the reason the drop cannot be made.
	 *
	 * @param source
	 *        The control the drag started in, {@code null} if the drop is refused.
	 * @param kind
	 *        The {@link DragSourceControl#dragKind() kind} of the drag, {@code null} for a drag
	 *        without a kind and if the drop is refused.
	 * @param objects
	 *        The dragged objects, {@code null} if the drop is refused.
	 * @param refusal
	 *        Why the drop cannot be made, {@code null} if it can.
	 */
	public record Dragged(ReactControl source, String kind, List<?> objects, ResKey refusal) {

		/**
		 * The objects of a drop that cannot be made for the given reason.
		 */
		public static Dragged refused(ResKey reason) {
			return new Dragged(null, null, null, reason);
		}

	}

	/**
	 * The {@link DropPlace#location(DropMode) place} of a drop resolved from its client-side
	 * identities: either the {@link DropRequest} to put to the drop target together with the place
	 * it was made at, or the reason it cannot be made.
	 *
	 * @param request
	 *        The drop to check, {@code null} if it is refused.
	 * @param place
	 *        Where the drop was made, {@code null} if it is refused.
	 * @param refusal
	 *        Why the drop cannot be made, {@code null} if it can.
	 */
	private record Resolved(DropRequest request, DropPlace place, ResKey refusal) {

		static Resolved refused(ResKey reason) {
			return new Resolved(null, null, reason);
		}

	}

	private final ReactControl _owner;

	/** What dropped objects are done with, {@code null} while the owner accepts no drop. */
	private DropTarget _target;

	/** The {@link DropProbeArguments#getDrag() drag} the {@link #_verdicts} belong to. */
	private String _probedDrag;

	/** The verdicts answered to the probes of {@link #_probedDrag}, see {@link #DROP_VERDICTS}. */
	private Map<String, Object> _verdicts = new LinkedHashMap<>();

	/**
	 * Creates a {@link DropSupport}.
	 *
	 * @param owner
	 *        The control accepting the drops.
	 */
	public DropSupport(ReactControl owner) {
		_owner = owner;
	}

	/**
	 * The control registered in the owner's window under the given id, or {@code null} if none is
	 * (or the id is missing).
	 */
	public ReactCommandTarget registeredControl(String controlId) {
		SSEUpdateQueue queue = _owner.getReactContext().getSSEQueue();
		if (controlId == null || queue == null) {
			return null;
		}
		return queue.getControl(controlId);
	}

	/**
	 * Resolves the dragged objects a client drop names through the {@link DragSourceControl} the
	 * {@link DropArguments#getSource() source id} designates.
	 *
	 * <p>
	 * A drop from a control that is no drag source or has dragging switched off, of a kind that is
	 * not among the accepted ones,
	 * naming no object the source still displays, or including an object the source does not let be
	 * {@link DragSourceControl#isDraggable(Object) dragged} is refused.
	 * </p>
	 *
	 * @param accepted
	 *        The {@link DragSourceControl#dragKind() kinds} the owner accepts a drop of.
	 * @param args
	 *        The client drop.
	 */
	public Dragged dragged(AcceptedKinds accepted, DropArguments args) {
		ReactCommandTarget registered = registeredControl(args.getSource());
		if (!(registered instanceof DragSourceControl source) || !(registered instanceof ReactControl sourceControl)) {
			return Dragged.refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		if (!source.isDragEnabled() || !accepted.accepts(source.dragKind())) {
			return Dragged.refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		List<?> objects = args.isSelection() ? source.dragSelection() : source.dragObjects(args.getKeys());
		if (objects.isEmpty()) {
			return Dragged.refused(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(args.getKeys()));
		}
		for (Object object : objects) {
			if (!source.isDraggable(object)) {
				return Dragged.refused(I18NConstants.ERROR_DROP_NOT_DRAGGABLE);
			}
		}
		return new Dragged(sourceControl, source.dragKind(), objects, null);
	}

	/**
	 * Makes the owner accept a drop of the objects the given target accepts, and apply such a drop
	 * through it.
	 *
	 * <p>
	 * The owner announces the target to the client with {@link #targetState()}.
	 * </p>
	 *
	 * @param target
	 *        What dropped objects are done with, or {@code null} to accept no drop.
	 */
	public void setTarget(DropTarget target) {
		_target = target;
	}

	/**
	 * What dropped objects are done with, {@code null} while the owner accepts no drop.
	 */
	public DropTarget getTarget() {
		return _target;
	}

	/**
	 * The state announcing the {@link #setTarget(DropTarget) target} to the client: the values of
	 * {@link #DROP_ACCEPTS_ANY}, {@link #DROP_ACCEPTS} and {@link #DROP_MODES}, by state key.
	 *
	 * <p>
	 * The owner puts them into its state whenever the target or its answers change.
	 * </p>
	 */
	public Map<String, Object> targetState() {
		DropTarget target = _target;
		AcceptedKinds accepted = target == null ? AcceptedKinds.NONE : target.acceptedKinds();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(DROP_ACCEPTS_ANY, Boolean.valueOf(accepted.any()));
		result.put(DROP_ACCEPTS, List.copyOf(accepted.kinds()));
		result.put(DROP_MODES, target == null ? List.of() : wireNames(target.dropModes()));
		return result;
	}

	/**
	 * Resolves the client-side identities a drop names.
	 *
	 * <p>
	 * The dragged objects are resolved by the {@link DragSourceControl} the
	 * {@link DropArguments#getSource() source id} designates (see
	 * {@link #dragged(AcceptedKinds, DropArguments)}), the place by the owner. A drop on an owner
	 * without a {@link #setTarget(DropTarget) target}, naming an unknown zone, or naming an item
	 * the owner no longer displays is refused. A target offering only drops on the control as a
	 * whole ignores the item: the place is then the one beside the items.
	 * </p>
	 */
	private Resolved resolve(DropArguments args, DropPlace.Resolver places) {
		DropTarget target = _target;
		if (target == null) {
			return Resolved.refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		Dragged dragged = dragged(target.acceptedKinds(), args);
		if (dragged.refusal() != null) {
			return Resolved.refused(dragged.refusal());
		}

		String itemKey = null;
		DropZone zone = DropZone.NONE;
		if (!target.dropModes().stream().allMatch(mode -> mode == DropMode.CONTROL)) {
			zone = DropZone.fromWire(args.getZone());
			if (zone == null) {
				return Resolved.refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
			}
			String targetKey = args.getTargetKey();
			if (targetKey != null && !targetKey.isEmpty()) {
				itemKey = targetKey;
			} else {
				zone = DropZone.NONE;
			}
		}
		DropPlace place = places.place(itemKey, zone);
		if (place == null) {
			return Resolved.refused(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(itemKey));
		}
		return new Resolved(new DropRequest(dragged.source(), dragged.kind(), dragged.objects(), place::location),
			place, null);
	}

	/**
	 * Applies a drop the client made on the owner.
	 *
	 * <p>
	 * The arguments name client-side identities only, so both ends of the gesture are resolved by
	 * the control that owns them. A drop the resolution or the {@link DropTarget#check(DropRequest)
	 * target's check} refuses is answered with a warning naming the reason and not applied - the
	 * client-side acceptance check that precedes it narrows the gesture for the user, it does not
	 * decide it.
	 * </p>
	 *
	 * @param args
	 *        The client drop.
	 * @param places
	 *        Resolves the item the drop names into the place it was made at.
	 */
	public HandlerResult drop(DropArguments args, DropPlace.Resolver places) {
		Resolved resolved = resolve(args, places);
		if (resolved.refusal() != null) {
			return refused(resolved.refusal());
		}
		return apply(resolved.request());
	}

	/**
	 * Answers whether a drop right where a drag hovers would be accepted, without applying it.
	 *
	 * <p>
	 * The drop is resolved exactly like {@link #drop(DropArguments, DropPlace.Resolver) a drop} and
	 * then put to the {@link DropTarget#check(DropRequest) target's check}; an acceptance is answered
	 * with the {@link DropPlace#marker(DropLocation) marker} of the location it accepts the drop at.
	 * </p>
	 *
	 * @param args
	 *        The probe.
	 * @param places
	 *        Resolves the item the probe names into the place it was made at.
	 * @return All verdicts of the probe's drag, the value of the owner's {@link #DROP_VERDICTS}
	 *         state.
	 *
	 * @see #answerProbe(DropProbeArguments, ResKey, DropMarker, String)
	 */
	public Map<String, Object> probe(DropProbeArguments args, DropPlace.Resolver places) {
		Resolved resolved = resolve(args, places);
		ResKey refusal = resolved.refusal();
		DropMarker marker = null;
		String markerKey = null;
		if (refusal == null) {
			DropVerdict verdict = _target.check(resolved.request());
			refusal = verdict.reason();
			if (verdict.isAccepted()) {
				marker = resolved.place().marker(verdict.location());
				markerKey = resolved.place().markerKey(marker);
			}
		}
		return answerProbe(args, refusal, marker, markerKey);
	}

	/**
	 * Applies the given drop through the {@link #setTarget(DropTarget) target}, at the location its
	 * {@link DropTarget#check(DropRequest) check} accepts it at, unless the check refuses it.
	 */
	private HandlerResult apply(DropRequest request) {
		DropVerdict verdict = _target.check(request);
		if (!verdict.isAccepted()) {
			return refused(verdict.reason());
		}
		_target.onDrop(request.event(verdict.location()));
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Rewrites a client {@link #CMD_DROP} into the replay-stable {@link #CMD_DROP_OBJECTS} form,
	 * naming the location the {@link #setTarget(DropTarget) target} accepts the drop at.
	 *
	 * @param arguments
	 *        The arguments of the client drop.
	 * @param places
	 *        Resolves the item the drop names into the place it was made at.
	 * @return The recorded step, or {@code null} when the drop is refused or cannot be named.
	 *
	 * @see #recordDrop(Map, Function)
	 */
	public RecordedCommand recordDrop(Map<String, Object> arguments, DropPlace.Resolver places) {
		return recordDrop(arguments, args -> {
			Resolved resolved = resolve(args, places);
			if (resolved.refusal() != null) {
				return null;
			}
			return _target.check(resolved.request()).location();
		});
	}

	/**
	 * Applies a drop of the objects named by their {@link ScriptingModelKey business identity} -
	 * the replay-stable counterpart of {@link #drop(DropArguments, DropPlace.Resolver)}, which a
	 * recorded drop is captured as so it survives sorting, filtering and a fresh session.
	 *
	 * <p>
	 * The drop is offered exactly the recorded location, for the recorded mode: the operation that
	 * applied it when it was recorded applies it again.
	 * </p>
	 *
	 * <p>
	 * Drift contract: a recorded identity that no longer designates a present object, or a location
	 * the owner does not display, is an explicit failure (replay reports {@code success:false}),
	 * never a partially applied drop. Unlike a refusal, a drift means the replayed script no longer
	 * matches the application, hence an error rather than a warning.
	 * </p>
	 *
	 * @param args
	 *        Carries the identities of the dropped objects and of the reference objects of the
	 *        location.
	 * @param displayed
	 *        Whether the owner displays the place the resolved location names - the item dropped
	 *        onto, the parent and the item inserted before. Not asked for a
	 *        {@link DropLocation.Control} location.
	 */
	public HandlerResult dropObjects(DropObjectsArguments args, Predicate<DropLocation> displayed) {
		DropTarget target = _target;
		if (target == null || !target.acceptedKinds().accepts(args.getKind())) {
			return refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		ActionContext actionContext = ScriptingModelKey.newActionContextOrNull();

		List<ModelName> unresolved = new ArrayList<>();
		List<Object> objects = locateAll(actionContext, args.getObjects(), unresolved);
		DropLocation location = recordedLocation(actionContext, args, unresolved);
		if (location == null) {
			return refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		// The reference objects must be places of the owner: a recorded drop that lands somewhere
		// else is a drift, not a drop.
		boolean elsewhere = unresolved.isEmpty() && !(location instanceof DropLocation.Control)
			&& !displayed.test(location);
		if (elsewhere) {
			for (ModelName reference : new ModelName[] { args.getTargetObject(), args.getParent(), args.getBefore() }) {
				if (reference != null) {
					unresolved.add(reference);
				}
			}
		}
		if (elsewhere || !unresolved.isEmpty() || objects.isEmpty()) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(unresolved));
		}

		return apply(DropRequest.at(null, args.getKind(), objects, location));
	}

	/**
	 * The {@link #DROP_MODES} state value announcing the given modes.
	 */
	public static List<String> wireNames(Set<DropMode> modes) {
		List<String> result = new ArrayList<>(modes.size());
		for (DropMode mode : modes) {
			result.add(mode.wireName());
		}
		return result;
	}

	/**
	 * Records the verdict on a probe without a marker.
	 *
	 * @see #answerProbe(DropProbeArguments, ResKey, DropMarker, String)
	 */
	public Map<String, Object> answerProbe(DropProbeArguments args, ResKey refusal) {
		return answerProbe(args, refusal, null, null);
	}

	/**
	 * Records the verdict on a probe, and returns all verdicts of the probe's drag as the value of
	 * the owner's {@link #DROP_VERDICTS} state.
	 *
	 * @param args
	 *        The probe.
	 * @param refusal
	 *        Why a drop at the probed target is refused, {@code null} if it is accepted.
	 * @param marker
	 *        What the client draws for an accepted drop, {@code null} for nothing beyond its own
	 *        feedback. Ignored for a refusal.
	 * @param markerKey
	 *        The client-side key of the item the marker is drawn at, {@code null} for a marker of
	 *        the control as a whole.
	 */
	public Map<String, Object> answerProbe(DropProbeArguments args, ResKey refusal, DropMarker marker,
			String markerKey) {
		String drag = args.getDrag();
		if (!drag.equals(_probedDrag)) {
			_probedDrag = drag;
			_verdicts = new LinkedHashMap<>();
		}
		Map<String, Object> verdict = new LinkedHashMap<>();
		verdict.put(VERDICT_ACCEPTED, Boolean.valueOf(refusal == null));
		if (refusal != null) {
			verdict.put(VERDICT_REASON, Resources.getInstance().getString(refusal));
		} else if (marker != null) {
			verdict.put(VERDICT_MARKER, marker.wireName());
			if (markerKey != null) {
				verdict.put(VERDICT_MARKER_KEY, markerKey);
			}
		}
		_verdicts.put(args.getProbe(), verdict);
		return new LinkedHashMap<>(_verdicts);
	}

	/**
	 * The objects the given business identities designate.
	 *
	 * @param context
	 *        The action context to resolve the identities in, {@code null} if there is none.
	 * @param names
	 *        The identities to resolve.
	 * @param unresolvedOut
	 *        Receives each identity that designates no object.
	 * @return The designated objects, in the order of their identities.
	 */
	public static List<Object> locateAll(ActionContext context, List<ModelName> names,
			List<ModelName> unresolvedOut) {
		List<Object> result = new ArrayList<>(names.size());
		for (ModelName name : names) {
			Object object = ScriptingModelKey.locate(context, null, name);
			if (object == null) {
				unresolvedOut.add(name);
			} else {
				result.add(object);
			}
		}
		return result;
	}

	/**
	 * Rewrites a client {@link #CMD_DROP} into the replay-stable {@link #CMD_DROP_OBJECTS} form: the
	 * live drop names the dragged items and the place it was made at by session-bound client keys,
	 * the recorded step names the business objects and the {@link DropLocation} the drop is applied
	 * at.
	 *
	 * <p>
	 * Recording the location rather than the place keeps a replay independent of what the control
	 * displays next to the place: a drop below a row inserts before the row following it, which
	 * after a sort is another one.
	 * </p>
	 *
	 * @param arguments
	 *        The arguments of the client drop.
	 * @param locationOf
	 *        The location the owner applies the given drop at, {@code null} for a drop it refuses.
	 *        Must not modify anything.
	 * @return The recorded step, or {@code null} when the drop is refused or an object cannot be
	 *         named, so the drop is recorded verbatim rather than as an incomplete set.
	 */
	public RecordedCommand recordDrop(Map<String, Object> arguments, Function<DropArguments, DropLocation> locationOf) {
		if (!(_owner.commandItem(CMD_DROP, arguments) instanceof DropArguments args)) {
			return null;
		}
		if (!(registeredControl(args.getSource()) instanceof DragSourceControl source)) {
			return null;
		}
		List<?> objects = args.isSelection() ? source.dragSelection() : source.dragObjects(args.getKeys());
		if (objects.isEmpty()) {
			return null;
		}
		DropLocation location = locationOf.apply(args);
		if (location == null) {
			return null;
		}
		DropObjectsArguments recorded = TypedConfiguration.newConfigItem(DropObjectsArguments.class);
		recorded.setName(CMD_DROP_OBJECTS);
		recorded.setKind(source.dragKind());
		for (Object object : objects) {
			ModelName name = ScriptingModelKey.name(null, object);
			if (name == null) {
				return null;
			}
			recorded.getObjects().add(name);
		}
		recorded.setMode(location.mode().wireName());
		if (location instanceof DropLocation.Onto onto) {
			ModelName target = ScriptingModelKey.name(null, onto.target());
			if (target == null) {
				return null;
			}
			recorded.setTargetObject(target);
		} else if (location instanceof DropLocation.Insert insert) {
			if (insert.parent() != null) {
				ModelName parent = ScriptingModelKey.name(null, insert.parent());
				if (parent == null) {
					return null;
				}
				recorded.setParent(parent);
			}
			if (insert.before() != null) {
				ModelName before = ScriptingModelKey.name(null, insert.before());
				if (before == null) {
					return null;
				}
				recorded.setBefore(before);
			}
		}
		return new RecordedCommand(recorded);
	}

	/**
	 * The {@link DropLocation} a recorded drop names.
	 *
	 * @param context
	 *        The action context to resolve the identities in, {@code null} if there is none.
	 * @param args
	 *        The recorded drop.
	 * @param unresolvedOut
	 *        Receives each identity of a reference object that designates no object; the location
	 *        then holds {@code null} in its place.
	 * @return The location, or {@code null} if the recorded drop names an unknown mode or omits the
	 *         reference object its mode requires.
	 */
	public static DropLocation recordedLocation(ActionContext context, DropObjectsArguments args,
			List<ModelName> unresolvedOut) {
		DropMode mode = DropMode.fromWire(args.getMode());
		if (mode == null) {
			return null;
		}
		switch (mode) {
			case CONTROL:
				return new DropLocation.Control();
			case ONTO:
				if (args.getTargetObject() == null) {
					return null;
				}
				return new DropLocation.Onto(locate(context, args.getTargetObject(), unresolvedOut));
			case ORDERED:
				return new DropLocation.Insert(locate(context, args.getParent(), unresolvedOut),
					locate(context, args.getBefore(), unresolvedOut));
		}
		throw new IllegalArgumentException("Unknown drop mode: " + mode);
	}

	/**
	 * The object the given identity designates, {@code null} for no identity; an identity
	 * designating no object is added to the given list.
	 */
	private static Object locate(ActionContext context, ModelName name, List<ModelName> unresolvedOut) {
		if (name == null) {
			return null;
		}
		Object object = ScriptingModelKey.locate(context, null, name);
		if (object == null) {
			unresolvedOut.add(name);
		}
		return object;
	}

	/**
	 * The answer to a drop that is refused for the given reason.
	 *
	 * <p>
	 * A refused drop is no malfunction but a refusal like that of a command its executability rule
	 * forbids: the result is the {@link HandlerResult#notExecutable(ExecutableState) warning} of
	 * such a command, naming the reason.
	 * </p>
	 */
	public static HandlerResult refused(ResKey reason) {
		return HandlerResult.notExecutable(ExecutableState.createDisabledState(reason));
	}

}
