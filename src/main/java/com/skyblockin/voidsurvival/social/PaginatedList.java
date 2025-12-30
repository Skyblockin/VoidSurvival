package com.skyblockin.voidsurvival.social;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaginatedList<T> {

    private final ArrayList<T> list;
    private final int pageSize;

    public PaginatedList(int pageSize) {
        this.pageSize = pageSize;
        this.list = new ArrayList<>();
    }

    public PaginatedList(Collection<T> collection, int pageSize) {
        this.list = new ArrayList<>(collection);
        this.pageSize = pageSize;
    }

    public List<T> getPage(int page) {
        if (page >= getPageCount()) {
            page = getPageCount() - 1;
        }
        return list.subList((page - 1) * pageSize, Math.min(page * pageSize, list.size()));
    }

    public int getPageCount() {
        return (list.size() / pageSize) + 1;
    }

    public void add(T item) {
        list.add(item);
    }

}
