/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import static java.util.Objects.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.dsa.util.DataAccessProxyBinaryContent;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * A base class for {@link Test} about the {@link TLModel}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@DeactivatedTest("Abstract base class for test, not a test itself.")
public abstract class TLModelTest extends BasicTestCase {

	private TLModel _model;

	private TLFactory _factory;

	/**
	 * The {@link TLModel} to test.
	 * 
	 * @see #setUpModel()
	 * @see #tearDownModel()
	 */
	protected TLModel getModel() {
		return _model;
	}

	/**
	 * The {@link TLFactory} to create instances in {@link #getModel()}.
	 */
	public TLFactory getFactory() {
		return _factory;
	}

	private void setModel(TLModel model, TLFactory factory) {
		_model = model;
		_factory = factory;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setModel(setUpModel(), setUpFactory());
	}

	/**
	 * Creates the {@link TLModel} to test.
	 * <p>
	 * The created {@link TLModel} has to be torn down in {@link #tearDownModel}.
	 * </p>
	 */
	protected abstract TLModel setUpModel();

	/**
	 * Creates the {@link TLFactory} for test.
	 */
	protected abstract TLFactory setUpFactory();

	@Override
	protected void tearDown() throws Exception {
		tearDownModel();
		setModel(null, null);
		super.tearDown();
	}

	/**
	 * For the model created in {@link #setUpModel()}.
	 * <p>
	 * For example to delete persistent models.
	 * </p>
	 */
	protected abstract void tearDownModel();

	/**
	 * Extend the {@link #getModel()} with the given {@link TLModel} xml.
	 */
	protected void extendModel(BinaryContent modelXml) {
		extendModel(getModel(), getFactory(), modelXml);
	}

	/**
	 * Try to extend the {@link #getModel()} with the given {@link TLModel} xml, but expect the
	 * extension to fail.
	 * 
	 * @throws ExpectedFailure
	 *         The expected failure.
	 */
	protected void extendModelExpectFailure(BinaryContent modelXml) {
		DynamicModelService.extendModel(new ExpectedFailureProtocol(), getModel(), getFactory(), modelXml);
	}

	private void extendModel(TLModel model, TLFactory factory, BinaryContent modelXml) {
		DynamicModelService.extendModel(new AssertProtocol(), model, factory, modelXml);
	}

	/**
	 * Resolves the {@link TLClass} with the given qualified name.
	 * 
	 * @return Never null.
	 */
	protected TLClass type(String qualifiedName) {
		return (TLClass) resolveType(qualifiedName);
	}

	/** Returns the {@link TLClass} with the given name. */
	protected TLClass type(String module, String type) {
		return (TLClass) module(module).getType(type);
	}

	/** Returns the item in given revision. */
	protected <T extends TLObject> T inRevision(T item, Revision revision) {
		return WrapperHistoryUtils.getWrapper(revision, item);
	}

	/**
	 * Resolves the {@link TLEnumeration} with the given qualified name.
	 * 
	 * @return Never null.
	 */
	protected TLEnumeration enumeration(String qualifiedName) {
		return (TLEnumeration) resolveType(qualifiedName);
	}

	private TLType resolveType(String qualifiedName) {
		TLType type = requireNonNull(TLModelUtil.findType(getModel(), qualifiedName));
		assertSame("General qualified name resolving and special type resolving return same objects.", type,
			resolve(qualifiedName));
		return type;
	}

	/**
	 * Resolves the {@link TLStructuredTypePart} with the given qualified name.
	 * 
	 * @return Never null.
	 */
	protected TLTypePart part(String qualifiedName) {
		TLTypePart part = requireNonNull(TLModelUtil.findPart(getModel(), qualifiedName));
		assertSame("General qualified name resolving and special part resolving return same objects.", part,
			resolve(qualifiedName));
		return part;
	}

	/**
	 * Resolves the {@link TLModelPart} with the given qualified name.
	 * 
	 * @return Never null.
	 */
	protected TLObject resolve(String qualifiedName) {
		return requireNonNull(TLModelUtil.resolveQualifiedName(getModel(), qualifiedName));
	}

	/**
	 * Resolves the {@link TLModule} with the given qualified name.
	 * 
	 * @return Never null.
	 */
	protected TLModule module(String name) {
		return requireNonNull(getModel().getModule(name));
	}

	/**
	 * Adds a {@link String} valued {@link TLClassProperty} with the given partName to the given
	 * {@link TLClass}.
	 */
	protected TLClassProperty addStringProperty(TLClass clazz, String partName) {
		return addProperty(clazz, partName, Kind.STRING);
	}

	/**
	 * Adds a {@link TLClassProperty} with the given {@link Kind kind} and partName to the given
	 * {@link TLClass}.
	 */
	protected TLClassProperty addProperty(TLClass clazz, String partName, Kind kind) {
		TLModel model = clazz.getModel();
		TLClassProperty property = TLModelUtil.addProperty(clazz, partName, TLCore.getPrimitiveType(model, kind));
		assertSame(model, property.getModel());
		return property;
	}

	/**
	 * Adds a {@link TLClass} with the given className to the given {@link TLModule}.
	 */
	protected TLClass addClass(TLModule module, String className) {
		TLClass clazz = TLModelUtil.addClass(module, className);
		assertSame(module.getModel(), clazz.getModel());
		return clazz;
	}

	/**
	 * Adds a class with the given className to the module with the given name. If no such
	 * {@link TLModule} exists, it is created.
	 */
	protected TLClass addClass(String moduleName, String className) {
		TLModule module = getModel().getModule(moduleName);
		if (module == null) {
			module = addModule(moduleName);
		}
		TLClass clazz = TLModelUtil.addClass(module, className);
		assertSame(getModel(), clazz.getModel());
		return clazz;
	}

	/**
	 * Adds a {@link TLModule} with the given moduleName.
	 */
	protected TLModule addModule(String moduleName) {
		TLModule module = TLModelUtil.addModule(getModel(), moduleName);
		assertSame(getModel(), module.getModel());
		return module;
	}

	/**
	 * Adds the {@link TLCore#TL_CORE core module} and default primitive types.
	 */
	protected void addCoreModule(TLModel model, TLFactory factory) {
		extendModel(model, factory, new DataAccessProxyBinaryContent("webinf://model/tl.core.model.xml"));
	}

	/**
	 * Test suite for a {@link TLModelTest} that uses a transient model.
	 */
	protected static Test suiteTransient(Class<? extends Test> testClass) {
		return suiteTransient(new TestSuite(testClass));
	}

	/**
	 * Test suite for a {@link TLModelTest} that uses a transient model.
	 */
	protected static Test suiteTransient(Test t) {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(t, AttributeSettings.Module.INSTANCE, CompatibilityService.Module.INSTANCE));
	}

}
