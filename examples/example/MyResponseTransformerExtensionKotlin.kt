package examples.example

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

class MyResponseTransformerExtensionKotlin : ResponseTransformerV2 {
    override fun transform(
        p0: Response,
        p1: ServeEvent
    ): Response? {
        return Response.Builder()
            .status(p0.status)
            .headers(p0.headers)
            .body("Hey from MyResponseTransformerExtensionKotlin")
            .build()
    }

    override fun getName(): String {
        return "MyResponseTransformerExtensionKotlin"
    }

    override fun applyGlobally(): Boolean  = false
}