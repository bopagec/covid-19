package com.blackpawsys.api.covid19.Util;

import com.blackpawsys.api.covid19.model.Record;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RecordUtil {

  private static final String UTF8_BOM = "\uFEFF";
  private static final String FILE_TYPE = "csv";
  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
  private static String reportUrl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";

  public static String createReportUrl(LocalDate currLocalDate) {
    StringBuilder sb = new StringBuilder();

    sb.append(reportUrl)
        .append(currLocalDate.format(FORMATTER))
        .append(".")
        .append(FILE_TYPE);

    return sb.toString();
  }

  public static List<Record> parseRecord(String recordStr, LocalDate lastUpdate) throws IOException {
    StringReader in = new StringReader(removeUTF8BOM(recordStr));
    List<Record> recordList = new ArrayList<>();

    if (!StringUtils.isEmpty(recordStr)) {

      Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
      for (CSVRecord rec : records) {
        String state = (rec.isMapped("Province/State") ? rec.get("Province/State") : null);
        String country = (rec.isMapped("Country/Region") ? rec.get("Country/Region") : null);
        String combinedKey = (rec.isMapped("Combined_Key") ? rec.get("Combined_Key") : null);
        Long confirmed = (rec.isMapped("Confirmed") && !StringUtils.isEmpty(rec.get("Confirmed"))) ? Long.parseLong(rec.get("Confirmed")) : null;
        Long deaths = (rec.isMapped("Deaths") && !StringUtils.isEmpty(rec.get("Deaths"))) ? Long.parseLong(rec.get("Deaths")) : null;

        Record record = Record.builder()
            .state(state)
            .country(country)
            .combinedKey(combinedKey)
            .confirmed(confirmed)
            .deaths(deaths)
            .lastUpdated(lastUpdate)
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
