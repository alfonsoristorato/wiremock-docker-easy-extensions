package examples.example

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import org.apache.commons.lang3.StringUtils

class ResponseTransformerExtensionWithDependenciesKotlin : ResponseTransformerV2 {
    override fun transform(
        p0: Response,
        p1: ServeEvent
    ): Response? {
        return Response.Builder()
            .status(p0.status)
            .headers(p0.headers)
            .body("Response from ${this.javaClass.simpleName} using ${StringUtils.capitalize("stringUtils")} from Apache Commons Lang3")
            .build()
    }

    override fun getName(): String = this.javaClass.simpleName

    override fun applyGlobally(): Boolean  = false
}