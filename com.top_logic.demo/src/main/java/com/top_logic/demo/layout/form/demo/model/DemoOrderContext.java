/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.beans.IntrospectionException;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.BeanAccessor;
import com.top_logic.layout.basic.GoogleSearchRenderer;
import com.top_logic.layout.basic.PlainTextRenderer;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.RegExpConstraint;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.format.FillFormat;
import com.top_logic.layout.form.format.StringTokenFormat;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.resources.NestedResourceView;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Utils;

/**
 * Custom {@link FormContext} that has {@link FormField}s to create or edit an
 * {@link DemoOrder} object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoOrderContext extends FormContext {

	private static final DemoAddress ADDRESS_1 = new DemoAddress("R¸sselsheimer Straﬂe", "22", "DE-60326", "Frankfurt am Main");
	private static final DemoAddress ADDRESS_2 = new DemoAddress("Dornhofstraﬂe", "18", "DE-65234", "Neu-Isenburg");
	private static final DemoAddress ADDRESS_3 = new DemoAddress("Bahnhofstraﬂe", "2b", "DE-32199", "Allerliebstlingen");

	private static final AddressLabelProvider ADDRESS_LABEL_PROVIDER =
		new AddressLabelProvider();

	/*package protected*/ final static class AddressLabelProvider implements LabelProvider {
		@Override
		public String getLabel(Object object) {
			DemoAddress address = (DemoAddress) object;

			return
				address.getStreet() + " " + address.getStreetNumber() + ", " +
				address.getZipCode() + " " + address.getCity();
		}
	}

	private static final String NEED_ENSURANCE_NAME = "needEnsurance";

	/** The name of the {@link #hasSeparateBillingAddress} member. */
	public static final String HAS_SEPARATE_BILLING_ADDRESS_NAME = "hasSeparateBillingAddress";

	/** The name of the {@link #carrier} member. */
	public static final String CARRIER_NAME = "carrier";

	/** The name of the {@link #purchaser} member. */
	public static final String PURCHASER_NAME = "purchaser";

	/** The name of the {@link #dialPrefix} member. */
	public static final String DIAL_PREFIX_NAME = "dialPrefix";

	/** The name of the {@link #directDialPrefix} member. */
	public static final String DIRECT_DIAL_PREFIX_NAME = "directDialPrefix";

	/** The name of the {@link #deliveryAddress} member. */
	public static final String DELIVERY_ADDRESS_NAME = "deliveryAddress";

	/** The name of the {@link #billingAddress} member. */
	public static final String BILLING_ADDRESS_NAME = "billingAddress";

	/** The name of the {@link #phoneNumber} member. */
	public static final String PHONE_NUMBER_NAME = "phoneNumber";

	/** The name of the {@link #isbn} member. */
	public static final String ISBN_NAME = "isbn";

	/** The name of the {@link #duns} member. */
	public static final String DUNS_NAME = "DUNS";

	/** The name of the {@link #options} member. */
	public static final String OPTIONS_NAME = "options";

	/** The name of the {@link #multiOptions} member. */
	public static final String MULTI_OPTIONS_NAME = "multiOptions";

	/** The name of the {@link #number} member. */
	public static final String NUMBER_NAME = "number";

	/** The name of the {@link #numbers} member. */
	public static final String NUMBERS_NAME = "numbers";

    /** List of {@link DemoCarrier} objects. */
	public static final ArrayList CARRIERS = new ArrayList();
	static {
		CARRIERS.add(null);

		List tmobileNumbers = Arrays.asList( new String[] {
		        "0133","0134", "0135", "0166"} );
		CARRIERS.add(new DemoCarrier("T-Mobile", tmobileNumbers));

        List vodafoneNumbers = Arrays.asList( new String[] {
		  "0172", "0173", "0185", "0184"} );
		CARRIERS.add(new DemoCarrier("Vodafone", vodafoneNumbers));

		ArrayList o2Numbers = new ArrayList();
		o2Numbers.add("0181");
		o2Numbers.add("0182");
		o2Numbers.add("0183");
		o2Numbers.add("0184");

		CARRIERS.add(new DemoCarrier("O2", o2Numbers));
	}

	public static final ArrayList DELIVERY_OPTIONS = new ArrayList();
	static {
		DELIVERY_OPTIONS.add("fast");
		DELIVERY_OPTIONS.add("save");
		DELIVERY_OPTIONS.add("secure");
		DELIVERY_OPTIONS.add("reliable");
		DELIVERY_OPTIONS.add("in order");
	}

    /** Nested {@link FormContext} for the "purchaser" part of the form. */
	public final DemoPersonContext purchaser =
		new DemoPersonContext(PURCHASER_NAME);

	public final SelectField carrier =
		FormFactory.newSelectField(CARRIER_NAME, CARRIERS);
	{
		carrier.setOptionLabelProvider(new LabelProvider() {
			@Override
			public String getLabel(Object object) {
				if (object == null) return "any";
				return DemoCarrierLabelProvider.INSTANCE.getLabel(object);
			}
		});

		carrier.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List selection = (List) newValue;
				if (selection.size() == 0) {
					dialPrefix.setOptions(Collections.EMPTY_LIST);
					return;
				}

				DemoCarrier theCarrier = (DemoCarrier) selection.get(0);

				Object currentlySelectedDialPrefix = dialPrefix.getSingleSelection();
				if (theCarrier == null) {
					dialPrefix.setOptions(allPrefixes);

					// Since "all" in carrier cause the dial prefix selection to
					// hold the superset of all dial prefix sets, the current
					// selection can be reestablished after setting the options
					// of the dial prefix field.
					if (currentlySelectedDialPrefix != null) {
						dialPrefix.setAsSingleSelection(currentlySelectedDialPrefix);
					}
				} else {
					dialPrefix.setOptions(theCarrier.getDialPrefixes());
				}
			}
		});
	}

	public final SelectField directDialPrefix;
	ArrayList allPrefixes = new ArrayList();
	{
		TreeSet buffer = new TreeSet();
		for (Iterator it = CARRIERS.iterator(); it.hasNext(); ) {
			DemoCarrier theCarrier = (DemoCarrier) it.next();
			if (theCarrier == null)
			    continue;

			buffer.addAll(theCarrier.getDialPrefixes());
		}
		allPrefixes.addAll(buffer);

		directDialPrefix = FormFactory.newSelectField(DIRECT_DIAL_PREFIX_NAME, allPrefixes);

		directDialPrefix.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				for (int n = 0, cnt = CARRIERS.size(); n < cnt; n++) {
					DemoCarrier theCarrier = (DemoCarrier) CARRIERS.get(n);
					if (theCarrier == null) continue;

					Object selectedPrefix = directDialPrefix.getSingleSelection();
					if (theCarrier.dialPrefixes.contains(selectedPrefix)) {
						// Fill carrier and prefix fields.
						carrier.setAsSingleSelection(theCarrier);
						dialPrefix.setAsSingleSelection(selectedPrefix);
						break;
					}
				}

			}
		});
	}

	public static final List LARGE_INTEGER_LIST;
	static {
		LARGE_INTEGER_LIST = new ArrayList();
		for (int n = 0; n < 2347; n++) {
			LARGE_INTEGER_LIST.add(Integer.valueOf(n));
		}
	}

	public final StringField isbn = FormFactory.newStringField(ISBN_NAME);
	{
		isbn.setTooltipCaption("Testing encoding in captions: <\"&'\\>");
		isbn.setTooltip("Testing encoding in tooltip: <\"&'\\>");
	}

	public final ComplexField duns = createDUNSField();

	public final SelectField options = FormFactory.newSelectField(OPTIONS_NAME, DELIVERY_OPTIONS);

	public final SelectField multiOptions = FormFactory.newSelectField(MULTI_OPTIONS_NAME, DELIVERY_OPTIONS, true, false);

	public static final Filter fixedNumbersFilter = new Filter() {
		@Override
		public boolean accept(Object anObject) {
			int value = ((Integer) anObject).intValue();
			return value % 13 == 2 || value % 42 == 3;
		}
	};

	public final SelectField number = FormFactory.newSelectField(NUMBER_NAME, LARGE_INTEGER_LIST, false, false);
	{
		number.setFixedOptions(fixedNumbersFilter);
	}

	public final SelectField numbers = FormFactory.newSelectField(NUMBERS_NAME, LARGE_INTEGER_LIST, true, false);
	{
		numbers.setFixedOptions(fixedNumbersFilter);
		numbers.addConstraint(new SelectionSizeConstraint(0, 5));
	}

	public final SelectField dialPrefix = FormFactory.newSelectField(DIAL_PREFIX_NAME, Collections.EMPTY_LIST);

	public final ComplexField orderDate = FormFactory.newDateField("orderDate", null, false);

	{
		// Allow to enter abbreviated dates and have them expanded reasonably.
		orderDate.setParser(HTMLFormatter.getInstance().getShortDateFormat());
	}

	public final ComplexField colorChooser = FormFactory.newComplexField("colorChooser", ColorFormat.INSTANCE);

	public final StringField phoneNumber = FormFactory.newStringField(PHONE_NUMBER_NAME);

	public final DemoAddressContext billingAddress =
		new DemoAddressContext(BILLING_ADDRESS_NAME);

	public final SelectField lastAddresses;
	{
		// Create an accessor that fetches values from an option object given a
		// string key. This key is the column name of the object table in which
		// the option objects of the currently initialized select field will be
		// displayed (it is shown in a selection table view).
		final BeanAccessor addressAccessor;
		try {
			// Note: This accessor only depends on the type of option objects in
			// the select field. In a real application, this accessor should be
			// a singleton (at least not being created per field).
			addressAccessor = new BeanAccessor(DemoAddress.class);
		} catch (IntrospectionException e) {
			throw new AssertionError(e);
		}

		final String fieldName = "lastAddresses";
		lastAddresses = FormFactory.newSelectField(fieldName, Arrays.asList(new Object[] {
			ADDRESS_1,
			new DemoAddress("Esslenstrasse", "3", "CH-8280", "Kreuzlingen"),
			new DemoAddress("Fritz-Walter-Straﬂe", "17", "DE-73145", "Lustiglingen"),
			new DemoAddress("Hauptstraﬂe", "17a", "DE-43211", "Unbekantingen"),
			ADDRESS_2,
			ADDRESS_3,
			new DemoAddress("Marktplatz", "1", "DE-63229", "Zuvielhausen")
		}), true, false);
		lastAddresses.setTableConfigurationProvider(new TableConfigurationProvider() {

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				defaultColumn.setRenderer(PlainTextRenderer.INSTANCE);
				defaultColumn.setAccessor(addressAccessor);
			}

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setResPrefix(new NestedResourceView(getResources(), fieldName));
				table.declareColumn("street").setRenderer(new GoogleSearchRenderer(DefaultLabelProvider.INSTANCE));
			}
		});

		// Annotate the label provider that translates the option values into
		// plain text strings for display in the popup selection dialog.
		lastAddresses.setOptionLabelProvider(ADDRESS_LABEL_PROVIDER);
		lastAddresses.setCustomOrder(true);
		List addressSelection = new ArrayList(lastAddresses.getOptions());
		addressSelection.remove(ADDRESS_1);
		addressSelection.remove(ADDRESS_2);

		// Custom initialization: By default, select all options.
		lastAddresses.initializeField(addressSelection);

		// Fix address 1, 2 and 3. Note: Filter must be installed, after setting
		// the value, because the field otherwise will reject setting fixed
		// options.
		lastAddresses.setFixedOptions(
			new SetFilter(new HashSet(Arrays.asList(new Object[] {ADDRESS_1, ADDRESS_2, ADDRESS_3}))));
	}

	public final BooleanField hasSeparateBillingAddress =
		FormFactory.newBooleanField(HAS_SEPARATE_BILLING_ADDRESS_NAME);

	public final DemoAddressContext deliveryAddress =
		new DemoAddressContext(DELIVERY_ADDRESS_NAME);

	{
		/**
		 * Set a dependency that makes the {@link #deliveryAddress} visible, iff
		 * the {@link #hasSeparateBillingAddress} field is checked.
		 */
		hasSeparateBillingAddress.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				billingAddress.setVisible(((Boolean) newValue).booleanValue());
			}
		});

		hasSeparateBillingAddress.initializeField(Boolean.FALSE);
	}

	public final BooleanField needEnsurance =
		FormFactory.newBooleanField(NEED_ENSURANCE_NAME, null, false, false, null);

	{
		needEnsurance.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (Utils.isTrue((Boolean) newValue)) {
					field.setCssClasses(null);
				} else {
					field.setCssClasses("problematic");
				}
			}
		});
	}

	private ComplexField createDUNSField(){
		Format fillFormat = new FillFormat(9,StringServices.START_POSITION_TAIL,'0');
		Format tokenFormat = new StringTokenFormat(fillFormat, ", ", null, true);
		ComplexField complexField = FormFactory.newComplexField(DUNS_NAME, tokenFormat);

		return complexField;
	}

    SelectField consumerSatisfaction =
    	FormFactory.newSelectField("satisfaction", Arrays.asList(new String[] {
		"keine Angabe", "gut", "befriedigend", "mangelhaft" }));

    StringField evaluation = FormFactory.newStringField("evaluation");
    StringField evaluationPreview = FormFactory.newStringField("evaluationPreview", true);
    {
    	// Copy all values from evaluation field to evaluationDisplay field.
    	evaluation.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				evaluationPreview.setValue(newValue);
			}
    	});
    }

	public DemoOrderContext(String name) {
		this(name, ResPrefix.legacyClass(DemoOrderContext.class));
	}

	public DemoOrderContext(String name, ResPrefix i18nPrefix) {
		super(name, i18nPrefix);

		isbn.setExampleValue("ISBN 3-89721-264-1");

		addMembers(new FormMember[] {
            purchaser,  // hint: nested FormContext
            billingAddress,
            lastAddresses,
            orderDate,
            colorChooser,
            carrier,
            dialPrefix,
            directDialPrefix,
            phoneNumber,
            hasSeparateBillingAddress,
            deliveryAddress,
            needEnsurance,
            consumerSatisfaction,
            evaluation,
            evaluationPreview,
            isbn,
            duns,
            options,
            multiOptions,
            number,
            numbers
        });

		isbn.addConstraint(new RegExpConstraint("^ISBN\\s(?=[-0-9xX ]{13}$)(?:[0-9]+[- ]){3}[0-9]*[xX0-9]$",i18nPrefix.key(".isbn.error")));
		duns.addWarningConstraint(new Constraint() {
            @Override
			public boolean check(Object aValue) throws CheckException {
                if (StringServices.isEmpty(aValue)) {
                    throw new CheckException("Dies ist nur eine Warnung. Sie wirkt sich nicht auf das Speichern-Kommando aus.<br/>Ein Feld kann auch mehrere Warnungen anzeigen (im Gegensatz zu nur einer Fehlermeldung).");
                }
                return true;
            }
        });
		duns.addWarningConstraint(new Constraint() {
		    @Override
			public boolean check(Object aValue) throws CheckException {
		        if (StringServices.isEmpty(aValue)) {
		            throw new CheckException("Der Werte sollte nicht leer sein.");
		        }
		        else {
		            throw new CheckException("Danke f¸r Ihre Eingabe.");
		        }
		    }
		});
	}

    public void loadWith(DemoOrder order) {
    	purchaser.loadWith(order.getPurchaser());
    	billingAddress.loadWith(order.getBillingAddress());
    	deliveryAddress.loadWith(order.getDeliveryAddress());
    }

    public void saveIn(DemoOrder order) {
    	purchaser.saveIn(order.getPurchaser());
    	billingAddress.saveIn(order.getBillingAddress());
    	deliveryAddress.saveIn(order.getDeliveryAddress());
    }

}
