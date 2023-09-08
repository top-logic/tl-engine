/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ListModelBuilder} for existing {@link ProcessExecution}s for a given
 * {@link Collaboration}.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ProcessExecutionListModelBuilder<C extends ProcessExecutionListModelBuilder.Config>
		extends AbstractConfiguredInstance<C>
		implements ListModelBuilder {

	public interface Config extends PolymorphicConfiguration<ProcessExecutionListModelBuilder<?>> {

	}

	public ProcessExecutionListModelBuilder(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return getAllProcessExecutionsForCurrentUser((Collaboration) businessModel);
	}

	/**
	 * All processes started by the current user in the given {@link Collaboration}.
	 */
	public static List<ProcessExecution> getAllProcessExecutionsForCurrentUser(Collaboration collaboration) {
		Set<ProcessExecution> allProcessExecutions = getAllProcessExecutions(collaboration);

		PersonContact myself = PersonContact.getCurrentPersonContact();
		List<ProcessExecution> hlp = new ArrayList<>();

		for (ProcessExecution pe : allProcessExecutions) {
			PersonContact createdByContact = pe.getCreatedByContact();
			if (WrapperHistoryUtils.equalsUnversioned(myself, createdByContact)) {
				hlp.add(pe);
			}
		}

		return hlp;
	}

	/**
	 * all {@link ProcessExecution} which are linked to the given collaboration
	 */
	protected static Set<ProcessExecution> getAllProcessExecutions(Collaboration collaboration) {
		TLStructuredTypePart ma = TlBpeExecutionFactory.getCollaborationProcessExecutionAttr();
		if (collaboration == null) {
			return Collections.emptySet();
		}
		return CollectionUtil.dynamicCastView(ProcessExecution.class,
			AttributeOperations.getReferers(collaboration, ma));
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof Collaboration;
//		if (aModel instanceof Collaboration) {
//			return getAllProcessExecutionsForCurrentUser((Collaboration) aModel).size() > 0;
//		}
//		return false;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof ProcessExecution;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anElement) {
		/* The list does not depend on the model. So any model is correct. For stability return
		 * current model. */
		return aComponent.getModel();
	}

}
