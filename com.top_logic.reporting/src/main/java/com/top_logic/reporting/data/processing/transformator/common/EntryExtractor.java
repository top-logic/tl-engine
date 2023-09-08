/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.transformator.common;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.reporting.data.base.table.ReportTable;
import com.top_logic.reporting.data.base.table.TableDescriptor;
import com.top_logic.reporting.data.base.table.common.DefaultTableDescriptor;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NullValue;
import com.top_logic.reporting.data.processing.transformator.TableTransformator;
import com.top_logic.reporting.data.processing.transformator.TransformatorFactory;

/**
 * What is this class used for ?
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class EntryExtractor extends AbstractConfiguredInstance<TransformatorFactory.Config.Transformator>
		implements TableTransformator {

	private static final String TRANSFORMATOR_ID       = "Entry_Extractor";
	private static final String INDEX_DELIMITER        = ",";


	private String displayNameKey;
	private int[] idxs;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param transformatorConfig
	 *        Configuration for a transformator.
	 */
	public EntryExtractor(InstantiationContext context, TransformatorFactory.Config.Transformator transformatorConfig) {
		super(context, transformatorConfig);

		String aDisplayKey = transformatorConfig.getDisplayKey();
		String indexString = transformatorConfig.getIndices();
		StringTokenizer st = new StringTokenizer(indexString, INDEX_DELIMITER);

		idxs = new int[st.countTokens()];

		try{
			for(int i=0; i < st.countTokens();i++){
				String anIdxString = st.nextToken();
				idxs[i] = Integer.parseInt(anIdxString);
			}
		}catch(NumberFormatException ne){
			throw new IllegalArgumentException(
				"Given indices invalid in configuration section " + transformatorConfig.getName());
		}


		if ((idxs.length == 0)) {
			throw new IllegalArgumentException(
				"No indices found in configuration section " + transformatorConfig.getName());
		}

		this.displayNameKey = aDisplayKey;
	}

	/**
	 * In this case a new ResultTable is returned, containing new Values with exact the entries specified with the
	 * indices array in the constructor.
	 * NOTE: this method has to take care of NullValues which may appear within the data
	 * @see com.top_logic.reporting.data.processing.transformator.TableTransformator#transform(
     *       com.top_logic.reporting.data.base.table.ReportTable)
	 */
	@Override
	public ReportTable transform(ReportTable aTable) throws IllegalArgumentException {
		List aHeaderRow = aTable.getTableDescriptor().getHeaderRow();
		List aHeaderColumn = aTable.getTableDescriptor().getHeaderColumn();
		List tableData = aTable.getTableData();
		List newTableData = new ArrayList();

		for (int rowidx = 0; rowidx < tableData.size(); rowidx++) {
			List aRow = (List) tableData.get(rowidx);
			List aNewRow = new ArrayList();
			for (int colidx = 0; colidx < aRow.size(); colidx++) {
				Value theValue = (Value) aRow.get(colidx);

                if ((theValue == null)) {
					throw new IllegalArgumentException("The given result conatins invalid entries or null entries");
				}

                Value theNewValue = null;

                if (!(theValue instanceof NullValue) ) {
					theNewValue = theValue.projectOn(idxs);
				}
                else {
					theNewValue = theValue; //if it's a NullValue take it as it is
				}
				aNewRow.add(theNewValue);
			}
			newTableData.add(aNewRow);
		}

		TableDescriptor newDescriptor = new DefaultTableDescriptor(aHeaderRow,aHeaderColumn,aTable.getTableDescriptor().getType(), this.getTransformedValueSize(), aTable.getTableDescriptor().getDisplayName()+"("+this.getDisplayName()+")");
		ReportTable newTable = new ReportTable(newDescriptor,newTableData);
		return newTable;
	}

	/**
	 * @see com.top_logic.reporting.data.processing.transformator.TableTransformator#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return this.displayNameKey;
	}

	/**
	 * @see com.top_logic.reporting.data.processing.transformator.TableTransformator#getName()
	 */
	@Override
	public String getName() {
		return TRANSFORMATOR_ID;
	}

	/**
	 * @see com.top_logic.reporting.data.processing.transformator.TableTransformator#getRequiredValueSize()
	 */
	@Override
	public int getRequiredValueSize() {
		return getMaxIndex()+1;
	}

	/**
	 * @see com.top_logic.reporting.data.processing.transformator.TableTransformator#getType()
	 */
	@Override
	public Class getType() {
		return Value.class;
	}

	private int getMaxIndex() {
		int tmp = Integer.MIN_VALUE;

		for (int i = 0; i < idxs.length; i++) {
			if (tmp < idxs[i]) {
				tmp = idxs[i];
			}
		}
		return tmp;
	}

	/**
	 * @see com.top_logic.reporting.data.processing.transformator.TableTransformator#getCompatibleTransformators()
	 */
	@Override
	public TableTransformator[] getCompatibleTransformators() {

			return TransformatorFactory.getInstance().getTransformatorsForTypeAndSize(Value.class,getTransformedValueSize());
	}

	/**
	 * @see com.top_logic.reporting.data.processing.transformator.TableTransformator#getTransformedValueSize()
	 */
	@Override
	public int getTransformedValueSize() {
		return idxs.length;
	}

}
