/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Locating the views of an application within its root view.
 *
 * <p>
 * {@link com.top_logic.layout.view.navigation.ViewMounts} scans the configuration reachable from a
 * root view and answers, for each view file, the
 * {@link com.top_logic.layout.view.navigation.MountPath places} it is displayed at. A place is a
 * sequence of {@link com.top_logic.layout.view.navigation.MountStep steps}, each naming a container
 * that chooses between its content groups (a sidebar, a tab bar, a master-detail element, a tile
 * stack) and the key of the group to display.
 * </p>
 *
 * <p>
 * The scan reads the shared element tree through
 * {@link com.top_logic.layout.view.UIElement#getChildGroups()}, so it also covers the containers
 * that create their content only when it is first displayed and whose mounts are consequently
 * absent from a session's control tree.
 * </p>
 *
 * <p>
 * {@link com.top_logic.layout.view.navigation.DisplayTargetService} holds the counterpart: the
 * {@link com.top_logic.layout.view.navigation.DisplayTarget}s declaring, per model type, which
 * views display an object of that type and which values their channels receive. Asking
 * {@link com.top_logic.layout.view.navigation.DisplayTargets} for a type answers the targets in
 * preference order - the most special type first, and among equal types the one displayed nearest
 * to where the object is offered.
 * </p>
 *
 * <p>
 * {@link com.top_logic.layout.view.navigation.ObjectNavigation} carries a request out. Everything a
 * window displays announces itself in the window's
 * {@link com.top_logic.layout.view.navigation.RevealRegistry} under the
 * {@link com.top_logic.layout.view.navigation.RevealPath place} it is displayed at, so the request
 * can walk from the root display down to the mount of the target's view - asking each container on
 * the way to reveal the child leading further down - and write the object into the channels of the
 * instance it finds there. A view no mount reaches is displayed anew: as a frame drilled down to on
 * a stack, or as a dialog. The
 * {@link com.top_logic.layout.view.navigation.ShowObjectAction &lt;show-object&gt;} action offers
 * all of this to a configured command.
 * </p>
 */
package com.top_logic.layout.view.navigation;
