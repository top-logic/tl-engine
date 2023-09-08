/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;


import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link KnowledgeEventConverter} rewrites {@link KnowledgeEvent} such that the events have types
 * which are known by a certain {@link MORepository}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KnowledgeEventConverter extends AbstractObjectConverter {

	/**
	 * @see #getTargetRepository()
	 */
	protected final MORepository _targetRepository;

	private final Mapping<String, String> _nameConversion;

	/**
	 * Creates a {@link KnowledgeEventConverter}.
	 * 
	 * @param targetRepository
	 *        the {@link MORepository} used to resolve types.
	 */
	public KnowledgeEventConverter(MORepository targetRepository) {
		this(targetRepository, Mappings.<String> identity());
	}

	/**
	 * Creates a {@link KnowledgeEventConverter}.
	 * 
	 * @param targetRepository
	 *        the {@link MORepository} used to resolve types.
	 * @param nameConversion
	 *        Mapping of the name in the
	 */
	public KnowledgeEventConverter(MORepository targetRepository, Mapping<String, String> nameConversion) {
		_targetRepository = targetRepository;
		_nameConversion = nameConversion;
	}

	/**
	 * Returns the {@link MORepository} the types are adopted to.
	 */
	public final MORepository getTargetRepository() {
		return _targetRepository;
	}

	/**
	 * Translates the given {@link ObjectBranchId} to some which is known by the target
	 * {@link MORepository}
	 * 
	 * @param id
	 *        the id to translate
	 * 
	 * @return the translated {@link ObjectBranchId}.
	 */
	@Override
	protected ObjectBranchId translate(ItemEvent event, ObjectBranchId id) {
		try {
			return KBUtils.transformId(_targetRepository, id, _nameConversion);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Unable to process " + event, ex);
		}
	}

	/**
	 * Translates the given {@link ObjectKey} to some which is known by the target
	 * {@link MORepository}
	 * @param value
	 *        the key to translate
	 * 
	 * @return the translated {@link ObjectKey}.
	 */
	@Override
	protected ObjectKey translate(ItemEvent event, String attribute, ObjectKey value) {
		try {
			return KBUtils.transformKey(_targetRepository, value, _nameConversion);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Unable to process " + event, ex);
		}
	}

	/**
	 * Creates an {@link EventWriter} which transforms the events such that the types come from the
	 * given {@link MORepository} and writes the transformed events to the given {@link EventWriter}
	 * .
	 */
	public static EventWriter createEventConverter(MORepository targetRepository, EventWriter out) {
		Mapping<String, String> nameConversion = Mappings.identity();
		return createEventWriter(targetRepository, out, nameConversion);
	}

	/**
	 * Creates an {@link EventWriter} which transforms the events such that the types come from the
	 * given {@link MORepository} and writes the transformed events to the given {@link EventWriter}
	 * .
	 * 
	 * @param nameConversion
	 *        Mapping of the name of type of the rewritten event to the name of the corresponding
	 *        type in the target {@link MORepository}.
	 */
	public static EventWriter createEventWriter(MORepository targetRepository, EventWriter out,
			Mapping<String, String> nameConversion) {
		return StackedEventWriter.createWriter(new KnowledgeEventConverter(targetRepository, nameConversion), out);
	}

}

