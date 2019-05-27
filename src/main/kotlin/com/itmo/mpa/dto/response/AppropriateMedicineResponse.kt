package com.itmo.mpa.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

data class AppropriateMedicineResponse(
        val medicine: MedicineResponse,
        val isNotRecommended: Boolean?,
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        val errorCause: List<String>?
)