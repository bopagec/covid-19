package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.service.Covid19Service;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/covid-19")
@Slf4j
public class UpdateController {

  @Autowired
  private Covid19Service dataService;

  private LocalDate firstDate = LocalDate.of(2020, 01, 22);

  @Autowired
  private RestTemplate restTemplate;

  @GetMapping("/fetch")
  public String fetchAllRecords() {
    log.info("fetchAllRecords method called.");
    dataService.deleteAll();
    log.info("all records deleted.");
    LocalDate currLocalDate = firstDate;

    fetchAndSave(currLocalDate);

    return "done";
  }

  @Scheduled(cron = "0 0 5 * * *")
  @PostConstruct
  @GetMapping("/update")
  public String updateRecords() {
    log.info("update method called.");
    Record record = dataService.findLatestRecord();

    if (record != null) {
      if (record.getLastUpdated().isBefore(LocalDate.now())) {
        fetchAndSave(record.getLastUpdated().plusDays(1));
        return "records updated.";
      }
    } else {
      fetchAllRecords();
    }
    return "no new records found.";
  }

  private void fetchAndSave(LocalDate currLocalDate) {
    LocalDate date = currLocalDate;

    while (date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now())) {

      try {
        String recordStr = restTemplate.getForObject(RecordUtil.createReportUrl(date), String.class);
        List<Record> recordList = RecordUtil.parseRecord(recordStr, date);
        updateRecordFields(recordList, date);

        dataService.saveAll(recordList);
        log.info("saved: {} records from {}", recordList.size(), date.toString());

      } catch (HttpClientErrorException e) {
        log.info(e.getMessage() + ":" + date.toString());
      } catch (FileNotFoundException fne) {
        log.info(fne.getMessage());
      } catch (IOException ioe) {
        log.info(ioe.getMessage());
      } finally {
        date = date.plusDays(1);
      }
    }
  }

  public List<Record> updateRecordFields(List<Record> records, LocalDate date) {

    records.stream().forEach(record -> {
      dataService.findByCountry(record, date.minusDays(1))
          .ifPresent(oldRecord -> {
            if (oldRecord.getConfirmed() != null) {
              record.setNewCases(record.getConfirmed() - oldRecord.getConfirmed());
            }
            if (oldRecord.getDeaths() != null) {
              record.setNewDeaths(record.getDeaths() - oldRecord.getDeaths());
            }
          });
    });

    return records;
  }

}
