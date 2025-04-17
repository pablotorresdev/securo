package com.mb.conitrack.dto;

import java.math.BigDecimal;

import com.mb.conitrack.enums.UnidadMedidaEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MagnitudDTO {

    BigDecimal cantidad;

    UnidadMedidaEnum unidadMedida;

}
