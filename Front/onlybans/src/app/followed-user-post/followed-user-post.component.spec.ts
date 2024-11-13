import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FollowedUserPostComponent } from './followed-user-post.component';

describe('FollowedUserPostComponent', () => {
  let component: FollowedUserPostComponent;
  let fixture: ComponentFixture<FollowedUserPostComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FollowedUserPostComponent]
    });
    fixture = TestBed.createComponent(FollowedUserPostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
