/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Bringing a nested part of the display into view.
 *
 * <p>
 * A container that shows one of its children at a time implements
 * {@link com.top_logic.layout.react.reveal.ChildRevealer}, so that a display request can walk from
 * the outermost container down to the place something is shown at, asking each container on the way
 * to reveal the child leading further down.
 * </p>
 */
package com.top_logic.layout.react.reveal;
