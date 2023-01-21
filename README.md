# YAML Merge Maven Plugin
This Plugin is intended for systems/plugins that cannot handle [YAML merge keys](https://yaml.org/type/merge.html) itself. 
The plugin can parse and process yaml files and store them in an output file with the merge keys resolve.

Releases are published to [Maven Central](https://search.maven.org/search?q=io.github.menostos).

## Maven pom.xml Configuration

### process a single file
If you just want to process a single file, you can specify an input and an output file path.
```xml
<plugin>
    <groupId>ch.codevo.maven</groupId>
    <artifactId>yaml-merge-maven-plugin</artifactId>
    <version>0.0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>resolve</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <input>${project.basedir}/src/main/resources/input.yaml</input>
        <output>${project.build.directory}/output.yaml</output>
    </configuration>
</plugin>
```

### process a folder
If you specify a folder as input the plugin will search for all .yaml and .yml files and process them.
```xml
<plugin>
    <groupId>ch.codevo.maven</groupId>
    <artifactId>yaml-merge-maven-plugin</artifactId>
    <version>0.0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>resolve</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <input>${project.basedir}/src/main/resources/yamls/</input>
        <output>${project.build.directory}/processed-yamls/</output>
    </configuration>
</plugin>
```

## Proxy Configuration (YAML from URL)
There are several options for configure a http proxy to be used to download a yaml file from an url.

### Plugin Configuration
there is the possibility to configure a proxy with the plugin configuration.
```xml
<plugin>
    ...
    <configuration>
       ...
       <proxy>
           <host>some-proxy</host>
           <port>8080</port>
           <noProxyHosts>noproxy1,noproxy2</noProxyHosts>
       </proxy>
    </configuration>
</plugin>
```

### Maven Settings
Providing a proxy via maven settings.xml, see [Maven Proxies](https://maven.apache.org/guides/mini/guide-proxies.html).

``nonProxyHosts`` is supported.

### System Properties
Providing a proxy via java system properties or the command line, see [Java HTTP Proxies](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)

``-Dhttp.proxyHost=some-proxy -Dhttp.proxyPort=8080``

or

``-Dhttps.proxyHost=some-proxy -Dhttps.proxyPort=8080``

``-Dhttp.nonProxyHosts`` and ``-Dhttps.nonProxyHosts`` is supported.

### Environment Variables
The plugin will read the environment variable ``http_proxy`` and ``https_proxy`` from your system and use them if available (lower and uppercase).

``no_proxy`` is supported.