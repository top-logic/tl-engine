/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.meta.OrderedListHelper;
import test.com.top_logic.element.meta.TestMetaElementFactory;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.PersistentObjectImpl;
import com.top_logic.element.meta.kbbased.storage.SetStorage;
import com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.AttributeConfigBase;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;


/**
 * Test the attribute implementation based on the KnowledgeBase.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Theo Sattler</a>
 */
@SuppressWarnings("javadoc")
public class TestKBBasedReferenceMetaAttributes extends BasicTestCase {

    /**
	 * CTor with a test method name
	 * 
	 * @param name the test method name
	 */
	public TestKBBasedReferenceMetaAttributes(String name) {
		super(name);
	}

    /**
     * Create MetaElements and MetaAttributes.
     * 
     * Tests creation and setting/resetting values and attribute inheritance.
     */
    public void testReferenceBuilderScenario() throws Exception {
        // INIT STUFF
        KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
        assertNotNull("KB is null!", theKB);
        
        startTime();
        
        // SETUP
        
		AttributedStructuredElementWrapper attributed = OrderedListHelper.getAttributedWrapper();
        
        
        // Create a three step hierarchy MetaElements and add them to the list (holder)
		TLModel tlModel = ModelService.getApplicationModel();
		TLModule module = TLModelUtil.makeModule(tlModel, TestKBBasedReferenceMetaAttributes.class.getName());
		TLClass me = TestMetaElementFactory.addME(module, attributed, "referenceTest");
        
		PersistentObjectImpl.setMetaElement(attributed, me);
		TLType referenceType = attributed.tType();

		ReferenceConfig setConfig =
			TestKBBasedMetaAttributes.referenceConfig("typedSet", TLModelUtil.qualifiedName(referenceType), 0.0, true);
		SetStorage.Config setStorage = TypedConfiguration.newConfigItem(SetStorage.Config.class);
		setStorage.setImplementationClass(SetStorage.class);
		setAnnotation(setConfig, storage(setStorage));
		TestKBBasedMetaAttributes.createReference(me, setConfig);

        assertTrue (MetaElementUtil.hasLocalMetaAttribute(me, "typedSet"));
        
        attributed.setValue("typedSet", Collections.singletonList(attributed));

        // check single wrapper attribute
		ReferenceConfig singletonConfig =
			TestKBBasedMetaAttributes.referenceConfig("typed", TLModelUtil.qualifiedName(referenceType), 0.0, false);
		SingletonLinkStorage.Config singletonStorage =
			TypedConfiguration.newConfigItem(SingletonLinkStorage.Config.class);
		singletonStorage.setImplementationClass(SingletonLinkStorage.class);
		setAnnotation(singletonConfig, storage(singletonStorage));
		TestKBBasedMetaAttributes.createReference(me, singletonConfig);

        assertTrue (MetaElementUtil.hasLocalMetaAttribute(me, "typed"));
        
        attributed.setValue("typed", attributed);
        
        // check attribute without reference builder
		ReferenceConfig regularConfig = TestKBBasedMetaAttributes.referenceConfig("typedNoRefBuilder",
			TLModelUtil.qualifiedName(referenceType), 0.0, false);
		SingletonLinkStorage.Config retularStorage =
			TypedConfiguration.newConfigItem(SingletonLinkStorage.Config.class);
		retularStorage.setImplementationClass(SingletonLinkStorage.class);
		setAnnotation(regularConfig, storage(retularStorage));
		TestKBBasedMetaAttributes.createReference(me, regularConfig);

        assertTrue (MetaElementUtil.hasLocalMetaAttribute(me, "typedNoRefBuilder"));
    }

	private void setAnnotation(AttributeConfigBase config, TLAttributeAnnotation annotation) {
		config.getAnnotations().add(annotation);
	}

	private TLStorage storage(PolymorphicConfiguration<StorageImplementation> storageConfig) {
		TLStorage result = TypedConfiguration.newConfigItem(TLStorage.class);
		result.setImplementation(storageConfig);
		return result;
	}

	/**
	 * Suite of tests.
	 */
    public static Test suite () {
        return ElementWebTestSetup.createElementWebTestSetup(new TestSuite(TestKBBasedReferenceMetaAttributes.class));
    }
    
}
