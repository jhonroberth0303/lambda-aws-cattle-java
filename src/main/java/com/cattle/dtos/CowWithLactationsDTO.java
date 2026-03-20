package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Vaca con sus lactancias")
public class CowWithLactationsDTO {

    @Schema(description = "ID del bovino", example = "172")
    private Integer bovineId;

    @Schema(description = "Lista de lactancias del bovino")
    private List<LactationSummaryDTO> lactations;
}
