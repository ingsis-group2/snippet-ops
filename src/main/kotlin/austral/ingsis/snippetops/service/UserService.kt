package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.permissions.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserService(
    @Value("\${okta.machine.client-id}") val clientId: String,
    @Value("\${okta.machine.client-secret}") val clientSecret: String,
    @Value("\${okta.oauth2.issuer}") val issuer: String,
    @Autowired var restTemplate: RestTemplate,
) {
    fun getUserById(userId: String): User? {
        val accessToken = this.getAccessToken()
        if (accessToken != null) {
            val url = issuer + "api/v2/users/$userId"
            val requestEntity = this.buildRequestEntity(accessToken)
            val responseEntity: ResponseEntity<Map<*, *>> =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map::class.java,
                )
            if (responseEntity.statusCode == HttpStatus.OK) {
                val responseBody = responseEntity.body
                return User(
                    userId,
                    responseBody?.get("nickname").toString(),
                    responseBody?.get("email").toString(),
                )
            }
        }
        return null
    }

    fun getUserByEmail(email: String): User? {
        val accessToken = this.getAccessToken()
        if (accessToken != null) {
            val url = issuer + "api/v2/users-by-email?email=$email"
            val requestEntity = this.buildRequestEntity(accessToken)
            val responseEntity: ResponseEntity<Array<*>> =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Array::class.java,
                )
            if (responseEntity.statusCode == HttpStatus.OK && responseEntity.body != null) {
                val responseBody = responseEntity.body!![0] as Map<*, *>
                val user =
                    User(
                        responseBody["user_id"].toString(),
                        responseBody["nickname"].toString(),
                        email,
                    )
                println("found user: ")
                print(user)
                return user
            }
        }
        return null
    }

    fun getAllUsers(
        page: Int,
        size: Int,
    ): List<User> {
        val accessToken = this.getAccessToken()
        if (accessToken != null) {
            val url = issuer + "api/v2/users?limit=$size&offset=${page * size}"
            val requestEntity = this.buildRequestEntity(accessToken)
            val responseEntity: ResponseEntity<List<Map<*, *>>> =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    object : ParameterizedTypeReference<List<Map<*, *>>>() {},
                )
            if (responseEntity.statusCode == HttpStatus.OK) {
                val responseBody = responseEntity.body
                return responseBody?.map { userMap ->
                    User(
                        userMap["user_id"].toString(),
                        userMap["nickname"].toString(),
                        userMap["email"].toString(),
                    )
                } ?: emptyList()
            }
        }
        return emptyList()
    }

    fun getNicknameById(userId: String): String? {
        val user = this.getUserById(userId)
        return when (user) {
            null -> null
            else -> user.username
        }
    }

    /*
        Access token is not static
     */
    private fun getAccessToken(): String? {
        val url = issuer + "oauth/token"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val audience = issuer + "api/v2/"
        val requestBody =
            mapOf(
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "audience" to audience,
                "grant_type" to "client_credentials",
            )
        val requestEntity = HttpEntity(requestBody, headers)
        val responseEntity: ResponseEntity<Map<*, *>> =
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map::class.java,
            )
        if (responseEntity.statusCode == HttpStatus.OK) {
            val responseBody = responseEntity.body
            val accessToken = responseBody?.get("access_token").toString()
            println("Management API Token: $accessToken")
            return accessToken
        } else {
            return null
        }
    }

    private fun buildRequestEntity(accessToken: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(accessToken)
        return HttpEntity<String>(headers)
    }
}
