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

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * The part of the drop protocol every control accepting drops shares: the wire names, resolving
 * the dragged objects from the source control, and answering drop probes.
 *
 * <p>
 * A control accepting drops creates one {@link DropSupport} and keeps to itself only what its
 * drop targets are: which client-side key designates which target, and what a drop on it does.
 * A control whose items are places of a drop - rows of a table, nodes of a tree - hands its
 * {@link DropTarget} to {@link #setTarget(DropTarget)} and resolves the item and zone a drop names
 * into a {@link DropPlace}; applying and probing a drop is then
 * {@link #drop(DropArguments, DropPlace.Resolver)} and
 * {@link #probe(DropProbeArguments, DropPlace.Resolver)}.
 * </p>
 *
 * <p>
 * A drop is recorded and replayed as it is dispatched: the ids it names are
 * {@link com.top_logic.layout.react.control.identity.DisplayedObjectId}s, see {@link DropArguments}.
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
	 * State key: for each of the listed {@link #DROP_ACCEPTS accepted kinds}, the
	 * {@link DropTarget#dropModes(String) modes} of the operations accepting a drag of that kind, by
	 * kind.
	 *
	 * <p>
	 * The client splits an item by the modes of the kind it drags; a drag of a kind not listed here
	 * is split by the {@link #DROP_MODES modes of the whole target}.
	 * </p>
	 */
	public static final String DROP_MODES_BY_KIND = "dropModesByKind";

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
	 * The control of the owner's window the given reference designates - its control id or its
	 * semantic address -, or {@code null} if none is (or the reference is missing).
	 *
	 * @see ScriptingSession#controlOf(String)
	 */
	public ReactControl registeredControl(String reference) {
		return ScriptingSession.forWindowOf(_owner).controlOf(reference);
	}

	/**
	 * Resolves the dragged objects a client drop names through the {@link DragSourceControl} the
	 * {@link DropArguments#getSource() source} designates.
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
		ReactControl sourceControl = registeredControl(args.getSource());
		if (!(sourceControl instanceof DragSourceControl source)) {
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
	 * {@link #DROP_ACCEPTS_ANY}, {@link #DROP_ACCEPTS}, {@link #DROP_MODES} and
	 * {@link #DROP_MODES_BY_KIND}, by state key.
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
		Map<String, List<String>> modesByKind = new LinkedHashMap<>();
		if (target != null) {
			for (String kind : accepted.kinds()) {
				modesByKind.put(kind, wireNames(target.dropModes(kind)));
			}
		}
		result.put(DROP_MODES_BY_KIND, modesByKind);
		return result;
	}

	/**
	 * Resolves the client-side identities a drop names.
	 *
	 * <p>
	 * The dragged objects are resolved by the {@link DragSourceControl} the
	 * {@link DropArguments#getSource() source} designates (see
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
	public void drop(DropArguments args, DropPlace.Resolver places) {
		Resolved resolved = resolve(args, places);
		if (resolved.refusal() != null) {
			throw refused(resolved.refusal());
		}
		apply(resolved.request());
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
	private void apply(DropRequest request) {
		DropVerdict verdict = _target.check(request);
		if (!verdict.isAccepted()) {
			throw refused(verdict.reason());
		}
		_target.onDrop(request.event(verdict.location()));
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
	 * The exception answering a drop that is refused for the given reason.
	 *
	 * <p>
	 * A refused drop is no malfunction but a refusal like that of a command its executability rule
	 * forbids: the exception is the {@link CommandErrors#notExecutable(ExecutableState) warning}
	 * of such a command, naming the reason.
	 * </p>
	 *
	 * @return The exception to throw.
	 */
	public static TopLogicException refused(ResKey reason) {
		return CommandErrors.notExecutable(ExecutableState.createDisabledState(reason));
	}

}
