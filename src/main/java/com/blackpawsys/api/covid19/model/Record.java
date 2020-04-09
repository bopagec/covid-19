package com.blackpawsys.api.covid19.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Record {

  private String state;
  private String country;
  private String combinedKey;
  private LocalDate lastUpdated;
  private Long confirmed;
  private Long deaths;
}
