package com.blackpawsys.api.covid19.service;

import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.repository.Covid19Repository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class Covid19ServiceImpl implements Covid19Service {

  @Autowired
  private Covid19Repository repository;

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

}
