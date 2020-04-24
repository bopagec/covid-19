package com.blackpawsys.api.covid19.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldGraphDataDto {
  private LocalDateTime lastUpdated;
  private Long newCases;
  private Long newDeaths;
}
