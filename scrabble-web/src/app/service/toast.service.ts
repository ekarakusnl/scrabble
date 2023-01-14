import { Injectable } from '@angular/core';

import { ToastrService } from 'ngx-toastr';

@Injectable()

export class ToastService {

  constructor(
    private toastrService: ToastrService,
  ) { }

  ngOnInit(): void {
  }

  playSound() {
    let audio = new Audio();
    audio.src = "../../assets/sounds/alert.mp3";
    audio.load();
    audio.play();
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