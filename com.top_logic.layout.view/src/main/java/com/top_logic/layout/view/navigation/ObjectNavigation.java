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
 * Displays a business object where the application shows objects of its type.
 *
 * <p>
 * The {@link DisplayTarget} of the object's type decides which views display it. Each of them is
 * brought into view in turn - by opening the containers on the way to the place it is mounted at, by
 * drilling down into a stack of frames, or by opening a dialog - and then receives the values its
 * {@link ShowStep#bindings() bindings} compute from the object.
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
		if (!(context instanceof ViewContext viewContext)) {
			throw new TopLogicException(I18NConstants.ERROR_CANNOT_DISPLAY_HERE);
		}
		if (!(object instanceof TLObject shown)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_MODEL_OBJECT__VALUE.fill(object));
		}
		new Display(viewContext, targets, shown, continuation).start();
	}

	/**
	 * One request to display an object, carried out step by step so that it can be suspended while
	 * the user is asked about unsaved changes.
	 */
	private static final class Display {

		private final ViewContext _origin;

		private final DisplayTargets _targets;

		private final TLObject _object;

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

		private List<ShowStep> _shows;

		/**
		 * The frames the drill-down is to end up with, in the order they were computed.
		 */
		private final List<TileFrame> _frames = new ArrayList<>();

		private ViewMounts _mounts;

		private boolean _mountsResolved;

		Display(ViewContext origin, DisplayTargets targets, TLObject object, Continuation continuation) {
			_origin = origin;
			_targets = targets;
			_object = object;
			_continuation = continuation;
			_current = origin;
			_registry = origin.getRevealRegistry();

			RevealPath here = RevealPath.of(origin);
			String viewRef = _registry == null ? null : _registry.viewAt(here);
			_hint = here.toMountPath(viewRef == null ? "" : viewRef);
		}

		void start() {
			TLType type = _object.tType();
			DisplayTarget target = _targets.resolveBest(type, _hint);
			if (target == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_DISPLAY_TARGET__TYPE.fill(type));
			}
			_shows = target.shows();
			step(0);
		}

		/**
		 * Displays the view of the given position, and everything after it.
		 */
		private void step(int index) {
			if (index >= _shows.size()) {
				_continuation.resume(_object);
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

			ViewMounts mounts = mounts();
			List<MountPath> places = mounts == null ? List.of() : mounts.getMounts(show.viewRef());
			if (places.isEmpty()) {
				drillDown(show, index);
				return;
			}
			MountPath mount = nearest(places);
			reveal(show, mount.steps(), 0, RevealPath.ROOT, () -> display(show, mount, index));
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
		 * Writes the object into the channels of the view instance displayed at the given mount,
		 * then continues with the view after it.
		 */
		private void display(ShowStep show, MountPath mount, int index) {
			RevealPath path = RevealPath.ROOT;
			for (MountStep step : mount.steps()) {
				path = path.append(step.container(), step.key());
			}
			ViewContext instance = _registry == null ? null : _registry.getView(show.viewRef(), path);
			if (instance == null) {
				throw new TopLogicException(I18NConstants.ERROR_VIEW_NOT_DISPLAYED__VIEW.fill(show.viewRef()));
			}
			bind(show, instance, 0, () -> {
				_current = instance;
				step(index + 1);
			});
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
			Object value = binding.evaluate(_object);
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
				params.put(binding.channel(), binding.evaluate(_object));
			}
			_frames.add(new TileFrame(show.viewRef(), show.labelFor(_object), params));

			List<TileFrame> current = stack.getPath();
			if (current.size() >= _frames.size() && current.subList(0, _frames.size()).equals(_frames)) {
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
			}, () -> step(index + 1));
		}

		/**
		 * Opens the given view as a dialog, carrying the values its bindings compute.
		 */
		private void openDialog(ShowStep show) {
			Map<String, Object> channelValues = new LinkedHashMap<>();
			for (Binding binding : show.bindings()) {
				channelValues.put(binding.channel(), binding.evaluate(_object));
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
		private MountPath nearest(List<MountPath> mounts) {
			MountPath result = mounts.get(0);
			int best = commonPrefixLength(result.steps(), _hint.steps());
			for (MountPath candidate : mounts.subList(1, mounts.size())) {
				int shared = commonPrefixLength(candidate.steps(), _hint.steps());
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
			ViewMounts declared = _targets.getMounts();
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
	}
}
