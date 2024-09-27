/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.lang.Math.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.Document;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.demo.Messages;
import com.top_logic.demo.edit.DemoFormContextModificator;
import com.top_logic.demo.model.types.A;
import com.top_logic.demo.model.types.B;
import com.top_logic.demo.model.types.BNotUnderA;
import com.top_logic.demo.model.types.C;
import com.top_logic.demo.model.types.DemoTypesA;
import com.top_logic.demo.model.types.DemoTypesFactory;
import com.top_logic.demo.model.types.Root;
import com.top_logic.demo.model.types.X;
import com.top_logic.element.core.util.ElementEventUtil;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.SimpleEditContext;
import com.top_logic.element.meta.complex.CountryOptionProvider;
import com.top_logic.element.meta.complex.LanguageOptionProvider;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.constraints.RangeConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.ValueBelowLabel;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.HiddenInEditMode;
import com.top_logic.util.Country;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * The class {@link TypeGenerator} contains two Handlers for creating one {@link B#B_TYPE} object
 * with {@link #NUMBER_OF_CHILDREN} many {@link A#A_TYPE} children, and a handler to delete them
 * all.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeGenerator {

	/**
	 * The minimum chance for <code>null</code> or the empty value.
	 * <p>
	 * This is used to increase the chance for the empty value. When there are many options the
	 * choose from, the chance for the empty value would be very low. But that is unwanted, as the
	 * empty value is a special value which appears often in real data and therefore needs to be
	 * tested thoroughly.
	 * </p>
	 */
	private static final double NULL_PROBABILITY = 0.25;

	/**
	 * The maximum number of values that should be generated for attributes which store a
	 * {@link Collection} of values.
	 */
	private static final int MAX_COLLECTION_VALUES = 10;

	static int NUMBER_OF_CHILDREN = 20;

	public static class GenerateDemoTypes extends AbstractCommandHandler {

		public GenerateDemoTypes(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			StructuredElement parent = (StructuredElement) model;

			DisplayDimension width = ThemeFactory.getTheme().getValue(Icons.TYPE_GENERATOR_WIDTH);
			DisplayDimension height = ThemeFactory.getTheme().getValue(Icons.TYPE_GENERATOR_HEIGHT);

			new GeneratingDialog(I18NConstants.TYPE_GENERATOR_DIALOG, width, height, parent).open(context);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return HiddenInEditMode.INSTANCE;
		}

	}

	static String getErrorKey(String key) {
		return I18NConstants.TYPE_GENERATOR + key;
	}

	/**
	 * creates a {@link B#B_TYPE} with the given number of {@link A#A_TYPE} children.
	 * 
	 * @param parent
	 *        The parent to generate children for.
	 * @param rootName
	 *        The name of the generated root node.
	 * @param numberOfChildren
	 *        the number of {@link A#A_TYPE}s to create
	 * 
	 * @return <code>true</code> iff something was created
	 */
	static boolean generate(StructuredElement parent, String rootName, int numberOfChildren) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		Transaction tx = kb.beginTransaction(Messages.GENERATED_DEMO_TYPES.fill());
		
		String parentType = parent.getElementType();
		assert ! StringServices.isEmpty(parentType);
		String generatedRootType = getGeneratedRootType(parentType);
		StructuredElement generatedRoot = parent.createChild(rootName, generatedRootType);

		if (generatedRootType.equals(A.A_TYPE)) {
			// Fill mandatory attributes to prevent the transaction from failing.
			A root = (A) generatedRoot;
			root.setBooleanMandatory(true);
			root.setBooleanSelectMandatory(true);
			root.setBooleanRadioMandatory(true);
		}

		int cId = 0;
		Random rand = new Random();
		try {
			for (int i = 0; i < numberOfChildren; i++) {
				if (generatedRootType.equals(A.A_TYPE)) {
					C child = (C) generatedRoot.createChild("C" + i, C.C_TYPE);
					child.setIsAssignable(rand.nextBoolean());
					child.setStringInBAndC("GeneratedString_" + rand.nextInt());

					sendCreateEvent(child);
					for (int n = 0; n < 2; n++) {
						StructuredElement cNode = child.createChild("C" + (cId++), C.C_TYPE);
						sendCreateEvent(cNode);
					}
					createXChild(generatedRoot, "X" + i);
				} else {
					DemoTypesA child = (DemoTypesA) generatedRoot.createChild("A" + i, A.A_TYPE);
					fillBooleanAttributes(child, rand);
					long YEAR = 365L * 24L * 60L * 60L * 1000L;

					// Date must be in the range 2000-01-01 - 2100-12-31 to comply with all
					// constraints.
					Date date1 = new Date(
						DemoFormContextModificator.getLowerDate().getTime() + ((long) (rand.nextDouble() * 20 * YEAR)));
					child.setDate(date1);
					int range = (int) (rand.nextDouble() * 2 * 365);
					Date date2 = DateUtil.addDays(date1, range);
					child.setDate2(date2);
					child.setFloat(rand.nextDouble());
					child.setFloatPercent(rand.nextDouble());
					child.setFloatDefault(rand.nextDouble());
					if (rand.nextBoolean()) {
						// Valid values are from -10 (inclusive) to +10 (inclusive).
						// But +10 itself is never generated, that would be to complicated.
						child.setFloatConstraint((rand.nextDouble() * 20) - 10);
					}
					child.setLong(rand.nextLong());
					if (rand.nextBoolean()) {
						// Valid values are from -10 (inclusive) to +10 (inclusive):
						child.setLongConstraint((long) (rand.nextInt(21) - 10));
					}
					child.setString("GeneratedString_" + rand.nextInt());
					List<Locale> languages = LanguageOptionProvider.INSTANCE.getOptionsList(TLContext.getLocale());
					child.setComplexLanguage(languages.get(rand.nextInt(languages.size())));
					List<Country> countries = CountryOptionProvider.INSTANCE.getOptionsList(TLContext.getLocale());
					child.setComplexCountry(countries.get(rand.nextInt(countries.size())));
					List<Unit> units = UnitWrapper.getAllUnits();
					child.setComplexUnit(units.get(rand.nextInt(units.size())));
					int setSize = rand.nextInt(10);
					HashSet<String> stringSet = CollectionUtil.newSet(setSize);
					for (int index = 0; index < setSize; index++) {
						stringSet.add("GeneratedString_" + rand.nextInt());
					}
					Set<TLClassifier> checkListElement = createWrapperValues(TLClassifier.class,
						DemoTypesFactory.getChecklistDemoTypesAAttr(), rand, MAX_COLLECTION_VALUES);
					child.setChecklist(checkListElement);
					Set<TLClassifier> checkListMultiElement = createWrapperValues(TLClassifier.class,
						DemoTypesFactory.getChecklistMultiDemoTypesAAttr(), rand, MAX_COLLECTION_VALUES);
					child.setChecklistMulti(checkListMultiElement);
					child.setChecklistSingle(createChecklistSingleValue(rand));
					fillClassificationAttributes(rand, child);

					child.setStringSet(stringSet);

					Person person = createWrapperValueNullable(Person.class, DemoTypesFactory.getAccountDemoTypesAAttr(), rand);
					child.setAccount(person);
					sendCreateEvent(child);

					for (int n = 0; n < 2; n++) {
						C cNode = (C) child.createChild("C" + (cId++), C.C_TYPE);
						
						cNode.setValue(C.DEPENDENT_DATE_ATTR,
							DateUtil.addDays(date1, (int) (rand.nextDouble() * range)));
						
						sendCreateEvent(cNode);
					}
					createXChild(child, "X" + i);
				}
			}
			tx.commit();
			Transaction structureReferenceUpdate = kb.beginTransaction(Messages.GENERATED_DEMO_TYPES.fill());
			if (!generatedRootType.equals(A.A_TYPE)) {
				for (StructuredElement child : generatedRoot.getChildren()) {
					DemoTypesA ANode = (DemoTypesA) child;
					ANode.setStructure(createWrapperValues(C.class,
						DemoTypesFactory.getStructureDemoTypesAAttr(), rand, MAX_COLLECTION_VALUES));
					C singleStructure = createWrapperValue(C.class,
						DemoTypesFactory.getSingleStructureDemoTypesAAttr(), rand);
					if (singleStructure != null) {
						ANode.setSingleStructure(singleStructure);
					}
				}
			}
			structureReferenceUpdate.commit();
		} catch (KnowledgeBaseException ex) {
			Logger.error("Unable to generate types", ex, GenerateDemoTypes.class);
			throw new TopLogicException(TypeGenerator.class, "generationFailed", ex);
		}
		return true;
	}

	private static void fillClassificationAttributes(Random random, DemoTypesA demoA) {
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationSingleDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationLocalDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationSingleLegacyDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationMultiDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationSinglePopupDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationMultiDropdownDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedMultiDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedSingleDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedSinglePopupDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedSingleRadioDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedSingleRadioInlineDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedMultiDropdownDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedMultiRadioDemoTypesAAttr());
		fillRandomlyWithClassifiers(random, demoA, DemoTypesFactory.getClassificationUnorderedMultiRadioInlineDemoTypesAAttr());

	}

	private static void fillRandomlyWithClassifiers(Random random, DemoTypesA demoA, TLClassPart attribute) {
		if (attribute.isMultiple()) {
			List<?> chosenClassifiers = randomClassifiers(random, attribute);
			if (attribute.isMandatory() && chosenClassifiers.isEmpty()) {
				demoA.tUpdate(attribute, list(randomClassifier(random, attribute)));
				return;
			}
			demoA.tUpdate(attribute, chosenClassifiers);
		} else {
			if (!attribute.isMandatory() && random.nextDouble() < NULL_PROBABILITY) {
				return;
			}
			demoA.tUpdate(attribute, randomClassifier(random, attribute));
		}
	}

	private static List<TLClassifier> randomClassifiers(Random random, TLClassPart attribute) {
		List<TLClassifier> chosenClassifiers =
			list(createWrapperValues(TLClassifier.class, attribute, random, MAX_COLLECTION_VALUES));
		Collections.shuffle(chosenClassifiers, random);
		return chosenClassifiers;
	}

	private static TLClassifier randomClassifier(Random random, TLClassPart attribute) {
		return createWrapperValue(TLClassifier.class, attribute, random);
	}

	private static void createXChild(StructuredElement parent, String name) {
		StructuredElement xNode = parent.createChild(name, X.X_TYPE);
		sendCreateEvent(xNode);
	}

	static void sendCreateEvent(StructuredElement element) {
		ElementEventUtil.sendEvent(element, MonitorEvent.CREATED);
	}

	static void sendDeleteEvent(StructuredElement deletedNode) {
		ElementEventUtil.sendEvent(deletedNode, MonitorEvent.DELETED);
	}

	private static TLClassifier createChecklistSingleValue(Random random) {
		TLStructuredTypePart attribute = DemoTypesFactory.getChecklistSingleDemoTypesAAttr();
		return createWrapperValueNullable(TLClassifier.class, attribute, random);
	}

	private static <E extends TLObject> Set<E> createWrapperValues(Class<E> contentType,
			TLStructuredTypePart attribute, Random rand, int maxElementCount) {
		if (rand.nextFloat() < NULL_PROBABILITY) {
			return Collections.emptySet();
		}
		OptionModel<?> options = AttributeOperations.allOptions(SimpleEditContext.createContext(attribute));
		if (options.getOptionCount() == 0) {
			return Collections.emptySet();
		}
		maxElementCount = min(maxElementCount, options.getOptionCount());
		int currentElementCount = rand.nextInt(maxElementCount + 1);
		Set<E> selectedWrappers = new HashSet<>();
		if (options instanceof ListOptionModel) {
			List<?> baseList = ((ListOptionModel<?>) options).getBaseModel();
			for (int i = 0; i < currentElementCount; i++) {
				int nextElementIndex = rand.nextInt(baseList.size());
				selectedWrappers.add(contentType.cast(baseList.get(nextElementIndex)));
			}
		} else if (options instanceof TreeOptionModel) {
			TLTreeModel<?> baseModel = ((TreeOptionModel<?>) options).getBaseModel();
			for (int i = 0; i < currentElementCount; i++) {
				selectedWrappers.add(contentType.cast(pseudoRandomNode(baseModel, rand)));
			}
		} else {
			Iterator<?> iterator = options.iterator();
			while (iterator.hasNext() && currentElementCount-- > 0) {
				selectedWrappers.add(contentType.cast(iterator.next()));
			}
		}
		return selectedWrappers;
	}

	private static <E extends TLObject> E createWrapperValueNullable(Class<E> contentType,
			TLStructuredTypePart attribute, Random rand) {
		if (rand.nextFloat() < NULL_PROBABILITY) {
			return null;
		}
		return createWrapperValue(contentType, attribute, rand);
	}

	private static <E extends TLObject> E createWrapperValue(Class<E> contentType, TLStructuredTypePart attribute,
			Random rand) {
		OptionModel<?> options = AttributeOperations.allOptions(SimpleEditContext.createContext(attribute));
		if (options.getOptionCount() == 0) {
			return null;
		}
		Object value;
		if (options instanceof ListOptionModel) {
			List<?> baseList = ((ListOptionModel<?>) options).getBaseModel();
			value = baseList.get(rand.nextInt(baseList.size()));
		} else if (options instanceof TreeOptionModel) {
			TLTreeModel<?> baseModel = ((TreeOptionModel<?>) options).getBaseModel();
			value = pseudoRandomNode(baseModel, rand);
		} else {
			value = options.iterator().next();
		}
		return contentType.cast(value);
	}

	private static <N> N pseudoRandomNode(TLTreeModel<N> baseModel, Random rand) {
		N parent = baseModel.getRoot();
		while (true) {
			List<? extends N> children = baseModel.getChildren(parent);
			if (children.isEmpty()) {
				return parent;
			}
			if (rand.nextFloat() < 0.3F) {
				return parent;
			}
			parent = children.get(rand.nextInt(children.size()));
		}
	}

	private static void fillBooleanAttributes(DemoTypesA demoA, Random random) {
		boolean booleanMandatory = random.nextBoolean();
		demoA.setBooleanMandatory(booleanMandatory);
		if (booleanMandatory) {
			// Do not violate demo constraint.
			demoA.setBoolean(random.nextBoolean());
		}
		demoA.setBooleanNullable(nextTriState(random));
		demoA.setBooleanRadio(random.nextBoolean());
		demoA.setBooleanRadioMandatory(random.nextBoolean());
		demoA.setBooleanRadioNullable(nextTriState(random));
		demoA.setBooleanSelect(random.nextBoolean());
		demoA.setBooleanSelectMandatory(random.nextBoolean());
		demoA.setBooleanSelectNullable(nextTriState(random));
	}

	/**
	 * Probability of null is 50%, false 25% and true 25%.
	 */
	private static Boolean nextTriState(Random random) {
		if (random.nextBoolean()) {
			return random.nextBoolean();
		}
		return null;
	}

	private static String getGeneratedRootType(String parentType) throws UnreachableAssertion {
		if (parentType.equals(Root.ROOT_TYPE)) {
			return BNotUnderA.B_NOT_UNDER_A_TYPE;
		} else if (parentType.equals(A.A_TYPE)) {
			return B.B_TYPE;
		} else if (parentType.equals(B.B_TYPE)) {
			return C.C_TYPE;
		} else if (parentType.equals(BNotUnderA.B_NOT_UNDER_A_TYPE)) {
			return A.A_TYPE;
		} else if (parentType.equals(C.C_TYPE)) {
			return B.B_TYPE;
		}
		throw new UnreachableAssertion("Unknown Type '" + parentType + "'!");
	}

	static HandlerResult showTime(DisplayContext context, StopWatch sw) {
		return MessageBox.confirm(context, MessageType.INFO, "Operation needed: " + sw.toString(),
				MessageBox.button(ButtonType.OK));
	}

	public static final class NameComparator implements Comparator<StructuredElement> {

		@Override
		public int compare(StructuredElement o1, StructuredElement o2) {
			String o1Name = o1.getName();
			String o2Name = o2.getName();
			try {
				int o1AsInt = Integer.parseInt(o1Name);
				try {
					return o1AsInt - Integer.parseInt(o2Name);
				} catch (NumberFormatException nex) {
					return 1;
				}
			} catch (NumberFormatException nex) {
				try {
					Integer.parseInt(o2Name);
					return -1;
				} catch (NumberFormatException nex2) {
					return o1Name.compareTo(o2Name);
				}
			}
		}
	}

	private static final class GeneratingDialog extends SimpleFormDialog {

		private static final String NUMBER_CHILDREN_FIELD = "numberChildren";
		private static final String ROOT_NAME_FIELD = "rootName";
		
		IntField _numberChildren;

		StringField _rootName;

		private final CommandModel _command;

		final StructuredElement _parent;

		public GeneratingDialog(ResPrefix resPrefix, DisplayDimension width, DisplayDimension height,
				StructuredElement parent) {
			super(I18NConstants.TYPE_GENERATOR_DIALOG, width, height);
			this._parent = parent;
			
			_command = new AbstractCommandModel() {
				@Override
				protected HandlerResult internalExecuteCommand(DisplayContext context) {
					FormContext formContext = getFormContext();
					if (!formContext.checkAll()) {
						return AbstractApplyCommandHandler.createErrorResult(formContext);
					}
					
					StopWatch sw = StopWatch.createStartedWatch();
					final boolean succeded = generate(
						_parent,
						_rootName.getAsString(),
						_numberChildren.getAsInt());
					sw.stop();
					if (succeded) {
						getDiscardClosure().executeCommand(context);
					}

					return showTime(context, sw);
				}
			};
			_command.setLabel(resPrefix.key("generate"));
		}

		@Override
		protected void fillButtons(List<CommandModel> buttons) {
			buttons.add(_command);
		}

		@Override
		protected void fillFormContext(FormContext context) {
			_rootName = FormFactory.newStringField(ROOT_NAME_FIELD, "Generated Root", false);
			_rootName.setLabel(I18NConstants.TYPE_GENERATOR_DIALOG.key(ROOT_NAME_FIELD));
			_rootName.setControlProvider(ValueBelowLabel.INSTANCE);
			
			_numberChildren = FormFactory.newIntField(NUMBER_CHILDREN_FIELD, NUMBER_OF_CHILDREN, false);
			_numberChildren
				.setLabel(I18NConstants.TYPE_GENERATOR_DIALOG.key(NUMBER_CHILDREN_FIELD));
			_numberChildren.setControlProvider(ValueBelowLabel.INSTANCE);
			_numberChildren.addConstraint(
				new RangeConstraint(1, Integer.MAX_VALUE)
					.setErrorKeyToSmall(I18NConstants.TYPE_GENERATOR_DIALOG.key("negative")));
			context.addMember(_rootName);
			context.addMember(_numberChildren);
		}

		@Override
		protected void createHeaderMembers(FormContext formContext) {
			// No header members.
		}

		private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
	    		"<div"
	    	+		" xmlns='" + HTMLConstants.XHTML_NS + "'"
	    	+  		" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
	    	+		" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
	    	+	">"
	        +		"<div class='mboxHeader'>"
	        +			"<t:text key='header'/>"
	        +		"</div>"
	        +		"<div class='mboxInput'>"
	        +			"<p:field name='" + ROOT_NAME_FIELD + "' />"
	        +			"<p:field name='" + NUMBER_CHILDREN_FIELD + "' />"
	        +		"</div>"
	        +	"</div>")
	    ;

		@Override
		protected FormTemplate getTemplate() {
			return new FormTemplate(I18NConstants.TYPE_GENERATOR_DIALOG, getControlProvider(), false, TEMPLATE);
		}

	}

}
