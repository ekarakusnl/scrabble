import { Injectable } from '@angular/core';

import Swal from 'sweetalert2';
import { ToastrService } from 'ngx-toastr';
//import { Toast, ToastModel } from '@syncfusion/ej2-notifications';  // Import the toast component

@Injectable()

export class ToastService {

  Toast;
//  public toastInstance: Toast;
  constructor(
    private toastrService: ToastrService,
  ) { }

  ngOnInit(): void {;
  }

  error(message: string): void {
    this.toastrService.error(message, 'Error', {
      timeOut: 3000,
      positionClass: 'toast-top-right',
    });
  }

  warning(message: string): void {
    this.toastrService.warning(message, 'Warning', {
      timeOut: 3000,
      positionClass: 'toast-top-right',
    });
  }

  info(message: string): void {
    this.toastrService.info(message, 'Info', {
      timeOut: 3000,
      positionClass: 'toast-top-right',
    });
  }

  success(message: string): void {
    this.toastrService.success(message, 'Success', {
      timeOut: 3000,
      positionClass: 'toast-top-right',
    });
  }

}