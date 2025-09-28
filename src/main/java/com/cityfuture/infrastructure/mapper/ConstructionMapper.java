package com.cityfuture.infrastructure.mapper;

import com.cityfuture.domain.model.ConstructionOrder;
import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConstructionMapper {

    ConstructionOrderEntity toEntity(ConstructionOrder domain);

    ConstructionOrder toDomain(ConstructionOrderEntity entity);
}
