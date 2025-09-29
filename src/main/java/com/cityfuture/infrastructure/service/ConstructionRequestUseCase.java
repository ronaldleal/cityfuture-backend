package com.cityfuture.infrastructure.service;

import com.cityfuture.api.exception.ConstructionOrderNotFoundException;
import com.cityfuture.domain.exception.InsufficientMaterialException;
import com.cityfuture.domain.model.ConstructionOrder;
import com.cityfuture.domain.model.ConstructionReport;
import com.cityfuture.domain.model.ConstructionTypeCriteria;
import com.cityfuture.domain.model.ProjectSummary;
import com.cityfuture.infrastructure.mapper.ConstructionMapper;
import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;
import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
import com.cityfuture.infrastructure.persistence.repository.JpaConstructionOrderRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ConstructionRequestUseCase {
    private static final Logger logger = LoggerFactory.getLogger(ConstructionRequestUseCase.class);

    private final JpaConstructionOrderRepository orderRepository;
    private final ConstructionMapper mapper;
    private final JpaMaterialRepository materialRepository;
    
    public ConstructionOrder createOrder(ConstructionOrder order) {
        logger.info("Iniciando creación de orden de construcción para proyecto: {}",
                order.projectName());

        try {
            ConstructionTypeCriteria criteria = validateConstructionType(order.typeConstruction());
            logger.debug("Tipo de construcción validado: {} - Días estimados: {}",
                    order.typeConstruction(), criteria.getEstimatedTime());

            // Validar coordenadas únicas ANTES de validar materiales
            validateUniqueLocation(order.location());
            logger.debug("Coordenadas validadas: lat={}, lon={}", order.location().latitude(),
                    order.location().longitude());

            validateMaterials(criteria.getMaterials());
            logger.debug("Materiales validados correctamente");

            ConstructionOrderEntity entity = mapper.toEntity(order);
            entity.setEstado("Pendiente");
            entity.setEstimatedDays(criteria.getEstimatedTime());

            // Calcular fecha de entrega basándose en la última orden existente
            LocalDate deliveryDate = calculateNextDeliveryDate(criteria.getEstimatedTime());
            entity.setEntregaDate(deliveryDate);

            // Calcular fecha de inicio basándose en la fecha de entrega
            LocalDate startDate = deliveryDate.minusDays(criteria.getEstimatedTime() - 1);
            entity.setStartDate(startDate);

            ConstructionOrderEntity saved = orderRepository.save(entity);
            logger.info(
                    "Orden de construcción creada exitosamente - ID: {}, Proyecto: {}, Inicio: {}, Entrega: {}",
                    saved.getId(), saved.getProjectName(), saved.getStartDate(),
                    saved.getEntregaDate());

            return mapper.toDomain(saved);

        } catch (IllegalArgumentException e) {
            logger.error(
                    "Error de validación al crear orden de construcción - Proyecto: {}, Error: {}",
                    order.projectName(), e.getMessage(), e);
            throw e;
        } catch (com.cityfuture.domain.exception.LocationAlreadyOccupiedException e) {
            logger.error(
                    "Error de ubicación ocupada al crear orden - Proyecto: {}, Coordenadas: [{},{}], Error: {}",
                    order.projectName(), order.location().latitude(), order.location().longitude(),
                    e.getMessage(), e);
            throw e;
        } catch (InsufficientMaterialException e) {
            logger.error(
                    "Error de materiales insuficientes al crear orden - Proyecto: {}, Error: {}",
                    order.projectName(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(
                    "Error inesperado al crear orden de construcción - Proyecto: {}, Error: {}",
                    order.projectName(), e.getMessage(), e);
            throw new RuntimeException("Error interno al crear la orden de construcción", e);
        }
    }

    private void validateUniqueLocation(com.cityfuture.domain.model.Coordinate location) {
        boolean locationExists = orderRepository.existsByLocationCoordinates(location.latitude(),
                location.longitude());

        if (locationExists) {
            throw new com.cityfuture.domain.exception.LocationAlreadyOccupiedException(
                    "Ya existe una orden de construcción en las coordenadas: " + location.latitude()
                            + ", " + location.longitude());
        }
    }

    private LocalDate calculateNextDeliveryDate(Integer estimatedDays) {
        return orderRepository.findFirstByOrderByEntregaDateDesc().map(lastOrder -> {
            LocalDate lastDeliveryDate = lastOrder.getEntregaDate();
            // La nueva construcción inicia al día siguiente de terminar la anterior
            // y termina después de los días estimados
            LocalDate startDate = lastDeliveryDate.plusDays(1);
            return startDate.plusDays(estimatedDays - 1); // -1 porque incluye el día de inicio
        }).orElse(LocalDate.now().plusDays(1).plusDays(estimatedDays - 1)); // Primera orden: inicia
                                                                            // mañana
    }

    public Integer getTotalConstructionDays() {
        return orderRepository.sumAllEstimatedDays();
    }

    public LocalDate getEstimatedDeliveryDate() {
        // La fecha de entrega es simplemente la fecha de entrega de la última orden
        return orderRepository.findFirstByOrderByEntregaDateDesc()
                .map(ConstructionOrderEntity::getEntregaDate).orElse(LocalDate.now());
    }

    public LocalDate getProjectStartDate() {
        // Usar el campo startDate real de la primera orden
        return orderRepository.findFirstByOrderByEntregaDateAsc()
                .map(ConstructionOrderEntity::getStartDate).orElse(LocalDate.now().plusDays(1));
    }

    public LocalDate getProjectEndDate() {
        // La fecha de fin del proyecto es la fecha de entrega de la última orden
        return getEstimatedDeliveryDate();
    }

    public List<ConstructionOrder> getAllOrders() {
        return orderRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    public ConstructionOrder getOrderById(Long id) {
        return orderRepository.findById(id).map(mapper::toDomain).orElseThrow(
                () -> new RuntimeException("Construction order not found with id: " + id));
    }

    public ConstructionOrder updateOrder(Long id, ConstructionOrder order) {
        return orderRepository.findById(id).map(existing -> {
            // Validar los materiales para el tipo de construcción actual
            ConstructionTypeCriteria currentCriteria =
                    validateConstructionType(existing.getTypeConstruction());
            validateMaterials(currentCriteria.getMaterials());

            // Validar si el tipo de construcción cambió
            if (!existing.getTypeConstruction().equalsIgnoreCase(order.typeConstruction())) {
                ConstructionTypeCriteria newCriteria =
                        validateConstructionType(order.typeConstruction());
                validateMaterials(newCriteria.getMaterials());
                existing.setEstimatedDays(newCriteria.getEstimatedTime());
            }

            // Actualizar campos sin cambiar la fecha de inicio ya establecida
            existing.setTypeConstruction(order.typeConstruction());
            existing.setProjectName(order.projectName());
            existing.setEstado("Pendiente");

            return mapper.toDomain(orderRepository.save(existing));
        }).orElseThrow(() -> new ConstructionOrderNotFoundException(
                "No existe una orden de construcción con el ID: " + id));
    }

    private ConstructionTypeCriteria validateConstructionType(String typeConstruction) {
        try {
            return ConstructionTypeCriteria.valueOf(typeConstruction.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de construcción no válido: " + typeConstruction);
        }
    }

    private void validateMaterials(Map<String, Integer> requiredMaterials) {
        for (var requiredMaterial : requiredMaterials.entrySet()) {
            String code = requiredMaterial.getKey();
            int requiredQuantity = requiredMaterial.getValue();

            // Buscar el material en la base de datos por código
            int availableQuantity =
                    materialRepository.findByCode(code).map(MaterialEntity::getQuantity).orElse(0);

            if (availableQuantity < requiredQuantity) {
                throw new InsufficientMaterialException(
                        "Material insuficiente para " + code + ": se requiere " + requiredQuantity
                                + ", pero hay disponible " + availableQuantity);
            }
        }
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("No existe una orden de construcción con el ID: " + id);
        }

        // Al eliminar una orden, necesitamos recalcular las fechas de las órdenes posteriores
        ConstructionOrderEntity orderToDelete = orderRepository.findById(id).orElse(null);
        if (orderToDelete != null) {
            LocalDate deletedStartDate = orderToDelete.getEntregaDate();
            Integer deletedDays = orderToDelete.getEstimatedDays();

            // Eliminar la orden
            orderRepository.deleteById(id);

            // Recalcular fechas de órdenes posteriores
            recalculateSubsequentOrders(deletedStartDate);
        }
    }

    private void recalculateSubsequentOrders(LocalDate deletedDeliveryDate) {
        // Obtener todas las órdenes ordenadas por fecha de entrega
        List<ConstructionOrderEntity> allOrders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> o1.getEntregaDate().compareTo(o2.getEntregaDate())).toList();

        // Obtener órdenes posteriores a la eliminada y recalcular sus fechas
        List<ConstructionOrderEntity> subsequentOrders = allOrders.stream()
                .filter(order -> order.getEntregaDate().isAfter(deletedDeliveryDate)).toList();

        // Buscar la nueva fecha base (última orden antes de las que se van a recalcular)
        LocalDate baseDate = allOrders.stream()
                .filter(order -> order.getEntregaDate().isBefore(deletedDeliveryDate))
                .reduce((first, second) -> second) // Obtener la última
                .map(ConstructionOrderEntity::getEntregaDate).orElse(LocalDate.now());

        for (ConstructionOrderEntity order : subsequentOrders) {
            // Cada orden inicia al día siguiente de terminar la anterior
            LocalDate newStartDate = baseDate.plusDays(1);
            LocalDate newDeliveryDate = newStartDate.plusDays(order.getEstimatedDays() - 1);

            order.setStartDate(newStartDate); // Actualizar fecha de inicio
            order.setEntregaDate(newDeliveryDate); // Actualizar fecha de entrega
            baseDate = newDeliveryDate; // Actualizar para la siguiente iteración
            orderRepository.save(order);
        }
    }

    public ProjectSummary getProjectSummary() {
        Integer totalDays = getTotalConstructionDays();
        LocalDate startDate = getProjectStartDate();
        LocalDate endDate = getProjectEndDate();
        LocalDate deliveryDate = getEstimatedDeliveryDate();
        int totalOrders = (int) orderRepository.count();

        String status = totalOrders == 0 ? "Sin órdenes"
                : LocalDate.now().isBefore(startDate) ? "No iniciado"
                        : LocalDate.now().isAfter(deliveryDate) ? "Completado" : "En progreso";

        return new ProjectSummary(totalDays, startDate, endDate, deliveryDate, totalOrders, status);
    }

    public void updateConstructionStatuses() {
        LocalDate today = LocalDate.now();

        // Iniciar construcciones que deben empezar hoy - usar startDate real
        List<ConstructionOrderEntity> pendingOrders = orderRepository.findPendingOrders();
        List<ConstructionOrderEntity> ordersToStart =
                pendingOrders.stream().filter(order -> order.getStartDate().equals(today)).toList();

        ordersToStart.forEach(order -> {
            order.setEstado("En progreso");
            orderRepository.save(order);
        });

        // Finalizar construcciones que deben terminar hoy
        List<ConstructionOrderEntity> ordersToFinish =
                orderRepository.findOrdersToFinishToday(today);
        ordersToFinish.forEach(order -> {
            if ("En progreso".equals(order.getEstado())) {
                order.setEstado("Finalizado");
                orderRepository.save(order);
            }
        });
    }

    public void processOverdueOrders() {
        LocalDate today = LocalDate.now();

        // Buscar órdenes pendientes cuya fecha de inicio ya pasó - usar startDate real
        List<ConstructionOrderEntity> pendingOrders = orderRepository.findPendingOrders();
        List<ConstructionOrderEntity> overdueOrders = pendingOrders.stream().filter(
                order -> order.getStartDate().isBefore(today) || order.getStartDate().equals(today))
                .toList();

        overdueOrders.forEach(order -> {
            LocalDate startDate = order.getStartDate();
            LocalDate endDate = order.getEntregaDate();

            if (today.isAfter(endDate)) {
                // Si ya pasó la fecha de entrega, marcar como finalizado
                order.setEstado("Finalizado");
            } else if (today.isAfter(startDate) || today.equals(startDate)) {
                // Si está en el período de construcción, marcar como en progreso
                order.setEstado("En progreso");
            }

            orderRepository.save(order);
        });
    }

    public Map<String, Object> validateConstructionRequest(ConstructionOrder order) {
        try {
            // Ejecutar todas las validaciones SIN crear la orden
            ConstructionTypeCriteria criteria = validateConstructionType(order.typeConstruction());
            validateUniqueLocation(order.location());
            validateMaterials(criteria.getMaterials());

            // Si llegamos aquí, todas las validaciones pasaron
            return Map.of("valid", true, "message", "La solicitud de construcción puede realizarse",
                    "estimatedDays", criteria.getEstimatedTime());

        } catch (IllegalArgumentException e) {
            return Map.of("valid", false, "error", "Tipo de construcción inválido", "message",
                    e.getMessage());
        } catch (com.cityfuture.domain.exception.LocationAlreadyOccupiedException e) {
            return Map.of("valid", false, "error", "Ubicación ocupada", "message", e.getMessage());
        } catch (InsufficientMaterialException e) {
            return Map.of("valid", false, "error", "Materiales insuficientes", "message",
                    e.getMessage());
        }
    }

    public ConstructionReport generateConstructionReport() {
        List<ConstructionOrder> allOrders = getAllOrders();

        // Contadores por estado
        int pendingCount = 0;
        int inProgressCount = 0;
        int finishedCount = 0;

        // Mapas por tipo de construcción
        Map<String, Integer> pendingByType = new HashMap<>();
        Map<String, Integer> inProgressByType = new HashMap<>();
        Map<String, Integer> finishedByType = new HashMap<>();

        // Procesar todas las órdenes
        for (ConstructionOrder order : allOrders) {
            String type = order.typeConstruction();
            String status = order.estado();

            switch (status) {
                case "Pendiente":
                    pendingCount++;
                    pendingByType.merge(type, 1, Integer::sum);
                    break;
                case "En progreso":
                    inProgressCount++;
                    inProgressByType.merge(type, 1, Integer::sum);
                    break;
                case "Finalizado":
                    finishedCount++;
                    finishedByType.merge(type, 1, Integer::sum);
                    break;
            }
        }

        ProjectSummary projectSummary = getProjectSummary();

        return new ConstructionReport(LocalDate.now(), allOrders.size(), pendingCount,
                inProgressCount, finishedCount, pendingByType, inProgressByType, finishedByType,
                projectSummary);
    }
}
