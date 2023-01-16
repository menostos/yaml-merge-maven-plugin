package io.github.menostos.yamlmergemavenplugin.proxy;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

import java.util.Optional;

public class MavenSettingsProxyProvider extends ValueBasedConfigurationProxyProvider {

    public MavenSettingsProxyProvider(Settings settings) {
        super(
                Optional.ofNullable(settings.getActiveProxy()).map(Proxy::getHost),
                Optional.ofNullable(settings.getActiveProxy()).map(Proxy::getPort),
                parseArray(Optional.ofNullable(settings.getActiveProxy()).map(Proxy::getNonProxyHosts), "\\|"),
                false
        );
    }

}
