package com.example.wilberies_computation.dtos.wilberies;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdditionalPaymentsRowDto {

  private BigDecimal paid;

}
