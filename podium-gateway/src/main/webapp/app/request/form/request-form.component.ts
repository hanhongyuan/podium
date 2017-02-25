/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiLanguageService, EventManager } from 'ng-jhipster';
import { RequestDetail } from '../../shared/request/request-detail';
import { RequestFormService } from './request-form.service';
import { RequestType } from '../../shared/request/request-type';
import { Organisation, OrganisationService } from '../../entities/organisation';
import { AttachmentService } from '../../shared/attachment/attachment.service';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { Principal } from '../../shared/auth/principal.service';
import { User } from '../../shared/user/user.model';
import { Attachment } from '../../shared/attachment/attachment';

@Component({
    selector: 'pdm-request-form',
    templateUrl: './request-form.component.html',
    styleUrls: ['request-form.scss']
})
export class RequestFormComponent implements OnInit {

    public requestFormDisabled: boolean;
    private currentUser: User;
    public error: string;
    public success: string;

    public requestBase: RequestBase;
    public request?: RequestDetail;
    public requestTypes = RequestType;

    public availableOrganisations: Organisation[];
    public availableRequestDrafts: RequestBase[];
    public requestDraftsAvailable: boolean;
    public selectedRequestDraft: RequestBase;

    attachments: Attachment[];

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestFormService: RequestFormService,
        private requestService: RequestService,
        private attachmentService: AttachmentService,
        private route: ActivatedRoute,
        private router: Router,
        private principal: Principal,
        private eventManager: EventManager,
        private organisationService: OrganisationService
    ) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit () {
        this.principal.identity().then((account) => {
            console.log('Got user ', account);
            this.currentUser = account;
            this.initializeRequestForm();
        });

        /**
         * Organisation resolve
         */
        console.log('Finding available organisations');
        this.organisationService.findAvailable().map((availableOrganisations) => {
            // this.availableOrganisations = availableOrganisations;
        });

        /**
         * Resolve Tags
         */
        console.log('Finding available tags');
    }

    ngAfterContentInit () {
        this.registerChangeInFilesUploaded();
    }

    initializeRequestForm () {
        // Resolve Draft Requests
        let uuid = this.currentUser.uuid;
        this.requestService.findAvailableRequestDrafts(uuid)
            .subscribe(
                (requestDrafts) => this.processAvailableDrafts(requestDrafts),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    registerChangeInFilesUploaded () {
        console.log('Registering for file upload change.');
        this.eventManager.subscribe('uploadListModification', (response) => this.loadAttachmentsForRequest());
    }

    loadAttachmentsForRequest () {
        this.attachmentService
            .findAttachmentsForRequest(this.requestBase.uuid)
            .subscribe(
                (attachments) => this.attachments = attachments,
                (error) => this.onError(error)
            );
    }

    processAvailableDrafts(requestDrafts) {
        if (!requestDrafts.length) {
            return this.initializeBaseRequest();
        }

        this.availableRequestDrafts = requestDrafts;
        this.requestDraftsAvailable = true;
    }

    initializeBaseRequest() {
        this.requestService.initRequestBase()
            .subscribe(
                (requestBase) => {
                    this.requestBase = requestBase;
                    this.request = new RequestDetail();
                },
                (error) => this.onError('Error initializing base request')
            );
    }

    selectRequestDraft (requestBase: RequestBase) {
        this.requestBase = requestBase;
        this.request = requestBase.detail;
    }

    saveRequestDraft () {
        this.requestService.saveRequestDraft(this.requestBase)
            .subscribe(
                (requestBase) => this.postSaveUpdate(requestBase),
                (error) => this.onError(error)
            );
    }

    private postSaveUpdate (requestBase: RequestBase) {
        console.log('Updated request with {}', requestBase);
    }

    onError(error) {
        console.log('An error occurred ', error);
    }

}
