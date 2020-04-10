package com.blackpawsys.api.covid19.Util;

import com.blackpawsys.api.covid19.model.Record;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecordUtil {
  private static final String UTF8_BOM = "\uFEFF";
  private static final String FILE_TYPE = "csv";
  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
  private static String reportUrl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";
  private static List<String> OVERSEAS_TERRITORY_GOVERN_ = new ArrayList<>(Arrays.asList("denmark", "france", "netherlands", "united kingdom"));

  public static String createReportUrl(LocalDate currLocalDate) {
    StringBuilder sb = new StringBuilder();

    sb.append(reportUrl)
        .append(currLocalDate.format(FORMATTER))
        .append(".")
        .append(FILE_TYPE);

    return sb.toString();
  }
  public static String formatLatLong(String val){
    int i = StringUtils.indexOf(val, ".");

    if(i > 1 ){
      return StringUtils.substring(val, 0, i + 2);
    }

    return val;
  }

  public static List<Record> parseRecord(String recordStr, LocalDate lastUpdate) throws IOException {
    StringReader in = new StringReader(removeUTF8BOM(recordStr));
    List<Record> recordList = new ArrayList<>();

    if (!StringUtils.isEmpty(recordStr)) {

      Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
      for (CSVRecord rec : records) {
        String state = (rec.isMapped("Province/State") ? rec.get("Province/State") : (rec.isMapped("Province_State") ? rec.get("Province_State") : null));
        String country = (rec.isMapped("Country/Region") ? rec.get("Country/Region") : (rec.isMapped("Country_Region") ? rec.get("Country_Region") : null));
        String combinedKey = (rec.isMapped("Combined_Key") ? rec.get("Combined_Key") : null);
        Long confirmed = (rec.isMapped("Confirmed") && !StringUtils.isEmpty(rec.get("Confirmed"))) ? Long.parseLong(rec.get("Confirmed")) : null;
        Long deaths = (rec.isMapped("Deaths") && !StringUtils.isEmpty(rec.get("Deaths"))) ? Long.parseLong(rec.get("Deaths")) : null;
        String lat = rec.isMapped("Lat") ? formatLatLong(rec.get("Lat")) : null;
        String longt = rec.isMapped("Long_") ? formatLatLong(rec.get("Long_")) : null;

        List<String> combinedKeyList = Collections.emptyList();

        if(StringUtils.isEmpty(state) && StringUtils.isEmpty(combinedKey)){
          combinedKey = country;
        }
        else if(state.equalsIgnoreCase(country) && StringUtils.isEmpty(combinedKey)){
          combinedKey = country;
        }
        if(!StringUtils.isEmpty(combinedKey)){
          String[] keys = StringUtils.split(combinedKey, ",");
          combinedKeyList = Stream.of(keys).filter(key -> !StringUtils.isEmpty(key)).collect(Collectors.toList());
        }

        if(!StringUtils.isEmpty(state) && OVERSEAS_TERRITORY_GOVERN_.contains(country.toLowerCase())){
          country = state;
        }

        Record record = Record.builder()
            .state(state)
            .country(country)
            .combinedKey(combinedKeyList)
            .confirmed(confirmed)
            .deaths(deaths)
            .lastUpdated(lastUpdate.format(FORMATTER))
            .lat(lat)
            .longt(longt)
            .build();

        recordList.add(record);

      }
    }
    return recordList;
  }

  private static String removeUTF8BOM(String s) {
    if (s.startsWith(UTF8_BOM)) {
      s = s.substring(1);
    }

    return s;
  }
}
