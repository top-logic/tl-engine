---
description: Read before generating or regenerating the Java binding of model types (goal generate-java of tl-maven-plugin, WrapperGenerator) - the generated interface, the Base interface and Impl class, the module factory, the annotations package-binding and implementation-binding - or when the generated code does not compile because an access method clashes with an inherited method (getLastModified, getTypes), which the annotation <java-name> (JavaName) resolves - or when using the generated interface on a form buffer or transient object (TLObjectOverlay, ObjectEditing, ObjectCreation, TransientObjectFactory, TypedObjectClasses), e.g. what tHandle, getValue/setValue, the TLScope methods and compareTo return there.
order: 40
---

# Java binding of model types

The Java binding gives application code typed access to the objects of a model module: a Java
interface per class with getters and setters for its attributes, an implementation class, and a
factory creating the objects. It is generated from the `*.model.xml` by the `WrapperGenerator`,
started through the goal `generate-java` of the `tl-maven-plugin`.

## Generating the binding

The goal has no default phase; an application declares an execution naming the modules to generate:

```xml
<plugin>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-maven-plugin</artifactId>
	<executions>
		<execution>
			<id>generate-binding</id>
			<phase>none</phase>
			<goals>
				<goal>generate-java</goal>
			</goals>
			<configuration>
				<modules>my.module</modules>
			</configuration>
		</execution>
	</executions>
</plugin>
```

and runs it explicitly after a model change:

```bash
mvn tl:generate-java@generate-binding
```

Further parameters: `languages` (comma separated languages of the generated resource files),
`outputDirectory` (default: the source directory of the project), `copyrightHolder` and
`copyrightHeader` (header of the generated files).

## What is generated

The packages come from the annotation `package-binding` of the module:

```xml
<module name="my.module">
	<annotations>
		<package-binding
			interface-package="com.example.model"
			implementation-package="com.example.model.impl"
		/>
	</annotations>
	...
</module>
```

For a class `Device` of this module the generator writes:

| File | Content | Regenerated |
|------|---------|-------------|
| `com/example/model/Device.java` | Interface for application code, extends `DeviceBase`. Own methods go here. | No, only created when missing. |
| `com/example/model/impl/DeviceBase.java` | Interface with the type name constant, a name constant per attribute (`LAST_MODIFIED_ATTR`), the table name, and default methods accessing the attributes. | Yes |
| `com/example/model/impl/DeviceImpl.java` | Implementation class, extends the implementation class of the primary generalization, or `AttributedWrapper` for a class without one. | Yes |
| `com/example/model/MyModuleFactory.java` | Factory of the module: `createDevice()`, lookup of the types and attributes. | Yes |

The annotation `implementation-binding` on a type names its implementation class (`class-name`)
and its interface (`interface-name`) explicitly, instead of the names derived from the
`package-binding`. An abstract class gets an implementation class only with an
`implementation-binding`.

The access methods of an attribute are built from its name: `getX()` and `setX(...)`, for a
multiple reference also `addY(...)` and `removeY(...)` with the singular `Y` of the name, and the
live view `getXModifiable()`.

## Choosing the Java name of an attribute

An attribute whose access methods would clash with a method that the generated interface or the
implementation class inherits gets a different Java name with the annotation `<java-name>`:

```xml
<class name="Device">
	<attributes>
		<property name="lastModified"
			type="tl.core:DateTime"
		>
			<annotations>
				<java-name value="configurationChanged"/>
			</annotations>
		</property>
	</attributes>
</class>
```

All access methods of the attribute are built from the Java name: `getConfigurationChanged()` and
`setConfigurationChanged(...)`; for a multiple reference `types` with
`<java-name value="itemTypes"/>` the methods are `getItemTypes()`, `setItemTypes(...)`,
`addItemType(...)`, `removeItemType(...)` and `getItemTypesModifiable()`. The name of the
attribute in the model, its name constant (`LAST_MODIFIED_ATTR`), and its storage stay unchanged -
TL-Script, views and the database still use `lastModified`, and `Wrapper.getLastModified()` keeps
returning the technical modification time of the object.

An attribute overriding the annotated attribute in a subclass inherits the Java name, since its
getter overrides the getter of the overridden attribute; the annotation cannot be redefined on the
override.

## Clashes with inherited methods

Before writing any file, the generator checks every access method against the methods inherited by
the generated interface and by the implementation class. It reports a clash and stops, if an
inherited method with the same signature

- has an incompatible return type - e.g. `java.util.Date getLastModified()` for an attribute
  `lastModified` against `Long Wrapper.getLastModified()`, or `Set<? extends Item> getTypes()` for a
  reference `types` of a specialization of `tl.element:StructuredElement` against
  `Collection<TLType> TLScope.getTypes()`;
- is declared in a class the implementation class extends - it would silently take precedence over
  the generated default method, so the getter would not return the attribute value;
- is inherited by the implementation class from an interface the generated interface does not
  extend - the implementation class would inherit two unrelated methods with the same signature.

Attributes overriding another attribute and the attribute `name` (implemented through `TLNamed`)
are not checked. The message names the type, the attribute, both methods, and the declaring class:

```
Generated method 'java.util.Date getLastModified()' for attribute 'lastModified' of type 'my.module:Device' clashes with method 'public default java.lang.Long com.top_logic.knowledge.wrap.Wrapper.getLastModified()' declared in 'com.top_logic.knowledge.wrap.Wrapper': the return types are incompatible. Choose a different Java name for the attribute with the annotation <java-name value="..."/>.
```

Annotate the attribute with `<java-name>` and generate again; renaming the attribute in the model
is not necessary.

## Form buffers and transient objects

The objects a form edits are not the persistent objects themselves but buffers: a
`TLObjectOverlay` in the React UI, an `ObjectEditing` (existing object) or `ObjectCreation` (new
object) in the classic UI. Transient drafts are created by the `TransientObjectFactory`. All of them
implement the generated interfaces of their type and its generalizations as well, so that
application code - e.g. an `AttributeValueListener` or a constraint check receiving the buffer -
uses the same typed access as on a persistent object. For a buffer of the demo class
`DemoPlain:B`:

```java
B b = (B) buffer;
String value = b.getStringInBAndC();
b.setStringInBAndC("changed");
```

The getter reads the buffered value (the edited value, if the user changed it), and the setter
writes to the buffer; the persistent object keeps its value until the form is saved. There is
exactly one object per buffer: the buffer is still an instance of its base class
(`instanceof TLObjectOverlay`), keeps its identity and `equals`, and every place the buffer passes
itself on receives the typed object. The classes are created at runtime by `TypedObjectClasses` as
subclasses of the buffer classes and cached per base class and set of interfaces.

The generated interface of a type below `tl.element:StructuredElement` extends `StructuredElement`
and thereby `Wrapper` and `TLScope`. On a buffer or transient object their methods behave as
follows:

| Method | Result |
|--------|--------|
| `tHandle()` | The handle of the persistent counterpart (the edited object, or the object created from an `ObjectCreation`); an `UnsupportedOperationException` for an object without one, as for every transient `TLObject`. |
| `getValue(String)`, `setValue(String, Object)` | Read and write the value of the buffer, like `tValueByName` and `tUpdateByName`. |
| `getTypes()`, `getType(String)`, `getClasses()`, ... (`TLScope`) | The types of the persistent counterpart; empty collections and `null` for a new object. |
| `getCreator()`, `getCreated()`, `getModifier()`, `getModified()`, `getLastModified()` (`tCreator()`, `tCreationDate()`, `tLastModifier()`, ...) | The creation and last modification of the persistent counterpart; `null` (`0` for a time stamp) for a new object, which has not been stored yet. A derived attribute computed from them (e.g. `<method-call method="com.top_logic.knowledge.wrap.Wrapper#getCreator()"/>`) is empty in a create form. |
| `compareTo(Wrapper)` | Orders by name. Objects with the same name: two persistent objects by their object keys, a persistent object before a transient one, two transient objects by their identity hash codes (stable while the objects live). The buffer of a persistent object shares its identity and is ordered like it. |
| `getName()`, `generateFullText(...)` | Read the buffered name. |

A transient object answers the single-valued backwards reference `parent` with the object that
refers to it in its `children`, so `getParent()` works on a draft below a transient container.

A generated interface with an abstract method that neither the buffer class, nor a default method,
nor one of the rules above implements is not added; the buffer then lacks only this interface and
keeps the others of its type. This applies to the framework interfaces of the `tl.model` types
(`TLClass`, `TLModule`, ...), and to a hand-written interface extending one with abstract methods
of its own: the demo interface `A` extends `WebFolderAware` and `MailFolderAware`, so a buffer of
`DemoTypes:A` implements `DemoTypesA` and the other generated interfaces of its generalizations, but
not `A`. To make an interface available on buffers, implement such methods as default methods.
