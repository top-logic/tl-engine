/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Tile-stack drill-down navigation for the new view layer.
 *
 * <p>
 * A {@link com.top_logic.layout.view.tiles.TileStackElement &lt;tile-stack&gt;} holds a path of
 * {@link com.top_logic.layout.view.tiles.TileFrame frames} and renders only the top frame. The
 * {@link com.top_logic.layout.view.tiles.NavigatePushCommand &lt;navigate-push&gt;} command (used
 * in any command slot inside a frame) pushes a new frame. The
 * {@link com.top_logic.layout.view.tiles.TileBreadcrumbElement &lt;tile-breadcrumb&gt;} element
 * reads the same path channel and renders crumbs for outward navigation.
 * </p>
 *
 * <p>
 * The path lives on a {@link com.top_logic.layout.view.channel.ViewChannel} of type
 * {@code List<TileFrame>}. Both push and pop are implemented as writes to this channel, so the
 * channel is the single source of truth and URL persistence becomes a normal
 * {@code <param-bindings>} concern external to the tile infrastructure.
 * </p>
 */
package com.top_logic.layout.view.tiles;
