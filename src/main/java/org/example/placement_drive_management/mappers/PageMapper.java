package org.example.placement_drive_management.mappers;

import org.example.placement_drive_management.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


public class PageMapper {
    public static <T> PageResponse<T> mapToPageResponse(Page<T> page) {
        return new PageResponse<>(
           page.getContent(),
           page.getNumber(),
           page.getSize(),
           page.getTotalElements(),
           page.getTotalPages(),
           page.hasNext(),
           page.hasPrevious()
        );
    }
}
