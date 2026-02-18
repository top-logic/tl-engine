---
name: toplogic-service
description: This skill should be used when creating TopLogic services, configuration interfaces, or classes extending ConfiguredManagedClass. Provides patterns for configuration properties, exception handling, and service dependencies.
version: 1.0.0
---

# TopLogic Service Patterns

## Configuration Property Constants

When defining configuration property name constants in service configuration interfaces, always include a `@see` JavaDoc reference to the corresponding getter method.

```java
public interface Config<I extends MyService> extends ConfiguredManagedClass.Config<I> {

    /**
     * Configuration property name for my property.
     *
     * @see #getMyProperty()
     */
    String MY_PROPERTY = "my-property";

    /**
     * Description of what this property does.
     */
    @Name(MY_PROPERTY)
    @StringDefault("default value")
    String getMyProperty();
}
```

This creates bidirectional documentation between the property name constant and its getter, making it easier to navigate and understand the configuration interface.

## Exception Handling in Services

When implementing TopLogic services (classes extending `ConfiguredManagedClass`), use appropriate exceptions:

- **ConfigurationError / ConfigurationException**: ONLY for reporting problems to end-users (e.g., invalid user input, configuration errors users can fix)
- **RuntimeException**: For technical errors in service initialization and operation (e.g., startup failures, resource initialization errors)

```java
@Override
protected void startUp() {
    super.startUp();

    try {
        // Service initialization code
        someResource.init();

    } catch (RuntimeException ex) {
        // Re-throw RuntimeExceptions as-is
        throw ex;
    } catch (Exception ex) {
        // Wrap checked exceptions in RuntimeException
        throw new RuntimeException("Failed to start service: " + ex.getMessage(), ex);
    }
}
```

Service errors are technical issues not visible to end-users. `ConfigurationError` and `ConfigurationException` are reserved for user-facing validation and configuration problems.

## Accessing Servlet Context Information

When implementing services that need access to the web application context (e.g., context path, servlet context), use the `ServletContextService`:

```java
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.services.ServletContextService;

@ServiceDependencies({
    ServletContextService.Module.class
})
public class MyService extends ConfiguredManagedClass<MyService.Config<?>> {

    @Override
    protected void startUp() {
        super.startUp();

        // Get the application context path
        String contextPath = ServletContextService.getInstance().getServletContext().getContextPath();

        // Use context path in service initialization
        String fullUrl = contextPath + "/my/endpoint";
    }
}
```

**Important:** When one service uses another service, you **must** declare the dependency using the `@ServiceDependencies` annotation. This ensures proper startup order - the dependent service will be started before your service.
