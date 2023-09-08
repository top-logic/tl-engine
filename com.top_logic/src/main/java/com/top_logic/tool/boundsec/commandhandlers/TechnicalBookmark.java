/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.tool.boundsec.ObjectNotFound;

/**
 * {@link BookmarkHandler} identifying an object by its {@link KnowledgeBase} internal values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TechnicalBookmark implements BookmarkHandler {

	@Override
	public boolean appendIdentificationArguments(StringBuilder url, boolean first, Object targetObject) {
		if (targetObject instanceof Wrapper) {
			Wrapper targetWrapper = (Wrapper) targetObject;
			String typeArg = targetWrapper.tTable().getName();
			String objectName = IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(targetWrapper));
			String branchArg = Long.toString(WrapperHistoryUtils.getBranch(targetWrapper).getBranchId());
			Revision revision = WrapperHistoryUtils.getRevision(targetWrapper);
			String revArg = (revision.isCurrent()) ? null : String.valueOf(revision.getCommitNumber());

			boolean stillFirst = first;
			stillFirst = URLUtilities.appendUrlArg(url, stillFirst, GotoHandler.COMMAND_PARAM_ID, objectName);
			stillFirst = URLUtilities.appendUrlArg(url, stillFirst, GotoHandler.COMMAND_PARAM_BRANCH, branchArg);
			stillFirst = URLUtilities.appendUrlArg(url, stillFirst, GotoHandler.COMMAND_PARAM_REVISION, revArg);
			stillFirst = URLUtilities.appendUrlArg(url, stillFirst, GotoHandler.COMMAND_PARAM_TYPE, typeArg);
			return stillFirst;
		} else {
			return first;
		}
	}

	/**
	 * Resolves a target object assuming that the following arguments are in the given argument map.
	 * 
	 * <dl>
	 * <dt>{@link GotoHandler#COMMAND_PARAM_TYPE}</dt>
	 * <dd>The plain {@link KnowledgeObject#tTable()} name</dd>
	 * <dt>{@link GotoHandler#COMMAND_PARAM_ID}</dt>
	 * <dd>The plain {@link KnowledgeObject#getObjectName()}</dd>
	 * <dt>{@link GotoHandler#COMMAND_PARAM_BRANCH}</dt>
	 * <dd>The plain {@link KnowledgeObject#getBranchContext()}</dd>
	 * <dt>{@link GotoHandler#COMMAND_PARAM_REVISION}</dt>
	 * <dd>The plain {@link KnowledgeObject#getHistoryContext()}</dd>
	 * </dl>
	 * 
	 * @param arguments
	 *        The arguments described above.
	 * @return The identified object, or <code>null</code>, if the object could not be found.
	 */
	@Override
	public Object getBookmarkObject(Map<String, Object> arguments) {
		String objectType = (String) arguments.get(DefaultBookmarkHandler.COMMAND_PARAM_TYPE);
		TLID objectName =
			IdentifierUtil.fromExternalForm((String) arguments.get(DefaultBookmarkHandler.COMMAND_PARAM_ID));
		String branchId = (String) arguments.get(DefaultBookmarkHandler.COMMAND_PARAM_BRANCH);
		String revisionId = (String) arguments.get(DefaultBookmarkHandler.COMMAND_PARAM_REVISION);

		try {
			return GotoHandler.findObjectByInternalIdentity(objectType, objectName, branchId, revisionId);
		} catch (ObjectNotFound ex) {
			throw new ObjectNotFound(I18NConstants.BOOKMARK_OBJECT_NOT_FOUND__ID__TYPE.fill(objectName, objectType));
		}
	}

}

