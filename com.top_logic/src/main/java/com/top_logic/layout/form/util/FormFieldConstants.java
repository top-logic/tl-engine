/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

/**
 * This interface contains useful constants for better readability of code.
 *
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface FormFieldConstants {

    /** Readable name for a {@code true} value passed as disabled flag. */
    public static final boolean DISABLED          = true;
    /** Readable name for a {@code true} value passed as mandatory flag. */
    public static final boolean MANDATORY         = true;
    /** Readable name for a {@code true} value passed as immutable flag. */
    public static final boolean IMMUTABLE         = true;
    /** Readable name for a {@code true} value passed as selectable flag. */
    public static final boolean SELECTABLE        = true;
    /** Readable name for a {@code true} value passed as multiple selection flag. */
    public static final boolean MULTI_SELECT      = true;
    /**
     * Readable name for a {@code true} value passed as flag to report a problem as warning only.
     */
    public static final boolean AS_WARNING        = true;
    /** Readable name for a {@code true} value passed as flag to ignore whitespace. */
    public static final boolean IGNORE_WHITESPACE = true;

}
