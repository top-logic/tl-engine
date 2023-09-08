/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MappedIterable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.dob.DataObjectException;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.PersistentAssociation;
import com.top_logic.element.model.PersistentTLModel;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.label.ObjectLabel;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.Messages;
import com.top_logic.util.model.ModelService;

/**
 * {@link ModelService} for {@link TLModel} based on {@link TLClass} and {@link TLStructuredTypePart}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	MetaElementFactory.Module.class
})
public class ElementModelService extends ModelService {

	private static final String APPLICATION_MODEL_NAME = "applicationModel";

	/**
	 * Creates a {@link ElementModelService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ElementModelService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Finds or creates the {@link TLModel} for the model service.
	 */
	@Override
	protected TLModel fetchModel(KnowledgeBase kb) {
		return toTLModel(mkModel(kb));
	}

	private TLModel toTLModel(KnowledgeObject tlModelKI) {
		return (TLModel) tlModelKI.getWrapper();
	}

	private final KnowledgeObject mkModel(KnowledgeBase kb) {
		KnowledgeObject applicationModel = ObjectLabel.getLabeledObject(APPLICATION_MODEL_NAME);
		if (applicationModel != null) {
			try {
				loadModelElements(kb, toTLModel(applicationModel));
			} catch (InvalidLinkException ex) {
				String message = "Unable to preload model parts.";
				throw new KnowledgeBaseRuntimeException(message, ex);
			}
			return applicationModel;
		}
		Transaction tx = kb.beginTransaction(Messages.CREATING_TL_MODEL);
		try {
			KnowledgeObject newModel = kb.createKnowledgeItem(PersistentTLModel.OBJECT_TYPE, KnowledgeObject.class);
			ObjectLabel.createLabel(APPLICATION_MODEL_NAME, newModel);
			tx.commit();
			return newModel;
		} catch (DataObjectException ex) {
			String message = "Unable to create new model of type " + PersistentTLModel.OBJECT_TYPE;
			throw new KnowledgeBaseRuntimeException(message, ex);
		} finally {
			tx.rollback();
		}
	}

	private void loadModelElements(KnowledgeBase kb, TLModel tlModel) throws InvalidLinkException {
		Mapping<TLObject, KnowledgeObject> koMapping = new Mapping<>() {

			@Override
			public KnowledgeObject map(TLObject input) {
				return (KnowledgeObject) input.tHandle();
			}
		};
		Collection<TLModule> modules = tlModel.getModules();
		/* It is currently not necessary to preload caches, because this is done implicit when
		 * loading the PersistentModule */
//		fillCaches(kb, koMapping, modules, PersistentModule.getAssociationQueries());
		
		ArrayList<TLClass> classes = new ArrayList<>();
		ArrayList<TLAssociation> associations = new ArrayList<>();
		ArrayList<TLEnumeration> enumerations = new ArrayList<>();
		for (TLModule module : modules) {
			classes.addAll(module.getClasses());
			associations.addAll(module.getAssociations());
			enumerations.addAll(module.getEnumerations());
		}
		fillCaches(kb, koMapping, classes, PersistentClass.getAssociationQueries());
		fillCaches(kb, koMapping, associations, PersistentAssociation.getAssociationQueries());
		fillCaches(kb, koMapping, enumerations, FastList.getAssociationQueries());

		ArrayList<TLClassPart> classParts = new ArrayList<>();
		for (TLClass tlClass : classes) {
			classParts.addAll(tlClass.getLocalClassParts());
		}
		fillCaches(kb, koMapping, classParts, KBBasedMetaAttribute.getAssociationQueries());
	}

	private void fillCaches(KnowledgeBase kb, Mapping<TLObject, KnowledgeObject> koMapping,
			Collection<? extends TLObject> parts, List<? extends AbstractAssociationQuery<? extends TLObject, ?>> queries)
			throws InvalidLinkException {
		for (AbstractAssociationQuery<? extends TLObject, ?> query : queries) {
			kb.fillCaches(new MappedIterable<>(koMapping, parts), query);
		}
	}

}
