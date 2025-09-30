package com.cityfuture.infrastructure.service;

import com.cityfuture.api.exception.ConstructionOrderNotFoundException;
import com.cityfuture.domain.exception.InsufficientMaterialException;
import com.cityfuture.domain.model.*;
import com.cityfuture.infrastructure.mapper.ConstructionMapper;
import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;
import com.cityfuture.infrastructure.persistence.entity.CoordinateEmbeddable;
import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
import com.cityfuture.infrastructure.persistence.repository.JpaConstructionOrderRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConstructionRequestUseCaseTest {

    @Mock
    private JpaConstructionOrderRepository orderRepository;

    @Mock
    private ConstructionMapper mapper;

    @Mock
    private JpaMaterialRepository materialRepository;

    @InjectMocks
    private ConstructionRequestUseCase constructionRequestUseCase;

    private ConstructionOrder testOrder;
    private ConstructionOrderEntity testEntity;
    private MaterialEntity cementoEntity;
    private MaterialEntity gravaEntity;

    @BeforeEach
    void setUp() {
        // Crear datos de prueba
        Coordinate location = new Coordinate(10.0, 20.0);
        testOrder = new ConstructionOrder(
            1L,
            "Casa del Futuro",
            location,
            "CASA",
            "Pendiente",
            5,
            LocalDate.now().plusDays(5)
        );

        CoordinateEmbeddable locationEmbeddable = new CoordinateEmbeddable();
        locationEmbeddable.setLatitude(10.0);
        locationEmbeddable.setLongitude(20.0);

        testEntity = new ConstructionOrderEntity();
        testEntity.setId(1L);
        testEntity.setProjectName("Casa del Futuro");
        testEntity.setLocation(locationEmbeddable);
        testEntity.setTypeConstruction("CASA");
        testEntity.setEstado("Pendiente");
        testEntity.setEstimatedDays(5);
        testEntity.setStartDate(LocalDate.now().plusDays(1));
        testEntity.setEntregaDate(LocalDate.now().plusDays(5));

        // Materiales de prueba
        cementoEntity = new MaterialEntity();
        cementoEntity.setId(1L);
        cementoEntity.setCode("Ce");
        cementoEntity.setMaterialName("Cemento");
        cementoEntity.setQuantity(100);

        gravaEntity = new MaterialEntity();
        gravaEntity.setId(2L);
        gravaEntity.setCode("Gr");
        gravaEntity.setMaterialName("Grava");
        gravaEntity.setQuantity(50);
    }

    @Test
    void createOrder_ValidOrder_ReturnsCreatedOrder() {
        // Arrange
        when(materialRepository.findByCode("Ce")).thenReturn(Optional.of(cementoEntity));
        when(materialRepository.findByCode("Gr")).thenReturn(Optional.of(gravaEntity));
        when(orderRepository.existsByLocationCoordinates(10.0, 20.0))
            .thenReturn(false);
        when(orderRepository.findFirstByOrderByEntregaDateDesc())
            .thenReturn(Optional.empty());
        when(mapper.toEntity(any(ConstructionOrder.class))).thenReturn(testEntity);
        when(orderRepository.save(any(ConstructionOrderEntity.class))).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testOrder);

        // Act
        ConstructionOrder result = constructionRequestUseCase.createOrder(testOrder);

        // Assert
        assertNotNull(result);
        assertEquals("Casa del Futuro", result.projectName());
        assertEquals("CASA", result.typeConstruction());
        assertEquals("Pendiente", result.estado());
        
        verify(orderRepository).save(any(ConstructionOrderEntity.class));
        verify(materialRepository, times(2)).save(any(MaterialEntity.class));
    }

    @Test
    void createOrder_InsufficientMaterials_ThrowsException() {
        // Arrange
        cementoEntity.setQuantity(1); // Insuficiente para CASA que requiere 5
        when(materialRepository.findByCode("Ce")).thenReturn(Optional.of(cementoEntity));
        when(materialRepository.findByCode("Gr")).thenReturn(Optional.of(gravaEntity));
        when(orderRepository.existsByLocationCoordinates(10.0, 20.0))
            .thenReturn(false);

        // Act & Assert
        InsufficientMaterialException exception = assertThrows(
            InsufficientMaterialException.class,
            () -> constructionRequestUseCase.createOrder(testOrder)
        );

        assertTrue(exception.getMessage().contains("Materiales insuficientes"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_LocationAlreadyOccupied_ThrowsException() {
        // Arrange
        when(orderRepository.existsByLocationCoordinates(10.0, 20.0))
            .thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(
            com.cityfuture.domain.exception.LocationAlreadyOccupiedException.class,
            () -> constructionRequestUseCase.createOrder(testOrder)
        );

        assertTrue(exception.getMessage().contains("coordenadas"));
        verify(orderRepository, never()).save(any());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void createOrder_InvalidConstructionType_ThrowsException() {
        // Arrange
        ConstructionOrder invalidOrder = new ConstructionOrder(
            null, "Test", new Coordinate(10.0, 20.0), "INVALID_TYPE", 
            "Pendiente", 5, LocalDate.now()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> constructionRequestUseCase.createOrder(invalidOrder)
        );

        assertTrue(exception.getMessage().contains("Tipo de construcción no válido"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_ExistingId_ReturnsOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testOrder);

        // Act
        ConstructionOrder result = constructionRequestUseCase.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Casa del Futuro", result.projectName());
        
        verify(orderRepository).findById(1L);
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void getOrderById_NonExistingId_ThrowsException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> constructionRequestUseCase.getOrderById(999L)
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(orderRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void getAllOrders_ReturnsAllOrders() {
        // Arrange
        List<ConstructionOrderEntity> entities = Arrays.asList(testEntity);
        when(orderRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testOrder);

        // Act
        List<ConstructionOrder> result = constructionRequestUseCase.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Casa del Futuro", result.get(0).projectName());
        
        verify(orderRepository).findAll();
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void getOrdersByStatus_ValidStatus_ReturnsFilteredOrders() {
        // Arrange
        List<ConstructionOrderEntity> entities = Arrays.asList(testEntity);
        when(orderRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testOrder);

        // Act
        List<ConstructionOrder> result = constructionRequestUseCase.getOrdersByStatus("Pendiente");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pendiente", result.get(0).estado());
        
        verify(orderRepository).findAll();
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void deleteOrder_ExistingOrder_DeletesSuccessfully() {
        // Arrange
        when(orderRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(materialRepository.findByCode("Ce")).thenReturn(Optional.of(cementoEntity));
        when(materialRepository.findByCode("Gr")).thenReturn(Optional.of(gravaEntity));

        // Act
        constructionRequestUseCase.deleteOrder(1L);

        // Assert
        verify(orderRepository).deleteById(1L);
        verify(materialRepository, times(2)).save(any(MaterialEntity.class));
    }

    @Test
    void deleteOrder_NonExistingOrder_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> constructionRequestUseCase.deleteOrder(999L)
        );

        assertTrue(exception.getMessage().contains("No existe una orden"));
        verify(orderRepository, never()).deleteById(any());
    }

    @Test
    void updateOrder_ExistingOrder_UpdatesSuccessfully() {
        // Arrange
        ConstructionOrder updatedOrder = new ConstructionOrder(
            1L, "Casa Actualizada", testOrder.location(), "CASA", 
            "Pendiente", 5, LocalDate.now()
        );
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(materialRepository.findByCode("Ce")).thenReturn(Optional.of(cementoEntity));
        when(materialRepository.findByCode("Gr")).thenReturn(Optional.of(gravaEntity));
        when(orderRepository.save(any(ConstructionOrderEntity.class))).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(updatedOrder);

        // Act
        ConstructionOrder result = constructionRequestUseCase.updateOrder(1L, updatedOrder);

        // Assert
        assertNotNull(result);
        assertEquals("Casa Actualizada", result.projectName());
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(ConstructionOrderEntity.class));
    }

    @Test
    void updateOrder_NonExistingOrder_ThrowsException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ConstructionOrderNotFoundException exception = assertThrows(
            ConstructionOrderNotFoundException.class,
            () -> constructionRequestUseCase.updateOrder(999L, testOrder)
        );

        assertTrue(exception.getMessage().contains("No existe una orden"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void validateConstructionRequest_ValidRequest_ReturnsValidTrue() {
        // Arrange
        when(materialRepository.findByCode("Ce")).thenReturn(Optional.of(cementoEntity));
        when(materialRepository.findByCode("Gr")).thenReturn(Optional.of(gravaEntity));
        when(orderRepository.existsByLocationCoordinates(10.0, 20.0))
            .thenReturn(false);

        // Act
        Map<String, Object> result = constructionRequestUseCase.validateConstructionRequest(testOrder);

        // Assert
        assertNotNull(result);
        assertEquals(true, result.get("valid"));
        assertEquals("La solicitud de construcción puede realizarse", result.get("message"));
        assertEquals(5, result.get("estimatedDays"));
    }

    @Test
    void validateConstructionRequest_InvalidType_ReturnsValidFalse() {
        // Arrange
        ConstructionOrder invalidOrder = new ConstructionOrder(
            null, "Test", new Coordinate(10.0, 20.0), "INVALID", 
            "Pendiente", 5, LocalDate.now()
        );

        // Act
        Map<String, Object> result = constructionRequestUseCase.validateConstructionRequest(invalidOrder);

        // Assert
        assertNotNull(result);
        assertEquals(false, result.get("valid"));
        assertEquals("Tipo de construcción inválido", result.get("error"));
    }

    @Test
    void generateConstructionReport_WithOrders_ReturnsCompleteReport() {
        // Arrange
        List<ConstructionOrderEntity> entities = Arrays.asList(testEntity);
        when(orderRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testOrder);
        when(orderRepository.count()).thenReturn(1L);
        when(orderRepository.findFirstByOrderByEntregaDateAsc()).thenReturn(Optional.of(testEntity));
        when(orderRepository.findFirstByOrderByEntregaDateDesc()).thenReturn(Optional.of(testEntity));

        // Act
        ConstructionReport result = constructionRequestUseCase.generateConstructionReport();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.totalOrders());
        assertEquals(1, result.pendingOrders());
        assertEquals(0, result.inProgressOrders());
        assertEquals(0, result.finishedOrders());
        assertNotNull(result.projectSummary());
    }

    @Test
    void getProjectSummary_WithOrders_ReturnsCorrectSummary() {
        // Arrange
        when(orderRepository.count()).thenReturn(2L);
        when(orderRepository.findFirstByOrderByEntregaDateAsc()).thenReturn(Optional.of(testEntity));
        when(orderRepository.findFirstByOrderByEntregaDateDesc()).thenReturn(Optional.of(testEntity));

        // Act
        ProjectSummary result = constructionRequestUseCase.getProjectSummary();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.totalOrders());
        assertEquals("En progreso", result.status());
        assertNotNull(result.projectStartDate());
        assertNotNull(result.projectEndDate());
    }

    @Test
    void updateConstructionStatuses_UpdatesStatusesCorrectly() {
        // Arrange
        List<ConstructionOrderEntity> pendingOrders = Arrays.asList(testEntity);
        List<ConstructionOrderEntity> ordersToFinish = Arrays.asList();
        
        when(orderRepository.findPendingOrders()).thenReturn(pendingOrders);
        when(orderRepository.findOrdersToFinishToday(any(LocalDate.class))).thenReturn(ordersToFinish);

        // Act
        constructionRequestUseCase.updateConstructionStatuses();

        // Assert
        verify(orderRepository).findPendingOrders();
        verify(orderRepository).findOrdersToFinishToday(any(LocalDate.class));
    }

    @Test
    void getTotalConstructionDays_WithOrders_ReturnsCorrectTotal() {
        // Arrange
        testEntity.setEstimatedDays(5);
        ConstructionOrderEntity secondEntity = new ConstructionOrderEntity();
        secondEntity.setEstimatedDays(3);
        
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testEntity, secondEntity));

        // Act
        Integer result = constructionRequestUseCase.getTotalConstructionDays();

        // Assert
        assertEquals(8, result);
        verify(orderRepository).findAll();
    }

    @Test
    void getProjectStartDate_WithOrders_ReturnsCorrectDate() {
        // Arrange
        when(orderRepository.findFirstByOrderByEntregaDateAsc()).thenReturn(Optional.of(testEntity));

        // Act
        LocalDate result = constructionRequestUseCase.getProjectStartDate();

        // Assert
        assertEquals(testEntity.getStartDate(), result);
        verify(orderRepository).findFirstByOrderByEntregaDateAsc();
    }

    @Test
    void getProjectEndDate_WithOrders_ReturnsCorrectDate() {
        // Arrange
        when(orderRepository.findFirstByOrderByEntregaDateDesc()).thenReturn(Optional.of(testEntity));

        // Act
        LocalDate result = constructionRequestUseCase.getProjectEndDate();

        // Assert
        assertEquals(testEntity.getEntregaDate(), result);
        verify(orderRepository).findFirstByOrderByEntregaDateDesc();
    }

    @Test
    void getEstimatedDeliveryDate_WithOrders_ReturnsCorrectDate() {
        // Arrange
        when(orderRepository.findFirstByOrderByEntregaDateDesc()).thenReturn(Optional.of(testEntity));

        // Act
        LocalDate result = constructionRequestUseCase.getEstimatedDeliveryDate();

        // Assert
        assertEquals(testEntity.getEntregaDate(), result);
        verify(orderRepository).findFirstByOrderByEntregaDateDesc();
    }
}