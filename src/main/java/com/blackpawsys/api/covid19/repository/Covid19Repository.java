package com.blackpawsys.api.covid19.repository;

import com.blackpawsys.api.covid19.model.Record;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Covid19Repository extends MongoRepository<Record, String> {

}
