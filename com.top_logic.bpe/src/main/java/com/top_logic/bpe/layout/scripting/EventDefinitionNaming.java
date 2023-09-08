/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.scripting;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.bpe.bpml.model.Event;
import com.top_logic.bpe.bpml.model.Named;
import com.top_logic.bpe.bpml.model.impl.TimerEventDefinitionImpl;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class EventDefinitionNaming
		extends AbstractModelNamingScheme<TimerEventDefinitionImpl, EventDefinitionNaming.EventDefinitionName> {

	/**
	 * {@link ModelName} of the {@link EventDefinitionNaming}.
	 */
	public interface EventDefinitionName extends ModelName {

		/**
		 * The path to a tab as "Breadcrumb String".
		 * 
		 * @see BreadcrumbStrings
		 */
		@Format(BreadcrumbStrings.class)
		List<String> getNodePath();

		/** @see #getNodePath() */
		void setNodePath(List<String> value);

	}

	@Override
	protected void initName(EventDefinitionName name, TimerEventDefinitionImpl model) {
		List<String> path = new ArrayList<>();
		Event event = model.getEvent();
		BPMLObjectNaming.buildPath(path, event);
		name.setNodePath(path);
	}

	@Override
	public TimerEventDefinitionImpl locateModel(ActionContext context, EventDefinitionName name) {
		List<String> names = name.getNodePath();
		Named resolve = BPMLObjectNaming.resolvePath(name, names, null);
		if (resolve instanceof Event) {
			return (TimerEventDefinitionImpl) ((Event) resolve).getDefinition();
		}
		return null;
	}

	@Override
	public Class<? extends EventDefinitionName> getNameClass() {
		return EventDefinitionName.class;
	}

	@Override
	public Class<TimerEventDefinitionImpl> getModelClass() {
		return TimerEventDefinitionImpl.class;
	}
}
