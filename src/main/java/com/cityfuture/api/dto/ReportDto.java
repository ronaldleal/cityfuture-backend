package com.cityfuture.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReportDto {
    private List<String> pendingConstructions;
    private Map<String, Integer> finishedByType;
    private Map<String, Integer> inProgressByType;
    private String projectEndDate;
}
