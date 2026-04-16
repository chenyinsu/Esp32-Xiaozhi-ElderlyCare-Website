package com.example.esp32_robot.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public abstract class BaseService {

    protected Pageable createPageable(Integer page, Integer size) {
        int pageNum = (page == null || page < 1) ? 0 : page - 1;
        int pageSize = (size == null || size < 1) ? 20 : size;
        return PageRequest.of(pageNum, pageSize);
    }

    protected Pageable createPageable(Integer page, Integer size, Sort sort) {
        int pageNum = (page == null || page < 1) ? 0 : page - 1;
        int pageSize = (size == null || size < 1) ? 20 : size;
        return PageRequest.of(pageNum, pageSize, sort);
    }
}
