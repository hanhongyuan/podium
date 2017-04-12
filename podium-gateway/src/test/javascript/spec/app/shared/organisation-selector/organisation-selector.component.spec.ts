/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { OrganisationSelectorComponent } from '../../../../../../main/webapp/app/shared/organisation-selector/organisation-selector.component';
import { Organisation } from '../../../../../../main/webapp/app/entities/organisation/organisation.model';
import { JhiLanguageService } from 'ng-jhipster';
import { TranslateService, TranslateLoader, TranslateParser } from 'ng2-translate';
import { OrganisationService } from '../../../../../../main/webapp/app/entities/organisation/organisation.service';
import { MockLanguageService } from '../../../helpers/mock-language.service';
import { BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { EventEmitter } from '@angular/core';

describe('OrganisationSelectorComponent (templateUrl)', () => {

    let comp: OrganisationSelectorComponent;
    let fixture: ComponentFixture<OrganisationSelectorComponent>;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                BaseRequestOptions,
                MockBackend,
                JhiLanguageService,
                TranslateService,
                TranslateLoader,
                TranslateParser,
                OrganisationService,
                {
                    provide: Http,
                    useFactory: (backendInstance: MockBackend, defaultOptions: BaseRequestOptions) => {
                        return new Http(backendInstance, defaultOptions);
                    },
                    deps: [MockBackend, BaseRequestOptions]
                },
                {
                    provide: JhiLanguageService,
                    useClass: MockLanguageService
                },
            ],
            imports: [FormsModule],
            declarations: [OrganisationSelectorComponent], // declare the test component
        }).overrideComponent(OrganisationSelectorComponent, {
            set: {
                template: ''
            }
        }).compileComponents();
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(OrganisationSelectorComponent);
        comp = fixture.componentInstance; // OrganisationSelectorComponent test instance
    });

    it('should not have organisation options and selected organisations', () => {
        expect(comp.selectedOrganisationValues).toBe(undefined);
        expect(comp.selectedOrganisations).toBe(undefined);
        expect(comp.organisationOptions).toBe(undefined);
    });

    describe('ngOnInit', () => {

        const mockResponse = [ {
            "id" : 1,
            "uuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
            "name" : "International variable name bank",
            "shortName" : "VarnameBank",
            "activated" : true,
            "organisationUuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a"
        }, {
            "id" : 1000,
            "uuid" : "549d67f8-7720-423a-ada9-bea83760e06a",
            "name" : "International VarnameBank2",
            "shortName" : "VarnameBank2",
            "activated" : false,
            "organisationUuid" : "549d67f8-7720-423a-ada9-bea83760e06a"
        }];

        it('should select organisation(s) based on input value on initialisation', () => {
            comp.organisations = [new Organisation(1000, '123', 'dummy')];
            comp.ngOnInit();
            expect(comp.selectedOrganisations).toEqual(['123']);
        });
    });

    describe('onChange', () => {

        beforeEach(() => {
            comp.organisationOptions = [
                new Organisation(1000, '123', 'dummy'),
                new Organisation(1001, '456', 'dummy')
            ];
            comp.selectedOrganisations = ['456'];
            comp.organisationChange = new EventEmitter();

            spyOn(comp.organisationChange,  'emit');
        });

        it('should update input value when selected organisations changed', () => {
            comp.onChange();
            expect(comp.organisations).toEqual([new Organisation(1001, '456', 'dummy')]);
        });

        it('should emit value when selection changed', () => {
            comp.onChange();
            expect(comp.organisationChange.emit).toHaveBeenCalled();
        });

    });


});
