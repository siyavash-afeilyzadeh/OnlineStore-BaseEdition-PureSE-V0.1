package com.black.onlinestorese.tickets.entity;

import com.black.onlinestorese.users.entity.User;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class TicketMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Ticket ticket;
    private User sender;
    private String content;

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Ticket getTicket() {
        return ticket;
    }
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getSender() {
        return sender;
    }
    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    // ==================== No-Arguments Constructor ====================

    public TicketMessage(){

    }

    // ==================== All-Arguments Constructor ====================

    public TicketMessage(Long id,
                         Long version,
                         boolean deleted,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt,
                         LocalDateTime deletedAt,
                         Ticket ticket,
                         User sender,
                         String content) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.ticket = ticket;
        this.sender = sender;
        this.content = content;
    }

    // ==================== Builder ====================

    private TicketMessage (Builder builder){
        this.id = builder.id;
        this.version = builder.version;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.ticket = builder.ticket;
        this.sender = builder.sender;
        this.content = builder.content;
    }

    public static class Builder {
        private Long id;
        private Long version;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private Ticket ticket;
        private User sender;
        private String content;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        public Builder version(Long version) {
            this.version = version;
            return this;
        }
        public Builder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }
        public Builder ticket(Ticket ticket) {
            this.ticket = ticket;
            return this;
        }
        public Builder sender(User sender) {
            this.sender = sender;
            return this;
        }
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        public TicketMessage build() {
            return new TicketMessage(this);
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    // ==================== Lifecycle Hooks ====================

    public void onCreated(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
        if (this.version == null) {
            this.version = 0L;
        }
    }
    public void onUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
    public void onDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // ==================== Equals & HashCode ====================

    @Override
    public final boolean equals(Object o){
        if (this == o) return true;
        if ((o == null) || getClass() != o.getClass()) return false;
        TicketMessage ticketMessage = (TicketMessage) o;
        return id != null && id.equals(ticketMessage.id);
    }

    @Override
    public final int hashCode(){
        return getClass().hashCode();
    }
}
