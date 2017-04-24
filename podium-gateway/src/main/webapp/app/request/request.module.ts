/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { PodiumGatewaySharedModule } from '../shared';

import {
    requestRoute,
    RequestFormService,
    RequestFormComponent,
    RequestFormSubmitDialogComponent,
} from './';
import { RequestOverviewService, RequestOverviewComponent } from './overview';
import { RequestMainDetailComponent } from './main-detail/request-main-detail.component';
import { RequestDetailComponent } from './main-detail/detail/request-detail.component';

import { RequestActionToolbarComponent }
    from '../shared/request/action-bars/request-action-toolbar/request-action-toolbar.component';
import { RequestProgressBarComponent } from './main-detail/progress-bar/request-progress-bar.component';
import { RequestStatusSidebarComponent } from '../shared/request/status-sidebar/status-sidebar.component';

@NgModule({
    imports: [
        CommonModule,
        PodiumGatewaySharedModule,
        RouterModule.forChild(requestRoute)
    ],
    declarations: [
        RequestFormComponent,
        RequestFormSubmitDialogComponent,
        RequestOverviewComponent,
        RequestMainDetailComponent,
        RequestDetailComponent,
        RequestActionToolbarComponent,
        RequestProgressBarComponent,
        RequestStatusSidebarComponent
    ],
    entryComponents: [
        RequestFormSubmitDialogComponent
    ],
    providers: [
        RequestFormService,
        RequestOverviewService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayRequestModule {}
