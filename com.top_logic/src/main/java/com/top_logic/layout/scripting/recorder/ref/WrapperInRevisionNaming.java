/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.util.Utils;

/**
 * {@link ModelNamingScheme} for a {@link Wrapper} in a given {@link Revision}.
 * <p>
 * Does not supports {@link Wrapper}s that are deleted in the {@link Revision#CURRENT current
 * revision}, as the {@link Wrapper} itself is resolved in the current revision.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class WrapperInRevisionNaming extends AbstractModelNamingScheme<Wrapper, WrapperInRevisionNaming.WrapperInRevisionName> {

	/**
	 * The {@link ModelName} for the {@link WrapperInRevisionNaming}.
	 */
	public interface WrapperInRevisionName extends ModelName {

		/**
		 * The {@link ValueRef} for the {@link Revision}.
		 */
		ModelName getRevision();

		/**
		 * @see #getRevision()
		 */
		void setRevision(ModelName revision);

		/**
		 * The {@link ValueRef} for the {@link Wrapper}.
		 */
		ModelName getWrapper();

		/**
		 * @see #getWrapper()
		 */
		void setWrapper(ModelName wrapper);
	}

	@Override
	public Class<WrapperInRevisionName> getNameClass() {
		return WrapperInRevisionName.class;
	}

	@Override
	public Class<Wrapper> getModelClass() {
		return Wrapper.class;
	}

	@Override
	public Wrapper locateModel(ActionContext actionContext, WrapperInRevisionName name) {
		Revision revision = (Revision) ModelResolver.locateModel(actionContext, name.getRevision());
		Wrapper wrapper = (Wrapper) ModelResolver.locateModel(actionContext, name.getWrapper());
		KnowledgeItem knowledgeItem = getInRevision(revision, wrapper);
		return WrapperFactory.getWrapper((KnowledgeObject) knowledgeItem);
	}

	@Override
	protected void initName(WrapperInRevisionName name, Wrapper model) {
		name.setRevision(ModelResolver.buildModelName(getRevision(model)));
		KnowledgeItem knowledgeItem = getInRevision(Revision.CURRENT, model);
		Wrapper wrapperOnCurrent = WrapperFactory.getWrapper((KnowledgeObject) knowledgeItem);
		name.setWrapper(ModelResolver.buildModelName(wrapperOnCurrent));
	}

	@Override
	protected boolean isCompatibleModel(Wrapper model) {
		if (getWrappedObject(model) == null) {
			// Fake wrapper
			return false;
		}
		if (Utils.equals(getRevision(model), Revision.CURRENT)) {
			return false;
		}
		// The wrapper will be resolved in the current revision. That is only possible, if it is
		// alive.
		KnowledgeItem knowledgeItem = getInRevision(Revision.CURRENT, model);
		if (knowledgeItem == null || !knowledgeItem.isAlive()) {
			return false;
		}
		return true;
	}

	private KnowledgeItem getInRevision(Revision revision, Wrapper wrapper) {
		return HistoryUtils.getKnowledgeItem(revision, getWrappedObject(wrapper));
	}

	private Revision getRevision(Wrapper model) {
		return HistoryUtils.getRevision(getWrappedObject(model));
	}

	private KnowledgeObject getWrappedObject(Wrapper model) {
		return model.tHandle();
	}

}
