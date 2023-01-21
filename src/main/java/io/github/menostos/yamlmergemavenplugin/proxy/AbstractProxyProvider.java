package io.github.menostos.yamlmergemavenplugin.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;

public abstract class AbstractProxyProvider implements ProxyProvider {
    protected abstract boolean isConfigured();

    protected abstract String getProxyHost();

    protected abstract int getProxyPort();

    protected abstract String[] getNonProxyHosts();

    @Override
    public boolean doesApply(URL url) {
        if (!isConfigured()) {
            return false;
        }
        if (getNonProxyHosts() != null) {
            return Arrays.stream(getNonProxyHosts())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .noneMatch(noProxy -> {
                        if (noProxy.startsWith("*")) {
                            return url.getHost().endsWith(noProxy.substring(1));
                        }
                        return url.getHost().equals(noProxy);
                    });
        }
        return true;
    }

    @Override
    public Proxy getProxy() {
        if (!isConfigured()) {
            throw new RuntimeException("proxy not configured");
        }
        return new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress(
                        getProxyHost(),
                        getProxyPort()
                )
        );
    }
}
