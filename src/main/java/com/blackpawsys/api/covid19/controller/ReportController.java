package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.component.ComparisonReport;
import com.blackpawsys.api.covid19.component.DailyReport;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.service.Covid19Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/covid-19/stats")
public class ReportController {

  @Autowired
  private Covid19Service dataService;

  @GetMapping("/{date}")
  public List<ComparisonReport> dailyRecord(@PathVariable String date) {
    List<ComparisonReport> comparisonReports = new ArrayList<>();

    LocalDate localDate = LocalDate.parse(date, RecordUtil.FORMATTER);

    List<DailyReport> currDayReport = dataService.findByDate(date);
    List<DailyReport> previousDayReport = dataService.findByDate(RecordUtil.FORMATTER.format(localDate.minusDays(1)));

    currDayReport.stream().forEach(currReport -> {
      Optional<DailyReport> prevReport = previousDayReport.stream().filter(report -> report.getCountry().equalsIgnoreCase(currReport.getCountry()))
          .findFirst();

      prevReport.ifPresent(rep -> {
        ComparisonReport comparisonReport = ComparisonReport.builder()
            .confirmed(currReport.getConfirmed())
            .deaths(currReport.getDeaths())
            .country(currReport.getCountry())
            .newCases(currReport.getConfirmed() - prevReport.get().getConfirmed())
            .newDeaths(currReport.getDeaths() - prevReport.get().getDeaths())
            .build();

        comparisonReports.add(comparisonReport);

      });
    });

    return comparisonReports;
  }
}
