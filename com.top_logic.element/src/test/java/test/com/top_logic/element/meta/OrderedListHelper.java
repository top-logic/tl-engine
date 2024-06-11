/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import junit.framework.Assert;

import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.BNode;
import test.com.top_logic.element.structured.model.TestTypesFactory;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.label.ObjectLabel;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.config.annotation.MultiSelect;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.transform.ModelLayout;

/**
 * Helper for lazy creation of variants of {@link FastList} and such.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class OrderedListHelper extends Assert {
    
	/** The type name of the type. */
	public static final String META_ELEMENT_TYPE = "FastList.MetaElement";

	/** The type name of the super type. */
	public static final String SUPER_META_ELEMENT_TYPE = "FastList.SuperMetaElement";

	/** The type name of the sub type. */
	public static final String SUB_META_ELEMENT_TYPE = "FastList.SubMetaElement";
	
	private static final String AttributedWrapper_NAME= "____attr";

	private static AttributedStructuredElementWrapper __theAttributedWrapper = null;
    
    /** Flag to be set in tests so that no root list is created. */
	public static boolean doNotInitForTest = false;

    /*
     * Lazy Acessor to a constant AttributedWrapper
     */
	public static AttributedStructuredElementWrapper getAttributedWrapper() throws Exception {
    	if (__theAttributedWrapper != null) {
    		return __theAttributedWrapper;
    	}
    	
    	KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
    	
		AttributedStructuredElementWrapper newObject = __getAttributedWrapper(AttributedWrapper_NAME, theKB);
    	assertTrue(theKB.commit());
    	
		__theAttributedWrapper = newObject;
		return newObject;
    }
        
	/**
	 * Get an example FastList with three address objects
	 * 
	 * @param model
	 *        The {@link TLModel} to create global enumeration in.
	 * @param isMultiSelect
	 *        if true the list is multi-selectable
	 * @return an example FastList with three elements
	 */
	public static TLEnumeration createTestEnumeration(TLModel model, String aBaseName, boolean isMultiSelect) {
		// Create FastList
		String theName = aBaseName;
		String thePostFix = (isMultiSelect) ? "M" : "";

		TLModule globalEnumModule = TLModelUtil.makeModule(model, ModelLayout.TL5_ENUM_MODULE);
		String enumName = theName + thePostFix;
		TLType existingEnum = globalEnumModule.getType(enumName);
		if (existingEnum != null) {
			return (TLEnumeration) existingEnum;
		}
		TLEnumeration enumeration = TLModelUtil.addEnumeration(globalEnumModule, enumName);
		MultiSelect annotation = TypedConfiguration.newConfigItem(MultiSelect.class);
		annotation.setValue(isMultiSelect);
		enumeration.setAnnotation(annotation);
		enumeration.getClassifiers().add(model.createClassifier(aBaseName + "firstname"));
		enumeration.getClassifiers().add(model.createClassifier(aBaseName + "scdname"));
		enumeration.getClassifiers().add(model.createClassifier(aBaseName + "thirdname"));
		return enumeration;
	}
    
    /**
     * Get an Example AttributedWrapper.
     * 
     * @param  aBaseName return Example Object with that name.
     * @param  aKB	the KB to create the AttributedWrapper in
     * @return an example AttributedWrapper 
     * @throws Exception if creation fails
     */
	public static AttributedStructuredElementWrapper __getAttributedWrapper(String aBaseName, KnowledgeBase aKB)
                throws Exception {
		KnowledgeObject existing = ObjectLabel.getLabeledObject(aBaseName);
		if (existing != null) {
			return (AttributedStructuredElementWrapper) existing.getWrapper();
		}

		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		BNode child = (BNode) root.createChild(aBaseName, TestTypesFactory.getBNodeType());
		ObjectLabel.createLabel(aBaseName, child.tHandle());
		initMandatoryFields(root, child);

		return (AttributedStructuredElementWrapper) child;
	}

	/**
	 * Fills mandatory fields of the given {@link BNode}.
	 */
	public static void initMandatoryFields(ANode root, BNode child) throws ConfigurationException {
		child.setMandatoryBoolean(false);
		child.setMandatoryChecklist(
			Collections.singleton(
				(TLClassifier) TLModelUtil
					.resolveQualifiedName("enum:test.element.checklist#test.element.checklist.a")));
		child.setMandatoryChecklistSingle(
			(TLClassifier) TLModelUtil.resolveQualifiedName(
				"tl5.enum:test.element.classification.single#test.element.classification.single.b"));
		child.setMandatoryChecklistMulti(
			Collections.singleton((TLClassifier) TLModelUtil
				.resolveQualifiedName("enum:test.element.classification.multi#test.element.classification.multi.b")));
		child.setMandatoryCollection(Collections.singleton((AttributedStructuredElementWrapper) child));
		child.setMandatoryDate(new Date(0L));
		child.setMandatoryFloat(0.0);
		child.setMandatoryList(Arrays.asList(child, root));
		child.setMandatoryLong(0L);
		child.setMandatoryString("foobar");
		child.setMandatoryStringSet(new HashSet<>(Arrays.asList("foo", "bar")));
		child.setMandatoryStructure(new HashSet<>(Arrays.asList(child)));
		child.setMandatoryTypedSetOrdered(Arrays.asList(child, root));
		child.setMandatoryTypedSetUnordered(Collections.singleton((AttributedStructuredElementWrapper) child));
		child.setMandatoryTypedWrapper(child);
		child.setMandatoryUntypedWrapper(child);
	}

}
