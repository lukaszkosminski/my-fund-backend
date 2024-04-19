import {Component, inject, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CategoriesStore} from "../../../stores/categories.store";

@Component({
  selector: 'app-category-form',
  templateUrl: './category-form.component.html',
  styleUrl: './category-form.component.scss'
})
export class CategoryFormComponent implements OnInit{
  categoryForm: FormGroup;

  formBuilder = inject(FormBuilder);
  categoryStore = inject(CategoriesStore);
  formStatus = this.categoryStore.form.status;
  formMessage = this.categoryStore.form.message;

  ngOnInit() {
    this.categoryForm = this.formBuilder.group({
      name: ['', Validators.required],
      subCategories: this.formBuilder.array([
        this.formBuilder.group({
          name: ['']
        })
      ])
    });
  }

  get subCategories() {
    return this.categoryForm.get('subCategories') as FormArray;
  }

  removeSubcategory(index: number) {
    this.subCategories.removeAt(index);
  }

  addSubCategory() {
    this.subCategories.push(this.formBuilder.group({name: ''}));
  }

  onSubmit() {
    if(this.categoryForm.valid) {
      this.categoryStore.create(this.categoryForm.value);
    }

    Object.keys(this.categoryForm.controls).forEach(field => {
      const control = this.categoryForm.get(field);
      control!.markAsTouched({ onlySelf: true });
    });

  }

  protected readonly onsubmit = onsubmit;
}
