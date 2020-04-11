package com.blackpawsys.api.covid19.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.component.DailyReport;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.repository.Covid19Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
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
  public List<DailyReport> findByDate(LocalDate date) {
    Aggregation aggregation = RecordUtil.createAggregation(date, "lastUpdated","confirmed", "country");

    AggregationResults<DailyReport> aggregate = mongoTemplate.aggregate(aggregation, Record.class, DailyReport.class);

    List<DailyReport> mappedResults = aggregate.getMappedResults();

    return mappedResults;
  }

  @Override
  public Optional<Record> findByCountry(Record record, LocalDate date) {
    List<Record> records = repository.findByCountryAndStateAndLastUpdatedAndLatAndLongt(record.getCountry(), record.getState(), date, record.getLat(), record.getLongt());

    if (!records.isEmpty()) {
      return Optional.of(records.get(0));
    }

    return Optional.empty();
  }

  @Override
  public List<DailyReport> findByCountry(String country) {
    Aggregation aggregation = RecordUtil.createAggregation(country, "country","lastUpdated" , "lastUpdated");

    AggregationResults<DailyReport> aggregate = mongoTemplate.aggregate(aggregation, Record.class, DailyReport.class);

    List<DailyReport> mappedResults = aggregate.getMappedResults();

    return mappedResults;
  }

}
