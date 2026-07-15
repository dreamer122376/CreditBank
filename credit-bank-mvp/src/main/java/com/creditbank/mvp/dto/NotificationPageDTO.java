package com.creditbank.mvp.dto;

import java.util.List;

public class NotificationPageDTO {
    private List<NotificationItemDTO> records;
    private long total;
    private long unreadCount;
    private int page;
    private int size;

    public List<NotificationItemDTO> getRecords() { return records; }
    public void setRecords(List<NotificationItemDTO> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
