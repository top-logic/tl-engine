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
 * Captures everything needed to (re-)mount the referenced view: the view file to load, the
 * breadcrumb label, and the bound parameter values. Parameter values are snapshotted at push time
 * - the frame does not hold live channels.
 * </p>
 */
public final class TileFrame {

	private final String _viewRef;

	private final ResKey _label;

	private final Map<String, Object> _params;

	/**
	 * Creates a new {@link TileFrame}.
	 *
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
	public TileFrame(String viewRef, ResKey label, Map<String, Object> params) {
		_viewRef = Objects.requireNonNull(viewRef, "viewRef");
		_label = label;
		_params = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(params, "params")));
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TileFrame other)) {
			return false;
		}
		return _viewRef.equals(other._viewRef)
			&& Objects.equals(_label, other._label)
			&& _params.equals(other._params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_viewRef, _label, _params);
	}

	@Override
	public String toString() {
		return "TileFrame[" + _viewRef + ", label=" + _label + ", params=" + _params.keySet() + "]";
	}
}
