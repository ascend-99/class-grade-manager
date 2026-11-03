package com.zhihang.grade.model;

import java.util.Collections;
import java.util.List;

public class PageResult<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final long total;

    public PageResult(List<T> items, int page, int size, long total) {
        this.items = items == null ? Collections.emptyList() : items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }

    public int getTotalPages() {
        if (size <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil((double) total / size));
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public int getPrevPage() {
        return Math.max(1, page - 1);
    }

    public int getNextPage() {
        return Math.min(getTotalPages(), page + 1);
    }

    public long getFrom() {
        return total == 0 ? 0 : (long) (page - 1) * size + 1;
    }

    public long getTo() {
        return Math.min((long) page * size, total);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
