package com.blackpawsys.api.covid19.component;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Data
public class ComparisonReport extends DailyReport {

  private Long newCases;
  private Long newDeaths;

  @Builder
  public ComparisonReport(Long confirmed, Long deaths, String country, Long newCases, Long newDeaths) {
    super(confirmed, deaths, country);
    this.newCases = newCases;
    this.newDeaths = newDeaths;
  }
}
