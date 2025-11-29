import { inject, Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { marked } from 'marked';

@Pipe({
  name: 'markdownToHtml',
  standalone: true
})
export class MarkdownToHtmlPipe implements PipeTransform {

  private sanitzer = inject(DomSanitizer);

  transform(value: string): SafeHtml {

    if (!value) return '';

    const html = marked.parse(value) as string;

    return this.sanitzer.bypassSecurityTrustHtml(html);
  }

}
