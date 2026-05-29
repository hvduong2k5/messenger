package com.midterm.team12345.data.remote.dto.response;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}
