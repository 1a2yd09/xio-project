package com.cat.controller;

import com.cat.pojo.dto.OrderCount;
import com.cat.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/order")
    public String order() {
        return "order";
    }

    @PostMapping("/echarts")
    public ModelAndView echarts(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        List<OrderCount> countList = this.orderService.getCompletedOrderCountByRange(30);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(countList));
        writer.flush();
        return null;
    }
}
