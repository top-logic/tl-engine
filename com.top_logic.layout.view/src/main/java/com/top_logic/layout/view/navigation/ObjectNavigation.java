/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DirtyConfirmDialogControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileStackScope;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.util.error.TopLogicException;

/**
 * Displays a business object where the application shows objects of its type, or a named list of
 * views with the values a request carries.
 *
 * <p>
 * The {@link DisplayTarget} of the object's type decides which views display it. Each of them is
 * brought into view in turn - by opening the containers on the way to the place it is mounted at, by
 * drilling down into a stack of frames, or by opening a dialog - and then receives the values its
 * {@link ShowStep#bindings() bindings} compute from the object. A request that names the views
 * itself is carried out the same way, with whatever value the bindings are applied to.
 * </p>
 *
 * <p>
 * A view is looked for within the view displayed before it, and only then within the window as a
 * whole. A tab of a frame that the preceding view pushed onto a stack is thereby revealed like any
 * other place, although the frame itself is part of no statically scanned {@link MountPath}.
 * </p>
 *
 * <p>
 * Among the targets declared for the type, the one displayed nearest to where the request comes from
 * wins, and a view mounted at several places is opened at the one nearest to it. What is displayed
 * on the way may hold unsaved changes and veto: the user is then asked how to proceed, and the
 * display continues once they answered - which is why the caller hands over a {@link Continuation}
 * rather than being returned to.
 * </p>
 */
public final class ObjectNavigation {

	private ObjectNavigation() {
		// Static utility.
	}

	/**
	 * Displays the given object at the place the application declares for its type.
	 *
	 * @param context
	 *        Where the request comes from; decides which of several places is the nearest one.
	 * @param object
	 *        The object to display.
	 * @param continuation
	 *        Resumed with the displayed object once it is displayed, aborted when the user declined
	 *        to give up unsaved changes on the way.
	 * @throws TopLogicException
	 *         If the application displays no objects of the given object's type, or the declared
	 *         place cannot be reached.
	 *
	 * @see DisplayTargetService
	 */
	public static void show(ReactContext context, Object object, Continuation continuation) {
		show(context, DisplayTargetService.getInstance().getTargets(), object, continuation);
	}

	/**
	 * Displays the given object at the place the given targets declare for its type.
	 *
	 * @param targets
	 *        The catalog of places objects are displayed at.
	 *
	 * @see #show(ReactContext, Object, Continuation)
	 */
	public static void show(ReactContext context, DisplayTargets targets, Object object,
			Continuation continuation) {
		ViewContext origin = viewContext(context);
		if (!(object instanceof TLObject shown)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_MODEL_OBJECT__VALUE.fill(object));
		}
		MountPath hint = hint(origin);
		TLType type = shown.tType();
		DisplayTarget target = targets.resolveBest(type, hint);
		if (target == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_DISPLAY_TARGET__TYPE.fill(type));
		}
		new Display(origin, hint, targets::getMounts, target.shows(), shown, continuation).start();
	}

	/**
	 * Displays the given views in turn, each with the values its bindings compute from the given
	 * value.
	 *
	 * <p>
	 * The views are brought into view the same way a target's views are, so a view is looked for
	 * within the view the show before it displayed first, and only then within the window as a
	 * whole. The value takes the place the object takes when a target is displayed: it is what the
	 * bindings and the label expressions are applied to, and what the continuation is resumed with.
	 * Any value does, a business object as well as a text a filter is set to.
	 * </p>
	 *
	 * @param context
	 *        Where the request comes from; decides which of several places is the nearest one.
	 * @param shows
	 *        The views to display, outermost first.
	 * @param value
	 *        What the bindings compute their values from.
	 * @param continuation
	 *        Resumed with the value once the views are displayed, aborted when the user declined to
	 *        give up unsaved changes on the way.
	 * @throws TopLogicException
	 *         If one of the views cannot be reached.
	 */
	public static void show(ReactContext context, List<ShowStep> shows, Object value,
			Continuation continuation) {
		ViewContext origin = viewContext(context);
		new Display(origin, hint(origin), windowMounts(origin), shows, value, continuation).start();
	}

	/**
	 * The view context the request is carried out in.
	 */
	private static ViewContext viewContext(ReactContext context) {
		if (!(context instanceof ViewContext result)) {
			throw new TopLogicException(I18NConstants.ERROR_CANNOT_DISPLAY_HERE);
		}
		return result;
	}

	/**
	 * The place the request comes from, deciding which of several declared or mounted places is the
	 * nearest one.
	 */
	private static MountPath hint(ViewContext origin) {
		RevealRegistry registry = origin.getRevealRegistry();
		RevealPath here = RevealPath.of(origin);
		String viewRef = registry == null ? null : registry.viewAt(here);
		return here.toMountPath(viewRef == null ? "" : viewRef);
	}

	/**
	 * The places the views are displayed at, as seen from the root of the window the request comes
	 * from.
	 */
	private static Supplier<ViewMounts> windowMounts(ViewContext origin) {
		RevealRegistry registry = origin.getRevealRegistry();
		String rootView = registry == null ? null : registry.getRootView();
		return () -> rootView == null ? null : ViewMounts.forRootView(rootView);
	}

	/**
	 * One request to display an object, carried out step by step so that it can be suspended while
	 * the user is asked about unsaved changes.
	 */
	private static final class Display {

		private final ViewContext _origin;

		/**
		 * The places the views are declared to be displayed at, asked once and only when a view is
		 * not found within the view displayed before it.
		 */
		private final Supplier<ViewMounts> _declaredMounts;

		private final Object _value;

		private final Continuation _continuation;

		private final RevealRegistry _registry;

		/**
		 * The place the request comes from, deciding which of several declared or mounted places is
		 * the nearest one.
		 */
		private final MountPath _hint;

		/**
		 * The context of the view displayed last, where the stack a drilled-down frame is pushed
		 * onto is looked up. The requesting context until a view was displayed.
		 */
		private ViewContext _current;

		/**
		 * Path of the view file {@link #_current} displays, within which the view of the next
		 * {@link ShowStep} is looked for. {@code null} until a view was displayed.
		 */
		private String _currentView;

		private final List<ShowStep> _shows;

		/**
		 * The frames the drill-down is to end up with, in the order they were computed.
		 */
		private final List<TileFrame> _frames = new ArrayList<>();

		private ViewMounts _mounts;

		private boolean _mountsResolved;

		Display(ViewContext origin, MountPath hint, Supplier<ViewMounts> declaredMounts, List<ShowStep> shows,
				Object value, Continuation continuation) {
			_origin = origin;
			_hint = hint;
			_declaredMounts = declaredMounts;
			_shows = shows;
			_value = value;
			_continuation = continuation;
			_current = origin;
			_registry = origin.getRevealRegistry();
		}

		void start() {
			step(0);
		}

		/**
		 * Displays the view of the given position, and everything after it.
		 */
		private void step(int index) {
			if (index >= _shows.size()) {
				_continuation.resume(_value);
				return;
			}

			ShowStep show = _shows.get(index);
			if (show.dialog()) {
				if (index != _shows.size() - 1) {
					throw new TopLogicException(I18NConstants.ERROR_DIALOG_IS_NOT_LAST__VIEW.fill(show.viewRef()));
				}
				openDialog(show);
				step(index + 1);
				return;
			}

			List<Place> places = places(show);
			if (places.isEmpty()) {
				drillDown(show, index);
				return;
			}
			Place place = nearest(places);
			reveal(show, place.steps(), 0, place.start(), () -> display(show, place, index));
		}

		/**
		 * The places the given view is displayed at: those within the view displayed before it, or
		 * else those within the window as a whole.
		 *
		 * @return The candidates to pick the nearest one from, empty for a view that is displayed
		 *         neither within the one before it nor anywhere in the window.
		 */
		private List<Place> places(ShowStep show) {
			if (_currentView != null && !_currentView.equals(show.viewRef())) {
				// A view's own scan reports the view itself, so a show repeating the view displayed
				// last is located in the window instead of staying where it is.
				RevealPath start = RevealPath.of(_current);
				List<MountPath> within = ViewMounts.forRootView(_currentView).getMounts(show.viewRef());
				if (!within.isEmpty()) {
					return within.stream().map(mount -> new Place(start, mount.steps())).toList();
				}
			}
			ViewMounts mounts = mounts();
			if (mounts == null) {
				return List.of();
			}
			return mounts.getMounts(show.viewRef()).stream()
				.map(mount -> new Place(RevealPath.ROOT, mount.steps()))
				.toList();
		}

		/**
		 * Asks the container of the given position to display the child leading further down, then
		 * continues with the next one.
		 */
		private void reveal(ShowStep show, List<MountStep> steps, int index, RevealPath here, Runnable onDone) {
			if (index >= steps.size()) {
				onDone.run();
				return;
			}
			MountStep step = steps.get(index);
			ChildRevealer container = _registry == null ? null : _registry.getContainer(step.container(), here);
			if (container == null) {
				throw new TopLogicException(I18NConstants.ERROR_CONTAINER_NOT_DISPLAYED__CONTAINER_VIEW
					.fill(step.toString(), show.viewRef()));
			}
			guarded(() -> container.revealChild(step.key()),
				() -> reveal(show, steps, index + 1, here.append(step.container(), step.key()), onDone));
		}

		/**
		 * Writes the value into the channels of the view instance displayed at the given place, then
		 * continues with the view after it.
		 */
		private void display(ShowStep show, Place place, int index) {
			ViewContext instance = _registry == null ? null : _registry.getView(show.viewRef(), place.path());
			if (instance == null) {
				throw new TopLogicException(I18NConstants.ERROR_VIEW_NOT_DISPLAYED__VIEW.fill(show.viewRef()));
			}
			bind(show, instance, 0, () -> {
				displayed(show.viewRef(), instance);
				step(index + 1);
			});
		}

		/**
		 * Remembers the view a {@link ShowStep} brought into view, within which the show after it is
		 * looked for.
		 */
		private void displayed(String viewRef, ViewContext instance) {
			_current = instance;
			_currentView = viewRef;
		}

		/**
		 * Writes the binding of the given position, then the ones after it.
		 */
		private void bind(ShowStep show, ViewContext instance, int index, Runnable onDone) {
			List<Binding> bindings = show.bindings();
			if (index >= bindings.size()) {
				onDone.run();
				return;
			}
			Binding binding = bindings.get(index);
			if (!instance.hasChannel(binding.channel())) {
				throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_CHANNEL__CHANNEL_VIEW
					.fill(binding.channel(), show.viewRef()));
			}
			ViewChannel channel = instance.resolveChannel(new ChannelRef(binding.channel()));
			Object value = binding.evaluate(_value);
			guarded(() -> channel.set(value), () -> bind(show, instance, index + 1, onDone));
		}

		/**
		 * Drills the surrounding stack down to the given view, then continues with the view after
		 * it.
		 *
		 * <p>
		 * The frames already showing what is wanted stay as they are, so that a display request
		 * arriving where the user already is leaves their drill-down untouched.
		 * </p>
		 */
		private void drillDown(ShowStep show, int index) {
			TileStackScope stack = _current.getScope(TileStackScope.class);
			if (stack == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_TILE_STACK__VIEW.fill(show.viewRef()));
			}

			Map<String, Object> params = new LinkedHashMap<>();
			for (Binding binding : show.bindings()) {
				params.put(binding.channel(), binding.evaluate(_value));
			}
			_frames.add(new TileFrame(show.viewRef(), show.labelFor(_value), params));

			List<TileFrame> current = stack.getPath();
			if (current.size() >= _frames.size() && current.subList(0, _frames.size()).equals(_frames)) {
				enterFrame(stack, show);
				step(index + 1);
				return;
			}
			int common = commonPrefixLength(current, _frames);
			guarded(() -> {
				stack.popTo(common);
				for (int n = common; n < _frames.size(); n++) {
					TileFrame frame = _frames.get(n);
					stack.push(frame.getViewRef(), frame.getLabel(), frame.getParams());
				}
			}, () -> {
				enterFrame(stack, show);
				step(index + 1);
			});
		}

		/**
		 * Continues within the frame the given view was drilled down to, so that the show after it
		 * is looked for inside that frame.
		 *
		 * <p>
		 * The view displayed so far stays the one to continue from while the stack does not say
		 * where its frames sit, or while the frame has not announced itself.
		 * </p>
		 */
		private void enterFrame(TileStackScope stack, ShowStep show) {
			RevealPath place = stack.framePlace(_frames.size() - 1);
			if (place == null || _registry == null) {
				return;
			}
			ViewContext frame = _registry.getView(show.viewRef(), place);
			if (frame != null) {
				displayed(show.viewRef(), frame);
			}
		}

		/**
		 * Opens the given view as a dialog, carrying the values its bindings compute.
		 */
		private void openDialog(ShowStep show) {
			Map<String, Object> channelValues = new LinkedHashMap<>();
			for (Binding binding : show.bindings()) {
				channelValues.put(binding.channel(), binding.evaluate(_value));
			}
			OpenDialogAction.openDialog(_current, ViewLoader.fullPath(show.viewRef()),
				OpenDialogAction.Config.CLOSE_ON_BACKDROP_DEFAULT, channelValues, List.of());
		}

		/**
		 * Carries out one step of the display, asking the user how to proceed when what is displayed
		 * holds unsaved changes.
		 *
		 * @param step
		 *        The step to carry out. Retried once the unsaved changes are saved or discarded, so
		 *        it must be able to run twice.
		 * @param onDone
		 *        What follows the step.
		 */
		private void guarded(Runnable step, Runnable onDone) {
			try {
				step.run();
			} catch (ChannelVetoException veto) {
				DialogManager dialogs = _origin.getDialogManager();
				if (dialogs == null) {
					// Nothing can ask the user, and displaying the object would silently discard
					// what they entered.
					_continuation.abort();
					return;
				}
				DirtyConfirmDialogControl.openDialog(_origin, dialogs, veto.getDirtyHandlers(),
					() -> guarded(step, onDone),
					() -> {
						Runnable rollback = veto.getRollback();
						if (rollback != null) {
							rollback.run();
						}
						_continuation.abort();
					});
				return;
			}
			onDone.run();
		}

		/**
		 * The place among the given ones that shares the longest way with where the request comes
		 * from.
		 */
		private Place nearest(List<Place> places) {
			Place result = places.get(0);
			int best = commonPrefixLength(result.path().mountSteps(), _hint.steps());
			for (Place candidate : places.subList(1, places.size())) {
				int shared = commonPrefixLength(candidate.path().mountSteps(), _hint.steps());
				if (shared > best) {
					best = shared;
					result = candidate;
				}
			}
			return result;
		}

		/**
		 * The places the views are displayed at, as seen from the root of this window.
		 *
		 * @return The scan, or {@code null} while nothing says what this window displays; every view
		 *         is then reached by displaying it anew.
		 */
		private ViewMounts mounts() {
			if (!_mountsResolved) {
				_mountsResolved = true;
				_mounts = resolveMounts();
			}
			return _mounts;
		}

		private ViewMounts resolveMounts() {
			ViewMounts declared = _declaredMounts.get();
			String rootView = _registry == null ? null : _registry.getRootView();
			if (rootView == null || declared == null || rootView.equals(declared.getRootView())) {
				return declared;
			}
			// This window shows something else than the application's default display, so the
			// declared places do not describe it.
			return ViewMounts.forRootView(rootView);
		}

		private static int commonPrefixLength(List<?> left, List<?> right) {
			int limit = Math.min(left.size(), right.size());
			int result = 0;
			while (result < limit && left.get(result).equals(right.get(result))) {
				result++;
			}
			return result;
		}

		/**
		 * Where the view of a {@link ShowStep} is displayed: the place it is reached from, and the
		 * containers to ask from there on.
		 *
		 * @param start
		 *        The place the first container is asked at, {@link RevealPath#ROOT} for a view
		 *        located within the window as a whole.
		 * @param steps
		 *        The containers to ask, outermost first.
		 */
		private record Place(RevealPath start, List<MountStep> steps) {

			/**
			 * The place the view itself is displayed at.
			 */
			RevealPath path() {
				RevealPath result = start;
				for (MountStep step : steps) {
					result = result.append(step.container(), step.key());
				}
				return result;
			}
		}
	}
}
