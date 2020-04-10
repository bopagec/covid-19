package com.blackpawsys.api.covid19.service;

import com.blackpawsys.api.covid19.component.DailyReport;
import com.blackpawsys.api.covid19.model.Record;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

public interface Covid19Service {

  void deleteAll();

  void saveAll(List<Record> recordList);

  List<Record> findAll(Sort sort);

  List<DailyReport> findByDate(String date);

  Optional<Record> findByCountry(Record record, String date);

  List<DailyReport> findByCountry(String country);
}
