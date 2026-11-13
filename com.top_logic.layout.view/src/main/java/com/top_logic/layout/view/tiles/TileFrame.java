/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.util.ResKey;

/**
 * A single entry on a {@link TileStackScope tile stack}.
 *
 * <p>
 * Captures everything needed to (re-)mount the referenced view: the {@link FrameRoute declaration}
 * of the frame, the view file to load, the breadcrumb label, and the bound parameter values. Parameter values are snapshotted at push time
 * - the frame does not hold live channels.
 * </p>
 */
public final class TileFrame {

	private final String _frameId;

	private final String _viewRef;

	private final ResKey _label;

	private final Map<String, Object> _params;

	private final boolean _declaredLabel;

	/**
	 * Creates a new {@link TileFrame} named by a label of its own, which stays as given.
	 *
	 * @param frameId
	 *        The {@link FrameRoute#id() id} of the declaration the frame is pushed by.
	 * @param viewRef
	 *        Path of the view file to load (relative to {@code /WEB-INF/views/}).
	 * @param label
	 *        Label shown for this frame in the breadcrumb. May be {@code null} for unnamed frames
	 *        (in which case the breadcrumb falls back to the view path).
	 * @param params
	 *        Snapshot of bound channel values. Each entry becomes a channel of the same name in
	 *        the frame's child {@link com.top_logic.layout.view.ViewContext} when the frame is
	 *        mounted. May be empty but not {@code null}.
	 */
	public TileFrame(String frameId, String viewRef, ResKey label, Map<String, Object> params) {
		this(frameId, viewRef, label, params, false);
	}

	/**
	 * Creates a new {@link TileFrame}.
	 *
	 * @param frameId
	 *        See {@link #TileFrame(String, String, ResKey, Map)}.
	 * @param viewRef
	 *        See {@link #TileFrame(String, String, ResKey, Map)}.
	 * @param label
	 *        See {@link #TileFrame(String, String, ResKey, Map)}.
	 * @param params
	 *        See {@link #TileFrame(String, String, ResKey, Map)}.
	 * @param declaredLabel
	 *        Whether the label is the one the {@link FrameRoute declaration} of the frame computes
	 *        from the params, see {@link #hasDeclaredLabel()}.
	 */
	TileFrame(String frameId, String viewRef, ResKey label, Map<String, Object> params, boolean declaredLabel) {
		_frameId = Objects.requireNonNull(frameId, "frameId");
		_viewRef = Objects.requireNonNull(viewRef, "viewRef");
		_label = label;
		_params = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(params, "params")));
		_declaredLabel = declaredLabel;
	}

	/**
	 * The {@link FrameRoute#id() id} of the declaration the frame is pushed by, within the stack it
	 * is on.
	 */
	public String getFrameId() {
		return _frameId;
	}

	/**
	 * Path of the view file to load.
	 */
	public String getViewRef() {
		return _viewRef;
	}

	/**
	 * Breadcrumb label for this frame, or {@code null}.
	 */
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * Snapshot of bound channel values, keyed by the channel name in the mounted view.
	 */
	public Map<String, Object> getParams() {
		return _params;
	}

	/**
	 * Whether the {@link #getLabel() label} is the one the {@link FrameRoute declaration} of the
	 * frame computes from its {@link #getParams() params}.
	 *
	 * <p>
	 * Such a label follows the objects it is computed from: the {@link TileStackScope} the frame is
	 * on names the frame anew when one of them changes. A label the navigation pushing the frame
	 * computed in its own context stays as it is.
	 * </p>
	 */
	public boolean hasDeclaredLabel() {
		return _declaredLabel;
	}

	/**
	 * This frame, named by the given label that its declaration computes.
	 *
	 * @param label
	 *        The label the {@link FrameRoute declaration} of the frame computes from the params now.
	 * @return A frame that {@link #showsSame(TileFrame) shows the same} as this one.
	 */
	TileFrame relabel(ResKey label) {
		return new TileFrame(_frameId, _viewRef, label, _params, true);
	}

	/**
	 * Whether the given frame displays the same as this one: the same declaration and view with the
	 * same parameter values.
	 *
	 * <p>
	 * Unlike {@link #equals(Object)}, the {@link #getLabel() label} is not compared: it names the
	 * frame in the breadcrumb, and a frame pushed by one navigation may be named differently than
	 * the same frame requested by another one, or renamed after an object it is named by changed.
	 * </p>
	 *
	 * @param other
	 *        The frame to compare with.
	 */
	public boolean showsSame(TileFrame other) {
		return _frameId.equals(other._frameId) && _viewRef.equals(other._viewRef) && _params.equals(other._params);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Whether the label is {@link #hasDeclaredLabel() declared} is not compared: it tells how the
	 * label is kept up to date, not how the frame is named.
	 * </p>
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TileFrame other)) {
			return false;
		}
		return _frameId.equals(other._frameId)
			&& _viewRef.equals(other._viewRef)
			&& Objects.equals(_label, other._label)
			&& _params.equals(other._params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_frameId, _viewRef, _label, _params);
	}

	@Override
	public String toString() {
		return "TileFrame[" + _frameId + ": " + _viewRef + ", label=" + _label + ", params=" + _params.keySet() + "]";
	}
}
