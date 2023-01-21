package io.github.menostos.yamlmergemavenplugin.proxy;

import org.apache.maven.plugin.logging.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

public class EnvProxyProvider extends ValueBasedConfigurationProxyProvider {

    private EnvProxyProvider(
            Optional<String> host,
            Optional<String> port,
            Optional<String> nonProxyHosts,
            boolean onlyHttps
    ) {
        super(
                host,
                parseInt(port),
                parseArray(nonProxyHosts, ","),
                onlyHttps
        );
    }

    public static EnvProxyProvider http(Log log) {
        return create("http_proxy", false, log);
    }

    public static EnvProxyProvider https(Log log) {
        return create("https_proxy", true, log);
    }

    private static EnvProxyProvider create(String key, boolean onlyHttps, Log log) {
        Optional<Map.Entry<String, Integer>> proxyUrl = getEnvValueOrUpperCase(key)
                .map(s -> {
                    try {
                        return new URL(s);
                    } catch (MalformedURLException e) {
                        log.warn("could not parse proxy url " + s);
                        return null;
                    }
                })
                .map(url -> new AbstractMap.SimpleEntry<>(url.getHost(), url.getPort()));
        return new EnvProxyProvider(
                proxyUrl.map(Map.Entry::getKey),
                proxyUrl.map(Map.Entry::getValue).map(String::valueOf),
                getEnvValueOrUpperCase("no_proxy"),
                onlyHttps
        );
    }

    private static Optional<String> getEnvValueOrUpperCase(String key) {
        Map<String, String> env = System.getenv();
        Optional<String> wrapper = getValue(env::get, key);
        if (!wrapper.isPresent()) {
            wrapper = getValue(env::get, key.toUpperCase());
        }
        return wrapper;
    }

}
