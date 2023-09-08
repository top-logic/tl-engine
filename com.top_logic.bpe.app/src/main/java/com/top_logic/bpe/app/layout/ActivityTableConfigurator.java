/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.ColumnInliner;
import com.top_logic.layout.table.provider.DefaultTableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;

/**
 * {@link TableConfigurationProvider} building a {@link TableConfig} for the "active tokens table"
 * (with columns from the {@link Token} and its {@link Token#getProcessExecution() process
 * execution}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ActivityTableConfigurator extends DefaultTableConfigurationProvider {

	private LayoutComponent _component;

	/**
	 * Creates a {@link ActivityTableConfigurator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ActivityTableConfigurator(InstantiationContext context, PolymorphicConfiguration<?> config) {
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Collaboration collaboration = (Collaboration) _component.getModel();
		Set<TLClass> modelTypes = new HashSet<>();
		for (Participant participant : collaboration.getParticipants()) {
			TLClass modelType = (TLClass) participant.getModelType();
			if (modelType != null) {
				modelTypes.add(modelType);
			}
		}
		if (modelTypes.size() > 0) {
			new ColumnInliner(Token.PROCESS_EXECUTION_ATTR, modelTypes).adaptConfigurationTo(table);
		}
	}

}
