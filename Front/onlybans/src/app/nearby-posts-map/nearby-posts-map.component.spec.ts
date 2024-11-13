import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NearbyPostsMapComponent } from './nearby-posts-map.component';

describe('NearbyPostsMapComponent', () => {
  let component: NearbyPostsMapComponent;
  let fixture: ComponentFixture<NearbyPostsMapComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NearbyPostsMapComponent]
    });
    fixture = TestBed.createComponent(NearbyPostsMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
