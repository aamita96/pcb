import { Component, OnInit, NgZone } from '@angular/core';
import { FileService } from 'src/app/servers.service';
import { Response } from '@angular/http';
import { Observable } from 'rxjs';
import { CanServiceComponentDeactivate } from './candeactivate-service.service';
import { MESSAGE_CONST } from '../../error-messages';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit, CanServiceComponentDeactivate {
  services = [];
  deletedServiceArray = [];
  changesSaved = true;
  newserviceEle = new FormControl(null, [Validators.required, Validators.minLength(3)]);
  constructor(private fileService: FileService, private zone: NgZone) { }

  ngOnInit() {
    this.retriveExtn();
  }
  getErrorMessage() {
    return this.newserviceEle.hasError('required') ? 'You must enter a service name' :
      this.newserviceEle.hasError('minlength') ? 'required minimum length is 3' :
        '';
  }
  /**
   * To reterive list of services
   */
  retriveExtn() {
    this.fileService.getServices()
      .subscribe(
        (response: Response) => {
          this.services = response.json();

          console.log("Data is fetching...", this.services);
        },
        (error) => console.log(error)
      );
  }

  /**
   * Updates the already existing service. 
   * @param service takes service object
   */
  updateService(service) {
    service.name = this.newserviceEle.value;
    service.name = service.name.toLowerCase();
    console.log(service);
    this.fileService.updateServices(service)
      .subscribe(
        (response: Response) => {
          console.log("Your data is updated", response);
        },
        (error) => console.log(error)
      );

  }

  /**
   * Adds a textbox into the list for adding new service. 
   */
  addService() {
    this.newserviceEle.patchValue(null);
    this.services.unshift({ id: this.services.length + 1, name: null, add: true });
  }

  /**
   * Removes the textbox.
   * @param service contains service property
   */
  cancelClick(service) {
    if (service.add) {
      this.services.shift();
    } else {
      service.edit = false;
      service.add = false;
      service.name = service.originalData
    }
  }

  /**
   * Removes the service and stores into serviceArray  
   *
   * @param service takes service object
   */
  removeServices(service) {
    console.log(service);
    this.changesSaved = false;
    let index = this.services.indexOf(service);
    this.services.splice(index, 1);
    this.deletedServiceArray.push(service);
    console.log("services to delete:", this.deletedServiceArray);
    console.log("Remaining Services", this.services);
  }

  /**
   * Sends the Service array to database.
   */
  onSave() {
    if (this.deletedServiceArray.length !== 0) {
      let confirm = window.confirm("Are you sure you want to delete services ?");
      if (confirm === true) {
        this.fileService.deleteServices(this.deletedServiceArray).subscribe(
          (response: Response) => {
            console.log(response);
            this.changesSaved = true;
          },
          (error) => console.log(error)
        );
      } else {
        this.reloadPage();
      }
    }

  }
  reloadPage() {
    this.zone.runOutsideAngular(() => {
      location.reload();
    });
  }
  /**
   * This method shows warning before navigating to another page if changes are not saved.
   */
  canDeactivate(): Observable<boolean> | Promise<boolean> | boolean {
    if (!(this.changesSaved)) {
      return confirm(MESSAGE_CONST.AUTH_CHECK);
    }
    else {
      return true;
    }
  };

  editClick(service) {
    // this.newserviceEle.patchValue(null);
    if (service.edit)
      this.newserviceEle.patchValue(service.name);
  }

}
