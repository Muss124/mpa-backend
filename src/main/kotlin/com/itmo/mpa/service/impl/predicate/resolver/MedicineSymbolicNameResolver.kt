package com.itmo.mpa.service.impl.predicate.resolver

import com.itmo.mpa.entity.Medicine
import com.itmo.mpa.service.impl.predicate.parser.HAS_DELIMITER
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MedicineSymbolicNameResolver(
        @Value("\${mpa.predicate.prefix.medicine}") prefix: String
) : AbstractSymbolicNameResolver(prefix) {

    private val medicineId = "id"
    private val activeSubstanceId = "activeSubstanceId"

    override fun resolveValue(parameters: ResolvingParameters, propertyName: String): String {
        if (parameters.draft == null) throw IllegalArgumentException()
        return when (propertyName) {
            medicineId -> joinId(parameters.draft.medicines)
            activeSubstanceId -> joinSubstances(parameters.draft.medicines)
            else -> throw ResolvingException(code = ResolverErrorCode.MEDICINE.code, reason = propertyName)
        }
    }

    private fun joinId(medicines: Set<Medicine>): String {
        return medicines.joinToString(HAS_DELIMITER) { it.id.toString() }
    }

    private fun joinSubstances(medicines: Set<Medicine>): String {
        return medicines.asSequence()
                .map { it.activeSubstance }
                .flatten()
                .distinct()
                .map { it.id }
                .joinToString(HAS_DELIMITER)
    }
}
