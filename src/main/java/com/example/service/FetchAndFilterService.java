package com.example.service;

import com.example.dto.Center;
import com.example.dto.District;
import com.example.dto.Session;
import com.example.dto.State;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class FetchAndFilterService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpEntity<String> httpEntity;

    @Autowired
    private Logger log;

    private String todaysDate;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Map<String, List<Center>>> getAvailableCenters() {
        setTodaysDate();
        List<State> states = getAllStates();
        Map<String, Map<String, List<Center>>> countryData = new HashMap<>();
//        for(int i=0;i<states.size();i++) {
//            State state = states.get(i);
        State state = states.stream().filter(state1 -> state1.getStateName().equals("Maharashtra")).findAny().get();
        List<District> districts = getAllDistrictsByState(state.getStateId());
        Map<String, List<Center>> stateData = new HashMap<>();
        for (District district : districts) {
            List<Center> centers = getAllCentersByDistrict(district.getDistrictId());
            List<Center> availableCenters = filterCenters(centers);
            if (!CollectionUtils.isEmpty(availableCenters))
                stateData.put(district.getDistrictName(), availableCenters);
        }
        countryData.put(state.getStateName(), stateData);
//        }
        return countryData;
    }

    private List<Center> filterCenters(List<Center> centers) {
        for (Center center : centers) {
            Predicate<Session> predicateCondition = session -> (session.getMinAgeLimit() == 18 && session.getAvailableCapacity() > 0);
            center.setSessions(center.getSessions().stream().filter(session -> predicateCondition.test(session)).collect(Collectors.toList()));
        }
        return centers.stream().filter(center -> !CollectionUtils.isEmpty(center.getSessions())).collect(Collectors.toList());
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
        todaysDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
