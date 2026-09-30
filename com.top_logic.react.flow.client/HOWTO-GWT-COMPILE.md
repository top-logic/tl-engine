# GWT compile

When GWT compiling from Eclipse GWT plugin, you have to add the following advanced options to the compile dialog:

```
-generateJsInteropExports
-sourceLevel 17
-war ../com.top_logic.react.flow.server/src/main/webapp
```

This ensures that the sources can be parsed and the result is placed in the workspace to be picked up when running 
in IDE mode.

Also add the following VM arguments to ensure enough memory is available to the compiler:

```
-Xmx1g
```
