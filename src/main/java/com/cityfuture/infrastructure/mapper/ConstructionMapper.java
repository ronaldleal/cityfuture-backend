package com.cityfuture.infrastructure.mapper;

import org.mapstruct.Mapper;
import com.cityfuture.domain.model.ConstructionOrder;
import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;

@Mapper(componentModel = "spring")
public interface ConstructionMapper {

    ConstructionOrderEntity toEntity(ConstructionOrder domain);

    ConstructionOrder toDomain(ConstructionOrderEntity entity);
}
