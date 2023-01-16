package io.github.menostos.yamlmergemavenplugin.proxy;

import java.net.Proxy;
import java.net.URL;

public interface ProxyProvider {

    boolean doesApply(URL url);

    Proxy getProxy();

}
