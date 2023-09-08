/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.similar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.common.webfolder.ui.commands.AbstractWebfolderAction;
import com.top_logic.knowledge.analyze.AnalyseHelper;
import com.top_logic.knowledge.analyze.KnowledgeObjectResult;
import com.top_logic.knowledge.analyze.SearchResult;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ShowSimilarExecutable} searches similar objects, filters the result to get similar
 * documents and opens a {@link SimilarDocumentsDialog} to show them.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ShowSimilarExecutable extends AbstractWebfolderAction {

	/**
	 * Creates a {@link ShowSimilarExecutable}.
	 * 
	 * @see AbstractWebfolderAction#AbstractWebfolderAction(FolderContent)
	 */
	public ShowSimilarExecutable(FolderContent node) {
		super(node);
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		KnowledgeObject ko = getContentObject().tHandle();
		final Collection<? extends KnowledgeObjectResult> similar = new AnalyseHelper().searchSimilar(ko);
		if (similar.isEmpty()) {
			return emptySearchResult(context);
		}
		List<Document> similarDocs = extractDocuments(similar);
		if (similarDocs.isEmpty()) {
			return emptySearchResult(context);
		}
		SimilarDocumentsDialog dialog =
			new SimilarDocumentsDialog(similarDocs, DisplayDimension.FIFTY_PERCENT, DisplayDimension.FIFTY_PERCENT);
		return dialog.open(context);
	}

	/**
	 * Informs about an empty search result.
	 */
	protected HandlerResult emptySearchResult(DisplayContext context) {
		return MessageBox.confirm(context, MessageType.INFO,
			context.getResources().getString(I18NConstants.EMPTY_RESULT), MessageBox.button(ButtonType.CLOSE));
	}

	/**
	 * Unpacks the {@link SearchResult} and adds the actual result (if it is a {@link Document}) to
	 * the result list.
	 */
	private List<Document> extractDocuments(Collection<? extends KnowledgeObjectResult> searchSimilar) {
		ArrayList<Document> result = new ArrayList<>(searchSimilar.size());
		for (KnowledgeObjectResult searchResult : searchSimilar) {
			KnowledgeObject ko = searchResult.getKnowledgeObject();
			Wrapper potentialDocument = WrapperFactory.getWrapper(ko);
			if (potentialDocument instanceof Document) {
				result.add((Document) potentialDocument);
			}
		}
		return result;
	}

	@Override
	protected ExecutableState calculateExecutability() {
		return ExecutableState.EXECUTABLE;
	}

}
