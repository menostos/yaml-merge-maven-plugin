package io.github.menostos.yamlmergemavenplugin;

import org.apache.maven.plugins.annotations.Parameter;

public class ProxyConfiguration {

    /**
     * a host which will be used as a http proxy address
     *
     * @since 0.0.1
     */
    @Parameter(name = "host")
    private String host;

    /**
     * a host which will be used as a http proxy port
     *
     * @since 0.0.1
     */
    @Parameter(name = "port")
    private Integer port;

    /**
     * an optional list of noProxyHosts which will skip using a proxy
     *
     * @since 0.0.1
     */
    @Parameter(name = "noProxyHosts")
    private String noProxyHosts;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getNoProxyHosts() {
        return noProxyHosts;
    }

    public void setNoProxyHosts(String noProxyHosts) {
        this.noProxyHosts = noProxyHosts;
    }
}
