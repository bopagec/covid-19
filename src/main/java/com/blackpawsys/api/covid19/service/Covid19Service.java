package com.blackpawsys.api.covid19.service;

import com.blackpawsys.api.covid19.dto.DailyReportDataDto;
import com.blackpawsys.api.covid19.model.Record;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Covid19Service {

  void deleteAll();

  void saveAll(List<Record> recordList);

  Record findLatestRecord();

  List<Record> findByDate(LocalDate date, Optional<String> optGroupBy);

  DailyReportDataDto findByCountry(String country);
}
