/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import java.util.Collection;
import java.util.Date;

import com.top_logic.element.comment.wrap.Comment;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ModelNamingScheme} for {@link Comment}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommentNamingScheme extends AbstractModelNamingScheme<Comment, CommentNamingScheme.CommentName> {

	/**
	 * {@link ModelName} for a {@link Comment}.
	 * 
	 * <p>
	 * A {@link Comment} is identified by its create revision. That is currently (for example in the
	 * comments table) the only unique identifier.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface CommentName extends ModelName {

		/**
		 * The {@link ModelName} of the revision in witch the comment was created.
		 */
		ModelName getCreateRevision();

		/**
		 * Setter for {@link #getCreateRevision()}.
		 */
		void setCreateRevision(ModelName name);

	}

	@Override
	protected void initName(CommentName name, Comment model) {
		Date created = model.getCreated();
		Revision createRev = model.tHandle().getKnowledgeBase().getHistoryManager().getRevisionAt(created.getTime());
		name.setCreateRevision(ModelResolver.buildModelName(createRev));
	}

	@Override
	public Comment locateModel(ActionContext context, CommentName name) {
		Revision storedRevision = (Revision) context.resolve(name.getCreateRevision());
		long storedRev = storedRevision.getCommitNumber();
		Collection<KnowledgeObject> allComments =
			PersistencyLayer.getKnowledgeBase().getAllKnowledgeObjects(Comment.KO_TYPE);
		for (KnowledgeObject candidate : allComments) {
			long createRev = candidate.getCreateCommitNumber();
			if (storedRev == createRev) {
				return (Comment) candidate.getWrapper();
			}
		}
		throw ApplicationAssertions.fail(name, "No comment created at revision: " + storedRevision);
	}

	@Override
	public Class<? extends CommentName> getNameClass() {
		return CommentName.class;
	}

	@Override
	public Class<Comment> getModelClass() {
		return Comment.class;
	}

}

