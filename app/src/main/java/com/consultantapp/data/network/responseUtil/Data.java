package com.consultantapp.data.network.responseUtil;

import java.util.List;

public class Data {
    private List<ImageItem> image;
    private String createdAt;
    private String subject;
    private int V;
    private String tenantId;
    private String description;
    private String id;
    private int type;
    private String ownerId;
    private String propertyId;
    private int status;
    private String updatedAt;

    public List<ImageItem> getImage() {
        return image;
    }

    public void setImage(List<ImageItem> image) {
        this.image = image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getV() {
        return V;
    }

    public void setV(int V) {
        this.V = V;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return
                "Template{" +
                        "image = '" + image + '\'' +
                        ",createdAt = '" + createdAt + '\'' +
                        ",subject = '" + subject + '\'' +
                        ",__v = '" + V + '\'' +
                        ",tenantId = '" + tenantId + '\'' +
                        ",description = '" + description + '\'' +
                        ",_id = '" + id + '\'' +
                        ",type = '" + type + '\'' +
                        ",ownerId = '" + ownerId + '\'' +
                        ",propertyId = '" + propertyId + '\'' +
                        ",status = '" + status + '\'' +
                        ",updatedAt = '" + updatedAt + '\'' +
                        "}";
    }
}