/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import static java.util.Objects.*;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for sending and receiving an update of an association with TL-Sync.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLSyncAssociationUpdate extends AbstractTLSyncTest {

	private static final String MODULE_NAME = TestTLSyncAssociationUpdate.class.getName();

	private static final String COMMON_TYPE_NAME = "CommonType";

	private static final String SOURCE_TYPE_NAME = "SourceType";

	private static final String TARGET_TYPE_NAME = "TargetType";

	private static final String REFERENCE_NAME = "associationReference";

	public void test() {
		TLObject sourceContainer = sync(this::createContainerWithTwoEntries);
		try {
			TLObject targetContainer = requireNonNull(findTargetObject(sourceContainer));

			sync(() -> setSortOrderOfAssociations(sourceContainer, 3, 7));
			assertSortOrderOfAssociations(targetContainer, 3, 7);

			sync(() -> setSortOrderOfAssociations(sourceContainer, 13, 5));
			assertSortOrderOfAssociations(targetContainer, 5, 13);
		} finally {
			sync(() -> deleteContainerWithElements(sourceContainer));
		}
	}

	private TLObject createContainerWithTwoEntries() {
		TLObject container = createObject();
		TLObject content0 = createObject();
		TLObject content1 = createObject();
		setReference(container, content0, content1);
		return container;
	}

	private void setSortOrderOfAssociations(TLObject container, int value0, int value1) {
		List<KnowledgeAssociation> associations = getAssocations(container);
		/* Retrieve them before changing the sort sort, as changing the sort order directly changes
		 * their position in the list. */
		KnowledgeAssociation association0 = associations.get(0);
		KnowledgeAssociation association1 = associations.get(1);
		association0.setAttributeValue(LinkStorage.SORT_ORDER, value0);
		association1.setAttributeValue(LinkStorage.SORT_ORDER, value1);
	}

	private void assertSortOrderOfAssociations(TLObject container, int value0, int value1) {
		List<KnowledgeAssociation> associations = getAssocations(container);
		assertEquals(value0, associations.get(0).getAttributeValue(LinkStorage.SORT_ORDER));
		assertEquals(value1, associations.get(1).getAttributeValue(LinkStorage.SORT_ORDER));
	}

	private void deleteContainerWithElements(TLObject sourceContainer) {
		KBUtils.deleteAll(getReference(sourceContainer));
		sourceContainer.tDelete();
	}

	private ListStorage<?> getReferenceStorage() {
		return (ListStorage<?>) getReferenceAttribute().getStorageImplementation();
	}

	// For a test, it is good enough to suppress the warning.
	@SuppressWarnings("unchecked")
	private List<TLObject> getReference(TLObject container) {
		return (List<TLObject>) container.tValueByName(REFERENCE_NAME);
	}

	private void setReference(TLObject container, TLObject... elements) {
		container.tUpdateByName(REFERENCE_NAME, List.of(elements));
	}

	private List<KnowledgeAssociation> getAssocations(TLObject sourceContainer) {
		KnowledgeObject selfKO = (KnowledgeObject) sourceContainer.tHandle();
		return kb().resolveLinks(selfKO, getReferenceStorage().getOutgoingQuery());
	}

	private TLObject findTargetObject(TLObject source) {
		return findReceivedObjectFor(type(MODULE_NAME, TARGET_TYPE_NAME), source);
	}

	private TLObject createObject() {
		return newObject(MODULE_NAME, SOURCE_TYPE_NAME);
	}

	private TLClassPart getReferenceAttribute() {
		return getAttribute(MODULE_NAME, COMMON_TYPE_NAME, REFERENCE_NAME);
	}

	private TLClassPart getAttribute(String module, String type, String attribute) {
		return (TLClassPart) getClass(module, type).getPart(attribute);
	}

	private TLClass getClass(String module, String type) {
		return (TLClass) getModule(module).getType(type);
	}

	private TLModule getModule(String module) {
		return ModelService.getApplicationModel().getModule(module);
	}

	/**
	 * Creates a {@link TestSuite} for all the tests in {@link TestTLSyncAssociationUpdate}.
	 */
	public static Test suite() {
		return suite(TestTLSyncAssociationUpdate.class);
	}

}
