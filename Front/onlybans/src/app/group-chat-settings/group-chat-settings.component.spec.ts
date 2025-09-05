import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupChatSettingsComponent } from './group-chat-settings.component';

describe('GroupChatSettingsComponent', () => {
  let component: GroupChatSettingsComponent;
  let fixture: ComponentFixture<GroupChatSettingsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GroupChatSettingsComponent]
    });
    fixture = TestBed.createComponent(GroupChatSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
