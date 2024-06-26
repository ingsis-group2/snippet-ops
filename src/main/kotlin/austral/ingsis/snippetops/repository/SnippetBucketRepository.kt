package austral.ingsis.snippetops.repository

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.Optional

class SnippetBucketRepository(
    val url: String,
    val restTemplate: RestTemplate,
) : BucketRepository {
    override fun get(
        key: String,
        container: String,
    ): Optional<Any> {
        val url = "$url/$container/$key"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, String::class.java)
            return when (response.statusCode) {
                HttpStatus.OK -> {
                    println(response.body)
                    return response.body?.let { Optional.of(it) }!!
                }
                else -> Optional.empty()
            }
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    override fun save(
        key: String,
        container: String,
        content: String,
    ): Optional<Any> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(content, headers)

        return try {
            val response =
                restTemplate.exchange(
                    "$url/$container/$key",
                    HttpMethod.POST,
                    requestEntity,
                    Void::class.java,
                )
            Optional.of(response.statusCode == HttpStatus.CREATED)
        } catch (ex: HttpClientErrorException) {
            Optional.empty()
        }
    }

    override fun delete(
        key: String,
        container: String,
    ): Optional<Any> {
        val url = "$url/$container/$key"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.DELETE, null, Void::class.java)
            Optional.of(response.statusCode == HttpStatus.OK)
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    override fun getRules(
        key: String,
        container: String,
    ): Optional<Any> {
        val url = "$url/$container/$key"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, Map::class.java)
            return when (response.statusCode) {
                HttpStatus.OK -> {
                    println(response.body)
                    return response.body?.let { Optional.of(it) }!!
                }
                else -> Optional.empty()
            }
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    override fun saveRules(
        key: String,
        container: String,
        content: Map<String, Any>,
    ): Optional<Any> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(content, headers)

        // First, delete the existing rules
        deleteRules(key, container)

        return try {
            val response =
                restTemplate.exchange(
                    "$url/$container/$key",
                    HttpMethod.POST,
                    requestEntity,
                    Void::class.java,
                )
            Optional.of(response.statusCode == HttpStatus.CREATED)
        } catch (ex: HttpClientErrorException) {
            Optional.empty()
        }
    }

    override fun deleteRules(
        key: String,
        container: String,
    ): Optional<Any> {
        val url = "$url/$container/$key"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.DELETE, null, Void::class.java)
            Optional.of(response.statusCode == HttpStatus.OK)
        } catch (e: Exception) {
            Optional.empty()
        }
    }
}
