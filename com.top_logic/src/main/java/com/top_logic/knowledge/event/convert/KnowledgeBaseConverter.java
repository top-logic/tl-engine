/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.List;

import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link KnowledgeBaseCopy} that applies local transformations to all
 * {@link KnowledgeEvent}s during copy.
 * 
 * @see KnowledgeBaseCopy Copying {@link KnowledgeBase} data without any
 *      transformations.
 *      
 * @see #convert() 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeBaseConverter extends KnowledgeBaseCopy {

	private List<? extends EventRewriter> _conversions;

	/**
	 * Creates a {@link KnowledgeBaseConverter} for the given {@link EventRewriter}s.
	 * 
	 * @param sourceKb
	 *        The {@link KnowledgeBase} from which data is read.
	 * @param conversions
	 *        The conversions of the event.
	 */
	public KnowledgeBaseConverter(KnowledgeBase sourceKb, KnowledgeBase destKb,
			List<? extends EventRewriter> conversions) {
		super(sourceKb, destKb);
		
		this._conversions = conversions;
	}

	@Override
	protected EventWriter adoptWriter(EventWriter eventWriter) {
		EventWriter postProcessingWriter;
		if (this._conversions == null || this._conversions.size() == 0) {
			postProcessingWriter = eventWriter;
		} else {
			postProcessingWriter = StackedEventWriter.createWriter(0, eventWriter, this._conversions);
		}
		return super.adoptWriter(postProcessingWriter);
	}
	
}