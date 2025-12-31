package com.skyblockin.voidsurvival.social;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaginatedList<T> extends ArrayList<T> {

    private final int pageSize;

    public PaginatedList() {
        this(10);
    }

    public PaginatedList(Collection<T> collection) {
        this(collection, 10);
    }

    public PaginatedList(int pageSize) {
        this.pageSize = pageSize;
    }

    public PaginatedList(Collection<T> collection, int pageSize) {
        super(collection);
        this.pageSize = pageSize;
    }

    public List<T> getPage(int page) {
        if (page >= getPageCount()) {
            return subList(Math.max(0, size() - pageSize), size());
        }
        if (page < 1) {
            page = 1;
        }
        return subList((page - 1) * pageSize, Math.min(page * pageSize, size()));
    }

    public int getPageCount() {
        return (size() / pageSize) + 1;
    }

}
