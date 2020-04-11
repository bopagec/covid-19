package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.component.DailyReport;
import com.blackpawsys.api.covid19.service.Covid19Service;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/covid-19/report")
@Slf4j
public class ReportController {

  @Autowired
  private Covid19Service dataService;

  @GetMapping("/{date}")
  public List<DailyReport> dailyRecord(@PathVariable String date) {
    log.info("dailyRecord method called: {}", date);
    return dataService.findByDate(LocalDate.parse(date, RecordUtil.FORMATTER));
  }

  @GetMapping("/country/{country}")
  public List<DailyReport> countryReport(@PathVariable String country) {
    log.info("countryReport method called : {}", country);
    return dataService.findByCountry(country);
  }
}
