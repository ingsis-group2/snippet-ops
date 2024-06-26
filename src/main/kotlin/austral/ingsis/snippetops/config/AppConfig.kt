package austral.ingsis.snippetops.config

import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.repository.BucketRepositoryImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
class AppConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .additionalInterceptors(
                ClientHttpRequestInterceptor { request, body, execution ->
                    println("Request URI: ${request.uri}")
                    println("Request Body: ${String(body)}")
                    execution.execute(request, body)
                },
            )
            .build()
    }

    @Bean
    fun bucketRepository(
        restTemplate: RestTemplate,
        @Value("\${spring.services.snippet.bucket}") url: String,
    ): BucketRepository {
        return BucketRepositoryImpl(url, restTemplate)
    }
}
