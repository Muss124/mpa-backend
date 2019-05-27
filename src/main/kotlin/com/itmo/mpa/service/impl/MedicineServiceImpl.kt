package com.itmo.mpa.service.impl

import com.itmo.mpa.dto.response.AppropriateMedicineResponse
import com.itmo.mpa.entity.Contraindications
import com.itmo.mpa.entity.Patient
import com.itmo.mpa.entity.Status
import com.itmo.mpa.repository.ContraindicationsRepository
import com.itmo.mpa.service.MedicineService
import com.itmo.mpa.service.PredicateService
import com.itmo.mpa.service.impl.entityservice.PatientStatusEntityService
import com.itmo.mpa.service.mapping.toResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MedicineServiceImpl(
        private val patientStatusEntityService: PatientStatusEntityService,
        private val contraindicationsRepository: ContraindicationsRepository,
        private val predicateService: PredicateService
) : MedicineService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getAppropriateMedicine(patientId: Long): List<AppropriateMedicineResponse> {
        val (status, patient) = patientStatusEntityService.requireDraftWithPatient(patientId)
        return contraindicationsRepository.findAll()
                .groupBy { it.medicine.id }
                .map { (_, contraindications) -> contraindications.formResponse(patient, status) }
                .also { logger.debug("getAvailableTransitions: result {}", it) }
    }

    private fun List<Contraindications>.formResponse(
            patient: Patient,
            status: Status
    ): AppropriateMedicineResponse {
        val medicineResponse = this.first().medicine.toResponse()
        val notRecommendedResults = this.map { it.isNotRecommended(patient, status) }
        val notRecommendedList = notRecommendedResults.map { it.first }
        val errors = notRecommendedResults.mapNotNull { it.second }
        val isNotRecommended = when {
            errors.isNotEmpty() -> null
            else -> notRecommendedList.contains(true)
        }
        val errorsList = when {
            errors.isNotEmpty() -> errors
            else -> null
        }
        return AppropriateMedicineResponse(medicineResponse, isNotRecommended, errorsList)
    }

    private fun Contraindications.isNotRecommended(
            patient: Patient,
            status: Status
    ): Pair<Boolean?, String?> {
        return try {
            predicateService.testPredicate(patient, status, predicate) to null
        } catch (e: Exception) {
            null to e.message
        }
    }
}
