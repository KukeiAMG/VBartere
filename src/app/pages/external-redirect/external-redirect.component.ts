import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-external-redirect',
  template: '<div>Перенаправление...</div>',
  standalone: true
})
export class ExternalRedirectComponent implements OnInit {
  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    window.location.href = 'http://localhost:8080';
  }
} 