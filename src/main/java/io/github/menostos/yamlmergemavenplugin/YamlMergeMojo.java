package io.github.menostos.yamlmergemavenplugin;

import io.github.menostos.yamlmergemavenplugin.proxy.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "resolve", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class YamlMergeMojo extends AbstractMojo {

    @Parameter(name = "input", defaultValue = "${project.basedir}", required = true)
    String input;

    @Parameter(name = "output", defaultValue = "${project.build.directory}", required = true)
    private File output;

    @Parameter(name = "skip", defaultValue = "false")
    private Boolean skip;

    @Parameter(name = "proxyHost")
    private String proxyHost;

    @Parameter(name = "proxyPort")
    private Integer proxyPort;

    @Parameter(name = "noProxyHosts")
    private String noProxyHosts;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().debug("skipping yaml processing goal");
            return;
        }

        if (input == null || input.isEmpty()) {
            input = project.getBasedir().getAbsolutePath();
        }
        if (output == null) {
            output = new File(project.getBuild().getOutputDirectory());
        }

        Path outputFile = output.toPath();
        try {
            if (isHttpUrl(input)) {
                try {
                    URL url = new URL(input);
                    processUrl(url, outputFile);
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException("no valid url supplied " + input, e);
                }
            } else {
                Path inputFile = Paths.get(input);
                if (Files.notExists(inputFile)) {
                    throw new MojoFailureException("the input " + inputFile.toAbsolutePath() + " does not exist");
                }

                if (Files.isDirectory(inputFile) && !Files.isDirectory(outputFile)) {
                    throw new MojoFailureException("if input is a directory output has also to be a directory");
                }

                if (Files.isDirectory(inputFile)) {
                    processDirectory(inputFile, outputFile);
                } else {
                    processFile(inputFile, outputFile);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("failed to process YAML file", e);
        }
    }

    private void processDirectory(Path inputDir, Path outputDir) throws IOException, MojoFailureException {
        List<Path> filesToProcess = Files.list(inputDir)
                .filter(path -> path.getFileName().endsWith(".yml") || path.getFileName().endsWith(".yaml"))
                .collect(Collectors.toList());

        getLog().info("found " + filesToProcess + " yaml files in " + inputDir.toAbsolutePath());

        for (Path path : filesToProcess) {
            processFile(path, outputDir);
        }
    }

    private void processFile(Path inputFile, Path out) throws IOException, MojoFailureException {
        Path outputFile;
        if (Files.isDirectory(out)) {
            out.toFile().mkdirs();
            outputFile = out.resolve(inputFile.getFileName());
        } else {
            out.getParent().toFile().mkdirs();
            outputFile = out;
        }
        getLog().info("processing yaml file " + inputFile.toAbsolutePath() + " --> " + outputFile.toAbsolutePath());
        try (InputStream fileInputStream = Files.newInputStream(inputFile)) {
            processStream(fileInputStream, outputFile);
        }
    }

    private void processUrl(URL url, Path out) throws IOException, MojoExecutionException, MojoFailureException {
        Optional<Proxy> proxy = Arrays.stream(new ProxyProvider[]{
                        new PluginConfigurationProxyProvider(proxyHost, proxyPort, noProxyHosts),
                        new MavenSettingsProxyProvider(settings),
                        SystemPropertiesProxyProvider.https(),
                        SystemPropertiesProxyProvider.http(),
                        EnvProxyProvider.https(),
                        EnvProxyProvider.http()
                })
                .filter(proxyProvider -> proxyProvider.doesApply(url))
                .map(ProxyProvider::getProxy)
                .findFirst();

        HttpURLConnection urlConnection;
        if (proxy.isPresent()) {
            urlConnection = (HttpURLConnection) url.openConnection(proxy.get());
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }
        urlConnection.setRequestMethod("GET");
        if (urlConnection.getResponseCode() != 200) {
            throw new MojoExecutionException("failed to fetch " + input + " response code was " + urlConnection.getResponseCode());
        }

        Path outputFile;
        if (Files.isDirectory(out)) {
            String outputFileName = extractFileNameFromUrl(url).orElse("output.yaml");
            out.toFile().mkdirs();
            outputFile = out.resolve(outputFileName);
        } else {
            out.getParent().toFile().mkdirs();
            outputFile = out;
        }

        try (InputStream urlInputStream = urlConnection.getInputStream()) {
            processStream(urlInputStream, outputFile);
        }
    }

    private void processStream(InputStream inputStream, Path outputFile) throws IOException, MojoFailureException {
        if (Files.isDirectory(outputFile)) {
            throw new MojoFailureException(outputFile.toAbsolutePath() + " is expected to be a file");
        }

        Yaml yamlParser = new Yaml();
        Object content = yamlParser.load(inputStream);

        Object resolvedContent = resolveRecursively(content);

        DumperOptions dumpOptions = new DumperOptions();
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yamlDumper = new Yaml(dumpOptions);
        Writer writer = Files.newBufferedWriter(outputFile);
        yamlDumper.dump(resolvedContent, writer);
    }


    private Object resolveRecursively(Object object) {
        if (object instanceof Map) {
            Map<Object, Object> map = new LinkedHashMap<>();
            Map<?, ?> mappingNode = (Map<?, ?>) object;
            mappingNode.forEach((key, value) -> map.put(
                    resolveRecursively(key),
                    resolveRecursively(value)
            ));
            return map;
        } else if (object instanceof List) {
            List<Object> list = new ArrayList<>();
            List<?> mappingList = (List<?>) object;
            mappingList.forEach(child ->
                    list.add(resolveRecursively(child))
            );
            return list;
        } else {
            return object;
        }
    }

    private boolean isHttpUrl(String input) {
        return input.startsWith("http://") || input.startsWith("https://");
    }

    private Optional<String> extractFileNameFromUrl(URL url) {
        return Optional.ofNullable(url.getPath())
                .map(s -> {
                    int lastSlash = url.getPath().lastIndexOf("/");
                    if (lastSlash > -1) {
                        return s.substring(lastSlash).trim();
                    }
                    return null;
                })
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    if (!s.endsWith(".yml") && !s.endsWith(".yaml")) {
                        return s + ".yaml";
                    }
                    return s;
                });
    }

}
