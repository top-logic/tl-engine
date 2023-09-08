/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.DestinationReference;
import com.top_logic.knowledge.service.db2.SourceReference;

/**
 * Rewriter that deletes association events and inlines the corrsponding (singleton) references into
 * the source or destination object.
 * 
 * <p>
 * Often a {@link KnowledgeAssociation} is actually a "to one" reference and can be stored in the
 * source (or destination) object of the {@link KnowledgeAssociation}. This {@link Rewriter} removes
 * such {@link KnowledgeAssociation} links and connects the ends using a reference attribute.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AssociationInline extends Rewriter {

	/**
	 * Configuration of an {@link AssociationInline}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("association-inline")
	public interface Config extends Rewriter.Config {

		/** Name of {@link #getReferenceValue()}. */
		String REFERENCE_VALUE = "reference-value";

		/** Name of {@link #getReferenceName()}. */
		String REFERENCE_NAME = "reference-name";

		/** Name of {@link #getTargetObject()}. */
		String TARGET_OBJECT = "target-object";

		/**
		 * Name of the reference column in the association table which refers to the object that
		 * actually has the "to one" reference column in its storage table.
		 * 
		 * <p>
		 * Mostly either {@value SourceReference#REFERENCE_SOURCE_NAME} or
		 * {@value DestinationReference#REFERENCE_DEST_NAME} in case of generic associations.
		 * </p>
		 * 
		 * @see #getReferenceValue()
		 */
		@Name(TARGET_OBJECT)
		@Mandatory
		String getTargetObject();

		/**
		 * @see #getTargetObject()
		 */
		void setTargetObject(String value);

		/**
		 * Name of the reference column in {@link #getTargetObject() the target object's table}
		 * which should store the referenced value.
		 */
		@Name(REFERENCE_NAME)
		@Mandatory
		String getReferenceName();

		/**
		 * @see #getReferenceName()
		 */
		void setReferenceName(String value);

		/**
		 * Name of the attribute in the association table which holds the referenced object.
		 * 
		 * <p>
		 * Mostly either {@value SourceReference#REFERENCE_SOURCE_NAME} or
		 * {@value DestinationReference#REFERENCE_DEST_NAME} in case of generic associations.
		 * </p>
		 * 
		 * @see #getTargetObject()
		 */
		@Name(REFERENCE_VALUE)
		@Mandatory
		String getReferenceValue();

		/**
		 * @see #getReferenceValue()
		 */
		void setReferenceValue(String value);

		/**
		 * Name of the association table types which actually store the "to one" reference.
		 * 
		 * @see com.top_logic.knowledge.event.convert.TypesConfig#getTypeNames()
		 */
		@Override
		Set<String> getTypeNames();

		@Override
		@ClassDefault(AssociationInline.class)
		Class<? extends Rewriter> getImplementationClass();

	}

	private String _targetObject;

	private String _referenceName;

	private String _referenceValue;

	private List<ItemEvent> _additionalEvents = new ArrayList<>();

	/**
	 * Creates a new {@link AssociationInline}.
	 */
	public AssociationInline(InstantiationContext context, Config config) {
		super(context, config);

		_targetObject = config.getTargetObject();
		_referenceName = config.getReferenceName();
		_referenceValue = config.getReferenceValue();
	}


	@Override
	protected Object processCreateObject(ObjectCreation event) {
		Map<String, Object> values = event.getValues();
		ObjectKey targetKey = (ObjectKey) values.get(_targetObject);
		ObjectKey refKey = (ObjectKey) values.get(_referenceValue);
		
		ItemUpdate setRef = new ItemUpdate(event.getRevision(), ObjectBranchId.toObjectBranchId(targetKey), true);
		setRef.setValue(_referenceName, null, refKey);
		_additionalEvents.add(setRef);
		
		return SKIP_EVENT;
	}

	@Override
	protected void processCommit(ChangeSet cs) {
		cs.mergeAll(_additionalEvents);
		_additionalEvents.clear();
		super.processCommit(cs);
	}

	@Override
	protected Object processUpdate(ItemUpdate event) {
		// Note: In an update event, only the changed values are available. Therefore, a incremental
		// change in an inlined association cannot be processed.
		migration().error("Inlined association changes internally: " + event);
		return SKIP_EVENT;
	}

	@Override
	protected Object processDelete(ItemDeletion event) {
		Map<String, Object> values = event.getValues();
		ObjectKey targetKey = (ObjectKey) values.get(_targetObject);
		ObjectKey refKey = (ObjectKey) values.get(_referenceValue);

		ItemUpdate setRef = new ItemUpdate(event.getRevision(), ObjectBranchId.toObjectBranchId(targetKey), true);
		setRef.setValue(_referenceName, refKey, null);
		_additionalEvents.add(setRef);

		return SKIP_EVENT;
	}

}
