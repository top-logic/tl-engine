/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service.exporter;

import java.util.Map;
import java.util.Set;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.kafka.sync.knowledge.service.KafkaSyncConfig;
import com.top_logic.kafka.sync.knowledge.service.importer.KafkaImportConfiguration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * Configuration for the types and attributes which are exported by kafka.
 * 
 * @see DefaultKafkaKBExportRewriter
 * @see KafkaImportConfiguration
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KafkaExportConfiguration extends KafkaSyncConfig {

	/**
	 * The association types which holds may hold references with exported objects as source or
	 * destination end.
	 * 
	 * <p>
	 * This set typically holds sub types of
	 * {@link ApplicationObjectUtil#WRAPPER_ATTRIBUTE_ASSOCIATION_BASE}, and types which are used by
	 * reference attributes to store reference.
	 * </p>
	 */
	Set<MetaObject> getAssociationTypes();

	/**
	 * All {@link ObjectKey}s of all exported {@link TLStructuredTypePart parts}.
	 * 
	 * <p>
	 * Note that for all exported attributes not the {@link TLStructuredTypePart} itself but
	 * {@link TLStructuredTypePart#getDefinition()} is contained.
	 * </p>
	 */
	Set<ObjectKey> getExportAttributeIds();

	/**
	 * A cache for getting the name of an exported attribute.
	 * <p>
	 * This prevents that the attribute-id has to be resolved.
	 * </p>
	 * 
	 * @param attributeId
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	String getAttributeName(ObjectKey attributeId);

	/**
	 * A cache for getting the owner-id of an exported attribute.
	 * <p>
	 * This prevents that the attribute-id has to be resolved.
	 * </p>
	 * 
	 * @param attributeId
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	ObjectKey getAttributeOwnerId(ObjectKey attributeId);

	/**
	 * Mapping of the {@link TLType} to the exported attributes.
	 * 
	 * <p>
	 * More specific: The key in the map is the {@link TLObject#tId() id} of some
	 * {@link TLType} to export. The value of the key is a mapping defining the exported attribute
	 * names. The key in such mapping is the name of an exported {@link MOAttribute} and the value
	 * is the name of an exported {@link TLStructuredTypePart part}.
	 * </p>
	 * 
	 * <p>
	 * The keys are {@link Revision#CURRENT_REV current} object keys.
	 * </p>
	 */
	Map<ObjectKey, Map<String, String>> getExportAttributeNames();

	/**
	 * Exporting {@link KnowledgeBase} .
	 */
	KnowledgeBase getKnowledgeBase();

}

