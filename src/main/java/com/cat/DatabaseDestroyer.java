package com.cat;

import com.cat.service.InventoryService;
import com.cat.service.MachineActionService;
import com.cat.service.SignalService;
import com.cat.service.StockSpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class DatabaseDestroyer {
    @Autowired
    MachineActionService actionService;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    SignalService signalService;
    @Autowired
    StockSpecificationService specificationService;

    @PreDestroy
    public void shutdown() {
        this.actionService.clearActionTable();
        this.actionService.clearCompletedActionTable();

        this.inventoryService.clearInventoryTable();

        this.signalService.clearSignalTable();

        this.specificationService.clearSpecTable();
    }
}
