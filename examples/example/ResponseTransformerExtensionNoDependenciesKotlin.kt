package examples.example

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

class ResponseTransformerExtensionNoDependenciesKotlin : ResponseTransformerV2 {
    override fun transform(
        p0: Response,
        p1: ServeEvent
    ): Response? {
        return Response.Builder()
            .status(p0.status)
            .headers(p0.headers)
            .body("Response from ${this.javaClass.simpleName}")
            .build()
    }

    override fun getName(): String = this.javaClass.simpleName

    override fun applyGlobally(): Boolean  = false
}