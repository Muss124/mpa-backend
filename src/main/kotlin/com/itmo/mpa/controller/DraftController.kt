package com.itmo.mpa.controller

import com.itmo.mpa.dto.request.StatusRequest
import com.itmo.mpa.dto.response.AppropriateMedicineResponse
import com.itmo.mpa.dto.response.AvailableTransitionResponse
import com.itmo.mpa.dto.response.DiseaseAttributeResponse
import com.itmo.mpa.dto.response.StatusResponse
import com.itmo.mpa.service.DraftService
import com.itmo.mpa.service.MedicineService
import com.itmo.mpa.service.StatusService
import com.itmo.mpa.service.TransitionService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@Api(value = "/patients/{patientId}")
@RequestMapping("patients/{patientId}")
class DraftController(
        private val statusService: StatusService,
        private val draftService: DraftService,
        private val transitionService: TransitionService,
        private val medicineService: MedicineService
) {

    @ApiOperation("Commit draft")
    @PostMapping("status/draft")
    fun commitDraft(@PathVariable patientId: Long): StatusResponse = statusService.commitDraft(patientId)

    @ApiOperation("Create draft")
    @PutMapping("status/draft")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDraft(
            @PathVariable patientId: Long,
            @Valid @RequestBody draftRequest: StatusRequest
    ): Unit = draftService.rewriteDraft(patientId, draftRequest)

    @ApiOperation("Get current draft")
    @GetMapping("status/draft")
    fun getDraftByPatientId(@PathVariable patientId: Long): StatusResponse = draftService.findDraft(patientId)

    @ApiOperation("Get available disease attributes")
    @GetMapping("status/draft/attributes")
    fun getDiseaseAttributesByPatientId(
            @PathVariable patientId: Long
    ): List<DiseaseAttributeResponse> = draftService.getDiseaseAttributes(patientId)

    @ApiOperation("Get available transitions from current state")
    @GetMapping("status/draft/states")
    fun getAvailableTransitionsByPatientId(
            @PathVariable patientId: Long
    ): List<AvailableTransitionResponse> = transitionService.getAvailableTransitions(patientId)

    @ApiOperation("Get medicine compatibility info from current state")
    @GetMapping("status/draft/medicine")
    fun getAppropriateMedicinesByPatientId(
            @PathVariable patientId: Long
    ): List<AppropriateMedicineResponse> = medicineService.getAppropriateMedicine(patientId)
}