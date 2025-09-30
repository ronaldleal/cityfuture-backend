package com.cityfuture.infrastructure.service;

import com.cityfuture.api.exception.MaterialAlreadyExistsException;
import com.cityfuture.api.exception.MaterialNotFoundException;
import com.cityfuture.domain.model.Material;
import com.cityfuture.infrastructure.mapper.MaterialMapper;
import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
import com.cityfuture.infrastructure.persistence.entity.MaterialStockEntity;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceUseCaseTest {

    @Mock
    private JpaMaterialRepository materialRepository;

    @Mock
    private JpaMaterialStockRepository stockRepository;

    @Mock
    private MaterialMapper mapper;

    @InjectMocks
    private MaterialServiceUseCase materialServiceUseCase;

    private Material testMaterial;
    private MaterialEntity testEntity;
    private MaterialStockEntity testStock;

    @BeforeEach
    void setUp() {
        // Material domain model
        testMaterial = new Material(1L, "Cemento", "Ce", 100);

        // Material entity
        testEntity = new MaterialEntity();
        testEntity.setId(1L);
        testEntity.setMaterialName("Cemento");
        testEntity.setCode("Ce");
        testEntity.setQuantity(100);

        // Material stock entity
        testStock = new MaterialStockEntity();
        testStock.setId(1L);
        testStock.setMaterial(testEntity);
        testStock.setQuantity(0);
    }

    @Test
    void createMaterial_ValidMaterial_ReturnsCreatedMaterial() {
        // Arrange
        when(materialRepository.findByMaterialName("Cemento")).thenReturn(Optional.empty());
        when(mapper.toEntity(testMaterial)).thenReturn(testEntity);
        when(materialRepository.save(any(MaterialEntity.class))).thenReturn(testEntity);
        when(stockRepository.findByMaterialId(1L)).thenReturn(Optional.empty());
        when(stockRepository.save(any(MaterialStockEntity.class))).thenReturn(testStock);
        when(mapper.toDomain(testEntity)).thenReturn(testMaterial);

        // Act
        Material result = materialServiceUseCase.createMaterial(testMaterial);

        // Assert
        assertNotNull(result);
        assertEquals("Cemento", result.materialName());
        assertEquals("Ce", result.code());
        assertEquals(100, result.quantity());

        verify(materialRepository).findByMaterialName("Cemento");
        verify(materialRepository).save(any(MaterialEntity.class));
        verify(stockRepository).findByMaterialId(1L);
        verify(stockRepository).save(any(MaterialStockEntity.class));
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void createMaterial_MaterialAlreadyExists_ThrowsException() {
        // Arrange
        when(materialRepository.findByMaterialName("Cemento")).thenReturn(Optional.of(testEntity));

        // Act & Assert
        MaterialAlreadyExistsException exception = assertThrows(
            MaterialAlreadyExistsException.class,
            () -> materialServiceUseCase.createMaterial(testMaterial)
        );

        assertTrue(exception.getMessage().contains("ya existe"));
        verify(materialRepository, never()).save(any());
        verify(stockRepository, never()).save(any());
    }

    @Test
    void createMaterial_StockAlreadyExists_SkipsStockCreation() {
        // Arrange
        when(materialRepository.findByMaterialName("Cemento")).thenReturn(Optional.empty());
        when(mapper.toEntity(testMaterial)).thenReturn(testEntity);
        when(materialRepository.save(any(MaterialEntity.class))).thenReturn(testEntity);
        when(stockRepository.findByMaterialId(1L)).thenReturn(Optional.of(testStock));
        when(mapper.toDomain(testEntity)).thenReturn(testMaterial);

        // Act
        Material result = materialServiceUseCase.createMaterial(testMaterial);

        // Assert
        assertNotNull(result);
        verify(stockRepository, never()).save(any());
    }

    @Test
    void getAllMaterials_ReturnsMaterialList() {
        // Arrange
        List<MaterialEntity> entities = Arrays.asList(testEntity);
        when(materialRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testMaterial);

        // Act
        List<Material> result = materialServiceUseCase.getAllMaterials();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cemento", result.get(0).materialName());

        verify(materialRepository).findAll();
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void getAllMaterials_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(materialRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Material> result = materialServiceUseCase.getAllMaterials();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(materialRepository).findAll();
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void getMaterialById_ExistingId_ReturnsMaterial() {
        // Arrange
        when(materialRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testMaterial);

        // Act
        Material result = materialServiceUseCase.getMaterialById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Cemento", result.materialName());

        verify(materialRepository).findById(1L);
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void getMaterialById_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        MaterialNotFoundException exception = assertThrows(
            MaterialNotFoundException.class,
            () -> materialServiceUseCase.getMaterialById(999L)
        );

        assertTrue(exception.getMessage().contains("No existe un material"));
        verify(materialRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void updateMaterial_ExistingMaterial_UpdatesSuccessfully() {
        // Arrange
        Material updatedMaterial = new Material(1L, "Cemento Premium", "Ce", 150);
        MaterialEntity updatedEntity = new MaterialEntity();
        updatedEntity.setId(1L);
        updatedEntity.setMaterialName("Cemento Premium");
        updatedEntity.setCode("Ce");
        updatedEntity.setQuantity(150);

        when(materialRepository.findByMaterialName("Cemento Premium"))
            .thenReturn(Optional.empty());
        when(materialRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mapper.toEntity(updatedMaterial)).thenReturn(updatedEntity);
        when(materialRepository.save(any(MaterialEntity.class))).thenReturn(updatedEntity);
        when(mapper.toDomain(updatedEntity)).thenReturn(updatedMaterial);

        // Act
        Material result = materialServiceUseCase.updateMaterial(1L, updatedMaterial);

        // Assert
        assertNotNull(result);
        assertEquals("Cemento Premium", result.materialName());
        assertEquals(150, result.quantity());

        verify(materialRepository).findById(1L);
        verify(materialRepository).save(any(MaterialEntity.class));
        verify(mapper).toDomain(updatedEntity);
    }

    @Test
    void updateMaterial_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        Material updatedMaterial = new Material(999L, "Material Test", "MT", 50);
        when(materialRepository.findByMaterialName("Material Test"))
            .thenReturn(Optional.empty());
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        MaterialNotFoundException exception = assertThrows(
            MaterialNotFoundException.class,
            () -> materialServiceUseCase.updateMaterial(999L, updatedMaterial)
        );

        assertTrue(exception.getMessage().contains("No existe un material"));
        verify(materialRepository).findById(999L);
        verify(materialRepository, never()).save(any());
    }

    @Test
    void updateMaterial_NameAlreadyExistsForDifferentMaterial_ThrowsAlreadyExistsException() {
        // Arrange
        MaterialEntity existingEntity = new MaterialEntity();
        existingEntity.setId(2L);
        existingEntity.setMaterialName("Cemento Premium");

        Material updatedMaterial = new Material(1L, "Cemento Premium", "Ce", 150);
        when(materialRepository.findByMaterialName("Cemento Premium"))
            .thenReturn(Optional.of(existingEntity));

        // Act & Assert
        MaterialAlreadyExistsException exception = assertThrows(
            MaterialAlreadyExistsException.class,
            () -> materialServiceUseCase.updateMaterial(1L, updatedMaterial)
        );

        assertTrue(exception.getMessage().contains("ya existe"));
        verify(materialRepository, never()).findById(any());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void updateMaterial_SameNameForSameMaterial_AllowsUpdate() {
        // Arrange
        Material updatedMaterial = new Material(1L, "Cemento", "Ce", 150);
        MaterialEntity updatedEntity = new MaterialEntity();
        updatedEntity.setId(1L);
        updatedEntity.setMaterialName("Cemento");
        updatedEntity.setCode("Ce");
        updatedEntity.setQuantity(150);

        when(materialRepository.findByMaterialName("Cemento"))
            .thenReturn(Optional.of(testEntity));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mapper.toEntity(updatedMaterial)).thenReturn(updatedEntity);
        when(materialRepository.save(any(MaterialEntity.class))).thenReturn(updatedEntity);
        when(mapper.toDomain(updatedEntity)).thenReturn(updatedMaterial);

        // Act
        Material result = materialServiceUseCase.updateMaterial(1L, updatedMaterial);

        // Assert
        assertNotNull(result);
        assertEquals("Cemento", result.materialName());
        assertEquals(150, result.quantity());

        verify(materialRepository).save(any(MaterialEntity.class));
    }

    @Test
    void deleteMaterial_ExistingMaterial_DeletesSuccessfully() {
        // Arrange
        when(materialRepository.existsById(1L)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> materialServiceUseCase.deleteMaterial(1L));

        // Assert
        verify(materialRepository).existsById(1L);
        verify(materialRepository).deleteById(1L);
    }

    @Test
    void deleteMaterial_NonExistingMaterial_ThrowsRuntimeException() {
        // Arrange
        when(materialRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> materialServiceUseCase.deleteMaterial(999L)
        );

        assertTrue(exception.getMessage().contains("No existe un material"));
        verify(materialRepository).existsById(999L);
        verify(materialRepository, never()).deleteById(any());
    }

    @Test
    void deleteMaterial_DatabaseError_ThrowsRuntimeException() {
        // Arrange
        when(materialRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("Database connection error"))
            .when(materialRepository).deleteById(1L);

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> materialServiceUseCase.deleteMaterial(1L)
        );

        assertEquals("Database connection error", exception.getMessage());
        verify(materialRepository).deleteById(1L);
    }

    @Test
    void createMaterial_UnexpectedError_ThrowsRuntimeException() {
        // Arrange
        when(materialRepository.findByMaterialName("Cemento")).thenReturn(Optional.empty());
        when(mapper.toEntity(testMaterial)).thenReturn(testEntity);
        when(materialRepository.save(any(MaterialEntity.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> materialServiceUseCase.createMaterial(testMaterial)
        );

        assertTrue(exception.getMessage().contains("Error interno al crear"));
        verify(materialRepository).save(any(MaterialEntity.class));
    }
}