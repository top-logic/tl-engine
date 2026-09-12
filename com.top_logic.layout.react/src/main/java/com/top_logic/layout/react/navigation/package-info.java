/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Leading the user to the place a business object is displayed at.
 *
 * <p>
 * A control that shows a business object offers it as a link exactly when the
 * {@link com.top_logic.layout.react.navigation.ObjectNavigator} of its
 * {@link com.top_logic.layout.react.ReactContext} says the object can be displayed, and hands the
 * object to that navigator when the user follows the link. Where objects are displayed is decided
 * outside this module, so a control links to them without knowing anything about the places they
 * are displayed at.
 * </p>
 */
package com.top_logic.layout.react.navigation;
