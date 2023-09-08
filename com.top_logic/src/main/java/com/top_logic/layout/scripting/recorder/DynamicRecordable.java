/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder;

/**
 * Marker interface that allows excluding operations from being recorded by the
 * {@link ScriptingRecorder}.
 * 
 * <p>
 * Implementors of this interface must override {@link #shouldRecord()} to enable script recording
 * in specific situations. By default, {@link DynamicRecordable}s are not recorded.
 * </p>
 * 
 * <p>
 * This "annotation" interface can be used for static excludes from recording instead of dynamic
 * exclusion, see
 * {@link ScriptingRecorder#annotateAsDontRecord(com.top_logic.basic.col.TypedAnnotatable)}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicRecordable {

	/**
	 * Whether this action should be recorded, <code>false</code> by default.
	 * 
	 * <p>
	 * Note: This method is only consulted, if this instance is not otherwise explicitly marked as
	 * excluded using
	 * {@link ScriptingRecorder#annotateAsDontRecord(com.top_logic.basic.col.TypedAnnotatable)}.
	 * </p>
	 */
	default boolean shouldRecord() {
		return false;
	}

}
