package io.github.menostos.yamlmergemavenplugin.proxy;

import java.util.Optional;

public class PluginConfigurationProxyProvider extends ValueBasedConfigurationProxyProvider {

    public PluginConfigurationProxyProvider(String host, Integer port, String nonProxyHosts) {
        super(
                Optional.ofNullable(host).filter(h -> !h.isEmpty()),
                Optional.ofNullable(port).filter(p -> p > 0),
                parseArray(Optional.ofNullable(nonProxyHosts), ","),
                false
        );
    }

    @Override
    protected boolean isConfigured() {
        if (
                (getProxyHost() != null && getProxyPort() <= 0) ||
                        (getProxyHost() == null && getProxyPort() > 0)) {
            throw new RuntimeException("proxyHost and proxyPort need to be specified both");
        }
        return super.isConfigured();
    }
}
