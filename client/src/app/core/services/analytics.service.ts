import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { CashFlowChartPoint, CategoryChartPoint, ChartPoint, MultiSeriesChart } from "../models/analytics.model";
import { Observable } from "rxjs";
import { ApiPaths } from "../../constans/api-paths";
import { formatDate } from "@angular/common";
import { FinancialSummaryResponse } from "../../features/dashboard/models/financial-summary.model";

@Injectable(
    {
        providedIn: "root"
    }
)
export class AnalyticsService {
    private http = inject(HttpClient);

    getAccountBalanceHistory(accountId: number, from: Date, to: Date): Observable<ChartPoint[]> {
        const params = this.getDateParams(from, to);
        return this.http.get<ChartPoint[]>(`${ApiPaths.Analytics.Account.BALANCE_HISTORY(accountId)}`, { params });
    }

    getAccountCategoryBreakdown(accountId: number, from: Date, to: Date, type: string): Observable<CategoryChartPoint[]> {
        let params = this.getDateParams(from, to);
        params = params.set("type", type);

        return this.http.get<CategoryChartPoint[]>(`${ApiPaths.Analytics.Account.CATEGORY_BREAKDOWN(accountId)}`, { params });
    }

    getAccountCashFlow(accountId: number, from: Date, to: Date): Observable<CashFlowChartPoint[]> {
        const params = this.getDateParams(from, to);

        return this.http.get<CashFlowChartPoint[]>(`${ApiPaths.Analytics.Account.CASH_FLOW(accountId)}`, { params });
    }

    getGlobalBalanceHistory(from: Date, to: Date): Observable<MultiSeriesChart> {
        const params = this.getDateParams(from, to);

        return this.http.get<MultiSeriesChart>(`${ApiPaths.Analytics.Global.BALANCE_HISTORY}`, { params });
    }

    getGlobalCategoryBreakdown(from: Date, to: Date, type: string): Observable<CategoryChartPoint[]> {
        let params = this.getDateParams(from, to);
        params = params.set("type", type);

        return this.http.get<CategoryChartPoint[]>(`${ApiPaths.Analytics.Global.CATEGORY_BREAKDOWN}`, { params });
    }

    getGlobalCashFlow(from: Date, to: Date): Observable<CashFlowChartPoint[]> {
        const params = this.getDateParams(from, to);

        return this.http.get<CashFlowChartPoint[]>(`${ApiPaths.Analytics.Global.CASH_FLOW}`, { params });
    }

    getGlobalFinancial(from: Date, to: Date): Observable<FinancialSummaryResponse> {
        const params = this.getDateParams(from, to);

        return this.http.get<FinancialSummaryResponse>(`${ApiPaths.Analytics.Global.SUMMARY}`, { params });
    }

    generateAccountReport(accountId: number, from: Date, to: Date): Observable<void> {
        const params = this.getDateParams(from, to);

        return this.http.get<void>(`${ApiPaths.Analytics.Account.REPORT(accountId)}`, { params });
    }

    private getDateParams(from: Date, to: Date): HttpParams {
        const fromStr = formatDate(from, 'yyyy-MM-dd', 'en-US');
        const toStr = formatDate(to, 'yyyy-MM-dd', 'en-US');

        const params = new HttpParams()
            .set('from', fromStr)
            .set('to', toStr);

        return params;
    }
}