/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.keywords;

import java.util.Collection;

import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.common.webfolder.ui.commands.AbstractWebfolderAction;
import com.top_logic.knowledge.analyze.AnalyseHelper;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ShowKeywordExecutable} searches keywords for a the {@link #getContentObject()} and opens a
 * {@link KeywordsDialog} to display the results.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ShowKeywordExecutable extends AbstractWebfolderAction {

	/**
	 * Creates a {@link ShowKeywordExecutable}
	 * 
	 * @see AbstractWebfolderAction#AbstractWebfolderAction(FolderContent)
	 */
	public ShowKeywordExecutable(FolderContent node) {
		super(node);
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		KnowledgeObject ko = getContentObject().tHandle();
		Collection<String> keywords = new AnalyseHelper().searchKeywords(ko);
		if (keywords.isEmpty()) {
			return emptySearchResult(context);
		}
		KeywordsDialog dialog =
			new KeywordsDialog(keywords, DisplayDimension.FIFTY_PERCENT, DisplayDimension.dim(520, DisplayUnit.PIXEL));
		return dialog.open(context);
	}

	/**
	 * Informs about an empty search result.
	 */
	protected HandlerResult emptySearchResult(DisplayContext context) {
		final String msg = context.getResources().getString(I18NConstants.EMPTY_RESULT);
		return MessageBox.confirm(context, MessageType.INFO, msg, MessageBox.button(ButtonType.CLOSE));
	}

	@Override
	protected ExecutableState calculateExecutability() {
		return ExecutableState.EXECUTABLE;
	}

}
