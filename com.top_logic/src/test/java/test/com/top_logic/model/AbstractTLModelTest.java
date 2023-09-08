/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.AbstractKnowledgeBaseTest;

import com.top_logic.knowledge.wrap.MapValueProvider;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.util.model.ModelService;

/**
 * Super class for tests using the {@link TLModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTLModelTest extends AbstractKnowledgeBaseTest {

	/**
	 * The application model.
	 */
	public TLModel model() {
		return ModelService.getApplicationModel();
	}

	/**
	 * The {@link TLModule} that has the name of the calling class, or one of its super
	 *         classes.
	 * 
	 * @throws AssertionFailedError
	 *         when no such module exists.
	 */
	public TLModule module() {
		Class<?> c = getClass();
		while (c != Object.class) {
			String moduleName = c.getName();
			TLModule module = module(moduleName);
			if (module != null) {
				return module;
			}
			c = c.getSuperclass();
		}
		throw new AssertionFailedError("No module found with name of class or super class: " + getClass().getName());
	}

	/**
	 * {@link TLModule} with the given name. May be <code>null</code>.
	 * 
	 */
	public TLModule module(String name) {
		return model().getModule(name);
	}

	/**
	 * The type with the given name in the module with the given name. Fails when there is
	 *         no such module. May be <code>null</code>.
	 */
	public TLType type(String module, String name) {
		return module(module).getType(name);
	}

	/**
	 * the type with the given name in module {@link #module()}.
	 */
	public TLType type(String name) {
		return module().getType(name);
	}

	/**
	 * Finds the {@link TLStructuredTypePart} with the given name in the given type in the given
	 * module.
	 */
	public TLStructuredTypePart part(TLModule module, String type, String name) {
		return ((TLStructuredType) module.getType(type)).getPart(name);
	}

	/**
	 * Finds the {@link TLStructuredTypePart} with the given name in the given type in the given
	 * module.
	 * 
	 * @see #part(TLModule, String, String)
	 */
	public TLStructuredTypePart part(String module, String type, String name) {
		return part(module(module), type, name);
	}

	/**
	 * Finds the {@link TLStructuredTypePart} with the given name in the given type in
	 * {@link #module()}.
	 * 
	 * @see #part(TLModule, String, String)
	 */
	public TLStructuredTypePart part(String type, String name) {
		return part(module(), type, name);
	}

	/**
	 * Creates a new instance of the {@link TLClass} with the given name.
	 * 
	 * @param className
	 *        Name of a {@link TLClass} in {@link #module()}.
	 */
	public TLObject newObject(String className) {
		return newObject(null, className, new MapValueProvider());
	}

	/**
	 * Creates a new instance of the {@link TLClass} with the given name.
	 * 
	 * @param className
	 *        Name of a {@link TLClass} in {@link #module()}.
	 * 
	 * @see TLFactory#createObject(TLClass, TLObject, ValueProvider)
	 */
	public TLObject newObject(TLObject context, String className, ValueProvider initialValues) {
		return newObject(context, module().getName(), className, initialValues);
	}

	/**
	 * Creates a new instance of the {@link TLClass} with the given name in the given module.
	 */
	public TLObject newObject(String moduleName, String className) {
		return newObject(null, moduleName, className, new MapValueProvider());
	}

	TLObject newObject(TLObject context, String moduleName, String className, ValueProvider initialValues)
			throws AssertionFailedError {
		ModelService modelService = ModelService.getInstance();
		TLFactory factory = modelService.getFactory();
		TLClass type = (TLClass) modelService.getModel().getModule(moduleName).getType(className);
		return factory.createObject(type, context, initialValues);
	}

	/**
	 * Gets the value for the given attribute in the given object.
	 */
	public Object getValue(TLObject obj, String attribute) {
		TLStructuredTypePart part = obj.tType().getPart(attribute);
		return obj.tValue(part);
	}

	/**
	 * Sets the value for the given attribute in the given object.
	 */
	public void setValue(TLObject obj, String attribute, Object value) {
		TLStructuredTypePart part = obj.tType().getPart(attribute);
		obj.tUpdate(part, value);
	}

	/**
	 * Creates a new {@link Test} based on the given {@link AbstractTLModelTest test class}.
	 */
	public static Test suite(Class<?> testClass) {
		return suite(testClass, DefaultTestFactory.INSTANCE);
	}

	/**
	 * Creates a new {@link Test} based on the given {@link AbstractTLModelTest test class}.
	 * 
	 * @param factory
	 *        The {@link TestFactory} creating the actual test.
	 */
	public static Test suite(Class<?> testClass, TestFactory factory) {
		factory = ServiceTestSetup.createStarterFactory(ModelService.Module.INSTANCE, factory);
		return KBSetup.getKBTest(testClass, factory);
	}

}

