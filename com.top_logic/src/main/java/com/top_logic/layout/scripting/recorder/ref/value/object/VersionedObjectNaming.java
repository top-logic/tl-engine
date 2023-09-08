/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * Base class for {@link ModelNamingScheme}s for persistent objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class VersionedObjectNaming<M extends TLObject, N extends VersionedObjectNaming.Name> extends
		GlobalModelNamingScheme<M, N> {

	/**
	 * Configuration options for {@link VersionedObjectNaming}.
	 */
	@Abstract
	public interface Name extends ModelName {

		/**
		 * Description of the branch this object resides in.
		 * 
		 * <p>
		 * <code>null</code> means trunk.
		 * </p>
		 */
		ModelName getBranch();

		/** @see #getBranch() */
		void setBranch(ModelName value);

		/**
		 * Description of the revision this object resides in.
		 * 
		 * <p>
		 * <code>null</code> means current.
		 * </p>
		 */
		ModelName getRevision();

		/** @see #getRevision() */
		void setRevision(ModelName value);

	}

	/**
	 * Sets the {@link Branch} and {@link Revision} from the given object to the given reference.
	 * 
	 * @param name
	 *        The reference to modify.
	 * @param model
	 *        The context to take the version information from.
	 * @return The given reference for call chaining.
	 */
	protected N setVersionContext(N name, TLObject model) {
		KnowledgeItem handle = model.tHandle();
		return setVersionContext(name, HistoryUtils.getBranch(handle), HistoryUtils.getRevision(handle));
	}

	private N setVersionContext(N name, Branch branch, Revision revision) {
		if (branch.getBranchId() != TLContext.TRUNK_ID) {
			name.setBranch(ModelResolver.buildModelName(branch));
		}

		if (!revision.equals(Revision.CURRENT)) {
			name.setRevision(ModelResolver.buildModelName(revision));
		}

		return name;
	}

	/**
	 * Resolves the {@link Branch} encoded into the given {@link Name}.
	 */
	protected final Branch getBranch(ActionContext arg, Name name) {
		ModelName branchName = name.getBranch();
		if (branchName == null) {
			// Compatibility and shortness.
			return HistoryUtils.getTrunk();
		} else {
			return (Branch) ModelResolver.locateModel(arg, branchName);
		}
	}

	/**
	 * Resolves the {@link Revision} encoded into the given {@link Name}.
	 */
	protected final Revision getRevision(ActionContext arg, Name name) {
		ModelName revisionName = name.getRevision();
		if (revisionName == null) {
			// Compatibility and shortness.
			return Revision.CURRENT;
		} else {
			return (Revision) ModelResolver.locateModel(arg, revisionName);
		}
	}

}
