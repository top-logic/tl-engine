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

    /** Column name of the size of a document. */
    public static final String SIZE = "_size";
    
	/** Column name of the description of a folder content. */
	public static final String DESCRIPTION = "_description";

    /** Column name of the MIME type of a document. */
    public static final String TYPE = "_type";
    
    /** Column name of the modification date of a folder content. */
    public static final String DATE = "_date";

    /** Column name of the download command. */
    public static final String DOWNLOAD = "_download";

    /** Column name of the command adding content to the clipboard. */
    public static final String CLIPBOARD = "_clipboard";
    
    /** Column name of the lock command of a document. */
    public static final String LOCK = "_lock";
    
    /** Column name of the version (history) command of a document. */
    public static final String VERSION = "_version";
    
    /** Column name of the delete command. */
    public static final String DELETE = "_delete";

	/** Column name of the command showing similar documents. */
	public static final String SIMILAR_DOCUMENTS = "_similar";

	/** Column name of the command showing the keywords of a document. */
	public static final String KEYWORDS = "_keywords";

    /** Column name of the author of a document. */
    public static final String AUTHOR = "_author";
    
	/** Columns displayed in a web folder table by default. */
	public static final String[] DEFAULT_COLUMNS =
		new String[] { WebFolder.NAME, DESCRIPTION, DOWNLOAD, TYPE, SIZE, DATE,
			LOCK, VERSION, CLIPBOARD, SIMILAR_DOCUMENTS, KEYWORDS, DELETE };

    private static final Map<String, PropertyAccessor<? super FolderNode>> PROPERTIES = new MapBuilder<String, PropertyAccessor<? super FolderNode>>()
		.put(WebFolder.NAME, SelfPropertyAccessor.INSTANCE)
			.put(DESCRIPTION, wrap(WebFolderDescriptionProperty.INSTANCE))
			.put(SIZE, wrap(WebFolderSizeProperty.INSTANCE))
			.put(TYPE, wrap(WebFolderMimetypeProperty.INSTANCE))
			.put(DATE, wrap(WebFolderDateProperty.INSTANCE))
		.toMap();

	/** {@link Accessor} for the columns of a web folder table. */
	public static final Accessor<FolderNode> INSTANCE =
		new DispatchingAccessor<>(PROPERTIES, new TreeTableAccessor(WrapperAccessor.INSTANCE));

	private static PropertyAccessor<TLTreeNode> wrap(final PropertyAccessor<Object> impl) {
		return new UserObjectPropertyAccessor(new DocumentAccessor(impl));
    }
}

