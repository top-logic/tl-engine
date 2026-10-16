---
description: Read before defining an application primitive type (<datatype kind="Custom">) whose display and input notation depends on the object owning the value (a unit, a numbering style, a format chosen by a project or container) - value class, StorageMapping, a PrimitiveStorage subclass set on the type with <storage-algorithm>, a TLAttributeAnnotation with a TL-Script context expression, a LabelProvider registered with kind="CODE" in LabelProviderService, and an <input-control> (ReactFieldControlProvider, ReactTextInputControl.applyRawClientValue). Also covers changing an existing attribute to such a type.
order: 10
---

# A primitive type displayed in the notation of its context

## The problem

An attribute holds a simple value that the database stores as a plain number or string, but users read and enter it in a domain notation - and the notation is not a property of the value alone, but of the context of the object owning it: the project, the mandator, the container it belongs to. A temperature shown in the unit of the site its sensor belongs to; a group address written `8/0/9`, `8/9` or `16393` depending on the project; an amount shown in the currency of its contract.

The requirements:

- The database keeps the plain value. Queries and sorting on the raw value keep working, no schema or data change is needed.
- The presentation is defined once, on the type. Every attribute of the type and every display - form field, read-only display, table cell, search, filter and export text, label - shows the value the same way, without per-view configuration.
- Where the notation comes from is declared in the model, per attribute, not hard-coded in Java.
- Input is read in the notation of the context, and invalid input is rejected with a message.

The running example of this article is the temperature demo of `com.top_logic.demo.react` (Demos → Temperatures): a `DemoSite` has a `temperatureUnit` (°C, °F or K) and composes `DemoSensor`s, whose `minTemperature` and `maxTemperature` are stored in degrees Celsius and displayed and entered in the unit of their site. The full code is in

- `com.top_logic.demo.react/src/main/java/com/top_logic/demo/react/temperature/` (Java),
- `com.top_logic.demo.react/src/main/webapp/WEB-INF/model/demo.temperature.model.xml` (model),
- `com.top_logic.demo.react/src/main/webapp/WEB-INF/views/demo/temperature-demo.view.xml` and `create-sensor.view.xml` (views),
- `com.top_logic.demo.react/src/main/webapp/WEB-INF/conf/demoReactConf.config.xml` (label provider, access).

## Why the obvious approaches fail

- **A datatype with a format or a `StorageMapping` alone** sees only the value. `StorageMapping.getBusinessObject(Object)` gets the stored value without its owner, `LabelProvider.getLabel(Object)` gets the value without its owner, and the read-only display paths - `FieldControlService.createDisplayControl(…)` for a field that is not edited, `CellRenderer.render(value)` for a table cell - get the value (and at most the attribute), never the row object. A notation that depends on the owner cannot be applied there.
- **An input control alone** (a `ReactFieldControlProvider` reading `AttributeFieldModel.getObject()`) covers forms and editable cells only. Tables, labels and search texts keep showing the raw value, and the display becomes inconsistent.

## The approach: attach the context when the value is read

The one place that knows the owner of every value is the attribute storage: `StorageImplementation.getAttributeValue(TLObject, TLStructuredTypePart)` is called with the object. The storage attaches the context there, so the value handed out is self-describing, and every value-only display path - label provider, cell renderer, display control, the React state serializer - can render it without knowing the owner. Six pieces make this work.

### 1. The value class

The value class wraps the raw value together with the notation derived from the context. Equality, hash code and ordering use the raw value only, so that two values differing only in their notation are the same value - a form is not dirty because a value was re-read in another notation, and sorting by the column sorts by the raw value. `toString()` returns the display text: the React state serializer writes an object it does not know as its `toString()`, so a text field showing the value needs no further conversion.

```java
public final class Temperature implements Comparable<Temperature> {
	private final double _celsius;          // the raw value, as stored
	private final TemperatureUnit _unit;    // the notation, attached from the context

	public static Temperature ofCelsius(double celsius) { … }

	public Temperature withUnit(TemperatureUnit unit) {
		return unit == _unit ? this : new Temperature(_celsius, unit);
	}

	public String format() { return _unit.format(_celsius); }   // e.g. "68.0 °F"

	public static Temperature parse(String text, TemperatureUnit unit) { … }

	@Override public int compareTo(Temperature other) { return Double.compare(_celsius, other._celsius); }
	@Override public boolean equals(Object obj) { … Double.compare(_celsius, other._celsius) == 0; }
	@Override public int hashCode() { return Double.hashCode(_celsius); }
	@Override public String toString() { return format(); }
}
```

The notation itself is a plain Java enum (`TemperatureUnit`) with conversion, formatting and parsing. Formatting is locale-independent, so that every text the field shows can be read back.

### 2. The datatype and its `StorageMapping`

The model declares the type as a custom datatype with the database type of the raw value and a `StorageMapping` converting between the stored value and the value class. The mapping knows no context: what it reads gets a default notation, which the storage replaces (step 3).

```xml
<datatype name="Temperature"
	db_type="double"
	kind="Custom"
>
	<annotations>…</annotations>
	<temperature-mapping/>
</datatype>
```

`TemperatureMapping` extends `AbstractConfiguredStorageMapping`; its `Config` has `@TagName("temperature-mapping")`, which is how the datatype names it. Besides the value class, `getStorageObject(…)` and `isCompatible(…)` accept the raw type (any `Number`). A script, an import or a migration can then keep writing plain numbers, and the seed data of the demo (`WEB-INF/data/Z06-temperature-demo.objects.xml`) gives temperatures as plain numbers: without a `@Format` on the value class, the object importer parses a value of a custom datatype by its database type and hands it to the mapping.

```java
@Override
public Temperature getBusinessObject(Object storageObject) {
	return storageObject == null ? null : Temperature.ofCelsius(((Number) storageObject).doubleValue());
}

@Override
public Object getStorageObject(Object businessObject) {
	if (businessObject == null) return null;
	if (businessObject instanceof Temperature temperature) return Double.valueOf(temperature.getCelsius());
	return Double.valueOf(((Number) businessObject).doubleValue());
}

@Override
public boolean isCompatible(Object businessObject) {
	return businessObject == null || businessObject instanceof Temperature || businessObject instanceof Number;
}
```

### 3. The storage, set on the type

`TLStorage` (`<storage-algorithm>`) is an attribute annotation and a type annotation at the same time. For a property that declares no storage, `StorageImplementationFactory` uses the storage annotation of the property's type, so a storage set on the datatype applies to every attribute of that type:

```xml
<datatype name="Temperature" db_type="double" kind="Custom">
	<annotations>
		<storage-algorithm>
			<temperature-storage/>
		</storage-algorithm>
		…
	</annotations>
	<temperature-mapping/>
</datatype>
```

The storage extends `PrimitiveStorage` - storing stays exactly as for any primitive value, through the mapping of the type - and overrides reading. Its `Config` redeclares the tag name:

```java
public class TemperatureStorage<C extends TemperatureStorage.Config<?>> extends PrimitiveStorage<C> {

	@TagName("temperature-storage")
	public interface Config<I extends TemperatureStorage<?>> extends PrimitiveStorage.Config<I> {
		// No additional options.
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		Object value = super.getAttributeValue(object, attribute);
		if (value instanceof Temperature temperature) {
			return temperature.withUnit(getUnit(object));
		}
		return value;
	}

	public TemperatureUnit getUnit(TLObject object) { … }   // see step 4
}
```

A storage instance is created per attribute, so it can hold attribute-specific state computed in `init(TLStructuredTypePart)`.

### 4. The context source as an attribute annotation

Where the notation comes from is model knowledge, so it is an annotation of the attribute holding a TL-Script function from the owning object to the notation:

```xml
<property name="minTemperature"
	type="Temperature"
>
	<annotations>
		<temperature-unit unit="s -> $s.container().get(`demo.temperature:DemoSite#temperatureUnit`)"/>
	</annotations>
</property>
```

The annotation is an application interface extending `TLAttributeAnnotation`, with a `@TagName` and an `Expr` property. It needs no registration: the annotations of a model part are resolved by tag name through the `TypeIndex`, which lists every subtype of `TLAttributeAnnotation` on the class path.

```java
@TagName("temperature-unit")
public interface TemperatureUnitAnnotation extends TLAttributeAnnotation {
	@Name("unit")
	@Mandatory
	Expr getUnit();
}
```

The storage compiles the function once, in `init(…)`, and evaluates it per read. The function is evaluated without access checks (`QueryExecutor.disableSecurity()`): the notation is part of the display, not data the reader asks for, and a reader who may see the value must see it in the right notation even if they may not read the context object. An attribute without the annotation, and an object for which the function yields nothing, gets the default notation. In the demo the function returns a classifier of the model enumeration `demo.temperature:TemperatureUnit`, which is mapped to the Java enum by name.

```java
@Override
public void init(TLStructuredTypePart attribute) {
	super.init(attribute);
	TemperatureUnitAnnotation annotation = attribute.getAnnotation(TemperatureUnitAnnotation.class);
	if (annotation != null) {
		_unitFunction = QueryExecutor.compile(annotation.getUnit());
		_unitFunction.disableSecurity();
	}
}

public TemperatureUnit getUnit(TLObject object) {
	if (_unitFunction == null || object == null) {
		return TemperatureUnit.CELSIUS;
	}
	Object result = _unitFunction.execute(object);
	TemperatureUnit unit = result instanceof TLClassifier classifier ? TemperatureUnit.fromClassifier(classifier) : null;
	return unit == null ? TemperatureUnit.CELSIUS : unit;
}
```

`QueryExecutor.compile(Expr)` may be called while the model is still being loaded: before the knowledge base and the model service are active, it returns an executor that compiles on first use, and `disableSecurity()` is remembered for it.

If the context can sit at different depths (a sensor in a sub-area of a site), the function walks up, e.g. `s -> recursion($s, x -> $x.container()).filter(x -> $x.instanceOf(`my.module:Site`)).firstElement().get(…)`.

### 5. The label provider

Table cells, filter and search texts, exports and every other place that shows a value by its label ask the `LabelProviderService`. A provider registered for the value class with `kind="CODE"` renders the value in the notation it carries:

```xml
<config service-class="com.top_logic.layout.provider.LabelProviderService">
	<instance>
		<label-providers>
			<provider
				implementation="com.top_logic.demo.react.temperature.TemperatureLabelProvider"
				kind="CODE"
				type="com.top_logic.demo.react.temperature.Temperature"
			/>
		</label-providers>
	</instance>
</config>
```

```java
public class TemperatureLabelProvider implements LabelProvider {
	@Override
	public String getLabel(Object object) {
		return object == null ? null : ((Temperature) object).format();
	}
}
```

### 6. The input control, set on the type

`<input-control>` (`TLInputControl`) is a type annotation as well, with `DefaultStrategy.VALUE_TYPE`: set on the datatype, it selects the control for every attribute of the type (see [Forms and input fields](doc:view-layer/forms) for the order in which a field's control is chosen).

```xml
<datatype name="Temperature" db_type="double" kind="Custom">
	<annotations>
		…
		<input-control>
			<impl class="com.top_logic.demo.react.temperature.TemperatureControlProvider"/>
		</input-control>
	</annotations>
	<temperature-mapping/>
</datatype>
```

The provider creates a subclass of `ReactTextInputControl`. The field shows the value's `toString()`; reading typed text happens in `applyRawClientValue(Object)`, which parses in the notation of the current value. For an empty field there is no value to take the notation from, so the control asks the storage of the attribute for the notation of the edited object. A text that cannot be read is reported with `AbstractFieldModel.setError(ResKey)` (an `I18NConstants` key): the rejected text stays in the field, the field keeps its last accepted value, and the error is shown at once. A successful parse clears the error and is applied with `applyClientValue(…)`.

```java
@Override
protected void applyRawClientValue(Object rawValue) {
	String text = rawValue == null ? "" : rawValue.toString().trim();
	if (text.isEmpty()) {
		setInputError(null);
		applyClientValue(null);
		return;
	}
	Temperature temperature = Temperature.parse(text, unit());
	if (temperature == null) {
		setInputError(I18NConstants.ERROR_INVALID_TEMPERATURE__VALUE.fill(text));
		return;
	}
	setInputError(null);
	applyClientValue(temperature);
}

private TemperatureUnit unit() {
	FieldModel model = getFieldModel();
	if (model.getValue() instanceof Temperature current) {
		return current.getUnit();
	}
	if (model instanceof AttributeFieldModel attributeModel
		&& AttributeOperations.getStorageImplementation(attributeModel.getPart()) instanceof TemperatureStorage<?> storage) {
		return storage.getUnit(attributeModel.getObject());
	}
	return TemperatureUnit.CELSIUS;
}

private void setInputError(ResKey error) {
	if (getFieldModel() instanceof AbstractFieldModel abstractModel) {
		abstractModel.setError(error);
	}
}
```

The parser of the demo also accepts an explicit unit (`20 °C`, `68 F`, `300 K`) and converts it into the unit of the field.

## Things to know

**Ask the storage of the attribute, not of the object.** `AttributeOperations.getStorageImplementation(TLObject, TLStructuredTypePart)` returns `TransientStorage` for a transient object - the object being created in a create dialog - not the storage of the type. Values of a transient object are therefore read without context. The control above uses the overload taking only the attribute, `getStorageImplementation(TLStructuredTypePart)`, which always returns the storage of the type, and evaluates the context itself.

**An object without context.** An object being created usually has no context yet: in the demo's "New Sensor" dialog the transient sensor belongs to no site until Create adds it to one, so the context function yields nothing. Decide and document the policy. The demo takes a number without unit in the default unit (°C) and accepts any other unit when it is typed explicitly; once created, the sensor's temperatures are displayed in the unit of its site (`20` typed in the dialog shows as `68.0 °F` at a site using Fahrenheit). Other choices are to require an explicit notation, or to create the object in its container first.

**Typed text is not echoed back converted.** While the user edits, the field keeps the text exactly as typed: a value the control applies from the client is not sent back to the same client, so that a late echo cannot overwrite further typing. `20 °C` typed into a field displaying Fahrenheit stays `20 °C` until the field is displayed again (after saving or switching the mode), and then reads `68.0 °F`.

**Changes of the context.** The value carries the notation of the moment it was read. A display shows a changed notation when it reads the value again. A `<table>` reloads its rows when an object of one of its `observed-types` changes, so the sensor table of the demo lists the context type as well and follows a changed site unit immediately:

```xml
<table
	observed-types="demo.temperature:DemoSite,demo.temperature:DemoSensor"
	selection="sensor"
	types="demo.temperature:DemoSensor"
>
```

A form showing a sensor is not refreshed when only its site changes; it shows the changed notation after it is built again.

**Access.** The application's types need grants and role rules like any other type, see [Access configuration](doc:access-configuration); the security coverage check at startup reports a type without them.

## Changing an existing attribute to such a type

When an attribute that already holds data (say `tl.core:Double`) is changed to the application datatype in the `*.model.xml`, the incremental model upgrade at startup does not change the type of the existing attribute in place. It logs

```
Renaming 'demo.temperature:DemoSensor#minTemperature' to 'ToBeDeleted_…'.
Deleting part with name 'demo.temperature:DemoSensor#ToBeDeleted_…'
Adding part 'minTemperature to type 'demo.temperature:DemoSensor'.
```

and proposes the corresponding migration processors (`update-property` renaming the old attribute, `delete-property`, `create-property`). In the demo - a flex attribute whose stored value is a `double` before and after the change - the values survived this in both directions: the attribute replacing the deleted one found the values stored under its name, and the mapping of the target type read them. This is a consequence of how the data happens to be stored, not a guarantee: it does not hold when the database type changes (a stored string read by a mapping expecting a number), when the attribute is stored in a column of its own, or when the data must be converted.

For production data, ship an explicit migration (a processor in a `*.migration.xml` of the application) that changes the type of the existing attribute instead of replacing it:

```xml
<update-property name="my.module:MyType#address"
	new-type="my.module:GroupAddress"
/>
```

Convert the stored values in the same migration where the raw representation changes.

## Building and testing

**Translations of added I18N constants.** The `messages_en.properties` entries of an added `I18NConstants` class or configuration interface are generated by the doclet, which runs in the `prepare-package` phase - after the `test` phase. A build that runs the tests before any build has generated the entries checks the constants against messages that do not exist yet, so `TestI18NConstantsTranslations` can fail. Build once without tests, review the generated entries (and the seeded German ones, see [I18N](doc:i18n)), then run the tests.

**Tests.**

- The label path: `LabelProviderService.getInstance().getLabelProvider(value)` for a value of the value class must render the notation the value carries. The service wraps a registered `LabelProvider` in an adapter (`LabelResourceProvider`), so compare the label, not the provider class.
- The storage path: in a test with a running knowledge base and model, create an owning object in a context, set the attribute (a plain number works as well, see step 2), and read it with `tValue(part)`: the value must carry the notation of the context, and change it when the context changes.
- The value class itself - parsing, formatting, conversion, equality on the raw value - needs no services and is a plain unit test.
