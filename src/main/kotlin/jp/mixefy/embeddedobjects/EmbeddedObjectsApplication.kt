package jp.mixefy.embeddedobjects

import com.google.cloud.spring.data.spanner.core.admin.SpannerDatabaseAdminTemplate
import com.google.cloud.spring.data.spanner.core.admin.SpannerSchemaUtils
import com.google.cloud.spring.data.spanner.core.mapping.Embedded
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey
import com.google.cloud.spring.data.spanner.core.mapping.Table
import com.google.cloud.spring.data.spanner.repository.SpannerRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*
import java.util.*

@SpringBootApplication
class EmbeddedObjectsApplication

fun main(args: Array<String>) {
    runApplication<EmbeddedObjectsApplication>(*args)
}

data class EntityHistory(
    val createdAt: Date,
    val createdBy: String,
    val updatedAt: Date,
    val updatedBy: String
) {
    constructor(userId: String) : this(
        createdAt = Date(System.currentTimeMillis()),
        createdBy = userId,
        updatedAt = Date(System.currentTimeMillis()),
        updatedBy = userId
    )
}

@Table(name = "Requests")
data class Request(
    @PrimaryKey val requestId: String,
    val payload: String,
    @Embedded val entityHistory: EntityHistory
)

interface RequestRepository : SpannerRepository<Request, String>

@RestController
class ApiController(
    val requestRepository: RequestRepository,
    val schemaUtils: SpannerSchemaUtils,
    val databaseAdmin: SpannerDatabaseAdminTemplate
) {

    @GetMapping("/create-table")
    fun createTable() {
        val createStrings =
            schemaUtils.getCreateTableDdlStringsForInterleavedHierarchy(Request::class.java)
        this.databaseAdmin.executeDdlStrings(createStrings, true)
    }

    @GetMapping("/requests/{requestId}")
    fun getRequest(@PathVariable requestId: String): Optional<Request> {
        // fails with "SpannerDataException: Column not found: entityHistory"
        return requestRepository.findById(requestId)
    }

    @PostMapping("/requests")
    fun saveRequest(@RequestBody payload: String): String {
        val requestId = UUID.randomUUID().toString()

        // works fine
        requestRepository.save(Request(requestId, payload, EntityHistory("some-user-id")))

        return requestId
    }
}
