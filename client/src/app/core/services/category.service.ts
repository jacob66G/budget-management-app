import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Category } from "../models/category.model";
import { ApiPaths } from "../../constans/api-paths";
import { UpdateCategory } from "../../features/categories/category.model";

@Injectable({
    providedIn: "root"
})
export class CategoryService {
    private http = inject(HttpClient);

    public getCategories(type?: string): Observable<Category[]> {
        let params;
        if (type) {
            params = new HttpParams().set('type', type);
        }

        return this.http.get<Category[]>(ApiPaths.Categories.CATEGORIES, { params });
    }

    public createCategory(data: UpdateCategory): Observable<Category> {
        return this.http.post<Category>(ApiPaths.Categories.CATEGORIES, data);
    }

    public updateCategory(id: number, data: UpdateCategory): Observable<Category> {
        return this.http.patch<Category>(`${ApiPaths.Categories.CATEGORIES}/${id}`, data);
    }

    public deleteCategory(id: number): Observable<void> {
        return this.http.delete<void>(`${ApiPaths.Categories.CATEGORIES}/${id}`);
    }

     public reassignCategory(oldId: number, newId: number): Observable<void> {
        return this.http.post<void>(`${ApiPaths.Categories.CATEGORIES}/${oldId}/reassign`, {newCategoryId: newId});
    }
}