package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.component.DailyReport;
import com.blackpawsys.api.covid19.service.Covid19Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/covid-19/report")
public class ReportController {

  @Autowired
  private Covid19Service dataService;

  @GetMapping("/{date}")
  public List<DailyReport> dailyRecord(@PathVariable String date) {
    return dataService.findByDate(date);
  }

  @GetMapping("/country/{country}")
  public List<DailyReport> countryReport(@PathVariable String country){
    return dataService.findByCountry(country);
  }
}
