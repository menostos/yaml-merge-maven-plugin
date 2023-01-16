package io.github.menostos.yamlmergemavenplugin.proxy;

public class SystemPropertiesProxyProvider extends ValueBasedConfigurationProxyProvider {

    private SystemPropertiesProxyProvider(
            String hostKey,
            String portKey,
            String nonProxyKey,
            boolean onlyHttps
    ) {
        super(
                getValue(System::getProperty, hostKey),
                parseInt(getValue(System::getProperty, portKey)),
                parseArray(getValue(System::getProperty, nonProxyKey), "\\|"),
                onlyHttps
        );
    }

    public static SystemPropertiesProxyProvider http() {
        return new SystemPropertiesProxyProvider(
                "http.proxyHost",
                "http.proxyPort",
                "http.nonProxyHosts",
                false
        );
    }

    public static SystemPropertiesProxyProvider https() {
        return new SystemPropertiesProxyProvider(
                "https.proxyHost",
                "https.proxyPort",
                "https.nonProxyHosts",
                true
        );
    }

}
