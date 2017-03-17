/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.dto;


import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import nl.thehyve.podium.domain.Attachment;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.domain.enumeration.RequestStatus;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;

/**
 * A DTO for the Request entity.
 */
public class RequestDTO implements Serializable {

    private Long id;

    @NotNull
    private RequestStatus status;

    private List<OrganisationDTO> organisations = new ArrayList<>();

    private RequestDTO parentRequest;

    private RequestDetail requestDetail;

    private Set<Attachment> attachments = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public List<OrganisationDTO> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<OrganisationDTO> organisations) {
        this.organisations = organisations;
    }

    public RequestDTO getParentRequest() {
        return parentRequest;
    }

    public void setParentRequest(RequestDTO requestId) {
        this.parentRequest = requestId;
    }

    public RequestDetail getRequestDetail() {
        return requestDetail;
    }

    public void setRequestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestDTO requestDTO = (RequestDTO) o;

        if ( ! Objects.equals(id, requestDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RequestDTO{" +
            "id=" + id +
            ", status=" + status +
            ", organisations=" + organisations +
            ", parentRequest=" + parentRequest +
            ", requestDetail=" + requestDetail +
            ", attachments=" + attachments +
            '}';
    }
}