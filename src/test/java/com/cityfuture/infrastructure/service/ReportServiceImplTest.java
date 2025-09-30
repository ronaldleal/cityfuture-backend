package com.cityfuture.infrastructure.service;

import com.cityfuture.domain.model.Coordinate;
import com.cityfuture.domain.model.ConstructionOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @InjectMocks
    private ReportServiceImpl reportService;

    private List<ConstructionOrder> testOrders;

    @BeforeEach
    void setUp() {
        Coordinate location1 = new Coordinate(10.0, 20.0);
        Coordinate location2 = new Coordinate(15.0, 25.0);

        ConstructionOrder order1 = new ConstructionOrder(
            1L, "Casa del Futuro", location1, "CASA", "Pendiente", 5, LocalDate.now().plusDays(5)
        );

        ConstructionOrder order2 = new ConstructionOrder(
            2L, "Edificio Central", location2, "EDIFICIO", "En Progreso", 10, LocalDate.now().plusDays(10)
        );

        ConstructionOrder order3 = new ConstructionOrder(
            3L, "Puente Norte", location1, "PUENTE", "Finalizado", 15, LocalDate.now().minusDays(5)
        );

        testOrders = Arrays.asList(order1, order2, order3);
    }

    @Test
    void generateMaterialReport_ReturnsSuccessMessage() {
        // Act
        String result = reportService.generateMaterialReport();

        // Assert
        assertNotNull(result);
        assertEquals("Reporte de Materiales generado correctamente.", result);
    }

    @Test
    void generateConstructionReport_WithOrders_ReturnsReportWithCount() {
        // Act
        String result = reportService.generateConstructionReport(testOrders);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Reporte de Construcciones"));
        assertTrue(result.contains("total órdenes = 3"));
        assertEquals("Reporte de Construcciones: total órdenes = 3", result);
    }

    @Test
    void generateConstructionReport_WithEmptyList_ReturnsReportWithZeroCount() {
        // Arrange
        List<ConstructionOrder> emptyOrders = Collections.emptyList();

        // Act
        String result = reportService.generateConstructionReport(emptyOrders);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("total órdenes = 0"));
        assertEquals("Reporte de Construcciones: total órdenes = 0", result);
    }

    @Test
    void generateConstructionReport_WithSingleOrder_ReturnsReportWithOneCount() {
        // Arrange
        List<ConstructionOrder> singleOrder = Arrays.asList(testOrders.get(0));

        // Act
        String result = reportService.generateConstructionReport(singleOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("total órdenes = 1"));
        assertEquals("Reporte de Construcciones: total órdenes = 1", result);
    }

    @Test
    void generateConstructionReport_WithNullList_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            reportService.generateConstructionReport(null);
        });
    }

    @Test
    void generateConstructionReport_WithLargeList_ReturnsCorrectCount() {
        // Arrange
        List<ConstructionOrder> largeList = Arrays.asList(
            testOrders.get(0), testOrders.get(1), testOrders.get(2),
            testOrders.get(0), testOrders.get(1), testOrders.get(2),
            testOrders.get(0), testOrders.get(1), testOrders.get(2),
            testOrders.get(0)
        );

        // Act
        String result = reportService.generateConstructionReport(largeList);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("total órdenes = 10"));
        assertEquals("Reporte de Construcciones: total órdenes = 10", result);
    }

    @Test
    void generateMaterialReport_MultipleInvocations_ReturnsConsistentResult() {
        // Act
        String result1 = reportService.generateMaterialReport();
        String result2 = reportService.generateMaterialReport();
        String result3 = reportService.generateMaterialReport();

        // Assert
        assertEquals(result1, result2);
        assertEquals(result2, result3);
        assertEquals("Reporte de Materiales generado correctamente.", result1);
    }

    @Test
    void generateConstructionReport_MultipleInvocationsWithSameData_ReturnsConsistentResult() {
        // Act
        String result1 = reportService.generateConstructionReport(testOrders);
        String result2 = reportService.generateConstructionReport(testOrders);

        // Assert
        assertEquals(result1, result2);
        assertEquals("Reporte de Construcciones: total órdenes = 3", result1);
    }

    @Test
    void generateConstructionReport_DifferentOrderCounts_ReturnsDifferentResults() {
        // Arrange
        List<ConstructionOrder> smallList = Arrays.asList(testOrders.get(0));
        List<ConstructionOrder> largeList = testOrders;

        // Act
        String smallResult = reportService.generateConstructionReport(smallList);
        String largeResult = reportService.generateConstructionReport(largeList);

        // Assert
        assertNotEquals(smallResult, largeResult);
        assertTrue(smallResult.contains("total órdenes = 1"));
        assertTrue(largeResult.contains("total órdenes = 3"));
    }

    @Test
    void serviceImplementsReportServiceInterface() {
        // Assert - Verify that ReportServiceImpl implements ReportService
        assertTrue(reportService instanceof com.cityfuture.application.service.ReportService);
    }
}