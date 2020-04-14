package com.blackpawsys.api.covid19.service;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.dto.DailyReportDto;
import com.blackpawsys.api.covid19.dto.DailyReportDataDto;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.repository.Covid19Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

@Service
public class Covid19ServiceImpl implements Covid19Service {

  @Autowired
  private Covid19Repository repository;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }

  @Override
  public void saveAll(List<Record> recordList) {
    repository.saveAll(recordList);
  }

  @Override
  public Record findLatestRecord() {
    return repository.findFirstByOrderByLastUpdatedDesc();
  }

  @Override
  public List<Record> findByDate(LocalDate date, Optional<String> optGroupBy) {
    Aggregation aggregation = RecordUtil.createAggregation(date, "lastUpdated", "confirmed", optGroupBy);

    AggregationResults<Record> aggregate = mongoTemplate.aggregate(aggregation, Record.class, Record.class);

    List<Record> results = aggregate.getMappedResults();

    return results;
  }

  @Override
  public DailyReportDataDto findByCountry(String country) {
    Aggregation aggregation = RecordUtil.createAggregation(country, "country", "lastUpdated", Optional.of("lastUpdated"));

    AggregationResults<DailyReportDto> aggregate = mongoTemplate.aggregate(aggregation, Record.class, DailyReportDto.class);

    List<DailyReportDto> results = aggregate.getMappedResults();

    return DailyReportDataDto.builder()
        .dailyReportDtoList(results)
        .build();
  }

}
