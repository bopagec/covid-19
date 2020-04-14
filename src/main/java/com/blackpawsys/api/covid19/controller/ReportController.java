package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.dto.DailyReportDto;
import com.blackpawsys.api.covid19.component.Response;
import com.blackpawsys.api.covid19.dto.DailyReportDataDto;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.service.Covid19Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
  public Response<DailyReportDataDto> dailyRecord(@PathVariable String date) {
    log.info("dailyRecord method called: {}", date);

    Response<DailyReportDataDto> response = new Response<>();
    List<Record> records = dataService.findByDate(LocalDate.parse(date, RecordUtil.FORMATTER), Optional.of("country"));
    List<DailyReportDto> dailyReportDtoList = RecordUtil.createDailyReportList(records);

    DailyReportDataDto dailyReportDataDto = DailyReportDataDto.builder()
        .dailyReportDtoList(dailyReportDtoList)
        .summary(RecordUtil.createSummary(dailyReportDtoList))
        .build();

    response.setCode("200");
    response.setPayload(dailyReportDataDto);

    return response;
  }

  @GetMapping("/country/{country}")
  public Response<DailyReportDataDto> countryReport(@PathVariable String country) {
    log.info("countryReport method called : {}", country);

    Response<DailyReportDataDto> response = new Response<>();

    DailyReportDataDto dailyReportDataDto = dataService.findByCountry(country);

    response.setCode("200");
    response.setPayload(dailyReportDataDto);

    return response;
  }
}
