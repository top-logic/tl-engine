/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapper;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapperImpl;

/**
 * {@link AbstractRowTransformer} that skips entries from the DO_STORAGE table for objects that are
 * skipped by the {@link IdMapper}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DOStorageRemovedValuesSkipper extends AbstractRowTransformer<DOStorageRemovedValuesSkipper.Config> {

	/**
	 * COnfiguration for the {@link DOStorageRemovedValuesSkipper}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractRowTransformer.Config<DOStorageRemovedValuesSkipper> {

		/**
		 * Name of attribute containing the {@link TLID} of the object.
		 */
		@Mandatory
		String getIDAttribute();

	}

	private IdMapper _mapper;

	/**
	 * Constructor creates a new {@link DOStorageRemovedValuesSkipper}.
	 */
	public DOStorageRemovedValuesSkipper(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Sets the {@link IdMapper}.
	 */
	@Inject
	public void setIdMapper(IdMapper mapper) {
		_mapper = mapper;
	}

	boolean isObjectSkipped(TLID id) {
		Set<ObjectBranchId> skippedCreations = ((IdMapperImpl) _mapper).getSkippedCreatedObjects();
		return skippedCreations.stream()
			.map(ObjectBranchId::getObjectName)
			.filter(skippedID -> id.equals(skippedID))
			.findAny()
			.isPresent();
	}

	@Override
	protected void internalTransform(RowValue row, RowWriter out) {
		Object id = row.getValues().get(getConfig().getIDAttribute());
		if (id instanceof TLID && isObjectSkipped((TLID) id)) {
			return;
		}
		out.write(row);
	}

}

