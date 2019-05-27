package com.itmo.mpa.service.impl.entityservice

import com.itmo.mpa.entity.Patient
import com.itmo.mpa.entity.State
import com.itmo.mpa.entity.Status
import com.itmo.mpa.repository.PatientRepository
import com.itmo.mpa.repository.StatusRepository
import com.itmo.mpa.service.exception.NoPendingDraftException
import com.itmo.mpa.service.exception.PatientNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PatientStatusEntityService(
        private val patientRepository: PatientRepository,
        private val statusRepository: StatusRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun findPatient(patientId: Long): Patient {
        logger.info("findPatient: {}", patientId)
        return patientRepository.findByIdOrNull(patientId) ?: throw PatientNotFoundException(patientId)
    }

    fun findDraftWithPatient(patientId: Long): Pair<Status?, Patient> {
        val patient = findPatient(patientId)
        return statusRepository.findStatusByPatientAndDraft(patient, draft = true) to patient
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    fun createOrUpdateDraft(patientId: Long, state: State): Status {
        val (oldDraft, patient) = findDraftWithPatient(patientId)
        val statusEntity = oldDraft ?: Status().also { it.patient = patient }
        statusEntity.submittedOn = Instant.now()
        statusEntity.state = state
        return statusRepository.save(statusEntity)
    }

    fun requireDraftWithPatient(patientId: Long): Pair<Status, Patient> {
        val (status, patient) = findDraftWithPatient(patientId)
        if (status == null) {
            throw NoPendingDraftException(patientId)
        }
        return status to patient
    }
}