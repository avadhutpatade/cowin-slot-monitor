package com.example.service;

import com.example.dto.Center;
import com.example.dto.District;
import com.example.dto.Session;
import com.example.dto.State;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class FilterService {

    @Value("${center.state.name}")
    private String stateName;

    @Value("${center.district.name}")
    private String districtName;

    @Value("${center.block.name}")
    private String blockName;

    @Value("${center.fee.type}")
    private String feeType;

    @Value("${center.session.min.age.limit}")
    private Long minAgeLimit;

    @Value("${center.session.vaccine.type}")
    private String vaccineType;

    @Value("${center.session.dose.number}")
    private Long doseNumber;

    public State getState(List<State> states) {
        return states.stream()
                .filter(state -> state.getStateName().equals(stateName))
                .findAny()
                .orElse(null);
    }

    public District getDistrict(List<District> districts) {
        return districts.stream()
                .filter(district -> district.getDistrictName().equals(districtName))
                .findAny()
                .orElse(null);
    }

    public List<Center> filterCenters(List<Center> centers) {
        for (Center center : centers) {
            center.setSessions(center.getSessions().stream().filter(getSessionPredicate()).collect(Collectors.toList()));
        }
        return centers.stream().filter(getCenterPredicate()).collect(Collectors.toList());
    }

    private Predicate<Session> getSessionPredicate() {
        return session -> {
            return session.getAvailableCapacity() > 0                              //capacityCheck
                    && minAgeLimit == null || minAgeLimit.equals(0L) || session.getMinAgeLimit().equals(minAgeLimit)            //minAgeCheck
                    && vaccineType == null || vaccineType.isBlank() || session.getVaccine().equals(vaccineType)                 //vaccineTypeCheck
                    && (doseNumber == null || doseNumber.equals(0L) || (doseNumber.equals(1L) ?
                        (session.getAvailableCapacityDose1() > 0L) :
                        (!doseNumber.equals(2L) || (session.getAvailableCapacityDose2() > 0L))))                                //doseNumberCheck
            ;
        };
    }

    private Predicate<Center> getCenterPredicate() {
        return center -> {
            return !CollectionUtils.isEmpty(center.getSessions())                              //sessionsAvailabilityCheck
                    && blockName == null || blockName.isBlank() || center.getBlockName().equals(blockName)                  //blockNameCheck
                    && feeType == null || feeType.isBlank() || center.getFeeType().equals(feeType)                          //feeTypeCheck
            ;
        };
    }
}
