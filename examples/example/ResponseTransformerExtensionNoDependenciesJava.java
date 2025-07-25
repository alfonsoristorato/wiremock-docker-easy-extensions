package examples.example;

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public class ResponseTransformerExtensionNoDependenciesJava implements ResponseTransformerV2 {
    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
        return new Response.Builder()
                .status(response.getStatus())
                .headers(response.getHeaders())
                .body("Hey from ResponseTransformerExtensionNoDependenciesJava")
                .build();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
