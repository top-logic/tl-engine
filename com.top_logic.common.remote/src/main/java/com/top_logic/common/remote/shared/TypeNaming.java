/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * Strategy that assigns network types to objects in a {@link ObjectScope}.
 * 
 * @see ObjectScope#getTypeNaming()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeNaming {

	/**
	 * The network type name to use for the given object.
	 * 
	 * <p>
	 * A network type is a transportable representation of the object's application type. Two
	 * associated {@link ObjectScope}s must use the same network types for their objects while they
	 * may use different concrete implementation types for the same network type.
	 * </p>
	 * 
	 * @param obj
	 *        The object to request its network type for.
	 */
	String networkType(ObjectData obj);

}
