import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { map, Observable, of, tap } from "rxjs";
import { ReferenceData } from "../models/reference-data.model";
import { ApiPaths } from "../../constans/api-paths";

@Injectable(
    {
        providedIn: "root"
    }
)
export class ReferenceDataService {
    private http = inject(HttpClient);
    
    data = signal<ReferenceData | null>(null);

    loadData(): Observable<void> {
    if (this.data()) return of(void 0);

    return this.http.get<ReferenceData>(ApiPaths.ReferenceData.REFERENCE_DATA).pipe(
      tap(response => this.data.set(response)),
      map(() => void 0)
    );
  }
  
  get currencies() { return this.data()?.currencies || []; }
  get accountTypes() { return this.data()?.accountTypes || []; }
  get categoryTypes() { return this.data()?.categoryTypes || []; }
  get userStatuses() { return this.data()?.userStatuses || []; }
  get accountStatuses() { return this.data()?.accountStatuses || []; }
  get budgetTypes() { return this.data()?.budgetTypes || []; }
  get accountIcons() { return this.data()?.accountIcons || []; }
  get categoryIcons() { return this.data()?.categoryIcons || []; }
  
}