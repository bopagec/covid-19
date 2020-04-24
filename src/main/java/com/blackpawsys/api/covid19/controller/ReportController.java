package com.blackpawsys.api.covid19.controller;

import com.blackpawsys.api.covid19.Util.RecordUtil;
import com.blackpawsys.api.covid19.component.Response;
import com.blackpawsys.api.covid19.dto.DailyReportDataDto;
import com.blackpawsys.api.covid19.dto.DailyReportDto;
import com.blackpawsys.api.covid19.dto.WorldGraphDataDto;
import com.blackpawsys.api.covid19.model.Record;
import com.blackpawsys.api.covid19.service.Covid19Service;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/covid-19/report")
@Slf4j
public class ReportController {

  @Autowired
  private Covid19Service dataService;

  @Value("${covid19.service.api.user.name}")
  private String userName;

  @Value("${covid19.service.api.user.password}")
  private String password;

  @GetMapping(value = {"/all", "/{optDate}"})
  public Response<DailyReportDataDto> dailyRecord(@PathVariable Optional<String> optDate, @RequestHeader(value = "Authorization") String token) {
    boolean authenticated = isAuthenticated(token);

    if (!authenticated) {
      Response<DailyReportDataDto> response = new Response<>();
      response.setCode(HttpStatus.UNAUTHORIZED.toString());
      return response;
    }

    String date = !optDate.isPresent() ? RecordUtil.FORMATTER.format(LocalDate.now().minusDays(1)) : optDate.get();
    log.info("dailyRecord method called: {} ", date);
    LocalDateTime dateTime = LocalDate.parse(date, RecordUtil.FORMATTER).atStartOfDay();
    Response<DailyReportDataDto> response = new Response<>();

    List<Record> records = dataService.findByDate(dateTime, Optional.of("country"));
    List<WorldGraphDataDto> worldGraphData = dataService.generateWorldGraphData(dateTime);

    List<DailyReportDto> dailyReportDtoList = RecordUtil.createDailyReportList(records);

    DailyReportDataDto dailyReportDataDto = DailyReportDataDto.builder()
        .dailyReportDtoList(dailyReportDtoList)
        .summary(RecordUtil.createSummary(dailyReportDtoList))
        .worldGraphData(worldGraphData)
        .build();

    response.setCode("200");
    response.setPayload(dailyReportDataDto);

    return response;
  }

  private boolean isAuthenticated(String token) {
    if (!token.startsWith("Basic ")) {
      return false;
    }

    String encodedCredentials = token.substring(token.indexOf(" ") + 1, token.length());
    byte[] decoded = Base64Utils.decode(encodedCredentials.getBytes(StandardCharsets.UTF_8));
    String decodedCredentials = new String(decoded, StandardCharsets.UTF_8);
    StringTokenizer stringTokenizer = new StringTokenizer(decodedCredentials, ":");

    return stringTokenizer.nextToken().equals(userName) && stringTokenizer.nextToken().equals(password);

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
        .summary(RecordUtil.createCountrySummary(dailyReportDtoList))
        .build();

    response.setCode("200");
    response.setPayload(dailyReportDataDto);

    return response;
  }
}
