/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.model.listen.ModelScope;

/**
 * Ambient handle to the surrounding {@link TileStackElement &lt;tile-stack&gt;}.
 *
 * <p>
 * Installed by the stack into its frame's child {@link com.top_logic.layout.view.ViewContext} via
 * {@link com.top_logic.layout.view.ViewContext#withScope(Class, Object)}. Commands and actions
 * inside a frame ({@link NavigatePushCommand &lt;navigate-push&gt;},
 * {@link NavigatePopCommand &lt;navigate-pop&gt;} / {@link NavigatePopAction the same as a chain
 * action}, {@link NavigatePopToCommand &lt;navigate-pop-to&gt;} / {@link NavigatePopToAction the
 * same as a chain action}) look it up via {@link #lookup(ReactContext, String)} and call
 * {@link #push(String, ResKey, Map)} / {@link #pop()} / {@link #popTo(int)} on it. A navigation
 * that replaces the upper part of the path by other frames, such as displaying an object in a frame
 * drilled down to, calls {@link #replaceFrom(int, List)}.
 * </p>
 *
 * <p>
 * The scope is a thin facade over the stack's path {@link ViewChannel}: all mutators read the
 * channel's current value, compute the new list and write it back. The channel is the sole source
 * of truth.
 * </p>
 *
 * <p>
 * A mutator that would drop a frame holding unsaved changes - {@link #pop()}, {@link #popTo(int)},
 * {@link #replaceFrom(int, List)} or a {@link #restore(int, FrameRoute, Map, long) restore} replacing
 * frames - is vetoed by the {@link ReactTileStackControl} displaying the stack: it throws a
 * {@link com.top_logic.layout.view.channel.ChannelVetoException} naming the forms of the dropped
 * frames and leaves the path unchanged. The exception's continuation performs the write once the
 * user has saved or discarded.
 * </p>
 *
 * <p>
 * The scope also carries the {@link #root() graph of frame declarations} of the stack: a frame is
 * pushed by the {@link FrameRoute#id() id} of its declaration, the declaration names a frame that is
 * pushed without a label of its own, and the declarations along the path are what
 * {@link TileFrameRouteParticipant} reflects the path in the URL with and restores it from.
 * </p>
 *
 * <p>
 * Together with the place of its stack in the display, the scope answers the
 * {@link #framePlace(int) place of a frame} of the path, so that what a frame contains can be
 * located and revealed like any other part of the display.
 * </p>
 *
 * <p>
 * A frame of the path named by the {@link TileFrame#hasDeclaredLabel() label of its declaration}
 * keeps that label up to date: the scope observes the objects the label is computed from (see
 * {@link FrameRoute#labelObserver(Map, Runnable)}) for as long as the frame is on the path, and
 * writes the path with the frame named anew when a change of one of them changes the label. The
 * frame keeps displaying what it displays - only its name in the breadcrumb and in the title of the
 * page changes. A label a navigation gives the frame it pushes stays as given.
 * </p>
 */
public class TileStackScope {

	/**
	 * The {@link TileStackScope} enclosing the given context.
	 *
	 * @param context
	 *        The context in which the requesting command or action executes.
	 * @param tag
	 *        Configuration tag of the requesting command or action, quoted in the failure message.
	 * @return The scope of the innermost enclosing {@link TileStackElement &lt;tile-stack&gt;}.
	 * @throws IllegalStateException
	 *         if the context is no {@link ViewContext}, or no tile stack encloses it.
	 */
	public static TileStackScope lookup(ReactContext context, String tag) {
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException(
				"<" + tag + "> requires a ViewContext, got " + context.getClass().getName());
		}
		TileStackScope scope = viewContext.getScope(TileStackScope.class);
		if (scope == null) {
			throw new IllegalStateException(
				"<" + tag + "> executed outside of any enclosing <tile-stack>.");
		}
		return scope;
	}

	private final ViewChannel _pathChannel;

	private final FrameRoute _root;

	private final Map<String, FrameRoute> _byId;

	/** The place of the stack in the display, {@code null} for a scope created without it. */
	private final RevealPath _place;

	/** The element a frame of the stack is addressed through, {@code null} without a place. */
	private final TileStackElement _element;

	private long _restoredAdoption;

	private List<TileFrame> _restoredPath = List.of();

	/**
	 * Supplies the scope the objects frame labels are computed from are observed in, {@code null}
	 * for a scope keeping the labels as computed when the frames were put on the path.
	 */
	private final Supplier<ModelScope> _modelScope;

	/**
	 * The subscription to the path, by which the {@link #_labelObservations} follow it; {@code null}
	 * without a {@link #_modelScope}, or once {@link #dispose() disposed}.
	 */
	private Registration _pathRegistration;

	/**
	 * The observation of the label of each frame of the path at the same position, {@code null}
	 * where the label of the frame is kept as it is.
	 */
	private List<LabelObservation> _labelObservations = new ArrayList<>();

	/**
	 * Creates a {@link TileStackScope} backed by the given path channel, whose place in the display
	 * is unknown, so that {@link #framePlace(int)} answers {@code null}, and that keeps the labels
	 * of its frames as computed when they were put on the path.
	 *
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path. Must not be {@code null}.
	 * @param frameRoutes
	 *        The declarations of the frames that may follow the initial view, in declaration order.
	 */
	public TileStackScope(ViewChannel pathChannel, List<FrameRoute> frameRoutes) {
		this(pathChannel, FrameRoute.initial(null, frameRoutes), null, null, null);
	}

	/**
	 * Creates a {@link TileStackScope} that also knows where its stack sits in the display.
	 *
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path. Must not be {@code null}.
	 * @param root
	 *        The {@link FrameRoute#initial(String, List) declaration of the initial view}, from which
	 *        the declarations of all frames of the stack are reached.
	 * @param place
	 *        The place of the stack in the display, or {@code null} to leave
	 *        {@link #framePlace(int)} unanswered.
	 * @param element
	 *        The element a frame of the stack is addressed through, the stack itself.
	 * @param modelScope
	 *        Supplies the scope the objects the labels of the frames are computed from are observed
	 *        in, so that a frame named by the {@link TileFrame#hasDeclaredLabel() label of its
	 *        declaration} is named anew when one of them changes. Asked whenever such a frame is
	 *        put on the path. {@code null} keeps every label as computed when the frame was put on
	 *        the path. A scope observing objects must be {@link #dispose() disposed}.
	 */
	public TileStackScope(ViewChannel pathChannel, FrameRoute root, RevealPath place, TileStackElement element,
			Supplier<ModelScope> modelScope) {
		_pathChannel = pathChannel;
		_root = root;
		_byId = Collections.unmodifiableMap(index(root));
		_place = place;
		_element = element;
		_modelScope = modelScope;
		if (modelScope != null) {
			_pathRegistration = pathChannel.addListener((sender, oldValue, newValue) -> observeLabels());
			observeLabels();
		}
	}

	/**
	 * Stops observing the objects the labels of the frames are computed from.
	 *
	 * <p>
	 * Called when the stack is disposed. The path stays as it is, with the labels the frames have.
	 * </p>
	 */
	public void dispose() {
		if (_pathRegistration != null) {
			_pathRegistration.dispose();
			_pathRegistration = null;
		}
		for (LabelObservation observation : _labelObservations) {
			if (observation != null) {
				observation.stop();
			}
		}
		_labelObservations = new ArrayList<>();
	}

	/**
	 * The declarations reachable from the given one, by id.
	 */
	private static Map<String, FrameRoute> index(FrameRoute root) {
		Map<String, FrameRoute> result = new LinkedHashMap<>();
		Deque<FrameRoute> pending = new ArrayDeque<>(root.successors());
		while (!pending.isEmpty()) {
			FrameRoute route = pending.removeFirst();
			if (result.putIfAbsent(route.id(), route) == null) {
				pending.addAll(route.successors());
			}
		}
		return result;
	}

	/**
	 * Current path (immutable snapshot).
	 */
	public List<TileFrame> getPath() {
		return readPath();
	}

	/**
	 * The place the frame at the given position of the path is displayed at.
	 *
	 * @param position
	 *        Index of the frame in the path, counted as {@link #getPath()} holds it.
	 * @return The place of that frame in the display, or {@code null} for a scope created without
	 *         the place of its stack.
	 */
	public RevealPath framePlace(int position) {
		if (_place == null) {
			return null;
		}
		return _place.append(_element, ReactTileStackControl.frameKey(position));
	}

	/**
	 * The channel holding the path, the stack's single source of truth.
	 *
	 * <p>
	 * Every navigation of the stack is a write to this channel, so watching it is how something
	 * outside the stack learns of one - the {@link TileFrameRouteParticipant} keeping the address bar
	 * on the displayed path, for instance.
	 * </p>
	 */
	public ViewChannel pathChannel() {
		return _pathChannel;
	}

	/**
	 * The declaration of the initial view of the stack, the start of the graph of its frame
	 * declarations: its {@link FrameRoute#successors() successors} are the frames that may follow
	 * the initial view.
	 */
	public FrameRoute root() {
		return _root;
	}

	/**
	 * All frame declarations of the stack.
	 */
	public Collection<FrameRoute> declarations() {
		return _byId.values();
	}

	/**
	 * Whether a frame of the stack can have an address: some declaration has a route.
	 */
	public boolean hasAddresses() {
		return _byId.values().stream().anyMatch(route -> route.pattern() != null);
	}

	/**
	 * The frame declaration of the stack with the given id, or {@code null} if it declares none.
	 *
	 * @param frameId
	 *        The {@link FrameRoute#id() id} of a declaration, as {@link TileFrame#getFrameId()} holds
	 *        it.
	 */
	public FrameRoute declaration(String frameId) {
		return _byId.get(frameId);
	}

	/**
	 * The declarations of the given frames, as far as the frames follow the graph of declarations.
	 *
	 * <p>
	 * The n-th entry is the declaration of the n-th frame, and the list ends before the first frame
	 * whose declaration is no {@link FrameRoute#successors() successor} of the declaration of the
	 * frame below it - of the initial view for the first frame.
	 * </p>
	 *
	 * @param frames
	 *        Frames of a path, lowest first.
	 */
	public List<FrameRoute> declarations(List<TileFrame> frames) {
		List<FrameRoute> result = new ArrayList<>(frames.size());
		FrameRoute below = _root;
		for (TileFrame frame : frames) {
			FrameRoute declaration = _byId.get(frame.getFrameId());
			if (declaration == null || !below.successors().contains(declaration)) {
				break;
			}
			result.add(declaration);
			below = declaration;
		}
		return result;
	}

	/**
	 * The declaration the frame put on top of the given frames follows, or {@code null} if the given
	 * frames do not follow the graph of declarations.
	 *
	 * @param frames
	 *        Frames of a path, lowest first.
	 * @return The declaration of the last of the given frames, the {@link #root()} for none.
	 */
	public FrameRoute top(List<TileFrame> frames) {
		List<FrameRoute> declarations = declarations(frames);
		if (declarations.size() < frames.size()) {
			return null;
		}
		return declarations.isEmpty() ? _root : declarations.get(declarations.size() - 1);
	}

	/**
	 * Appends a frame to the path.
	 *
	 * @param frameId
	 *        The {@link FrameRoute#id() id} of the declaration of the frame to push.
	 * @param label
	 *        Breadcrumb label, or {@code null} to use the one the declaration names the frame with.
	 * @param params
	 *        Bound parameter values captured at push time.
	 * @throws IllegalArgumentException
	 *         If the stack declares no frame of the given id.
	 */
	public void push(String frameId, ResKey label, Map<String, Object> params) {
		FrameRoute declaration = _byId.get(frameId);
		if (declaration == null) {
			throw new IllegalArgumentException(
				"The tile stack declares no frame '" + frameId + "', declared are: " + _byId.keySet());
		}
		List<TileFrame> next = new ArrayList<>(readPath());
		next.add(frame(declaration, label, params));
		_pathChannel.set(Collections.unmodifiableList(next));
	}

	/**
	 * Drops the topmost frame, if any.
	 */
	public void pop() {
		List<TileFrame> current = readPath();
		if (current.isEmpty()) {
			return;
		}
		_pathChannel.set(Collections.unmodifiableList(new ArrayList<>(current.subList(0, current.size() - 1))));
	}

	/**
	 * Truncates the path so that exactly the frames at positions {@code 0..size-1} remain. After
	 * the call, the new top frame is at index {@code size-1}; for {@code size == 0}, the stack
	 * shows its {@link TileStackElement.Config#getInitial() initial} view again.
	 *
	 * @param size
	 *        Number of frames to keep. Negative values clamp to 0, values larger than the current
	 *        size are no-ops.
	 */
	public void popTo(int size) {
		List<TileFrame> current = readPath();
		int target = Math.max(0, Math.min(size, current.size()));
		if (target == current.size()) {
			return;
		}
		_pathChannel.set(Collections.unmodifiableList(new ArrayList<>(current.subList(0, target))));
	}

	/**
	 * Keeps the frames of the path up to the given position and puts the given frames above them.
	 *
	 * <p>
	 * The path is written in one step, so that the display goes to the resulting path directly
	 * instead of passing through the shortened one, and a veto asks about all dropped frames at once.
	 * Its continuation performs that same write.
	 * </p>
	 *
	 * <p>
	 * A given frame that {@link TileFrame#showsSame(TileFrame) shows the same} as the one the path
	 * holds at its position stays displayed as the user left it. A given frame without a label is
	 * named by the label of its declaration, as {@link #push(String, ResKey, Map)} names it.
	 * </p>
	 *
	 * @param position
	 *        Number of frames of the current path to keep. Negative values clamp to 0, values larger
	 *        than the current size keep the whole path.
	 * @param frames
	 *        The frames to put above the kept ones, lowest first.
	 */
	public void replaceFrom(int position, List<TileFrame> frames) {
		_pathChannel.set(pathFrom(position, frames));
	}

	/**
	 * Puts the frame a URL names at the given position of the path, dropping whatever the path held
	 * from there on.
	 *
	 * <p>
	 * The path is written in one step, so that the frame the URL names replaces the one displayed
	 * instead of the display passing through the shortened path on its way there.
	 * </p>
	 *
	 * @param base
	 *        Number of frames the URL has already established, and therefore the position of the
	 *        frame it names now.
	 * @param declaration
	 *        The declaration of the frame the URL names.
	 * @param params
	 *        The parameter values the URL carries for the frame.
	 * @param adoptionId
	 *        Identifies the URL adoption this restore belongs to, so that
	 *        {@link #restoreBase(long)} can tell the frames it establishes from those of the
	 *        adoption before.
	 */
	public void restore(int base, FrameRoute declaration, Map<String, Object> params, long adoptionId) {
		List<TileFrame> path =
			pathFrom(base, List.of(new TileFrame(declaration.id(), declaration.viewRef(), null, params)));

		// Recorded before the write: writing the path mounts the frame, and the participant of that
		// frame asks for its base while the write is still in progress.
		_restoredAdoption = adoptionId;
		_restoredPath = path;

		_pathChannel.set(path);
	}

	/**
	 * The number of frames of the current path that the given URL adoption has established, and
	 * therefore the position at which the frame it names next belongs.
	 *
	 * <p>
	 * Zero for a path the adoption has not touched: the URL describes the path from its first frame,
	 * so what the display holds from an earlier navigation is replaced rather than extended.
	 * </p>
	 *
	 * @param adoptionId
	 *        Identifies the URL adoption in progress, see
	 *        {@link com.top_logic.layout.react.routing.RouteManager#adoptionId()}.
	 */
	public int restoreBase(long adoptionId) {
		List<TileFrame> current = readPath();
		if (_restoredAdoption != adoptionId || !_restoredPath.equals(current)) {
			return 0;
		}
		return current.size();
	}

	/**
	 * The frames of the current path up to the given position, followed by the given ones - each
	 * named by the label of its declaration where it carries none of its own.
	 */
	private List<TileFrame> pathFrom(int position, List<TileFrame> frames) {
		List<TileFrame> current = readPath();
		List<TileFrame> next =
			new ArrayList<>(current.subList(0, Math.max(0, Math.min(position, current.size()))));
		for (TileFrame frame : frames) {
			FrameRoute declaration = _byId.get(frame.getFrameId());
			next.add(frame.getLabel() != null || declaration == null ? frame
				: frame(declaration, null, frame.getParams()));
		}
		return Collections.unmodifiableList(next);
	}

	/**
	 * The frame of the given declaration, named by the given label or, without one, by the label of
	 * the declaration.
	 */
	private static TileFrame frame(FrameRoute declaration, ResKey label, Map<String, Object> params) {
		if (label != null) {
			return new TileFrame(declaration.id(), declaration.viewRef(), label, params);
		}
		return new TileFrame(declaration.id(), declaration.viewRef(), declaration.computeLabel(params), params, true);
	}

	/**
	 * Brings the observations of the frame labels in line with the current path.
	 *
	 * <p>
	 * The observation of a frame that stays at its position - showing the same, however it is named
	 * now - goes on; the observations of the frames the path dropped or replaced end, and the frames
	 * it took up are observed where they are named by their declaration.
	 * </p>
	 */
	private void observeLabels() {
		if (_pathRegistration == null) {
			return;
		}
		List<TileFrame> path = readPath();
		List<LabelObservation> before = _labelObservations;
		List<LabelObservation> after = new ArrayList<>(path.size());
		for (int position = 0; position < path.size(); position++) {
			TileFrame frame = path.get(position);
			LabelObservation kept = position < before.size() ? before.get(position) : null;
			if (kept != null && kept.follows(frame)) {
				before.set(position, null);
				kept.update(frame);
				after.add(kept);
			} else {
				after.add(observe(frame));
			}
		}
		_labelObservations = after;
		for (LabelObservation dropped : before) {
			if (dropped != null) {
				dropped.stop();
			}
		}
	}

	/**
	 * Starts observing the objects the label of the given frame is computed from.
	 *
	 * @return The observation, or {@code null} if the label of the frame is kept as it is.
	 */
	private LabelObservation observe(TileFrame frame) {
		if (!frame.hasDeclaredLabel()) {
			return null;
		}
		FrameRoute declaration = _byId.get(frame.getFrameId());
		if (declaration == null) {
			return null;
		}
		LabelObservation result = new LabelObservation(declaration, frame);
		ChannelObjectObserver observer = declaration.labelObserver(frame.getParams(), result::relabel);
		if (observer == null) {
			return null;
		}
		result.start(observer);
		return result;
	}

	/**
	 * The observation of the objects the label of a frame of the path is computed from.
	 */
	private final class LabelObservation {

		private final FrameRoute _declaration;

		/** The frame as the path holds it. */
		private TileFrame _frame;

		private ChannelObjectObserver _observer;

		LabelObservation(FrameRoute declaration, TileFrame frame) {
			_declaration = declaration;
			_frame = frame;
		}

		void start(ChannelObjectObserver observer) {
			_observer = observer;
			observer.attach(_modelScope.get());
		}

		void stop() {
			_observer.detach();
		}

		/**
		 * Whether this observation goes on for the given frame taking the place of the observed one.
		 */
		boolean follows(TileFrame frame) {
			return frame.hasDeclaredLabel() && _frame.showsSame(frame);
		}

		void update(TileFrame frame) {
			_frame = frame;
		}

		/**
		 * Computes the label again and writes the path with the frame named by it, if it changed.
		 */
		void relabel() {
			List<TileFrame> current = readPath();
			int position = _labelObservations.indexOf(this);
			if (position < 0 || position >= current.size() || current.get(position) != _frame) {
				return;
			}
			ResKey label = _declaration.computeLabel(_frame.getParams());
			if (Objects.equals(label, _frame.getLabel())) {
				return;
			}
			List<TileFrame> next = new ArrayList<>(current);
			next.set(position, _frame.relabel(label));
			_pathChannel.set(Collections.unmodifiableList(next));
		}
	}

	@SuppressWarnings("unchecked")
	private List<TileFrame> readPath() {
		Object value = _pathChannel.get();
		if (value instanceof List<?> list) {
			return (List<TileFrame>) list;
		}
		return Collections.emptyList();
	}
}
