package com.blackpawsys.api.covid19.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
public class DailyReport {
  private Long confirmed;
  private Long deaths;
  private String country;
}
