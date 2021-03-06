package com.itmo.mpa.service.mapping

import com.itmo.mpa.dto.response.MedicineResponse
import com.itmo.mpa.entity.Medicine

fun Medicine.toResponse() = MedicineResponse(
        id,
        name,
        activeSubstance.map { it.name }
)

