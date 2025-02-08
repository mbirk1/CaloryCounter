import {Component, input, InputSignal} from '@angular/core';
import {NgStyle} from '@angular/common';

@Component({
  selector: 'app-button',
  imports: [
  ],
  templateUrl: './button.component.html',
  styleUrl: './button.component.css'
})
export class ButtonComponent {
  text: InputSignal<string> = input<string>('');
  style: InputSignal<string> = input<string>('bg-button-primary hover:bg-button-primary-hover text-white rounded-lg p-2')
}
