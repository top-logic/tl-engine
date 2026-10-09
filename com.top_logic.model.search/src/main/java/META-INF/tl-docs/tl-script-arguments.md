---
description: Read before calling a TL-Script expression from Java with arguments, or before configuring a script property whose arguments or variables come from its context - how positional arguments are bound (Args, QueryExecutor.execute/executeWith, lambda currying), what happens with surplus or missing arguments, and how to give a script implicitly defined variables (Define.create / SearchExpressionFactory.lambda, @ScriptContextVariables, CheckExprWithAdditionalParams).
order: 40
---

# Passing arguments to TL-Script

A configured script (an `Expr` property) is compiled to a `QueryExecutor` and called from Java with
arguments. This article describes how the arguments reach the script and how a script can get
variables without declaring them itself.

## Positional arguments

`QueryExecutor.execute(Object...)` and `QueryExecutor.executeWith(Args)` pass a positional argument
list (`Args.none()`, `Args.some(…)`, `Args.cons(value, rest)`). A script receives them through
lambdas: `a -> b -> body`.

- **One argument per lambda.** A `Lambda` binds the first argument to its variable and evaluates its
  body with the remaining arguments (`Lambda.internalEval`). Nested lambdas therefore consume the
  arguments from left to right.
- **Surplus arguments are ignored.** A body that is not a lambda (a literal, a block, a call, …)
  ignores the arguments left over. A script may declare fewer parameters than it is called with:
  `objects -> $objects.size()` called with three arguments uses the first and drops the others. This
  is what lets a caller pass optional context after the main argument, with the main argument first.
- **Missing arguments yield a function, not an error.** A lambda evaluated without an argument
  returns itself as a function value (a closure over the current definitions). A script that
  declares *more* parameters than it is called with therefore runs nothing: its result is a function,
  and no side effect of its body happens. The same applies to a wrong argument count after a caller
  changed its signature. This is silent - the only trace is a function where a value was expected.
- **The order is the caller's contract.** Nothing checks that a script declares its parameters in
  the order the caller passes them. Document the order at the configuration property, keep the
  argument everyone needs first, and prefer implicit variables (below) when there are more than one
  or two arguments.

## Implicitly defined variables

A script can also get variables it does not declare: the caller wraps the configured expression in
one lambda per variable before compiling it and passes the values as leading arguments. The
configurer then writes `$name` directly, with no lambda header, so neither the order nor the number
of parameters can be wrong.

Canonical example: configured TL-Script functions (`ConfiguredScript`, `ScriptConfiguration`) whose
implementation uses its declared parameters as variables.

### Wrapping and calling

Wrap at the configuration level with `Define.create(name, expr)` (on `Expr`), or at the compiled
level with `SearchExpressionFactory.lambda(name, searchExpression)`. The outermost lambda binds the
first argument, so the wrapping order decides the argument order:

```java
// Variables a, b, c, called as executeWith(Args.some(valueA, valueB, valueC)):
Expr expr = config.getImplementation();
for (int n = names.size() - 1; n >= 0; n--) {   // wrap in reverse order
    expr = Define.create(names.get(n), expr);
}
QueryExecutor executor = QueryExecutor.compile(expr);
```

Wrapping in forward order and prepending the values with `Args.cons` in the same forward order is
equivalent (`ScriptFlowChartBuilder` does this for its handler variables): both loops reverse the
order. Arguments left over after the implicit variables reach the configured expression itself, so a
script may still be a function of further positional arguments.

The variable names must be known when the expression is compiled - they come from the configuration
(a list of parameter names, a map of handlers, the inputs of a command), never from the runtime
values.

### Configuration support

- **Syntax check.** A configured expression with implicit variables does not compile on its own (its
  variables are undefined). Check it together with the names: `CheckExprWithAdditionalParams` as a
  `@Constraint` on the `Expr` property, with the property holding the names (a list of
  `NamedConfiguration`) as its argument, wraps the expression and reports problems on the field. A
  property whose names live elsewhere uses its own `GenericValueDependency` doing the same wrapping
  (see `ScriptFlowChartBuilder.Config.SyntaxCheck`).
- **Completion in the script editor.** Annotate the `Expr` property with
  `@ScriptContextVariables(MyVariables.class)`, where `MyVariables` implements
  `ScriptContextVariablesProvider` and returns the variable names for the edited configuration item
  (`ScriptParameterVariables`, `ChartHandlerVariables`). The editor offers them at the top level of
  `$`-completion.
