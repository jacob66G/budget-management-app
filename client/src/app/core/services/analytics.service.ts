import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { CashFlowChartPoint, CategoryChartPoint, ChartPoint } from "../models/analytics.model";
import { Observable } from "rxjs";
import { ApiPaths } from "../../constans/api-paths";
import { formatDate } from "@angular/common";

@Injectable(
    {
        providedIn: "root"
    }
)
export class AnalyticsService {
    private http = inject(HttpClient);

    getBalanceHistory(accountId: number, from: Date, to: Date): Observable<ChartPoint[]> {
        const fromStr = formatDate(from, 'yyyy-MM-dd', 'en-US');
        const toStr = formatDate(to, 'yyyy-MM-dd', 'en-US');

        const params = new HttpParams()
            .set('from', fromStr)
            .set('to', toStr);

        return this.http.get<ChartPoint[]>(`${ApiPaths.Analytics.BALANCE_HISTORY}/${accountId}`, { params });
    }

     getCategoryBreakdown(accountId: number, from: Date, to: Date, type: string): Observable<CategoryChartPoint[]> {
        const fromStr = formatDate(from, 'yyyy-MM-dd', 'en-US');
        const toStr = formatDate(to, 'yyyy-MM-dd', 'en-US');

        const params = new HttpParams()
            .set('from', fromStr)
            .set('to', toStr)
            .set("type", type);

        return this.http.get<CategoryChartPoint[]>(`${ApiPaths.Analytics.CATEGORY_SUMS}/${accountId}`, { params });
    }

      getCashFlow(accountId: number, from: Date, to: Date): Observable<CashFlowChartPoint[]> {
        const fromStr = formatDate(from, 'yyyy-MM-dd', 'en-US');
        const toStr = formatDate(to, 'yyyy-MM-dd', 'en-US');

        const params = new HttpParams()
            .set('from', fromStr)
            .set('to', toStr);

        return this.http.get<CashFlowChartPoint[]>(`${ApiPaths.Analytics.CASH_FLOW}/${accountId}`, { params });
    }
}