# Folder for automatic configuration

All application configuration files (`*.config.xml`) put into this folder are automatically loaded 
during application startup. In contrast to regular configuration files in the `WEB-INF/conf` folder, 
files in this folder are not required to be referenced from a `metaConf.txt` file.

This folder is mainly used from in-app tooling that requires to modify the application configuration. 
Those tools put new configuration files into this folder instead of modifying the existing application
configuration. 

Please not that the following restrictions apply to configuration files in this folder:

 * The order in which configuration files are read is not consistent with the module inheritance 
   relation. Instead, configuration files are read in alphabetical order of their file names. Therefore,
   a configuration file `a.config.xml` is read before `b.config.xml` even if `b.config.xml` is located 
   in an inherited module.
 * Due to the loosely defined order of the files, using configuration directives that depend on the order 
   of the configurations should not be used (such as `config:override`, or `config:position`).
 * Only typed configurations may be used in this folder, no alias definitions.
 
 