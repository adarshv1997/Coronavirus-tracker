package io.adarsh.coronavirustracker.services;

import io.adarsh.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String CORONA_VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *") // second minute hour day month  --> at the 1st hour of every day
    public void fetchCoronaVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                                  .uri(URI.create(CORONA_VIRUS_DATA_URL))
                                  .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        /*System.out.println(httpResponse.body());*/

        StringReader csvBodyReader = new StringReader(httpResponse.body());

        CSVParser parse = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        List<CSVRecord> records = parse.getRecords();

        for(CSVRecord record : records) {
            /*if(record.get("Country/Region").matches("India")) {*/
                int prevDayCases = 0;
                LocationStats locationStat = new LocationStats();
                locationStat.setDate(parse.getHeaderNames().get(parse.getHeaderNames().size() - 1));
                locationStat.setState(record.get("Province/State"));
                locationStat.setCountry(record.get("Country/Region"));
                locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size() - 1)));
                int latestCases = Integer.parseInt(record.get(record.size() - 1));
                if(record.size() != 1) {
                    prevDayCases = Integer.parseInt(record.get(record.size() - 2));
                }
                locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
                newStats.add(locationStat);
            /*}*/
        }

        this.allStats = newStats;
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }
}
