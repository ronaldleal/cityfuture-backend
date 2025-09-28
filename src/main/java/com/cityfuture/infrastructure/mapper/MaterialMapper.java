package com.cityfuture.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.cityfuture.domain.model.Material;
import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;

@Mapper(componentModel = "spring")
public interface MaterialMapper {
    MaterialEntity toEntity(Material material);
    Material toDomain(MaterialEntity entity);
}
