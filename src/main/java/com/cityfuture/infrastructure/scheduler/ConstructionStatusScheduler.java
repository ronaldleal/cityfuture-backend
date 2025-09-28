package com.cityfuture.infrastructure.scheduler;

import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;
import com.cityfuture.infrastructure.persistence.repository.JpaConstructionOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ConstructionStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ConstructionStatusScheduler.class);

    private final JpaConstructionOrderRepository orderRepository;

    public ConstructionStatusScheduler(JpaConstructionOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // TEMPORAL: Ejecutar cada minuto para pruebas (cambiar después)
    @Scheduled(cron = "0 * * * * *") // Cada minuto
    public void checkConstructionsToStartTest() {
        logger.info("PRUEBA: Ejecutando validación de construcciones a iniciar");

        LocalDate today = LocalDate.now();
        List<ConstructionOrderEntity> pendingOrders = orderRepository.findPendingOrders();
        List<ConstructionOrderEntity> ordersToStart = pendingOrders.stream().filter(order -> {
            LocalDate startDate = order.getEntregaDate().minusDays(order.getEstimatedDays() - 1);
            return startDate.equals(today);
        }).toList();

        for (ConstructionOrderEntity order : ordersToStart) {
            if ("Pendiente".equals(order.getEstado())) {
                order.setEstado("En progreso");
                orderRepository.save(order);
                logger.info("PRUEBA: Orden {} cambiada a 'En progreso' - Proyecto: {}",
                        order.getId(), order.getProjectName());
            }
        }

        logger.info("PRUEBA: Validación completada. {} órdenes iniciadas", ordersToStart.size());
    }

    // TEMPORAL: Ejecutar cada 2 minutos para pruebas
    @Scheduled(cron = "30 */2 * * * *") // Cada 2 minutos en el segundo 30
    public void checkConstructionsToFinishTest() {
        logger.info("PRUEBA: Ejecutando validación de construcciones a finalizar");

        LocalDate today = LocalDate.now();
        List<ConstructionOrderEntity> ordersToFinish =
                orderRepository.findOrdersToFinishToday(today);

        for (ConstructionOrderEntity order : ordersToFinish) {
            if ("En progreso".equals(order.getEstado())) {
                order.setEstado("Finalizado");
                orderRepository.save(order);
                logger.info("PRUEBA: Orden {} cambiada a 'Finalizado' - Proyecto: {}",
                        order.getId(), order.getProjectName());
            }
        }

        logger.info("PRUEBA: Validación completada. {} órdenes finalizadas", ordersToFinish.size());
    }

    // Ejecutar cada día a las 8:00 AM - Validar inicio de construcciones
    @Scheduled(cron = "0 0 8 * * *")
    public void checkConstructionsToStart() {
        logger.info("Ejecutando validación matutina de construcciones a iniciar");

        try {
            LocalDate today = LocalDate.now();
            List<ConstructionOrderEntity> ordersToStart =
                    orderRepository.findOrdersToStartToday(today);

            for (ConstructionOrderEntity order : ordersToStart) {
                try {
                    if ("Pendiente".equals(order.getEstado())) {
                        order.setEstado("En progreso");
                        orderRepository.save(order);
                        logger.info("Orden {} cambiada a 'En progreso' - Proyecto: {}",
                                order.getId(), order.getProjectName());
                    }
                } catch (Exception e) {
                    logger.error("Error al cambiar estado de orden {} a 'En progreso'",
                            order.getId(), e);
                }
            }

            logger.info("Validación matutina completada. {} órdenes iniciadas",
                    ordersToStart.size());

        } catch (Exception e) {
            logger.error("Error crítico en scheduler matutino", e);
        }
    }

    // Ejecutar cada día a las 11:00 PM - Validar finalización de construcciones
    @Scheduled(cron = "0 0 23 * * *")
    public void checkConstructionsToFinish() {
        logger.info("Ejecutando validación nocturna de construcciones a finalizar");

        try {
            LocalDate today = LocalDate.now();
            List<ConstructionOrderEntity> ordersToFinish =
                    orderRepository.findOrdersToFinishToday(today);

            for (ConstructionOrderEntity order : ordersToFinish) {
                try {
                    if ("En progreso".equals(order.getEstado())) {
                        order.setEstado("Finalizado");
                        orderRepository.save(order);
                        logger.info("Orden {} cambiada a 'Finalizado' - Proyecto: {}",
                                order.getId(), order.getProjectName());
                    }
                } catch (Exception e) {
                    logger.error("Error al cambiar estado de orden {} a 'Finalizado'",
                            order.getId(), e);
                }
            }

            logger.info("Validación nocturna completada. {} órdenes finalizadas",
                    ordersToFinish.size());

        } catch (Exception e) {
            logger.error("Error crítico en scheduler nocturno", e);
        }
    }
}
