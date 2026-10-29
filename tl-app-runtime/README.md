# tl-app-runtime

The runtime image of TopLogic applications, `top-logic/tl-app-runtime:<TopLogic version>`: Apache
Tomcat on a Java runtime, hardened for operation. The image of an application is `FROM` this image
plus the unpacked application at the root of the server.

```
tl-app-runtime/build.sh            # builds top-logic/tl-app-runtime:<version of tl-bom>
```

What the image provides and how an application image is built: the developer article
`docker-image` of `com.top_logic` (`com.top_logic/src/main/java/META-INF/tl-docs/docker-image.md`).

- `Dockerfile` - the image.
- `setenv.sh` - the JVM options, read by Tomcat's `catalina.sh`.
- `toplogic.config.xml` - the configuration of the installation (`tl_config`): the secrets are the
  files of `/run/secrets`.
