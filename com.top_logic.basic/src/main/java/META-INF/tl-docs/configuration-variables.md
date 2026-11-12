---
description: Read before defining or setting a configuration variable (alias such as %STORAGE_PATH%) - the alias section of the XML configuration, ${env:NAME:default} references, the implicit environment name tl_<name> of an alias without such a reference, values stored in the application (variables.properties in the autoconf folder), the order of precedence, and the AliasManager API (getDefinition, AliasDefinition, AliasSource, storeValue).
order: 35
---

# Configuration variables (aliases)

A configuration variable (alias) is a name like `%STORAGE_PATH%` that is replaced by its value wherever it occurs in the configuration. Aliases are defined in the `alias` section of an XML configuration file under `WEB-INF/conf/`:

```xml
<alias>
	<entry name="%STORAGE_PATH%" value="${env:tl_storage_path:%APP_ROOT%/WEB-INF/app-data}"/>
	<entry name="%LDAP_URL%" value="ldap://localhost:389"/>
</alias>
```

The value of an alias may reference other aliases (`%APP_ROOT%/...`), independent of the order of definition. The programmatic aliases `%APP_ROOT%`, `%APP_CONTEXT%` and `%LOCAL_HOST_NAME%` are provided by the `AliasManager` itself and are no configured aliases.

## Setting a variable for an installation

An installation sets an alias without changing the configuration in one of these ways:

- **Environment names.** Each environment name of an alias is looked up as JNDI value `tl/<name>` of the application context, as system property and as environment variable, in this order.
  - An alias whose definition contains `${env:NAME:default}` references is set through the referenced names. When one of them is set, each reference is replaced by its value, or by its default (the empty string without default) if that name is not set.
  - An alias without `${env:...}` reference has the implicit environment name `tl_<name>`: the alias name without the `%`, lower-cased, with every character other than `a-z`, `0-9` and `_` replaced by `_` (`%STORAGE_PATH%` → `tl_storage_path`, `%LDAP_URL%` → `tl_ldap_url`). Its value replaces the whole definition. An alias with `${env:...}` references has no implicit name.
- **Stored value.** A value set in the application (view *Administration → System → Configuration variables* of the React UI) is stored in the file `variables.properties` of the `autoconf` folder of the installation (`tl_autoconf_dir`, otherwise `WEB-INF/autoconf` of the top-level webapp). It is a UTF-8 properties file keyed by the alias name including the `%`. The stored value replaces the whole definition, also the defaults of its `${env:...}` references.

## Order of precedence

The first origin that delivers a value determines the value of an alias:

1. JNDI value, system property, environment variable of an environment name (for several `${env:...}` references, the first one set in definition order names the origin),
2. the stored value,
3. the definition with the defaults of its `${env:...}` references.

So an environment setting always wins over a value set in the application. A stored value is used only while none of the alias' environment names is set.

## Changing a stored value

The configuration is resolved once at startup. A value stored or removed while the application runs takes effect after a restart. The stored values belong to the `autoconf` folder of one node: in a cluster, each node has its own.

The view *Administration → System → Configuration variables* (`admin/system/config-variables.view.xml` in `com.top_logic.layout.view`, TL-Script functions `configVariable...` of `ConfigVariableFunctions`) lists every configured alias with its environment names, the origin of its value, the running, default and stored value, and marks a variable whose value changes on the next restart. It sets and removes stored values. A variable set by one of its environment names cannot be set there, since the environment takes precedence; its stored value can still be removed. The value of a variable whose name matches a `protected-alias` pattern of the system environment configuration (`SystemEnvironmentBuilder.GlobalConfig`, by default e.g. `%DB_PASSWORD%` or `%CLIENT_SECRET%`) is never displayed, and such a variable is neither set nor removed in the view.

## API

`AliasManager` is the only place that interprets `${env:...}` references; consumers never parse definitions themselves.

- `getDefinition(name)` / `getDefinitions()` describe each configured alias as an `AliasDefinition`: the raw definition, the environment names and whether the name is implicit, the default value, the `AliasSource` of the running value (`JNDI`, `SYSTEM_PROPERTY`, `ENVIRONMENT`, `STORED`, `DEFINITION`) with the environment name that delivered it, the value before and after expanding other aliases, and the stored value as currently in the file. `isChangePending()` tells that the stored value was changed after startup in a way that changes the value after a restart. A programmatic alias has no description.
- `canStoreValues()` tells whether the file can be written: its folder is a writable directory, or does not exist and can be created in a writable parent directory.
- `storeValue(name, value)` and `removeValue(name)` replace the file atomically. Storing for a name that is no configured alias (or a programmatic one) is refused with an `I18NRuntimeException`.
- `implicitEnvironmentName(name)` computes the implicit environment name.

Values of aliases may be sensitive. The `AliasManager` never logs them. A secret does not belong in an alias at all, see [credentials](doc:credentials).
