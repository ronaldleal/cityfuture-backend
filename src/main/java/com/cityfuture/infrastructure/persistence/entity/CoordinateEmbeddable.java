package com.cityfuture.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class CoordinateEmbeddable {
    private Double latitude;
    private Double longitude;
}
