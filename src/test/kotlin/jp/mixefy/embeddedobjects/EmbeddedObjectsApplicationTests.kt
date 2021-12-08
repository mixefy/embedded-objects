package jp.mixefy.embeddedobjects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@SpringBootTest
class EmbeddedObjectsApplicationTests {

    @Autowired
    lateinit var requestRepository: RequestRepository

    @Test
    fun contextLoads() {

    }

    @Test
    fun repositoryIntegTest() {
        val requestId = UUID.randomUUID().toString()
        requestRepository.save(Request(requestId, "payload", EntityHistory("some-user-id")))

        val result = requestRepository.findByIdOrNull(requestId)
        assertEquals(result?.payload, "payload")
    }

}
