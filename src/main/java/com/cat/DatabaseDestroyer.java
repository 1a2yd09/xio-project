package com.cat;

import com.cat.service.Clearable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class DatabaseDestroyer {
    @Autowired
    List<Clearable> services;

    @PreDestroy
    public void shutdown() {
        for (Clearable service : this.services) {
            service.clearTable();
        }
    }
}
