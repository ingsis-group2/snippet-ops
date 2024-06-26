package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.dto.SnippetDTO
import austral.ingsis.snippetops.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/snippet")
class SnippetController(
    @Autowired val snippetService: SnippetService,
    @Autowired val restTemplate: RestTemplate,
) {
    @PostMapping("")
    fun createSnippet(
        @RequestBody body: SnippetCreate,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<SnippetDTO> {
        val userId = user.claims["sub"]
        return snippetService.createSnippet(body, userId.toString())
    }

    @PostMapping("/addReader")
    fun addReaderIntoSnippet(
        @AuthenticationPrincipal user: Jwt,
        @RequestParam readerMail: String,
        @RequestParam snippetId: Long,
    ): ResponseEntity<Boolean> {
        return this.snippetService.addNewReaderIntoSnippet(user.claims["sub"].toString(), readerMail, snippetId)
    }

    @GetMapping("/{id}")
    fun getSnippetById(
        @PathVariable(value = "id", required = true) id: Long,
    ): ResponseEntity<SnippetDTO> {
        return snippetService.getSnippet(id)
    }

    @PutMapping("/{id}")
    fun updateSnippet(
        @PathVariable id: Long,
        @RequestBody body: String,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Boolean> {
        val userId = user.claims["sub"]
        return snippetService.updateSnippet(id, body, userId.toString())
    }

    @GetMapping("/byWriter")
    fun getSnippetsByWriter(
        @AuthenticationPrincipal user: Jwt,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        val userId = user.claims["sub"]
        return this.snippetService.getSnippetByWriter(userId.toString(), page)
    }

    @GetMapping("/byReader")
    fun getSnippetsByReader(
        @AuthenticationPrincipal user: Jwt,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        val userId = user.claims["sub"]
        return this.snippetService.getSnippetByReader(userId.toString(), page)
    }

    @GetMapping("/byReaderAndWriter")
    fun getSnippetsByReaderAndWriter(
        @AuthenticationPrincipal user: Jwt,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        val userId = user.claims["sub"]
        return this.snippetService.getSnippetByReaderAndWriter(userId.toString(), page)
    }

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable("id") id: Long,
    ): ResponseEntity<Boolean> {
        return this.snippetService.deleteSnippet(id)
    }

    @GetMapping("/greet")
    @ResponseBody
    fun greet(): String {
        return "hello stranger"
    }

    @GetMapping("/greet/permissions")
    fun greetPermissions(): ResponseEntity<String> {
        val url = "http://snippet-permissions:8080/snippet/greetBack"
        val response = restTemplate.exchange(url, HttpMethod.GET, null, String::class.java)
        return ResponseEntity(response.body, response.statusCode)
    }

    @GetMapping("userId/{mail}")
    @ResponseBody
    fun getUserIdByMail(
        @PathVariable("mail") mail: String,
    ): String {
        return this.snippetService.getUserIdByEmail(mail)
    }
}
