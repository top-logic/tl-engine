---
description: Read before generating or regenerating the Java binding of model types (goal generate-java of tl-maven-plugin, WrapperGenerator) - the generated interface, the Base interface and Impl class, the module factory, the annotations package-binding and implementation-binding - or when the generated code does not compile because an access method clashes with an inherited method (getLastModified, getTypes), which the annotation <java-name> (JavaName) resolves.
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
