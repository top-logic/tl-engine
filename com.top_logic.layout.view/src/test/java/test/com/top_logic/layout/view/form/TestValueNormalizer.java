/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueNormalizer;
import com.top_logic.model.annotate.util.ValueNormalizer;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.providers.NormalizeByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that a field of the view layer brings an entered value into the canonical form its
 * {@link TLValueNormalizer} computes, before the value reaches the edited object.
 *
 * <p>
 * The ticket has a {@link #CODE} trimmed by a {@link NormalizeByExpression}, a {@link #TAG}
 * normalized by {@link AppendStatus} reading the {@link #STATUS} of the edited object, and the
 * {@link #TITLE} without a normalizer.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestValueNormalizer extends AbstractTicketFormTest {

	/** Name of the attribute whose entered value is trimmed. */
	private static final String CODE = "code";

	/** Name of the attribute whose entered value is normalized by {@link AppendStatus}. */
	private static final String TAG = "tag";

	private AttributeFieldControl _code;

	private AttributeFieldControl _tag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_code = field(CODE);
		_tag = field(TAG);
	}

	@Override
	protected void tearDown() throws Exception {
		_code = null;
		_tag = null;
		super.tearDown();
	}

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		try {
			TLModelUtil.addProperty(_ticketType, CODE, text)
				.setAnnotation(normalizerAnnotation(scriptNormalizer("value -> obj -> $value.trim()")));
			TLModelUtil.addProperty(_ticketType, TAG, text).setAnnotation(normalizerAnnotation(
				TypedConfiguration.createConfigItemForImplementationClass(AppendStatus.class)));
		} catch (ConfigurationException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * An entered value is stored in its canonical form, which the field shows and announces.
	 */
	public void testEnteredValueIsNormalized() {
		_form.enterEditMode();
		List<Object> changes = recordChanges(_code);

		model(_code).setValue("  x  ");

		assertEquals("x", model(_code).getValue());
		assertEquals("The overlay receives the canonical form.", "x", _form.getOverlay().tValue(part(CODE)));
		assertEquals("The input is told about the canonical form.", List.of("x"), changes);
		assertEquals("x", shown(_code));
		assertTrue(_code.isDirty());
	}

	/**
	 * A value rewritten to the value the field already holds is announced, so that the input
	 * replaces the text it was given, but it changes nothing.
	 */
	public void testValueRewrittenToCurrentValueIsAnnounced() {
		_ticket.tUpdateByName(CODE, "x");
		_form.enterEditMode();
		assertEquals("x", model(_code).getValue());
		List<Object> changes = recordChanges(_code);

		model(_code).setValue("  x ");

		assertEquals("The input is told to show the canonical form instead of the given text.",
			List.of("x"), changes);
		assertEquals("x", model(_code).getValue());
		assertFalse("The value has not changed.", _code.isDirty());
		assertFalse("The overlay is not changed.", _form.getOverlay().isChanged(part(CODE)));
	}

	/**
	 * A value given to the field equal to its canonical form is not announced again.
	 */
	public void testCanonicalValueAtCurrentValueIsNotAnnounced() {
		_ticket.tUpdateByName(CODE, "x");
		_form.enterEditMode();
		List<Object> changes = recordChanges(_code);

		model(_code).setValue("x");

		assertEquals(List.of(), changes);
	}

	/**
	 * An attribute without normalizer stores what is entered.
	 */
	public void testValueWithoutNormalizerIsStoredAsGiven() {
		_form.enterEditMode();

		model(_title).setValue("  x  ");

		assertEquals("  x  ", model(_title).getValue());
		assertEquals("  x  ", _form.getOverlay().tValue(part(TITLE)));
	}

	/**
	 * A value shown from the object is not normalized: stored data is shown as it is.
	 */
	public void testStoredValueIsShownAsItIs() {
		_ticket.tUpdateByName(CODE, "  stored  ");
		_form.enterEditMode();

		assertEquals("  stored  ", model(_code).getValue());
		assertFalse(_code.isDirty());
	}

	/**
	 * The normalizer sees the object the form edits, holding the values entered for the other
	 * attributes.
	 */
	public void testNormalizerSeesEnteredValues() {
		_form.enterEditMode();
		model(_status).setValue("closed");

		model(_tag).setValue("urgent");

		assertEquals("The normalizer read the entered status, not the stored one.",
			"urgent@closed", _form.getOverlay().tValue(part(TAG)));
		assertEquals("open", _ticket.tValueByName(STATUS));
	}

	/**
	 * A field that is not bound to an attribute normalizes with neither object nor attribute, as
	 * the field of a {@code <value-input>} does.
	 */
	public void testUnboundFieldNormalizes() throws ConfigurationException {
		AbstractFieldModel field = new AbstractFieldModel(null);
		field.setNormalizer(TypedConfigUtil.createInstance(
			scriptNormalizer("value -> obj -> if($obj == null, $value.trim(), 'bound')")));
		List<Object> changes = recordChanges(field);

		field.setValue("  x  ");

		assertEquals("x", field.getValue());
		assertEquals(List.of("x"), changes);

		field.setValue(" x");

		assertEquals("x", field.getValue());
		assertEquals(List.of("x", "x"), changes);
	}

	/**
	 * A field whose attribute rewrites what it is given holds a typed value back until the field is
	 * left.
	 */
	public void testNormalizedFieldSendsValueOnBlur() {
		assertTrue(spec(CODE).isSendValueOnBlur());
		assertFalse(spec(TITLE).isSendValueOnBlur());
	}

	private FieldSpec spec(String attribute) {
		TLStructuredTypePart part = part(attribute);
		return FieldControlService.fieldSpec(part.getType(), part, attribute, false, new AbstractFieldModel(null));
	}

	private TLStructuredTypePart part(String name) {
		return _ticketType.getPart(name);
	}

	private static TLValueNormalizer normalizerAnnotation(
			PolymorphicConfiguration<? extends ValueNormalizer> normalizer) {
		TLValueNormalizer result = TypedConfiguration.newConfigItem(TLValueNormalizer.class);
		result.setNormalizer(normalizer);
		return result;
	}

	private static NormalizeByExpression.Config<?> scriptNormalizer(String function) throws ConfigurationException {
		NormalizeByExpression.Config<?> result = TypedConfiguration.newConfigItem(NormalizeByExpression.Config.class);
		TypedConfigUtil.setProperty(result, NormalizeByExpression.Config.FUNCTION,
			ExprFormat.INSTANCE.getValue(NormalizeByExpression.Config.FUNCTION, function));
		return result;
	}

	private static List<Object> recordChanges(AttributeFieldControl field) {
		return recordChanges(model(field));
	}

	/**
	 * The values the given model announces to change to, from now on.
	 */
	private static List<Object> recordChanges(FieldModel model) {
		List<Object> result = new ArrayList<>();
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				result.add(newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not recorded.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Not recorded.
			}
		});
		return result;
	}

	/**
	 * {@link ValueNormalizer} appending the {@link #STATUS} of the edited object to the entered
	 * value.
	 */
	public static class AppendStatus implements ValueNormalizer {

		@Override
		public Object normalize(TLObject object, TLStructuredTypePart attribute, Object value) {
			String suffix = "@" + object.tValueByName(STATUS);
			String text = (String) value;
			return text == null || text.endsWith(suffix) ? text : text + suffix;
		}

	}

	/**
	 * Test suite requiring a knowledge base for the form, the {@link FieldControlService} building
	 * the inputs of the fields, and the {@link SearchBuilder} compiling the script normalizer.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestValueNormalizer.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE));
	}
}
