package utils

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object HttpUtils {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun get(url: String): String {
        val req =
            HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()
        val res = client.send(req, HttpResponse.BodyHandlers.ofString())
        if (res.statusCode() !in 200..299) {
            error("GET $url -> ${res.statusCode()} body=${res.body()}")
        }
        return res.body()
    }
}
