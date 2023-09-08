/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import static java.util.Arrays.*;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer.Index;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Rewriter} setting a sort order to an association value when there is none.
 * 
 * <p>
 * When an reference attribute was changed from "set reference" to "list reference", the values must
 * be sorted when this is not done, this rewriter assigns a sort order to those associations.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IntroduceRandomSortOrder extends Rewriter {

	private static final String MODULE_NAME_ATTRIBUTE = "name";

	private static final String MA_NAME_ATTRIBUTE = "name";

	/**
	 * Configuration for the {@link IntroduceRandomSortOrder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Rewriter.Config {

		/**
		 * The full qualified name of the reference {@link TLStructuredTypePart}. E.g.
		 * "orgStructure:OrgUnit.base#selectedMaturities".
		 */
		@Mandatory
		String getAttribute();

		/**
		 * Name of the sort order attribute.
		 */
		@Mandatory
		String getSortOrderAttribute();

		/**
		 * Name of the attribute in the association type holding the identifier of the
		 * {@link TLStructuredTypePart}.
		 */
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getMAAttributeName();
	}

	private Index _moduleIndex;

	private Index _typeIndex;

	private Index _partIndex;

	private String _partName;

	private String _typeName;

	private String _moduleName;

	private ObjectBranchId _attributeId;

	private Map<ObjectBranchId, ObjectBranchId> _sourceByAssociation = new HashMap<>();

	private Map<ObjectBranchId, Integer> _generatedSortOrder = new HashMap<>();

	/**
	 * Creates a new {@link IntroduceRandomSortOrder}.
	 */
	public IntroduceRandomSortOrder(InstantiationContext context, Config config) {
		super(context, config);
		splitAttributeName(context, config.getAttribute());
	}

	private void splitAttributeName(InstantiationContext context, String associationName) {
		int size = associationName.length();
		StringBuilder tmp = new StringBuilder();
		int index = 0;
		while(index < size) {
			char charAt = associationName.charAt(index++);
			if (charAt == TLModelUtil.QUALIFIED_NAME_SEPARATOR) {
				break;
			}
			tmp.append(charAt);
		}
		if (index == size) {
			context.error("Not a qualified part name: " +associationName);
		}
		_moduleName = tmp.toString();
		tmp.setLength(0);
		while (index < size) {
			char charAt = associationName.charAt(index++);
			if (charAt == TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR) {
				break;
			}
			tmp.append(charAt);
		}
		if (index == size) {
			context.error("Not a qualified part name: " + associationName);
		}
		_typeName = tmp.toString();
		_partName = associationName.substring(index);
	}
	
	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * Initialises the {@link Indexer}.
	 * 
	 * <p>
	 * The {@link Indexer} is used to find the {@link ObjectBranchId} of the
	 * {@link TLStructuredTypePart} for {@link Config#getAttribute()}.
	 * </p>
	 */
	@Inject
	public void initIndexer(Indexer indexer) {
		_moduleIndex = indexer.register(ApplicationObjectUtil.MODULE_OBJECT_TYPE,
			asList(MODULE_NAME_ATTRIBUTE),
			asList(Indexer.SELF_ATTRIBUTE));
		_typeIndex = indexer.register(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE,
			asList(ApplicationObjectUtil.META_ELEMENT_SCOPE_REF, ApplicationObjectUtil.META_ELEMENT_ME_TYPE_ATTR),
			asList(Indexer.SELF_ATTRIBUTE));
		_partIndex = indexer.register(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
			asList(ApplicationObjectUtil.META_ELEMENT_ATTR, MA_NAME_ATTRIBUTE),
			asList(Indexer.SELF_ATTRIBUTE));
		_partIndex.getValue(_partIndex);
	}
	
	@Override
	protected Object processDelete(ItemDeletion event) {
		_sourceByAssociation.remove(event.getObjectId());
		return super.processDelete(event);
	}

	@Override
	protected Object processUpdate(ItemUpdate event) {
		ObjectBranchId source = _sourceByAssociation.get(event.getObjectId());
		if (source != null) {
			Map<String, Object> values = event.getValues();
			Number changedSortOrder = (Number) values.get(getConfig().getSortOrderAttribute());
			if (changedSortOrder == null && values.containsKey(getConfig().getSortOrderAttribute())) {
				values.put(getConfig().getSortOrderAttribute(), nextSortOrder(source));
			}
		} else {
			// association is not regarded (e.g. for other meta attribute).
		}
		return super.processUpdate(event);
	}

	@Override
	protected Object processCreateObject(ObjectCreation event) {
		if (isRelevantEvent(event)) {
			Map<String, Object> values = event.getValues();
			ObjectBranchId source =
				ObjectBranchId.toObjectBranchId((ObjectKey) values.get(SourceReference.REFERENCE_SOURCE_NAME));
			Number initialSortOrder = (Number) values.get(getConfig().getSortOrderAttribute());
			if (initialSortOrder == null) {
				values.put(getConfig().getSortOrderAttribute(), nextSortOrder(source));
			}
			_sourceByAssociation.put(event.getObjectId(), source);
		}
		return super.processCreateObject(event);
	}

	private Integer nextSortOrder(ObjectBranchId source) {
		Integer generatedNumber = _generatedSortOrder.get(source);
		int order;
		if (generatedNumber == null) {
			order = -1;
		} else {
			order = generatedNumber.intValue() - 1;
		}
		Integer newSortOrder = Integer.valueOf(order);
		_generatedSortOrder.put(source, newSortOrder);
		return newSortOrder;
	}

	private boolean isRelevantEvent(ObjectCreation event) {
		initAttributeId();
		if (_attributeId == null) {
			// _attribute not found yet created;
			return false;
		}

		Map<String, Object> values = event.getValues();
		ObjectBranchId partId =
			ObjectBranchId.toObjectBranchId((ObjectKey) values.get(getConfig().getMAAttributeName()));
		return _attributeId.equals(partId);
	}

	private void initAttributeId() {
		Object moduleKey = _moduleIndex.getValue(_moduleName);
		if (moduleKey == null) {
			// scope not yet created
			return;
		}

		Object typeKey = _typeIndex.getValue(moduleKey, _typeName);
		if (typeKey == null) {
			// type not yet created
			return;
		}

		Object partKey = _partIndex.getValue(typeKey, _partName);
		if (partKey == null) {
			// part not yet created
			return;
		}
		_attributeId = ObjectBranchId.toObjectBranchId((ObjectKey) partKey);
	}

}

