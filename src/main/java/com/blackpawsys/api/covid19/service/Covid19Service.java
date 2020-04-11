package com.blackpawsys.api.covid19.service;

import com.blackpawsys.api.covid19.component.DailyReport;
import com.blackpawsys.api.covid19.dto.DailyReportDataDto;
import com.blackpawsys.api.covid19.model.Record;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Covid19Service {

  void deleteAll();

  void saveAll(List<Record> recordList);

  Record findLatestRecord();

  DailyReportDataDto findByDate(LocalDate date);

  Optional<Record> findByCountry(Record record, LocalDate date);

  DailyReportDataDto findByCountry(String country);
}
