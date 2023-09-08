/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import static com.top_logic.basic.util.ResKey.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.TypesConfig;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.util.model.ModelService;

/**
 * Rewriter that sets the {@link KBBasedMetaElement#SCOPE_REF} of global {@link TLClass}s to the
 * module. This is needed to change {@link TLClass}s which are defined in the root element in
 * 5.7.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaElementGlobalizer extends Rewriter {

	/**
	 * Configuration of a {@link MetaElementGlobalizer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Rewriter.Config {

		/**
		 * The name of the meta element which must be global.
		 */
		@Format(CommaSeparatedStringSet.class)
		Set<String> getMetaElements();
		
		@Override
		@FormattedDefault(KBBasedMetaElement.META_ELEMENT_KO)
		Set<String> getTypeNames();
	}

	private Set<String> _metaElements;

	private Map<String, ObjectKey> _moduleByTypeName = new HashMap<>();

	/**
	 * Creates a new {@link MetaElementGlobalizer}.
	 */
	public MetaElementGlobalizer(InstantiationContext context, Config config) {
		super(context, config);
		_metaElements = config.getMetaElements();
	}

	/**
	 * Initialises the {@link ModelService} for this {@link MetaElementGlobalizer}.
	 */
	@Inject
	public void initModelService(ModelService modelService) throws ConfigurationException {
		TLModel applicationModel = modelService.getModel();
		Collection<TLModule> modules = applicationModel.getModules();
		for (TLModule module : modules) {
			for (TLClass type : module.getClasses()) {
				if (!_metaElements.contains(type.getName())) {
					continue;
				}
				if (type.getScope() != module) {
					ResKey notGlobal = text(type.getName() + " is not a global type.");
					throw new ConfigurationException(notGlobal, TypesConfig.TYPES, _metaElements.toString());
				}
				ObjectKey moduleKey = module.tId();
				ObjectKey clash = _moduleByTypeName.put(type.getName(), moduleKey);
				if (clash != null && clash != moduleKey) {
					ResKey clashMsg = text("Multiple global types with name '" + type.getName()
						+ "' and different modules: " + moduleKey + ", " + clash);
					throw new ConfigurationException(clashMsg, TypesConfig.TYPES, _metaElements.toString());
				}
			}
		}
	}
	
	@Override
	protected Object processCreateObject(ObjectCreation event) {
		Map<String, Object> values = event.getValues();
		Object name = values.get(KBBasedMetaElement.NAME_ATTR);
		if (_metaElements.contains(name)) {
			ItemUpdate update = new ItemUpdate(event.getRevision(), event.getObjectId(), true);
			update.setValue(KBBasedMetaElement.SCOPE_REF, null, _moduleByTypeName.get(name));
			event.visitItemEvent(ChangeSet.MERGE_UPDATE, update);
		}
		return super.processCreateObject(event);
	}


}

