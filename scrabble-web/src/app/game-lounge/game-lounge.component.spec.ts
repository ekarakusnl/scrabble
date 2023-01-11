import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GameLoungeComponent } from "./GameLoungeComponent";

describe('GameLoungeComponent', () => {
  let component: GameLoungeComponent;
  let fixture: ComponentFixture<GameLoungeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GameLoungeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GameLoungeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
