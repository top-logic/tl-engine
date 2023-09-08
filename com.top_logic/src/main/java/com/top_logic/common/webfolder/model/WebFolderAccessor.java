/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

import java.util.Map;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.ui.DocumentAccessor;
import com.top_logic.common.webfolder.ui.WebFolderDateProperty;
import com.top_logic.common.webfolder.ui.WebFolderDescriptionProperty;
import com.top_logic.common.webfolder.ui.WebFolderMimetypeProperty;
import com.top_logic.common.webfolder.ui.WebFolderSizeProperty;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DispatchingAccessor;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.SelfPropertyAccessor;
import com.top_logic.layout.table.tree.TreeTableAccessor;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.UserObjectPropertyAccessor;

/**
 * Accessing methods to different aspects needed by the web folder display.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAccessor {

    public static final String SIZE = "_size";
    
	public static final String DESCRIPTION = "_description";

    public static final String TYPE = "_type";
    
    public static final String DATE = "_date";

    public static final String DOWNLOAD = "_download";

    public static final String CLIPBOARD = "_clipboard";
    
    public static final String LOCK = "_lock";
    
    public static final String VERSION = "_version";
    
    public static final String MAIL = "_mail";

    public static final String DELETE = "_delete";

	public static final String SIMILAR_DOCUMENTS = "_similar";

	public static final String KEYWORDS = "_keywords";

    public static final String AUTHOR = "_author";
    
	public static final String[] DEFAULT_COLUMNS =
		new String[] { WebFolder.NAME, DESCRIPTION, DOWNLOAD, TYPE, SIZE, DATE,
		LOCK, VERSION, CLIPBOARD, MAIL, SIMILAR_DOCUMENTS, KEYWORDS, DELETE };

    private static final Map<String, PropertyAccessor<? super FolderNode>> PROPERTIES = new MapBuilder<String, PropertyAccessor<? super FolderNode>>()
		.put(WebFolder.NAME, SelfPropertyAccessor.INSTANCE)
			.put(DESCRIPTION, wrap(WebFolderDescriptionProperty.INSTANCE))
			.put(SIZE, wrap(WebFolderSizeProperty.INSTANCE))
			.put(TYPE, wrap(WebFolderMimetypeProperty.INSTANCE))
			.put(DATE, wrap(WebFolderDateProperty.INSTANCE))
		.toMap();

	public static final Accessor<FolderNode> INSTANCE =
		new DispatchingAccessor<>(PROPERTIES, new TreeTableAccessor(WrapperAccessor.INSTANCE));

	private static PropertyAccessor<TLTreeNode> wrap(final PropertyAccessor<Object> impl) {
		return new UserObjectPropertyAccessor(new DocumentAccessor(impl));
    }
}

