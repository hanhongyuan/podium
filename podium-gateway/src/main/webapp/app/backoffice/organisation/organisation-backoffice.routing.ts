/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PodiumGatewayOrganisationModule } from '../modules/organisation/organisation.module';
import { PodiumGatewayUserMgmtModule } from '../modules/user-management/user-management.module';

export const BBMRI_ROUTES = [
    {
        path: 'organisation',
        data: {
            breadcrumb: 'organisation administration'
        },
        children: [
            {
                path: '',
                redirectTo: '/',
                pathMatch: 'full'
            },
            {
                path: 'configuration',
                data: {
                    breadcrumb: 'organisation management'
                },
                // loadChildren: () => PodiumGatewayOrganisationModule
                loadChildren: 'app/modules/organisation/organisation.module#PodiumGatewayOrganisationModule'
            },
            {
                path: 'user-management',
                data: {
                    breadcrumb: 'user management'
                },
                // loadChildren: () => PodiumGatewayUserMgmtModule,
                loadChildren: 'app/modules/user-management/user-management.module#PodiumGatewayUserMgmtModule'
            }
        ]
    }
];

@NgModule({
    imports: [
        RouterModule.forChild(BBMRI_ROUTES)
    ],
    exports: [RouterModule]
})

export class OrganisationBackofficeRoutingModule {}
