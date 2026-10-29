---
description: Read when writing a service (a ManagedClass with its BasicRuntimeModule) or defining a functional group of services - assigning a service to a category with @ServiceCategory.Of, the built-in categories in com.top_logic.basic.module.category, and defining an own category with @Label and @ServiceCategory.Order.
order: 50
---

# Service categories

The services administration lists every service of an application - often more than a hundred. To make a service easy to find, each service belongs to a functional category, by which the list is grouped (Administration → System → Services).

## Assigning a service to a category

The implementation class of a service names its category with `@ServiceCategory.Of`:

```java
@Label("Task scheduler")
@ServiceCategory.Of(OperationsCategory.class)
public class Scheduler extends ConfiguredManagedClass<SchedulerConfig> {
	...
}
```

- The annotation is inherited: a subclass of a service belongs to the category of its superclass unless it declares its own. Placed on an interface, it applies to every implementation that declares none.
- A service declaring no category belongs to `OtherCategory`, shown last.
- `ServiceCategory.of(implementationClass)` resolves the category; the TL-Script function `serviceCategory($module)` gives its label.

## Built-in categories

The framework defines these categories in `com.top_logic.basic.module.category`, in this order: `RuntimeCategory` (runtime & configuration), `ModelCategory`, `PersistenceCategory`, `AuthenticationCategory` (authentication & users), `AccessControlCategory`, `ScriptingCategory` (scripting & search), `I18NCategory` (I18N & formats), `UICategory` (user interface), `InterfacesCategory` (exchange with other systems), `OperationsCategory` (operations & monitoring), `DevelopmentCategory` (development tools) and `OtherCategory`. The JavaDoc of each says which services belong there.

## Defining an own category

A category is a marker interface extending `ServiceCategory`. A module or an application defines one where its services form a group the built-in categories do not describe:

```java
/**
 * Services processing the business processes of the application.
 */
@Label("Processes")
@ServiceCategory.Order(20)
public interface ProcessCategory extends ServiceCategory {
	// Pure marker interface.
}
```

- The label of the category is the label of the interface (`ResKey.forClass(category)`): the `@Label` annotation, or the first sentence of its JavaDoc, generated into `messages_en.properties` by the build; the German label is added to `messages_de.properties` by hand. The JavaDoc becomes its tooltip.
- `@ServiceCategory.Order` places the category among the others; the built-in ones use 1 to 11, `OtherCategory` 99. A category without the annotation is placed at `ServiceCategory.DEFAULT_ORDER` (90), after the built-in ones and before the other services. Categories at the same position are ordered by label.
