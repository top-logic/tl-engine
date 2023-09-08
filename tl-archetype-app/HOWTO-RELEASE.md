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
   
 * An account at Sonatype, see https://issues.sonatype.org/secure/Signup!default.jspa
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
        <tl.keypassid>tl-key-pass</tl.keypassid>
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

Having this all set up, you can prepare a release (after the version number has been incremented to the new version to 
release) using:

```
cd ../tl-parent-all
mvn  -P \!full-build -P maven-central
```

This results in a staging repository to be created at Sonatype:

```
[INFO] --- nexus-staging-maven-plugin:1.6.7:deploy (injected-nexus-deploy) @ tl-archetype-app ---
[INFO]  * Connected to Nexus at https://s01.oss.sonatype.org:443/, is version 2.15.1-02 and edition "Professional"
[INFO]  * Using staging profile ID "..." (matched by Nexus).
[INFO] Installing .../devel/tl-engine-2/tl-archetype-app/target/tl-archetype-app-7.5.1.jar to ...
[INFO] Installing .../devel/tl-engine-2/tl-archetype-app/pom.xml to ...
...
[INFO] Performing remote staging...
[INFO] 
[INFO]  * Remote staging into staging profile ID "..."
[INFO]  * Created staging repository with ID "comtop-logic-1002".
[INFO]  * Staging repository at https://s01.oss.sonatype.org:443/service/local/staging/deployByRepositoryId/comtop-logic-1002
[INFO]  * Uploading locally staged artifacts to profile com.top-logic
Uploading to tl-ossrh: https://s01.oss.sonatype.org:443/service/local/staging/deployByRepositoryId/comtop-logic-1002/...
Uploaded to tl-ossrh: https://s01.oss.sonatype.org:443/service/local/staging/deployByRepositoryId/comtop-logic-1002/...
...
[INFO]  * Upload of locally staged artifacts finished.
[INFO]  * Closing staging repository with ID "comtop-logic-1002".

Waiting for operation to complete...
................

[INFO] Remote staged 1 repositories, finished with success.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:49 min
[INFO] Finished at: 2023-06-26T18:26:05+02:00
[INFO] ------------------------------------------------------------------------
```

To finally release the staged artifacty to Maven Central, you have to issue the following command:

```
cd ../tl-parent-all
mvn nexus-staging:release -P \!full-build -P maven-central
```

This releases the staged artifacts:

```
[INFO] -------------------< com.top-logic:tl-archetype-app >-------------------
[INFO] Building tl-archetype-app 7.5.1
[INFO] --------------------------[ maven-archetype ]---------------------------
[INFO] 
[INFO] --- nexus-staging-maven-plugin:1.6.7:release (default-cli) @ tl-archetype-app ---
[INFO]  * Connected to Nexus at https://s01.oss.sonatype.org:443/, is version 2.15.1-02 and edition "Professional"
[INFO] Releasing staging repository with IDs=[comtop-logic-1002]

Waiting for operation to complete...
....

[INFO] Released
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  15.278 s
[INFO] Finished at: 2023-06-26T18:26:32+02:00
[INFO] ------------------------------------------------------------------------
```

If something went wrong and the release should be aborted, type:

```
mvn nexus-staging:drop
```
