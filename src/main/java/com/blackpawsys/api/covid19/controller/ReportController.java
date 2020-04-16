package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.dto.DailyReportDto;
import com.blackpawsys.api.covid19.component.Response;
import com.blackpawsys.api.covid19.dto.DailyReportDataDto;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.service.Covid19Service;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

  @GetMapping(value = {"/all", "/{optDate}"})
  public Response<DailyReportDataDto> dailyRecord(@PathVariable Optional<String> optDate) {
    String date = optDate.isEmpty() ? RecordUtil.FORMATTER.format(LocalDate.now().minusDays(1)) : optDate.get();

    log.info("dailyRecord method called: {} {}", date, optDate.isEmpty());

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
    String decodedCountry = null;
    try {
      decodedCountry = URLDecoder.decode(country, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage());
    }

    log.info("countryReport method called : {}", decodedCountry);

    Response<DailyReportDataDto> response = new Response<>();

    List<Record> records = dataService.findByCountry(decodedCountry);
    List<DailyReportDto> dailyReportDtoList = RecordUtil.createDailyReportList(records);

    DailyReportDataDto dailyReportDataDto = DailyReportDataDto.builder()
        .dailyReportDtoList(dailyReportDtoList)
        .summary(RecordUtil.createSummary(dailyReportDtoList))
        .build();

    response.setCode("200");
    response.setPayload(dailyReportDataDto);

    return response;
  }
}
