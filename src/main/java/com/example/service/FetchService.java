package com.example.service;

import com.example.dto.Center;
import com.example.dto.District;
import com.example.dto.State;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FetchService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpEntity<String> httpEntity;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterService filterService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Value("${session.history.file.name}")
    private String fileName;

    private String todaysDate;

    @Scheduled(fixedDelayString = "${scheduled.delay.milliseconds.time}")
    public Map<String, Map<String, List<Center>>> getAvailableCenters() {
        Map<String, Map<String, List<Center>>> countryData = new HashMap<>();
        List<State> states = getAllStates();
        State state = filterService.getState(states);

        if (null != state) {
            Map<String, List<Center>> stateData = new HashMap<>();
            List<District> districts = getAllDistrictsByState(state.getStateId());
            District district = filterService.getDistrict(districts);

            if (null != district) {
                setTodaysDate();
                List<Center> centers = getAllCentersByDistrict(district.getDistrictId());
                List<Center> availableCenters = filterService.filterByConfigurations(centers);

                if (!CollectionUtils.isEmpty(availableCenters)) {
                    Set<String> alreadyNotifiedSessionIds = readAlreadyNotifiedSessionIds();
                    Set<String> allSessionIds = filterService.filterByAlreadyNotifiedSessions(availableCenters, alreadyNotifiedSessionIds);
                    writeNotifiedSessionIds(allSessionIds);

                    List<Center> newCenters = filterService.filterCentersWithEmptySessions(availableCenters);
                    if (!CollectionUtils.isEmpty(newCenters)) {
                        stateData.put(district.getDistrictName(), newCenters);
                        countryData.put(state.getStateName(), stateData);
                        emailService.sendMail(countryData);
                    }
                }
            }
        }
        return countryData;
    }

    private Set<String> readAlreadyNotifiedSessionIds() {
        Set<String> sessionIds = null;
        try {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            sessionIds = (HashSet<String>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null != sessionIds ? sessionIds : new HashSet<>();
    }

    private void writeNotifiedSessionIds(Set<String> sessionIds) {
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(sessionIds);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<State> getAllStates() {
        String url = "https://cdn-api.co-vin.in/api/v2/admin/location/states";
        List<State> states = getResponse(url, "states", new TypeReference<List<State>>() {
        });
        return states;
    }

    private List<District> getAllDistrictsByState(Long stateId) {
        String url = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/" + stateId;
        List<District> districts = getResponse(url, "districts", new TypeReference<List<District>>() {
        });
        return districts;
    }

    public List<Center> getAllCentersByDistrict(Long districtId) {
        StringBuilder urlStringBuilder = new StringBuilder("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?")
                .append("district_id=").append(districtId).append("&")
                .append("date=").append(todaysDate);
        List<Center> centers = getResponse(urlStringBuilder.toString(), "centers", new TypeReference<List<Center>>() {
        });
        return centers;
    }

    public <T> List<T> getResponse(String url, String key, TypeReference<List<T>> typeReference) {
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
        Object object = responseEntity.getBody().get(key);
        List<T> response = objectMapper.convertValue(object, typeReference);
        return response;
    }

    private void setTodaysDate() {
        todaysDate = simpleDateFormat.format(new Date());
    }
}
