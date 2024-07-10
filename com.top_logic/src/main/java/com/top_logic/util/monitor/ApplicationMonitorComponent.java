/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import static com.top_logic.util.monitor.I18NConstants.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.version.Version;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.AbstractStartStopListener;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.license.LicenseTool;
import com.top_logic.util.license.TLLicense;
import com.top_logic.util.monitor.MonitorMessage.Status;

/**
 * Provides information of the running application.
 */
public class ApplicationMonitorComponent extends FormComponent {

	/**
	 * The name of the {@link FormField} for licence update.
	 */
	public static final String FIELD_LICENCE_UPDATE = "installLicence";

	/**
	 * The name of the {@link FormGroup} for system status
	 */
	public static final String GROUP_STATUS = "groupStatus";

	/**
	 * The name of the {@link FormField} for system status
	 */
	public static final String FIELD_STATUS = "fieldStatus";

	/**
	 * The name of the {@link FormGroup} for group of licence
	 */
	public static final String GROUP_LICENCE = "groupLicence";

	/**
	 * The name of the {@link FormField} for licence field
	 */
	public static final String FIELD_LICENCE = "fieldLicence";

	/**
	 * The name of the {@link FormGroup} for group system
	 */
	public static final String GROUP_SYSTEM = "groupSystem";

	/**
	 * The name of the {@link FormField} for Field system
	 */
	public static final String FIELD_SYSTEM = "fieldSystem";

	/**
	 * The name of the {@link FormGroup} for group java
	 */
	public static final String GROUP_JAVA = "groupJava";

	/**
	 * The name of the {@link FormField} for field java
	 */
	public static final String FIELD_JAVA = "fieldJava";

	/**
	 * The name of the {@link FormGroup} for group monitors
	 */
	public static final String GROUP_MONITORS = "groupMonitors";

	/**
	 * The name of the {@link FormField} for field monitors
	 */
	public static final String FIELD_MONITORS = "fieldMonitors";

	/**
	 * The name of the {@link FormGroup} for group memory
	 */
	public static final String GROUP_MEMORY = "groupMemory";

	/**
	 * The name of the {@link FormField} for field memory
	 */
	public static final String FIELD_MEMORY = "fieldMemory";

	/**
	 * The name of the monitor.xml
	 */
	private static final String TABLE_CONFIG_PROPERTIES = "fieldProperties";

	/**
	 * Configuration of the {@link ApplicationMonitorComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Model is not used to create {@link FormContext}. Therefore the component should also be
		 * displayed without model
		 */
		@BooleanDefault(true)
		@Override
		boolean getDisplayWithoutModel();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			/* SimpleBoundCommandGroup.WRITE is the command group used in AccessControlTag to refuse
			 * access if the user has not the right. For this reason the command group must be
			 * registered. */
			additionalGroups.add(SimpleBoundCommandGroup.WRITE);
			additionalGroups.add(SimpleBoundCommandGroup.EXPORT);
		}

	}

	/**
	 * Creates a {@link ApplicationMonitorComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ApplicationMonitorComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	/**
	 * Override the method createFormContext from {@link FormGroup}
	 */
	@Override
	public FormContext createFormContext() {
		final FormContext context = new FormContext(this);

		createMonitorsGroup(context);
		createMemoryGroup(context);
		createSystemGroup(context);
		createLicenceGroup(context);
		createJavaGroup(context);
		createStatusGroup(context);

		return context;
	}

	/**
	 * Create and fill the {@link FormGroup} containing {@link FormMember}s representing the
	 * application's java information.
	 * 
	 * @param context
	 *        the {@link FormContext} to create the {@link FormGroup} in java group
	 */
	private void createJavaGroup(final FormContext context) {
		final FormGroup group = new FormGroup(GROUP_JAVA, getResPrefix());
		context.addMember(group);

		// lese die TabellenKonfiguration definiert in der Datei layout.xml (monitor.xml)
		final TableConfiguration config = getTableConfiguration(TABLE_CONFIG_PROPERTIES);

		// definiere die Spalten, welche angezeigt werden sollen
		final List<String> columns = Arrays.asList(
			PropertyAccessor.PROPERTY_NAME,
			PropertyAccessor.PROPERTY_VALUE);

		// erstelle das Daten-Modell, welches in der Tabelle angezeigt wird
		final List<Property> rows = getJavaInfo();

		// erstelle das Daten-Model und die Felder
		final TableModel tableModel = new ObjectTableModel(columns, config, rows);
		group.addMember(FormFactory.newTableField(FIELD_JAVA, tableModel));
	}

	/**
	 * Return information about the application's java virtual machine.
	 * 
	 * @return a (possibly empty) {@link List} of {@link Property}s
	 */
	private List<Property> getJavaInfo() {
		final List<Property> result = new ArrayList<>();

		result.add(property(JAVA_VERSION, System.getProperty("java.vm.version") + " (" + System.getProperty("java.vm.name") + ')'));
		result.add(property(JAVA_RUNTIME, System.getProperty("java.runtime.name")));
		result.add(property(JAVA_INFO, System.getProperty("java.vm.info")));
		result.add(property(JAVA_VENDOR, System.getProperty("java.vm.vendor")));

		return result;
	}

	/**
	 * Create and fill the {@link FormGroup} containing {@link FormMember}s representing the
	 * application's memory group information.
	 * 
	 * @param context
	 *        the {@link FormContext} to create the {@link FormGroup} in memory group
	 */
	private void createMemoryGroup(FormContext context) {
		final FormGroup group = new FormGroup(GROUP_MEMORY, getResPrefix());
		context.addMember(group);

		final List<String> columns = Arrays.asList(
			PropertyAccessor.PROPERTY_NAME,
			PropertyAccessor.PROPERTY_VALUE);

		final TableConfiguration config = getTableConfiguration(TABLE_CONFIG_PROPERTIES);
		final List<Property> rows = getMemoryInfo(); // Model

		final TableModel tableModel = new ObjectTableModel(columns, config, rows);
		group.addMember(FormFactory.newTableField(FIELD_MEMORY, tableModel));

	}

	/**
	 * Return information about the application's memory info.
	 * 
	 * @return a (possibly empty) {@link List} of {@link Property}s
	 */
	private List<Property> getMemoryInfo() {
		final List<Property> infos = new ArrayList<>();

		final NumberFormat fmt = NumberFormat.getIntegerInstance(TLContext.getLocale());
		fmt.setGroupingUsed(true);

		final Runtime sys = Runtime.getRuntime();

		final double MB = 1d / (1024 * 1024);
		infos.add(property(MEMORY_MAX, fmt.format(sys.maxMemory() * MB) + " MB"));
		infos.add(property(MEMORY_TOTAL, fmt.format(sys.totalMemory() * MB) + " MB"));
		infos.add(property(MEMORY_AVAILABLE, fmt.format((sys.maxMemory() - sys.totalMemory()) * MB) + " MB"));
		infos.add(property(MEMORY_USED, fmt.format((sys.totalMemory() - sys.freeMemory()) * MB) + " MB"));
		infos.add(property(MEMORY_FREE, fmt.format(sys.freeMemory() * MB) + " MB"));

		return infos;
	}

	/**
	 * Create and fill the {@link FormGroup} containing {@link FormMember}s representing the
	 * application's monitors group information.
	 * 
	 * @param context
	 *        the {@link FormContext} to create the {@link FormGroup} in monitors group
	 */
	private void createMonitorsGroup(final FormContext context) {
		final FormGroup group = new FormGroup(GROUP_MONITORS, getResPrefix());
		context.addMember(group);

		// read the table configuration defined in layout.xml
		final TableConfiguration config = getTableConfiguration(FIELD_MONITORS);

		// define column which should be displayed
		final List<String> columns = Arrays.asList(
			MonitorMessageAccessor.PROPERTY_ICON,
			MonitorMessageAccessor.PROPERTY_MONITOR,
			MonitorMessageAccessor.PROPERTY_MESSAGE);

		// initialize/build the data model to be displayed in the table
		final ApplicationMonitor monitor = ApplicationMonitor.getInstance();
		final MonitorResult appState = monitor.checkApplication();
		final List<MonitorMessage> rows = appState.getMessages();

		// build the table model and field
		final TableModel tableModel = new ObjectTableModel(columns, config, rows);
		group.addMember(FormFactory.newTableField(FIELD_MONITORS, tableModel));
	}

	/**
	 * Create and fill the {@link FormGroup} containing {@link FormMember}s representing the
	 * application's status group information.
	 * 
	 * @param context
	 *        the {@link FormContext} to create the {@link FormGroup} in status group
	 */
	private void createStatusGroup(final FormContext context) {
		final FormGroup group = new FormGroup(GROUP_STATUS, getResPrefix());
		context.addMember(group);

		// read the table configuration defined in layout.xml (same as for monitors table)
		final TableConfiguration config = getTableConfiguration(FIELD_MONITORS);

		// define column which should be displayed
		final List<String> columns = Arrays.asList(
			MonitorMessageAccessor.PROPERTY_ICON,
			MonitorMessageAccessor.PROPERTY_MONITOR,
			MonitorMessageAccessor.PROPERTY_MESSAGE);

		// erstelle das Daten-Modell, welches in der Tabelle angezeigt wird
		final List<MonitorMessage> rows = getStatusInfo();

		// build the table model and field
		final TableModel tableModel = new ObjectTableModel(columns, config, rows);
		group.addMember(FormFactory.newTableField(FIELD_STATUS, tableModel));
	}

	/**
	 * Create and fill the {@link FormGroup} containing {@link FormMember}s representing the
	 * application's status information.
	 */
	private List<MonitorMessage> getStatusInfo() {
		final ApplicationMonitor monitor = ApplicationMonitor.getInstance();
		final MonitorResult result = monitor.checkApplication();
		final List<MonitorMessage> messages = result.getMessages();

		int errorCount = 0;
		int fatalCount = 0;

		for (final MonitorMessage message : messages) {
			if (MonitorMessage.Status.ERROR.equals(message.getType())) {
				errorCount++;
			}
			else if (MonitorMessage.Status.FATAL.equals(message.getType())) {
				fatalCount++;
			}
			else {
				// ignore infos
			}
		}

		Status type;
		final String state;
		final String message;

		if (result.isOK()) {
			type = MonitorMessage.Status.INFO;
			state = resource(STATE_OK);
			message = resource(STATE_MESSAGE_OK);
		}
		else if (result.isAlive()) {
			type = MonitorMessage.Status.ERROR;
			state = resource(STATE_ALIVE);

			if (errorCount == 1) {
				message = resource(STATE_MESSAGE_ALIVE1, errorCount);
			}
			else {
				message = resource(STATE_MESSAGE_ALIVE, errorCount);
			}
		}
		else {
			type = MonitorMessage.Status.FATAL;
			state = resource(STATE_DOWN);

			if (fatalCount == 1) {
				message = resource(STATE_MESSAGE_FATAL1, fatalCount);
			}
			else {
				message = resource(STATE_MESSAGE_FATAL, fatalCount);
			}
		}

		return Collections.singletonList(new MonitorMessage(type, message, state));
	}

	/**
	 * Create and fill the {@link FormGroup} containing {@link FormMember}s representing the
	 * application's licence group information.
	 * 
	 * @param context
	 *        the {@link FormContext} to create the {@link FormGroup} in licence group
	 */
	private void createLicenceGroup(final FormContext context) {
		final FormGroup group = new FormGroup(GROUP_LICENCE, I18NConstants.PERSON);
		context.addMember(group);

		// lese die TabellenKonfiguration definiert in der Datei layout.xml (monitor.xml)
		final TableConfiguration config = getTableConfiguration(TABLE_CONFIG_PROPERTIES);

		// definiere die Spalten, welche angezeigt werden sollen
		final List<String> columns = Arrays.asList(
			PropertyAccessor.PROPERTY_NAME,
			PropertyAccessor.PROPERTY_VALUE);

		// erstelle das Daten-Modell, welches in der Tabelle angezeigt wird
		final List<Property> rows = getLicenceInfo();

		// erstelle das Daten-Model und die Felder
		final TableModel tableModel = new ObjectTableModel(columns, config, rows);
		group.addMember(FormFactory.newTableField(FIELD_LICENCE, tableModel));
	}

	/**
	 * Return information about the application's licence info.
	 * 
	 * @return a (possibly empty) {@link List} of {@link Property}s
	 */
	private List<Property> getLicenceInfo() {
		final List<Property> result = new ArrayList<>();

		final TLLicense license = LicenseTool.getInstance().getLicense();
		final Formatter format = HTMLFormatter.getInstance();

		result.add(property(PRODUCT_TYPE, LicenseTool.productType()));
		result.add(property(LICENCE_STATE, resource(LicenseTool.licenseState(license))));

		result.add(property(LICENSE_KEY, license.getLicenseKey()));
		result.add(property(LICENSE_VALIDITY,
			license.getValidity() == null ? "--" : format.formatDate(license.getValidity())));
		result.add(property(LICENSE_EXPIRE_DATE,
			license.getExpireDate() == null ? resource(NO_EXPIRY) : format.formatDate(license.getExpireDate())));

		return result;
	}

	String resource(ResKey key, Object... args) {
		Object[] stringArgs = new Object[args.length];
		final Formatter format = HTMLFormatter.getInstance();
		for (int n = 0, cnt = args.length; n < cnt; n++) {
			if (args[n] instanceof Integer) {
				stringArgs[n] = format.formatInt((Integer) args[n]);
			} else {
				stringArgs[n] = args[n];
			}
		}
		return Resources.getInstance().getMessage(key, stringArgs);
	}

	static String resource(ResKey key, String... args) {
		return Resources.getInstance().getMessage(key, (Object[]) args);
	}

	static String resource(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	/**
	 * Create and fill the {@link FormGroup} containing {@link FormMember}s representing the
	 * application's system group information.
	 * 
	 * @param context
	 *        the {@link FormContext} to create the {@link FormGroup} in system group
	 */
	private void createSystemGroup(final FormContext context) {
		final FormGroup group = new FormGroup(GROUP_SYSTEM, getResPrefix());
		context.addMember(group);

		// lese die TabellenKonfiguration definiert in der Datei layout.xml (monitor.xml)
		final TableConfiguration config = getTableConfiguration(TABLE_CONFIG_PROPERTIES);

		// definiere die Spalten, welche angezeigt werden sollen
		final List<String> columns = Arrays.asList(
			PropertyAccessor.PROPERTY_NAME,
			PropertyAccessor.PROPERTY_VALUE);

		// erstelle das Daten-Modell, welches in der Tabelle angezeigt wird
		final List<Property> rows = getSystemInfo();

		// erstelle das Daten-Model und die Felder
		final TableModel tableModel = new ObjectTableModel(columns, config, rows);
		group.addMember(FormFactory.newTableField(FIELD_SYSTEM, tableModel));

	}

	/**
	 * Return information about the application's system info.
	 * 
	 * @return a (possibly empty) {@link List} of {@link Property}s
	 */
	private List<Property> getSystemInfo() {
		final List<Property> result = new ArrayList<>();

		result.add(property(SYSTEM_APPLICATION_VERSION, Version
		.getApplicationVersion().toString()));
		Date now = new Date();
		result.add(property(SYSTEM_TIME, HTMLFormatter.getInstance()
		.formatDateTime(now)));
		if (TimeZones.Module.INSTANCE.isActive()) {
			MetaLabelProvider labels = MetaLabelProvider.INSTANCE;
			result.add(property(SYSTEM_TIME_ZONE, labels.getLabel(TimeZones.systemTimeZone())));
			result.add(property(USER_TIME_ZONE, labels.getLabel(TimeZones.defaultUserTimeZone())));
		}

		Date startUpDate = AbstractStartStopListener.startUpDate();
		result.add(property(SYSTEM_STARTUP,
			Resources.getInstance().getString(SYSTEM_STARTUP__STARTUP__UPTIME.fill(HTMLFormatter.getInstance()
				.formatDateTime(startUpDate),
				StopWatch.toStringMillis(now.getTime() - startUpDate.getTime(), TimeUnit.SECONDS)))));

		try {
			final InetAddress host = InetAddress.getLocalHost();
			result.add(property(SYSTEM_SERVER, host.getHostName() + " (IP:"
			+ host.getHostAddress() + ")"));
		} catch (UnknownHostException ex) {
			Logger.info("unknown host", ex, this);
		}

		return result;
	}

	/**
	 * A {@link ReadOnlyAccessor} implementation for accessing properties of {@link MonitorMessage}
	 * es.
	 * 
	 * @author <a href=mailto:mri@top-logic.com>mri</a>
	 */
	public static class MonitorMessageAccessor extends ReadOnlyAccessor<MonitorMessage> {

		private static final String PROPERTY_ICON = "icon";

		private static final String PROPERTY_MONITOR = "monitor";

		private static final String PROPERTY_MESSAGE = "message";

		@Override
		public Object getValue(final MonitorMessage message, final String property) {
			if (PROPERTY_ICON.equals(property)) {
				return message.getType();
			}
			else if (PROPERTY_MONITOR.equals(property)) {
				final Object monitor = message.getComponent();

				// the instance is a String for the overall application state
				// and contains the translated status message.
				if (monitor instanceof String) {
					return monitor;
				}

				// the instance is a MonitorComponent for component
				// based messages.
				else if (monitor instanceof MonitorComponent) {
					return ((MonitorComponent) monitor).getName();
				}

				// we don't know what kind of component that is,
				// so just print the name of the class.
				else {
					return monitor.getClass().getName();
				}
			}
			else if (PROPERTY_MESSAGE.equals(property)) {
				return message.getMessage();
			}
			else {
				throw new IllegalArgumentException("unsupported property: " + property);
			}
		}
	}

	/**
	 * A {@link ResourceProvider} implementation resolving icons for
	 * {@link MonitorMessage#getType() MonitorMessage types}.
	 * 
	 * @author <a href=mailto:mri@top-logic.com>mri</a>
	 */
	public static class StatusIconResourceProvider extends DefaultResourceProvider {

		@Override
		public ThemeImage getImage(final Object type, final Flavor flavor) {
			final ThemeImage image;

			if (MonitorMessage.Status.INFO.equals(type)) {
				image = Icons.GREEN;
			}
			else if (MonitorMessage.Status.ERROR.equals(type)) {
				image = Icons.YELLOW;
			}
			else if (MonitorMessage.Status.FATAL.equals(type)) {
				image = Icons.RED_CROSS;
			}
			else {
				image = null;
			}

			return image;
		}

		@Override
		public String getLabel(final Object object) {
			return null;
		}
	}

	/**
	 * A {@link ReadOnlyAccessor} implementation for accessing properties of {@link Property}
	 * instances.
	 * 
	 * @author <a href=mailto:mri@top-logic.com>mri</a>
	 */
	public static class PropertyAccessor extends ReadOnlyAccessor<Property> {

		private static final String PROPERTY_NAME = "name";

		private static final String PROPERTY_VALUE = "value";

		@Override
		public Object getValue(final Property value, final String property) {
			if (PROPERTY_NAME.equals(property)) {
				return resource(value.getKey());
			}
			else if (PROPERTY_VALUE.equals(property)) {
				return value.getValue();
			}
			else {
				throw new IllegalArgumentException("unsupported property: " + property);
			}
		}
	}

	private static Property property(ResKey key, int value) {
		return property(key, HTMLFormatter.getInstance().formatInt(value));
	}

	private static Property property(ResKey key, String value) {
		return new Property(key, value);
	}

	/**
	 * A simple data container for properties consisting of name/value pairs.
	 * 
	 * @author <a href=mailto:mri@top-logic.com>mri</a>
	 */
	private static class Property {

		/**
		 * @see #getKey()
		 */
		private final ResKey _key;

		/**
		 * @see #getValue()
		 */
		private final Object _value;

		/**
		 * Create a new {@link Property}.
		 * 
		 * @param key
		 *        the property resource key
		 * @param value
		 *        the property value or {@code null}
		 */
		public Property(final ResKey key, final Object value) {
			_key = key;
			_value = value;
		}

		/**
		 * the property resource key
		 */
		public ResKey getKey() {
			return _key;
		}

		/**
		 * the property value or {@code null}
		 */
		public Object getValue() {
			return _value;
		}
	}

	/**
	 * An {@link AbstractCommandHandler} which resets the {@link FormContext} in order to display
	 * the most recent information.
	 * 
	 * @author <a href=mailto:mri@top-logic.com>mri</a>
	 */
	public static class RefreshApplicationMonitorCommand extends AbstractCommandHandler {

		/**
		 * Create a new {@link RefreshApplicationMonitorCommand}.
		 * 
		 * @param context
		 *        the {@link InstantiationContext} to create the command in
		 * @param config
		 *        the {@link com.top_logic.tool.boundsec.CommandHandler.Config} to initialize the
		 *        new command with
		 */
		public RefreshApplicationMonitorCommand(final InstantiationContext context, final Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(final DisplayContext context, final LayoutComponent component,
				Object model, final Map<String, Object> args) {

			if (component instanceof FormComponent) {
				final FormComponent formComponent = (FormComponent) component;

				if (formComponent.hasFormContext()) {
					formComponent.removeFormContext();
					formComponent.invalidate();
				}
			}

			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
