//package com.cityfuture.infrastructure.service;
//
//import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;
//import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
//import com.cityfuture.infrastructure.persistence.repository.JpaConstructionOrderRepository;
//import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@Transactional
//public class ConstructionOrderServiceImpl {
//
//    @Autowired
//    private JpaConstructionOrderRepository constructionOrderRepository;
//
//    @Autowired
//    private JpaMaterialRepository materialRepository;
//
//    @Autowired
//    private JpaConstructionOrderRepository constructionTypeRepository;
//
//    public ConstructionOrderEntity saveOrder(ConstructionOrderEntity order) {
//        // Validar que el tipo de construcción existe
//        if (order.getType() != null && order.getType().getId() != null) {
//            constructionTypeRepository.findById(order.getType().getId())
//                .orElseThrow(() -> new RuntimeException("Tipo de construcción no encontrado con ID: " + order.getType().getId()));
//        }
//
//        // Guarda la lista original de materiales antes de limpiarla
//        List<MaterialEntity> originalMaterials =
//                order.getMaterials() != null ? new ArrayList<>(order.getMaterials())
//                        : new ArrayList<>();
//
//        // Primero guarda la orden sin materiales para obtener el ID
//        order.setMaterials(new ArrayList<>());
//        ConstructionOrderEntity savedOrder = constructionOrderRepository.save(order);
//
//        // Luego procesa y asocia los materiales usando la lista original
//        List<MaterialEntity> attachedMaterials = new ArrayList<>();
//        for (MaterialEntity material : originalMaterials) {
//            if (material.getId() != null) {
//                // Busca el material existente
//                MaterialEntity attached = materialRepository.findById(material.getId())
//                        .orElseThrow(() -> new RuntimeException("Material no encontrado con ID: " + material.getId()));
//                // Establece la relación bidireccional
////                attached.setConstructionOrder(savedOrder);
//                attachedMaterials.add(attached);
//            } else {
//                // Si no tiene ID, es un material nuevo
////                material.setConstructionOrder(savedOrder);
//                MaterialEntity savedMaterial = materialRepository.save(material);
//                attachedMaterials.add(savedMaterial);
//            }
//        }
//
//        // Actualiza la orden con los materiales
//        savedOrder.setMaterials(attachedMaterials);
//        return constructionOrderRepository.save(savedOrder);
//    }
//}
