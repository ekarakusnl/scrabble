import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

import { AuthenticationService } from '../service/authentication.service';
import { ToastService } from '../service/toast.service';
import { UserProfileService } from '../service/user-profile.service';

import { UserProfile } from '../model/user-profile';

import { environment } from '../../environments/environment';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  @ViewChild('showProfilePictureDialogButton') private showProfilePictureDialogButton: ElementRef;
  @ViewChild('hideProfilePictureDialogButton') private hideProfilePictureDialogButton: ElementRef;

  profilePictureURL: string = environment.USER_IMAGE_URL;

  // a generated value added to the profile picture url as query parameter to avoid getting
  // the profile picture image file from the browser cache
  unixTime: number = Math.floor(Date.now() / 1000);

  userId: number;

  username = new FormControl({ value: '', disabled: true }, Validators.required);
  password = new FormControl(null, Validators.required);
  email = new FormControl({ value: '', disabled: true }, Validators.required);
  preferredLanguage = new FormControl();
  userForm!: FormGroup;
  user: UserProfile;
  profilePicture: File = null;
  profilePicturePreview: any;

  fakePassword = "0000000000";

  languages: string[] = [ 'en', 'de', 'fr', 'nl', 'tr' ];

  uploadInProgress: boolean;

  constructor(
    private userProfileService: UserProfileService,
    private translateService: TranslateService,
    private toastService: ToastService,
    private formBuilder: FormBuilder,
    private authenticationService: AuthenticationService,
  ) { }

  ngOnInit(): void {
    this.userId = this.authenticationService.getUserId();
    this.userProfileService.getUser().subscribe((user: UserProfile) => {
      this.user = user;
      this.username.setValue(user.username);
      this.password.setValue(this.fakePassword);
      this.email.setValue(user.email);
      this.preferredLanguage.setValue(user.preferredLanguage);
    });
    this.userForm = this.formBuilder.group({
      username: this.username,
      password: this.password,
      email: this.email,
      preferredLanguage: this.preferredLanguage
    });
  }

  onSubmit(): void {
    const updatedUser: UserProfile = {
      id: this.user.id,
      username: this.user.username,
      password: this.fakePassword === this.userForm.value.password ? null : this.userForm.value.password,
      email: this.user.email,
      preferredLanguage: this.userForm.value.preferredLanguage
    };
    this.userProfileService.saveUser(updatedUser).subscribe(() => {
      this.toastService.success(this.translateService.instant('user.updated'));
    });
  }

  onImageUpload(event: Event): void {
    if (event && event.target && event.target instanceof HTMLInputElement) {
      this.uploadInProgress = false;
      const file = (<HTMLInputElement> event.target).files[0];
      const mimeType = file.type;
      if (mimeType.match(/image\/*/) == null) {
        this.toastService.error(this.translateService.instant('validation.file.upload.image.only'));
        return;
      } else {
        this.profilePicture = file;
        this.showProfilePictureDialogButton.nativeElement.click();
        const reader = new FileReader();
        reader.readAsDataURL(file); 
        reader.onload = (_event) => { 
          this.profilePicturePreview = reader.result; 
        }
      }
    } else {
      this.toastService.error(this.translateService.instant('validation.unknown.file.type'));
    }
  }

  saveProfilePicture(): void {
    this.uploadInProgress = true;
    this.userProfileService.saveProfilePicture(this.profilePicture).subscribe(response => {
      this.hideProfilePictureDialogButton.nativeElement.click();
      this.toastService.success(this.translateService.instant('user.profile.picture.saved'));
      this.uploadInProgress = false;
      window.location.reload();
    });
  }

}
