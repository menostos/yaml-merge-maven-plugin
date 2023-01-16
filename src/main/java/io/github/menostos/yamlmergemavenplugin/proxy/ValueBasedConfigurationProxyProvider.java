package io.github.menostos.yamlmergemavenplugin.proxy;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public abstract class ValueBasedConfigurationProxyProvider extends AbstractProxyProvider {

    private final Optional<String> host;
    private final Optional<Integer> port;
    private final Optional<String[]> nonProxyHosts;
    private final boolean onlyHttps;

    protected ValueBasedConfigurationProxyProvider(
            Optional<String> host,
            Optional<Integer> port,
            Optional<String[]> nonProxyHosts,
            boolean onlyHttps
    ) {
        this.host = host;
        this.port = port;
        this.nonProxyHosts = nonProxyHosts;
        this.onlyHttps = onlyHttps;
    }

    protected static Optional<String> getValue(Function<String, String> supplier, String key) {
        return Optional.ofNullable(supplier.apply(key))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    protected static Optional<Integer> parseInt(Optional<String> value) {
        return value.map(Integer::parseInt).filter(p -> p > 0);
    }

    protected static Optional<String[]> parseArray(Optional<String> value, String separator) {
        return value.map(s -> s.split(separator))
                .map(a -> Arrays.stream(a)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new)
                );
    }

    @Override
    public boolean doesApply(URL url) {
        if (onlyHttps && !"https".equals(url.getProtocol())) {
            return false;
        }
        return super.doesApply(url);
    }

    @Override
    protected boolean isConfigured() {
        return host.isPresent() && port.isPresent();
    }

    @Override
    protected String getProxyHost() {
        return host.orElse(null);
    }

    @Override
    protected int getProxyPort() {
        return port.orElse(-1);
    }

    @Override
    protected String[] getNonProxyHosts() {
        return nonProxyHosts.orElse(null);
    }

}
