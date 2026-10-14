package com.black.onlinestorese.attachments.entity;

import com.black.onlinestorese.attachments.entity.enums.AttachmentOwnerType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Attachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private AttachmentOwnerType ownerType;
    private Long ownerId;
    private String title;
    private String fileName;
    private String description;
    private String keywords;
    private String contentType;
    private Long fileSize;
    private String storedName;

    // ==================== Getters & Setters ====================

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean getDeleted(){
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

    public AttachmentOwnerType getOwnerType() {
        return ownerType;
    }
    public void setOwnerType(AttachmentOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public Long getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStoredName() {
        return storedName;
    }
    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    // ==================== No-Arg Constructor ====================

    public Attachment() {}

    // ==================== All-Arguments Constructor ====================

    public Attachment(Long id,
                      Long version,
                      boolean deleted,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt,
                      LocalDateTime deletedAt,
                      AttachmentOwnerType ownerType,
                      Long ownerId,
                      String title,
                      String fileName,
                      String description,
                      String keywords,
                      String contentType,
                      Long fileSize,
                      String storedName
                      ) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.title = title;
        this.fileName = fileName;
        this.description = description;
        this.keywords = keywords;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storedName = storedName;
    }
    // ==================== Builder ====================

    private Attachment(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.ownerType = builder.ownerType;
        this.ownerId = builder.ownerId;
        this.title = builder.title;
        this.fileName = builder.fileName;
        this.description = builder.description;
        this.keywords = builder.keywords;
        this.contentType = builder.contentType;
        this.fileSize = builder.fileSize;
        this.storedName = builder.storedName;
    }

    private static class Builder {
        private Long id;
        private Long version;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private AttachmentOwnerType ownerType;
        private Long ownerId;
        private String title;
        private String fileName;
        private String description;
        private String keywords;
        private String contentType;
        private Long fileSize;
        private String storedName;

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
        public Builder ownerType(AttachmentOwnerType ownerType) {
            this.ownerType = ownerType;
            return this;
        }
        public Builder ownerId(Long ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        public Builder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }
        public Builder storedName(String storedName) {
            this.storedName = storedName;
            return this;
        }

        public Attachment build() {
            return new Attachment(this);
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    // ==================== Lifecycle Hooks ====================

    public void onCreated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
        if(this.version == null){
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
    public final boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass())
            return false;
        Attachment attachment = (Attachment) o;
        return id != null && id.equals(attachment.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

}
