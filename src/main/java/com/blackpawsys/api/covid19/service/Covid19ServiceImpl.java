package com.blackpawsys.api.covid19.service;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.repository.Covid19Repository;
import java.time.LocalDateTime;
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
  public List<Record> findByDate(LocalDateTime dateTime, Optional<String> optGroupBy) {
    Aggregation aggregation = RecordUtil.createAggregation(dateTime, "lastUpdated", "confirmed", optGroupBy);

    AggregationResults<Record> aggregate = mongoTemplate.aggregate(aggregation, Record.class, Record.class);

    List<Record> results = aggregate.getMappedResults();

    return results;
  }

  @Override
  public List<Record> findByCountry(String country) {
    Aggregation aggregation = RecordUtil.createAggregation(country, "country", "lastUpdated", Optional.of("lastUpdated"));

    AggregationResults<Record> aggregate = mongoTemplate.aggregate(aggregation, Record.class, Record.class);

    List<Record> results = aggregate.getMappedResults();

    return results;
  }

}
