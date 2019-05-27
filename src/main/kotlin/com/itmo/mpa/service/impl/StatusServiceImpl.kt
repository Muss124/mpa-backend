package com.itmo.mpa.service.impl

import com.itmo.mpa.dto.response.StatusResponse
import com.itmo.mpa.repository.PatientRepository
import com.itmo.mpa.repository.StatusRepository
import com.itmo.mpa.service.StatusService
import com.itmo.mpa.service.exception.AttributesNotSetException
import com.itmo.mpa.service.exception.StatusNotFoundException
import com.itmo.mpa.service.impl.entityservice.DiseaseAttributesEntityService
import com.itmo.mpa.service.impl.entityservice.PatientStatusEntityService
import com.itmo.mpa.service.mapping.toResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class StatusServiceImpl(
        private val patientRepository: PatientRepository,
        private val statusRepository: StatusRepository,
        private val patientStatusEntityService: PatientStatusEntityService,
        private val attributesEntityService: DiseaseAttributesEntityService
) : StatusService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun commitDraft(patientId: Long): StatusResponse {
        logger.info("commitDraft: create new status from draft for patient with id {}", patientId)

        val (statusDraft, patient) = patientStatusEntityService.requireDraftWithPatient(patientId)

        val requiredAttributeNames = attributesEntityService.getDiseaseAttributes(patient)
                .filter { it.isRequired }
                .mapTo(HashSet()) { it.id }

        val savedAttributeNames = statusDraft.diseaseAttributeValues
                .mapTo(HashSet()) { it.diseaseAttribute.attribute.id }

        if (!savedAttributeNames.containsAll(requiredAttributeNames)) {
            throw AttributesNotSetException(requiredAttributeNames - savedAttributeNames)
        }
        statusDraft.submittedOn = Instant.now()
        statusDraft.draft = false
        patient.currentStatus = statusRepository.save(statusDraft)
        patientRepository.save(patient)
        return statusDraft.toResponse()
    }

    override fun findCurrentStatus(patientId: Long): StatusResponse {
        logger.info("findCurrentStatus: patientId {}", patientId)
        return patientStatusEntityService.findPatient(patientId)
                .currentStatus
                .toResponse()
    }

    override fun findStatusById(patientId: Long, statusId: Long): StatusResponse {
        logger.info("findStatusById: patientId {}, statusId {}", patientId, statusId)
        val patient = patientStatusEntityService.findPatient(patientId)
        val status = statusRepository.findStatusByPatientAndId(patient, statusId)
                ?: throw StatusNotFoundException(patientId, statusId)
        return status.toResponse()
    }

    override fun findAllForPatient(patientId: Long): List<StatusResponse> {
        logger.info("findAllForPatient: {}", patientId)
        val patient = patientStatusEntityService.findPatient(patientId)
        return statusRepository.findStatusesByPatientOrderBySubmittedOnAsc(patient)
                .map { it.toResponse() }
    }
}