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

        return this.http.get<Category[]>(ApiPaths.Categories.BASE, { params });
    }

    public createCategory(data: UpdateCategory): Observable<Category> {
        return this.http.post<Category>(ApiPaths.Categories.BASE, data);
    }

    public updateCategory(id: number, data: UpdateCategory): Observable<Category> {
        return this.http.patch<Category>(`${ApiPaths.Categories.BY_ID(id)}`, data);
    }

    public deleteCategory(id: number): Observable<void> {
        return this.http.delete<void>(`${ApiPaths.Categories.BY_ID(id)}`);
    }

     public reassignCategory(oldId: number, newId: number): Observable<void> {
        return this.http.post<void>(`${ApiPaths.Categories.REASSIGN(oldId)}`, {newCategoryId: newId});
    }
}