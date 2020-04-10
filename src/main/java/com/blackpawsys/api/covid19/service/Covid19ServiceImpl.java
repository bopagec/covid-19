package com.blackpawsys.api.covid19.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import com.blackpawsys.api.covid19.component.DailyReport;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.repository.Covid19Repository;
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
  public List<Record> findAll(Sort sort) {
    return repository.findAll(sort);
  }

  @Override
  public List<DailyReport> findByDate(String date) {
    MatchOperation matchOperation = new MatchOperation(Criteria.where("lastUpdated").is(date));

    GroupOperation groupOperation = group("country")
        .first("country").as("country")
        .sum("confirmed").as("confirmed")
        .sum("deaths").as("deaths")
        .sum("newCases").as("newCases")
        .sum("newDeaths").as("newDeaths");

    Aggregation aggregation = newAggregation(
        matchOperation,
        groupOperation,
        sort(Direction.DESC, "confirmed")
    );

    AggregationResults<DailyReport> aggregate = mongoTemplate.aggregate(aggregation, Record.class, DailyReport.class);

    List<DailyReport> mappedResults = aggregate.getMappedResults();

    return mappedResults;
  }

  @Override
  public Optional<Record> findByCountry(Record record, String date) {
    List<Record> records = repository.findByCountryAndStateAndLastUpdatedAndLatAndLongt(record.getCountry(), record.getState(), date, record.getLat(), record.getLongt());

    if (!records.isEmpty()) {
      return Optional.of(records.get(0));
    }

    return Optional.empty();
  }

  @Override
  public List<DailyReport> findByCountry(String country) {
    return repository.findByCountry(country, Sort.by("_id").descending());
  }

}
