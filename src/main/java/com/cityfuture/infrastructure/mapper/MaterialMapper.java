package com.cityfuture.infrastructure.mapper;

import com.cityfuture.domain.model.Material;
import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MaterialMapper {
    MaterialEntity toEntity(Material material);
    Material toDomain(MaterialEntity entity);
}
