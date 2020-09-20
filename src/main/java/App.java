import Service.FileService;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class App {
    public static void main(final String[] args) {
        final URI uri = URI.create("http://0.0.0.0:8080/");
        final ResourceConfig resourceConfig = new ResourceConfig(FileService.class);
        resourceConfig.register(MultiPartFeature.class);
        resourceConfig.register(JacksonFeature.class);
        JettyHttpContainerFactory.createServer(uri, resourceConfig);
    }
}
