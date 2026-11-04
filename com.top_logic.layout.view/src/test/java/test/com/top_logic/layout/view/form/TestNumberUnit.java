/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.NumberInputControlProvider;
import com.top_logic.layout.view.form.TLUnit;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.Resources;

/**
 * Tests for the unit a {@link TLUnit} annotation writes after a number: a constant, a text in the
 * language of the user, a function of the object owning the value, and a unit of the form field
 * winning over the annotation.
 */
public class TestNumberUnit extends AbstractModelAccessTest {

	/** Whole number attribute of a {@link #PROJECT} whose unit is a text in several languages. */
	private static final String DURATION = "duration";

	/** Fractional attribute of a {@link #PROJECT} whose unit is the name of the project. */
	private static final String DISTANCE = "distance";

	/** Fractional attribute of a {@link #PROJECT} whose unit is an empty text. */
	private static final String WEIGHT = "weight";

	/** Fractional attribute of a {@link #PROJECT} without a unit. */
	private static final String HEIGHT = "height";

	private FormControl _form;

	private AttributeFieldControl _field;

	private ViewChannel _input;

	@Override
	protected void tearDown() throws Exception {
		_input = null;
		_field = null;
		_form = null;
		super.tearDown();
	}

	/** A constant internationalized unit is written in the language of the user. */
	public void testAConstantUnitIsWrittenInTheLanguageOfTheUser() throws Exception {
		createField(DURATION, null);

		String expected = Resources.getInstance()
			.getString(ResKey.builder().add(Locale.ENGLISH, "min").add(Locale.GERMAN, "Min.").build());
		assertEquals(expected, input().getUnit());
	}

	/** A function computes the unit from the object owning the attribute. */
	public void testAFunctionComputesTheUnitFromTheObject() throws Exception {
		createField(DISTANCE, null);

		assertEquals("project", input().getUnit());
	}

	/** An empty unit writes none. */
	public void testAnEmptyUnitIsNone() throws Exception {
		createField(WEIGHT, null);

		assertNull(input().getUnit());
	}

	/** An attribute without annotation has no unit. */
	public void testAnAttributeWithoutAnnotationHasNoUnit() throws Exception {
		createField(HEIGHT, null);

		assertNull(input().getUnit());
	}

	/** The unit of the number input of a form field wins over the annotation. */
	public void testTheUnitOfTheFieldWinsOverTheAnnotation() throws Exception {
		createField(DISTANCE, readControlConfig("<input-control class=\""
			+ NumberInputControlProvider.class.getName() + "\" " + NumberInputControlProvider.Config.UNIT
			+ "=\"'h'\"/>"));

		assertEquals("h", input().getUnit());
	}

	/** A number input of a form field without a unit of its own writes the unit of the annotation. */
	public void testAnInputWithoutUnitWritesTheAnnotatedOne() throws Exception {
		createField(DISTANCE, readControlConfig("<input-control class=\""
			+ NumberInputControlProvider.class.getName() + "\"/>"));

		assertEquals("project", input().getUnit());
	}

	/** A unit computed from the object follows the form to another object. */
	public void testAFunctionUnitFollowsTheObjectOfTheForm() throws Exception {
		createField(DISTANCE, null, _root);
		ReactNumberInputControl control = input();
		assertEquals("project", control.getUnit());

		_input.set(_otherProject);

		assertSame("The field keeps its control.", control, input());
		assertEquals("otherProject", control.getUnit());
	}

	/** The unit of the number input of a form field stays when the form switches objects. */
	public void testTheUnitOfTheFieldStaysOnAnotherObject() throws Exception {
		createField(DISTANCE, readControlConfig("<input-control class=\""
			+ NumberInputControlProvider.class.getName() + "\" " + NumberInputControlProvider.Config.UNIT
			+ "=\"'h'\"/>"), _root);

		_input.set(_otherProject);

		assertEquals("h", input().getUnit());
	}

	/** Where no object owns the value, a function is not called while a constant is still written. */
	public void testWithoutObjectOnlyAConstantIsWritten() {
		assertNull(unitWithoutObject(DISTANCE));
		assertNotNull(unitWithoutObject(DURATION));
	}

	private static String unitWithoutObject(String attribute) {
		TLStructuredTypePart part = part(PROJECT, attribute);
		return FieldControlService
			.fieldSpec(part.getType(), part, attribute, false, new AbstractFieldModel(null)).getUnit();
	}

	/** Creates the field of the given attribute of the project, displayed to its responsible. */
	private void createField(String attribute, PolymorphicConfiguration<? extends ReactFieldControlProvider> control) {
		createField(attribute, control, _responsible);
	}

	/** Creates the field of the given attribute of the project, displayed to the given user. */
	private void createField(String attribute, PolymorphicConfiguration<? extends ReactFieldControlProvider> control,
			Person user) {
		becomeUser(user);
		_form = new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), _project, "no model", NoTokenHandling.INSTANCE);
		_input = new DefaultViewChannel("input");
		_input.set(_project);
		_form.setInputChannel(_input);
		_field = new AttributeFieldControl(_form.getReactContext(), _form, _form, attribute, null, false, null, null,
			control);
		_field.createChromeControl();
		_form.attach();
	}

	private ReactNumberInputControl input() {
		ReactControl result = _field.getInnerControl();
		assertTrue("A number input edits the field, but is: " + result, result instanceof ReactNumberInputControl);
		return (ReactNumberInputControl) result;
	}

	/** The configuration of the control the given XML declares by its class. */
	@SuppressWarnings("unchecked")
	private static PolymorphicConfiguration<? extends ReactFieldControlProvider> readControlConfig(String xml)
			throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestNumberUnit.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("input-control",
			TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Object result = reader.read();
		context.checkErrors();
		return (PolymorphicConfiguration<? extends ReactFieldControlProvider>) result;
	}

	/**
	 * The test suite, additionally starting the {@link FieldControlService} building the inputs of
	 * the fields and the {@link SearchBuilder} compiling the scripts computing the units.
	 */
	public static Test suite() {
		return suiteWith(TestNumberUnit.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			FieldControlService.Module.INSTANCE);
	}

}
