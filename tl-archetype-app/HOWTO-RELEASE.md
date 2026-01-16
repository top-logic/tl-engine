# Releasing App Archetype to Maven Central

To release a new version of the TopLogic app archetype to Maven Central, you need the following prerequisites:

 * GPG key to sign artifacts.
   
   ```
   gpg --gen-key
   ```
 
 * Your GPG key uploaded to a public key server.
 
   ```
   gpg --keyserver keyserver.ubuntu.com --send-keys 989FC5...
   ```
   
 * An account at Sonatype, see https://central.sonatype.com/
 * A server entry in your local Maven settings providing user name and password for your Sonatype account

   ```
    <server>
      <id>tl-ossrh</id>
      <username>...</username>
      <password>{mW23aL3p...}</password>
    </server>
   ``` 

 * Your account at Sonatype must be linked to the `com.top-logic` group ID.
 * A local Maven profile selecting your signing key and the server ID with your key passphrase 

   ```
     <profile>
      <id>tl-ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <tl.keyname>989FC5...</tl.keyname>
        <tl.keypassid>tl-keypass</tl.keypassid>
      </properties>
    </profile>
    ```

  * A server entry in your local Maven settings providing your key passphrase

    ```
    <server>
      <id>tl-keypass</id>
      <passphrase>{FyY0t4...}</passphrase>
    </server>
    ```

A workaround for a Maven vs. Java 17 incompatibility is required to allow releasing (opening some JDK classes that are accessed from Maven plugins by reflection):

```
export MAVEN_OPTS="--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED"
```

Having this all set up, you can deploy a version (after incrementing the version number to the new version to be released)
 using the following:

```
cd ../tl-parent-all
mvn clean deploy -P \!full-build -P maven-central
```

```
...
[INFO] Created bundle successfully /home/dbu/Development/workspaces/CWS_3/git/tl-engine-7/tl-parent-all/target/central-staging/central-bundle.zip
[INFO] Going to upload /home/dbu/Development/workspaces/CWS_3/git/tl-engine-7/tl-parent-all/target/central-publishing/central-bundle.zip
[INFO] Uploaded bundle successfully, deployment name: Deployment, deploymentId: a865a3ce-bc4c-40ac-ae9f-6117956ac295. Deployment will publish automatically
[INFO] Waiting until Deployment a865a3ce-bc4c-40ac-ae9f-6117956ac295 is validated
[INFO] Deployment a865a3ce-bc4c-40ac-ae9f-6117956ac295 has been validated. To finish publishing visit https://central.sonatype.com/publishing/deployments
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for tl-parent-all 7.9.8:
[INFO] 
[INFO] tl-parent-all ...................................... SUCCESS [  1.408 s]
[INFO] tl-archetype-app ................................... SUCCESS [ 38.508 s]
[INFO] tl-parent-build .................................... SUCCESS [  0.758 s]
[INFO] tl-parent-core ..................................... SUCCESS [  1.409 s]
[INFO] tl-parent-app ...................................... SUCCESS [  1.506 s]
[INFO] tl-parent-app-internal ............................. SUCCESS [  1.111 s]
[INFO] tl-parent-core-internal ............................ SUCCESS [  0.909 s]
[INFO] tl-parent-gwt ...................................... SUCCESS [  7.541 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  53.650 s
[INFO] Finished at: 2026-01-16T15:18:24+01:00
[INFO] ------------------------------------------------------------------------
```
