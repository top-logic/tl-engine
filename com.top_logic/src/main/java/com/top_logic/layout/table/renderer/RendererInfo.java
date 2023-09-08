/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import com.top_logic.layout.Renderer;

/**
 * {@link Renderer} can implement this interface if they need addional information 
 * by the rendering process. The addtional information can be anything.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface RendererInfo {

	/**
	 * This method returns the renderer information. The infomation can be
	 * <code>null</code>.
	 */
	public Object getInfo();
	
	/**
	 * This method sets the renderer information.
	 * 
	 * @param anInfo
	 *            The info can be an arbitrary object. <code>Null</code> is
	 *            permitted.
	 */
	public void setInfo(Object anInfo);
	
}
