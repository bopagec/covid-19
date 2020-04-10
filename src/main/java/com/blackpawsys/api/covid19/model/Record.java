package com.blackpawsys.api.covid19.model;

import java.util.List;
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
  private List<String> combinedKey;
  private String lastUpdated;
  private Long confirmed;
  private Long deaths;
  private Long newCases;
  private Long newDeaths;
  private String lat;
  private String longt;
}
