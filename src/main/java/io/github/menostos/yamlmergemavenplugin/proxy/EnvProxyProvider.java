package io.github.menostos.yamlmergemavenplugin.proxy;

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

    public static EnvProxyProvider http() {
        return create("http_proxy", false);
    }

    public static EnvProxyProvider https() {
        return create("https_proxy", true);
    }

    private static EnvProxyProvider create(String key, boolean onlyHttps) {
        Optional<Map.Entry<String, Integer>> proxyUrl = getEnvValueOrUpperCase(key)
                .map(s -> {
                    try {
                        return new URL(s);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
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
