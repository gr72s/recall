package cc.green.recall.server.services

import cc.green.recall.*
import cc.green.recall.server.ConsumeRecord
import cc.green.recall.server.HasNullProtoException
import cc.green.recall.server.Response
import cc.green.recall.server.ServiceException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate

data class ConsumeRecordProto(
    val id: Long?,
    val payPlatformId: Long?,
    val sum: BigDecimal = 0.toBigDecimal(),
    val payAccountId: Long?,
    val consumeDate: LocalDate?,
    val consumePlatformId: Long?,
    val tagIds: List<Long> = listOf()
) {
    constructor() : this(null, null, 0.0.toBigDecimal(), null, null, null, listOf())
}

data class ConsumeRecordResProto(
    val id: Long?,
    val payPlatformIdentifier: String?,
    val sum: BigDecimal = 0.toBigDecimal(),
    val payAccountIdentifier: String?,
    val consumeDate: LocalDate?,
    val consumePlatformIdentifier: String?,
    val tags: List<String> = listOf()
)

@RestController
class ConsumeRecordController(val service: ConsumeRecordService) {

    @PostMapping("/consume/record/add")
    fun addConsumeRecord(@RequestBody proto: ConsumeRecordProto): ResponseEntity<Response> {
        proto.consumeDate ?: throw HasNullProtoException("consume date")
        return success(service.newConsumeRecord(proto))
    }

    @PostMapping("/repl/consume/record/add")
    fun addConsumeRecordForRepl(@RequestBody proto: ConsumeRecordProto): ResponseEntity<Response> {
        proto.consumeDate ?: throw HasNullProtoException("consume date")
        val entity = service.newConsumeRecord(proto)
        val resProto = ConsumeRecordResProto(
            entity.id,
            entity.consumePlatform?.identifier,
            entity.sum,
            entity.payAccount?.identifier,
            entity.consumeDate,
            entity.consumePlatform?.identifier,
            entity.tags.mapNotNull { it.identifier }.toList()
        )
        return success(resProto)
    }

    @PostMapping("/consume/record/update")
    fun updateConsumeRecord(@RequestBody proto: ConsumeRecordProto): ResponseEntity<Response> {
        proto.id ?: throw HasNullProtoException("id")
        return success(service.updateConsumeRecord(proto))
    }

    @PostMapping("/consume/record/del")
    fun delConsumeRecord(@RequestBody protos: List<ConsumeRecordProto>): ResponseEntity<Response> {
        protos.forEach { it.id ?: throw HasNullProtoException("id") }
        return success(service.delConsumeRecord(protos))
    }

    @GetMapping("/consume/record")
    fun getConsumeRecord(): ResponseEntity<Response> {
        return success(service.getConsumeRecord())
    }

}

@Service
class ConsumeRecordService(
    val repo: ConsumeRecordRepo,
    val payPlatformRepo: PayPlatformRepo,
    val payAccountRepo: PayAccountRepo,
    val tagRepo: ConsumeTagRepo,
    val consumePlatformRepo: ConsumePlatformRepo,
) {

    fun newConsumeRecord(proto: ConsumeRecordProto): ConsumeRecord {
        return updateOrCreateConsumeRecord(proto, null)
    }

    fun updateConsumeRecord(proto: ConsumeRecordProto): ConsumeRecord {
        val foundConsumeRecord = repo.findByIdOrThrow(proto.id!!)
        return updateOrCreateConsumeRecord(proto, foundConsumeRecord)
    }

    fun delConsumeRecord(protos: List<ConsumeRecordProto>): List<ConsumeRecordProto> {
        val filtered = protos.filter { it.id != null }
        repo.deleteAllById(filtered.map { it.id })
        return filtered
    }

    fun getConsumeRecord(): List<ConsumeRecord> {
        return repo.findAll()
    }

    @Throws(ServiceException::class)
    private fun updateOrCreateConsumeRecord(proto: ConsumeRecordProto, record: ConsumeRecord?): ConsumeRecord {
        val r = (record ?: ConsumeRecord()).apply {
            payPlatform = proto.payPlatformId?.let {
                payPlatformRepo.findByIdOrThrow(it)
            }
            payAccount = proto.payAccountId?.let {
                payAccountRepo.findByIdOrThrow(it)
            }
            sum = proto.sum
            consumeDate = proto.consumeDate
            consumePlatform = proto.consumePlatformId?.let {
                consumePlatformRepo.findByIdOrNull(it)
            }
            tags = proto.tagIds.mapNotNull { tagId ->
                tagRepo.findByIdOrNull(tagId)
            }.toMutableSet()
        }
        return repo.save(r)
    }

}

@Repository
interface ConsumeRecordRepo : JpaRepository<ConsumeRecord, Long>