# Database schema version folder

The application's schema version consists of a chain of migration descriptions located in this folder. 
Initially this chain is empty. A new migration description is added by calling the Ant task 
`create_migration_script_template` from the project's `build.xml`. Such migration description is executed, 
whenever an application starts the first time with that application version.
