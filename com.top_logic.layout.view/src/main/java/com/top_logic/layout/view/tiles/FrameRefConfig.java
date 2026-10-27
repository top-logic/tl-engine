/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;

/**
 * Reference to a {@link FrameRouteConfig &lt;frame&gt;} declaration of the same stack that may
 * follow the frame of the enclosing declaration.
 *
 * <p>
 * The referenced declaration is named by its {@link FrameRouteConfig#getId() id} and may be declared
 * anywhere in the stack, the enclosing declarations included. That is how a recursive structure is
 * declared - a folder that may contain folders:
 * </p>
 *
 * <pre>
 * &lt;frame id="folder" route="folder/:folder" view="files/folder.view.xml"&gt;
 *   &lt;param name="folder" expr="objectId($folder)" reverse="$value.objectResolve(`files:Folder`)"/&gt;
 *   &lt;frame-ref frame="folder"/&gt;
 *   &lt;frame id="file" route="file/:file" view="files/file.view.xml"&gt;...&lt;/frame&gt;
 * &lt;/frame&gt;
 * </pre>
 *
 * <p>
 * With it, the address <code>folder/1/folder/7/folder/9/file/3</code> restores four frames.
 * </p>
 */
@TagName(FrameRefConfig.TAG_NAME)
@TitleProperty(name = FrameRefConfig.FRAME)
public interface FrameRefConfig extends FrameSuccessorConfig {

	/** Configuration tag of a {@link FrameRefConfig}. */
	String TAG_NAME = "frame-ref";

	/** Configuration name for {@link #getFrame()}. */
	String FRAME = "frame";

	/**
	 * The {@link FrameRouteConfig#getId() id} of the referenced frame declaration.
	 *
	 * <p>
	 * An id that no declaration of the stack carries is a configuration error.
	 * </p>
	 */
	@Name(FRAME)
	@Mandatory
	String getFrame();
}
