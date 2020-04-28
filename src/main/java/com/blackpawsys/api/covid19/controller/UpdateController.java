package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.service.Covid19Service;
import com.blackpawsys.mail.Mail;
import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${app.notifications.email.address}")
  private String email;

  private LocalDateTime startDate = LocalDateTime.of(2020, 01, 22, 23, 00, 00);
  private LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 00, 00));
  private static final int PARTITION_MAX_SIZE = 100;
  private Mail mail = new Mail();

  //private LocalDateTime startDate = LocalDateTime.of(2020, 04, 19, 23, 00, 00);
  //private LocalDateTime endDate = LocalDateTime.of(2020, 04, 20, 23, 00, 00);

  @Autowired
  private RestTemplate restTemplate;

  @GetMapping("/fetch")
  public String fetchAllRecords() {
    log.info("fetchAllRecords method called");
    dataService.deleteAll();
    log.info("all records deleted.");
    LocalDateTime currLocalDateTime = startDate;

    fetchAndSave(currLocalDateTime);
    return "done";
  }

  @Scheduled(cron = "0 30 5 * * *") // start at 5:30 AM
  @PostConstruct
  @GetMapping("/update")
  public String updateRecords() {
    log.info("update method called.");
    Record record = dataService.findLatestRecord();

    if (record != null) {
      if (record.getLastUpdated().isBefore(LocalDate.now().atStartOfDay())) {
        fetchAndSave(record.getLastUpdated().plusDays(1));
        return "records updated.";
      }
    } else {
      fetchAllRecords();
    }
    return "no new records found.";
  }

  private void fetchAndSave(LocalDateTime currLocalDateTime) {
    LocalDateTime date = currLocalDateTime;

    while (date.isBefore(endDate) || date.isEqual(endDate)) {

      try {
        String reportUrl = RecordUtil.createReportUrl(date);
        String recordStr = restTemplate.getForObject(reportUrl, String.class);
        List<Record> recordList = RecordUtil.parseRecord(recordStr, date);

        List<Record> stateCountries = aggregateStateToCountry(recordList, date);

        // remove states records now
        stateCountries.stream().forEach(stateCountry -> {
          List<Record> states = recordList.stream().filter(rec -> rec.getCountry().equals(stateCountry.getCountry())).collect(Collectors.toList());
          recordList.removeAll(states);
        });

        recordList.addAll(stateCountries);
        // we need to partition the list to smaller size to avoid mongodb data base issues (MongoDB Atlass FREE account)
        updateRecordFields(recordList, date);

        List<List<Record>> listPartitions = Lists.partition(recordList, PARTITION_MAX_SIZE);
        int total = 0;

        for (List partition : listPartitions) {
          dataService.saveAll(partition);
          log.info("saved: {} records from {}", partition.size(), date.toString());
          total += partition.size();
        }

        mail.sendMail(email, "SUCCESS!" + ". Records updated: " + total + " for " + date.toString() , "RECORD UPDATES SUCCEEDED");

      } catch (HttpClientErrorException e) {
        mail.sendMail(email, e.getMessage() + date.toString(), "HttpClientErrorException occurred during fetchAndSave");
        log.info(e.getMessage() + ":" + date.toString());
      } catch (FileNotFoundException fne) {
        mail.sendMail(email, fne.getMessage(), "FileNotFoundException occurred during fetchAndSave");
        log.info(fne.getMessage());
      } catch (Exception e) {
        mail.sendMail(email, e.getMessage(), "Exception occurred during fetchAndSave");
        log.info(e.getMessage());
      } finally {
        date = date.plusDays(1);
      }
    }
  }

  private List<Record> aggregateStateToCountry(List<Record> records, LocalDateTime date) {
    List<Record> stateCountryList = new ArrayList<>();

    records.stream().forEach(record -> {
      if (record.getCountry().equalsIgnoreCase(record.getState()) && record.getCombinedKey().size() == 0) {
        record.setCombinedKey(Arrays.asList(record.getCountry()));
      }
    });

    Map<String, List<Record>> stateCountryMap = records.stream()
        .filter(record -> record.getCombinedKey().size() > 1)
        .collect(Collectors.groupingBy(record -> record.getCountry()));

    stateCountryMap.entrySet().stream().forEach(entrySet -> {
      List<Record> stateRecords = entrySet.getValue();
      String country = entrySet.getKey();

      long totalCases = stateRecords.stream().filter(value -> value.getConfirmed() != null).mapToLong(value -> value.getConfirmed()).sum();
      long totalDeaths = stateRecords.stream().filter(value -> value.getDeaths() != null).mapToLong(value -> value.getDeaths()).sum();
      long totalNewCases = stateRecords.stream().filter(value -> value.getNewCases() != null).mapToLong(value -> value.getNewCases()).sum();
      long totalNewDeaths = stateRecords.stream().filter(value -> value.getNewDeaths() != null).mapToLong(value -> value.getNewDeaths()).sum();

      if (stateRecords.size() > 0) {

        Record record = Record.builder()
            .deaths(totalDeaths)
            .confirmed(totalCases)
            .newCases(totalNewCases)
            .newDeaths(totalNewDeaths)
            .country(country)
            .combinedKey(Arrays.asList(country))
            .state(country)
            .lastUpdated(date)
            .build();

        stateCountryList.add(record);
      }

    });

    return stateCountryList;
  }

  // this will generate null value map of record object that will help to generate history data based on the previous record
  // these data is used to determine the previous record.
  public Map<String, Object> valueMap(Record record, boolean isPrevious) {
    Map<String, Object> valueMap = new HashMap<>();

    if (record.getLastUpdated() != null) {
      valueMap.put("lastUpdated", isPrevious ? record.getLastUpdated().plusDays(1) : record.getLastUpdated());
    }
    if (!StringUtils.isEmpty(record.getCountry())) {
      valueMap.put("country", record.getCountry());
    }
    if (!StringUtils.isEmpty(record.getState())) {
      valueMap.put("state", record.getState());
    }

    return valueMap;
  }

  public boolean validateValueMaps(Map<String, Object> newRecordMap, Map<String, Object> oldRecordMap) {
    return newRecordMap.entrySet()
        .stream()
        .allMatch(e -> e.getValue().equals(oldRecordMap.get(e.getKey())));
  }

  public List<Record> updateRecordFields(List<Record> records, LocalDateTime date) {
    List<Record> prevRecords = dataService.findByDate(date.minusDays(1), Optional.empty());

    if (!prevRecords.isEmpty()) {
      records.stream().forEach(record -> {
        Map<String, Object> newValueMap = valueMap(record, false);

        for (Record prevRec : prevRecords) {
          Map<String, Object> prevValueMap = valueMap(prevRec, true);

          if (validateValueMaps(newValueMap, prevValueMap)) {
            if (record.getConfirmed() != null && prevRec.getConfirmed() != null && (record.getConfirmed() - prevRec.getConfirmed() > 0)) {
              record.setNewCases(record.getConfirmed() - prevRec.getConfirmed());
            }
            if (record.getDeaths() != null && prevRec.getDeaths() != null && (record.getDeaths() - prevRec.getDeaths() > 0)) {
              record.setNewDeaths(record.getDeaths() - prevRec.getDeaths());
            }
            break;
          }
        }
      });
    }

    return records;
  }

}
