import { Component, OnInit, Renderer, ElementRef } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';

import { Register } from './register.service';
import { LoginModalService , EmailValidatorDirective} from '../../shared';

@Component({
    templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {

    confirmPassword: string;
    doNotMatch: string;
    error: string;
    errorEmailExists: string;
    errorUserExists: string;
    registerAccount: any;
    success: boolean;
    modalRef: NgbModalRef;
    specialismOptions : any;

    constructor(
        private languageService: JhiLanguageService,
        private loginModalService: LoginModalService,
        private registerService: Register,
        private elementRef: ElementRef,
        private renderer: Renderer
    ) {
        this.languageService.setLocations(['register']);
    }

    ngOnInit() {
        this.success = false;
        this.specialismOptions = [
            { value: '', display: 'Please select specialism' },
            { value: 'Gastroenterology', display: 'Gastroenterology'},
            { value: 'Gynaecology', display: 'Gynaecology'},
            { value: 'Dermatology', display: 'Dermatology'},
            { value: 'Medical Oncology', display: 'Medical Oncology'},
            { value: 'Internal Medicine', display: 'Internal Medicine'},
            { value: 'Radiology', display: 'Radiology'},
            { value: 'Radiotherapy', display: 'Radiotherapy'},
            { value: 'Haematology', display: 'Haematology'},
            { value: 'Throat-nose-ear', display: 'Throat-nose-ear'},
            { value: 'Surgery', display: 'Surgery'},
            { value: 'Epidemiology', display: 'Epidemiology'},
            { value: 'Primary care', display: 'Primary care'},
            { value: 'Cardiology', display: 'Cardiology'},
            { value: 'Pathology', display: 'Pathology'},
            { value: 'Lung Disease', display: 'Lung Disease'},
            { value: 'Urology', display: 'Urology'},
            { value: 'Neurology', display: 'Neurology'},
            { value: 'Endocrinology', display: 'Endocrinology'}
        ];
        this.registerAccount = {};
        this.registerAccount.specialism = '';
    }

    ngAfterViewInit() {
        this.renderer.invokeElementMethod(this.elementRef.nativeElement.querySelector('#login'), 'focus', []);
    }

    register() {
        if (this.registerAccount.password !== this.confirmPassword) {
            this.doNotMatch = 'ERROR';
        } else {
            this.doNotMatch = null;
            this.error = null;
            this.errorUserExists = null;
            this.errorEmailExists = null;
            this.languageService.getCurrent().then(key => {
                this.registerAccount.langKey = key;
                this.registerService.save(this.registerAccount).subscribe(() => {
                    this.success = true;
                }, (response) => this.processError(response));
            });
        }
    }

    openLogin() {
        this.modalRef = this.loginModalService.open();
    }

    private processError(response) {
        this.success = null;
        if (response.status === 400 && response._body === 'login already in use') {
            this.errorUserExists = 'ERROR';
        } else if (response.status === 400 && response._body === 'e-mail address already in use') {
            this.errorEmailExists = 'ERROR';
        } else {
            this.error = 'ERROR';
        }
    }
}
