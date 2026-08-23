package com.example.DTO;

import java.util.List;
import org.springframework.data.domain.Page;

public class RespuestaPaginada<T> {

    
    private List<T> content;    
    private boolean last;       
    private int totalPages;     
    private int number;         
    private long totalElements; 
    private int size;

    public RespuestaPaginada(Page<T> page){
        this.content = page.getContent();
        this.last = page.isLast();
        this.totalPages = page.getTotalPages();
        this.number = page.getNumber();
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
    }
    
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
