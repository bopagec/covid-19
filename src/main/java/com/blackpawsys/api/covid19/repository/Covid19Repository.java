package com.blackpawsys.api.covid19.repository;

import com.blackpawsys.api.covid19.model.Record;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Covid19Repository extends MongoRepository<Record, String> {

  List<Record> findByCountryAndStateAndLastUpdatedAndLatAndLongt(String country, String state, LocalDate lastUpdated, String lat, String longt);

  Record findFirstByOrderByLastUpdatedDesc();
}
