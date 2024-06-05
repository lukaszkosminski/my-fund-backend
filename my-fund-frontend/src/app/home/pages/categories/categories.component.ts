import { Component, inject, OnInit } from '@angular/core';
import { CategoriesStore } from '../../../stores/categories.store';
import { Category } from '../../../models/Category.model';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.scss',
})
export class CategoriesComponent implements OnInit {
  categoriesStore = inject(CategoriesStore);

  categories = this.categoriesStore.categories;

  ngOnInit() {
    this.categoriesStore.getAll();
  }

  deleteCategory(category: Category) {
    console.log(20, category);

    const isConfirmed = confirm(
      'Are you sure you want to delete this category?'
    );

    if (isConfirmed) {
      this.categoriesStore.delete(category);
    }
  }
}
