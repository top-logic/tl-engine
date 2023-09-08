/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.element.model.PersistentModule;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.util.model.ModelService;

/**
 * Rewriter that installs creates event to create {@link TLModule}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetTLModule extends Rewriter {

	/**
	 * Configuration for the {@link SetTLModule} rewriter.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Rewriter.Config {

		@Override
		@FormattedDefault(KBBasedMetaElement.META_ELEMENT_KO)
		Set<String> getTypeNames();

		/**
		 * The classes for which no module assignment must be occurred.
		 * 
		 * <p>
		 * This allows to configure type names which exists in more than one module. For such types
		 * the migration would fail.
		 * </p>
		 */
		@Format(CommaSeparatedStringSet.class)
		Set<String> getNoModuleAssignment();
	}

	static int INITIAL_REVISION = 2;

	private Map<String, ObjectKey> _moduleByTypeName = new HashMap<>();

	private Set<ObjectCreation> _moduleCreations = new HashSet<>();

	private Set<String> _noModuleAssignment;

	/**
	 * Creates a new {@link SetTLModule}.
	 */
	public SetTLModule(InstantiationContext context, Config config) {
		super(context, config);
		_noModuleAssignment = config.getNoModuleAssignment();
	}

	/**
	 * Sets the {@link ModelService} for this {@link SetTLModule}.
	 */
	@Inject
	public void initModelService(ModelService modelService) {
		TLModel applicationModel = modelService.getModel();
		Collection<TLModule> modules = applicationModel.getModules();
		for (TLModule module : modules) {
			for (TLClass type : module.getClasses()) {
				if (type.getScope() != module) {
					// local type
					continue;
				}
				if (!type.isAbstract()) {
					continue;
				}
				if (_noModuleAssignment.contains(type.getName())) {
					continue;
				}
				ObjectKey moduleKey = module.tId();
				String typeName = type.getName();
				ObjectKey clash = _moduleByTypeName.put(typeName, moduleKey);
				if (clash != null) {
					throw handleMultipleGlobalTypes(typeName, module, clash);
				}
				ObjectCreation createModule =
					new ObjectCreation(INITIAL_REVISION, ObjectBranchId.toObjectBranchId(moduleKey));
				createModule.setValue(PersistentModule.NAME_ATTR, null, module.getName());
				_moduleCreations.add(createModule);

			}
		}
	}

	private static RuntimeException handleMultipleGlobalTypes(String typeName, TLModule module1, ObjectKey module2) {
		TLModule clashModule = null;
		for (TLModule anyModules : module1.getModel().getModules()) {
			if (anyModules.tId().equals(module2)) {
				clashModule = anyModules;
				break;
			}
		}
		StringBuilder error = new StringBuilder();
		error.append("Multiple abstract global types with name '");
		error.append(typeName);
		error.append("': One in module ");
		error.append(module1.getName());
		error.append(", one in module ");
		error.append(clashModule == null ? "[module not found?]" : clashModule.getName());
		return new KnowledgeBaseRuntimeException(error.toString());
	}

	@Override
	protected void processCreations(ChangeSet cs) {
		super.processCreations(cs);
		if (cs.getRevision() == INITIAL_REVISION) {
			for (ObjectCreation moduleCreation : _moduleCreations) {
				cs.addCreation(moduleCreation);
			}
			_moduleCreations = null;
		}
	}

	@Override
	protected Object processCreateObject(ObjectCreation event) {
		Map<String, Object> values = event.getValues();
		Object name = values.get(KBBasedMetaElement.NAME_ATTR);
		ObjectKey module = _moduleByTypeName.get(name);
		if (module != null && values.get(KBBasedMetaElement.SCOPE_REF) == null) {
			event.setValue(KBBasedMetaElement.SCOPE_REF, null, module);
		}
		return super.processCreateObject(event);
	}
}

